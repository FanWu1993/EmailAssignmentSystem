package oocourse.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import oocourse.system.OOMailDelivery.TaskType;

import com.sun.mail.util.CRLFOutputStream;

//TODO 多线程下载
public class ReadMailSSL extends MyAbortableThread {
	String folderName;
	String mainPath;
	String suffix;
	OOMailDelivery user;
	TaskType taskType;
	int startIndex;
	 
	final String username = "@outlook.com";
	final String password = "";
	final String serverAddress = "imap-mail.outlook.com";

	Properties props;
	
	Session session;
	
	Store store;
	
	int taskCount;
	
	int fileIndex;
	
	HashMap<String, Date> recievedRecord;
	
	File directory;
	
	public ReadMailSSL(String folderName, String mainPath, String suffix, OOMailDelivery user, TaskType taskType, int startIndex) {

		super(mainPath, user, null, taskType, false);
		
		this.folderName = folderName;
		this.mainPath = mainPath;
		this.suffix = suffix;
		this.user = user;
		this.taskType = taskType;
		this.startIndex = startIndex;
		taskCount = 0;
		
		recievedRecord = new HashMap<String, Date>();
		
		directory = new File(mainPath);
		if(!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	private void deleteDirectory(File fp) {
		
		File[] fileList = fp.listFiles();
		int count = fileList.length;
		for(int i = 0; i < count; ++i) {
			if(!fileList[i].delete()) {
				deleteDirectory(fileList[i]);
			}
		}
		fp.delete();
	}
	
	public void run() {
		user.setBusy();

		props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		session = Session.getInstance(props, null);
		try {
			store = session.getStore();
			store.connect(serverAddress, username, password);
		} catch (MessagingException e) {
			e.printStackTrace();
			user.addToProgressDisplay("网络连接错误");
			user.clearBusy();
			return;
		}
		
		Folder inbox;
		Message[] msg;
		try {
			if (folderName.equals("")) {
				inbox = store.getDefaultFolder();
			} else {
				inbox = store.getFolder(folderName);
			}
			inbox.open(Folder.READ_ONLY);
			msg = inbox.getMessages();
		} catch (MessagingException e) {
			user.addToProgressDisplay("邮箱服务暂时不可用，请重试");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		int recieveCount = 0;
		
		for (int i = startIndex; i < msg.length; ++i) {
			if (abort) {
				user.addToProgressDisplay("终止接受任务于第" + i + "封邮件");
				user.setStartIndex(i);
				break;
			}
			
			String subject;
			try {
				subject = msg[i].getSubject();
			} catch (MessagingException e) {
				user.addToProgressDisplay("邮箱服务暂时不可用。终止于第" + i + "封邮件");
				user.setStartIndex(i);
				e.printStackTrace();
				user.clearBusy();
				return;
			}
			user.addToProgressDisplay("正在检索第" + i + "封邮件：" + subject);
			
			if(subject != null && subject.contains(suffix)) {
				int numberStartIndex = subject.indexOf("1");
				if (numberStartIndex < 0 || numberStartIndex + 8 > subject.length()) {
					continue;
				}
				boolean notANumber = false;
				for (int j = 1; j < 8; ++j) {
					if (subject.charAt(numberStartIndex + j) > '9' ||
							subject.charAt(numberStartIndex + j) <'0') {
						notANumber = true;
						break;
					}
				}
				if (notANumber) {
					continue;
				}
				
				try {
					if(recievedRecord.containsKey(subject)) {
						if(recievedRecord.get(subject).after(msg[i].getSentDate())) {
							user.addToProgressDisplay("已接收过较新的版本，跳过此邮件");
							continue;
						} else {
							user.addToProgressDisplay("已接收过较老的版本，删除老版本");
							File[] fileList = directory.listFiles();
							for(int j = fileList.length - 1; j > -1; --j) {
								if (fileList[j].getName().contains(subject)) {
									fileList[j].delete();
								}
							}
						}
					} else {
						recievedRecord.put(msg[i].getSubject(), msg[i].getSentDate());
					}
				} catch (MessagingException e3) {
					user.addToProgressDisplay("邮件读取错误。终止于第" + i + "封邮件");
					e3.printStackTrace();
					user.clearBusy();
					return;
				}
				
				user.addToProgressDisplay("匹配成功，开始读取");
				
				String number = subject.substring(subject.indexOf('1'), subject.indexOf('1') + 8);
				String address;
				try {
					address = MimeUtility.decodeText(InternetAddress.toString(msg[i].getFrom()));
				} catch (UnsupportedEncodingException e1) {
					user.addToProgressDisplay("不支持的邮件名格式。终止于第" + i + "封邮件");
					user.setStartIndex(i);
					e1.printStackTrace();
					user.clearBusy();
					return;
				} catch (MessagingException e1) {
					user.addToProgressDisplay("邮箱服务暂时不可用，请重试。终止于第" + i + "封邮件");
					user.setStartIndex(i);
					e1.printStackTrace();
					user.clearBusy();
					return;
				}
				
				int temp = (address.indexOf(' ') == -1) ?
						address.indexOf('<') :
							address.indexOf('<', address.indexOf(' '));
				if(temp != -1) {
					address = address.substring(temp + 1, address.indexOf('>', temp));
				}
				
				if(taskType != TaskType.APPEAL) {
					try {
						AddIntoList.add(number, address, mainPath);
					} catch (IOException e2) {
						e2.printStackTrace();
						user.addToProgressDisplay("文件输出出错。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						return;
					}
				}

				Multipart multipart = null;
				Object content = null;
				try {
					content = msg[i].getContent();
				} catch (IOException e1) {
					user.addToProgressDisplay("邮件读取错误。终止于第" + i + "封邮件");
					user.setStartIndex(i);
					e1.printStackTrace();
					user.clearBusy();
					return;
				} catch (MessagingException e1) {
					user.addToProgressDisplay("邮箱服务暂时不可用。终止于第" + i + "封邮件");
					user.setStartIndex(i);
					e1.printStackTrace();
					user.clearBusy();
					return;
				} catch (Exception e1) {
					user.addToProgressDisplay("未知错误。终止于第" + i + "封邮件");
					user.setStartIndex(i);
					e1.printStackTrace();
					user.clearBusy();
					return;
				}
				
				try {
					multipart = (Multipart) content;
				} catch (ClassCastException e) {
					String strContent = (String) content;
					try {
						FileOutputStream fileOutputStream =
								new FileOutputStream(new File(mainPath + msg[i].getSubject() +
										getDateDescription(msg[i].getSentDate()) + ".txt"));
						fileOutputStream.write(strContent.getBytes());
						fileOutputStream.close();
						continue;
					} catch (IOException e1) {
						user.addToProgressDisplay("输出文件错误于" + mainPath);
						user.setStartIndex(i);
						e1.printStackTrace();
						user.clearBusy();
						return;
					} catch (MessagingException e1) {
						user.addToProgressDisplay("邮件读取错误。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						e1.printStackTrace();
						user.clearBusy();
						return;
					}
				}
				
				recieveCount++;
				
				fileIndex = 0;
				
				if(taskType == TaskType.PROGRAM) {
					File fp = new File(mainPath + "测试任务/");
					if(!fp.exists()) {
						fp.mkdirs();
					}
					try {
						parseMultipart(multipart, mainPath + "测试任务/", taskType, msg[i].getSubject(), msg[i].getSentDate());
					} catch (MessagingException e1) {
						user.addToProgressDisplay("邮箱服务暂时不可用。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						e1.printStackTrace();
						user.clearBusy();
						return;
					} catch (IOException e1) {
						user.addToProgressDisplay("邮件读取错误。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						e1.printStackTrace();
						user.clearBusy();
						return;
					}
					
					try {
						@SuppressWarnings("deprecation")
						String zipName = mainPath + "/" + msg[i].getSubject() + " " +
								(msg[i].getSentDate().getMonth() + 1) + "." +
								msg[i].getSentDate().getDate() + "." +
								msg[i].getSentDate().getHours() + "." +
								msg[i].getSentDate().getMinutes() + "." +
								".zip";
						new ZipCompressor(zipName).compress(fp.getPath());
					} catch (MessagingException e) {
						user.addToProgressDisplay("邮箱服务暂时不可用。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						e.printStackTrace();
						user.clearBusy();
						return;
					}
					
					deleteDirectory(fp);
				} else {
					try {
						parseMultipart(multipart, mainPath, taskType, msg[i].getSubject(), msg[i].getSentDate());
					} catch (MessagingException e1) {
						user.addToProgressDisplay("邮箱服务暂时不可用。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						e1.printStackTrace();
						user.clearBusy();
						return;
					} catch (IOException e1) {
						user.addToProgressDisplay("邮件读取错误。终止于第" + i + "封邮件");
						user.setStartIndex(i);
						e1.printStackTrace();
						user.clearBusy();
						return;
					}
				}
				
				user.addToProgressDisplay("接收了第" + recieveCount + "封邮件：" + subject);
			}
		}
		user.addToProgressDisplay("共接收了" + recieveCount + "封邮件,接收结束");
		
		try {
			store.close();
		} catch (MessagingException e) {
			// TODO 自动生成的 catch 块
			user.addToProgressDisplay("邮箱服务暂时不可用，请重试");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		user.clearBusy();
	}
	
	@SuppressWarnings("deprecation")
	private String getDateDescription(Date date) {
		return ((date.getMonth() + 1) + "." + date.getDay() + "." + date.getHours() + "." + date.getMinutes());
	}
	
	@SuppressWarnings("deprecation")
	private void parseMultipart(Multipart multipart, String filePath, TaskType taskType, String fileName, Date sentDate)
			throws MessagingException, IOException {
		
		int count = multipart.getCount();
		
		for(int j = 0; j < count; ++j) {
			BodyPart bodyPart = multipart.getBodyPart(j);
			if(bodyPart.isMimeType("multipart/*")) {
				parseMultipart((Multipart) bodyPart.getContent(), filePath, taskType, fileName, sentDate);
			} else if(bodyPart.getFileName() != null && taskType != TaskType.RESULT) {
				String tempName = MimeUtility.decodeText(bodyPart.getFileName());
				user.addToProgressDisplay("当前附件名：" + tempName + "，大小：" +
						bodyPart.getSize() + " Bytes，开始下载");
				
				copy(bodyPart.getInputStream(), new FileOutputStream(
						new File(FileNameCheck.check
									(filePath +
										(
											(taskType == TaskType.APPEAL) ?
													(fileName + " " + getDateDescription(sentDate) + " " + tempName
													) : ("文件" + (fileIndex++)) + tempName.substring(tempName.indexOf('.'))
											)
										)
									)
						, taskType == TaskType.APPEAL));
			} else if(bodyPart.isMimeType("text/plain") && taskType != TaskType.PROGRAM) {
				user.addToProgressDisplay("开始下载正文");
				
				copy(bodyPart.getInputStream(), new CRLFOutputStream(new FileOutputStream(
						new File(FileNameCheck.check(filePath +
								fileName + " " +
								(sentDate.getMonth() + 1) + "." +
								sentDate.getDate() + "." +
								sentDate.getHours() + "." +
								sentDate.getMinutes() + " 正文.txt")), taskType == TaskType.APPEAL)));
			}
		}
	}
	
	protected void finishOneTask() {
		taskCount--;
	}

	private void copy(InputStream inputStream, OutputStream OutputStream)
			throws IOException {
		
		byte[] bytes = new byte[131072];
		int len = 0;
		int count = 0;
		Date frontDate = new Date();
		
		double block = 131.072;
				
		while((len = inputStream.read(bytes)) != -1) {
			OutputStream.write(bytes, 0, len);
			count += len;
			Date currentDate = new Date();
			
			if(len < block) {
				block = len;
			}
			
			user.addToProgressDisplay("已下载" + count + " Bytes  当前速度：" +
					((int)(block/(((double)currentDate.getTime() - frontDate.getTime())/1000))) + "K/s");
			frontDate = currentDate;
		}
		if(inputStream != null) {
			inputStream.close();
		}
		if(OutputStream != null) {
			OutputStream.close();
		}
	}
}
