package oocourse.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.mail.MessagingException;

import oocourse.system.OOMailDelivery.TaskType;

public class Delivery extends MyAbortableThread {
	
	int startIndex;
	String suffix;
	String announcement;
	FileOutputStream sendRecorder = null;
	
	public Delivery(String mainPath, String homeworkIndex, TaskType taskType,
			int startIndex, OOMailDelivery user, String announcement, boolean isAboutPrinciple) {
		super(mainPath, user, homeworkIndex, taskType, isAboutPrinciple);
		
		this.suffix = ((taskType == TaskType.PROGRAM || taskType == TaskType.SENDBACK) ? "作业" :
			(isAboutPrinciple ? "原则" : "测试")) + homeworkIndex;
		this.startIndex = startIndex;
		this.announcement = announcement;
		try {
			sendRecorder = new FileOutputStream(new File(mainPath +
					"sendingrecord" + (isAboutPrinciple ? "原则" : "测试") + ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "resource", "unused" })
	public void run() {
		user.setBusy();
		
		Scanner configScanner;
		try {
			if (TaskType.SENDBACK == taskType) {
				configScanner = new Scanner(new File(mainPath + "in.txt"));
			} else {
				configScanner = new Scanner(new File(mainPath + "match.txt"));
			}
		} catch (FileNotFoundException e) {
			user.addToProgressDisplay("找不到配置文件" + mainPath + "match.txt，分发终止");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		boolean flag = (startIndex != 0);
		
		File mainFolder = new File(mainPath);
		
		ArrayList<String> notFoundList = new ArrayList<String>();
		
		while (configScanner.hasNext()) {
			int fromNum;
			int toNum;
			String fromAddress;
			String toAddress;
			if (TaskType.SENDBACK == taskType) {
				fromNum = toNum = configScanner.nextInt();
				fromAddress = toAddress = configScanner.next();
			} else if ((TaskType.PROGRAM == taskType) ^ isAboutPrinciple) {
				fromNum = configScanner.nextInt();
				fromAddress = configScanner.next();
				toNum = configScanner.nextInt();
				toAddress = configScanner.next();
			} else {
				toNum = configScanner.nextInt();
				toAddress = configScanner.next();
				fromNum = configScanner.nextInt();
				fromAddress = configScanner.next();
			}

			if (abort) {
				user.addToProgressDisplay("终止发送任务于学号：" + fromNum);
				user.setStartIndex(fromNum);
				break;
			}
			
			if (flag) {
				if(fromNum != startIndex) {
					continue;
				} else {
					flag = false;
				}
			}
			
			user.addToProgressDisplay("正在将学号为：" + fromNum + ((TaskType.PROGRAM == taskType) ? "的程序发向" : "的结果发向") +
					toAddress);
			
			String[] name = mainFolder.list();
			boolean foundFlag = false;
			ArrayList<String> candidate = new ArrayList<String>();
			
			for(int i = 0; i < name.length; ++i) {
				if(name[i].contains(String.valueOf(fromNum)) && name[i].contains(suffix) && name[i].contains(".")) {
					candidate.add(name[i]);
				}
			}
			
			if(candidate.size() == 0) {
				user.addToProgressDisplay(fromNum + "未发现匹配文件！");
				notFoundList.add(String.valueOf(fromNum));
				continue;
			}
			
			String latest = candidate.get(0);
			user.addToProgressDisplay("发现匹配文件：" + latest);
			
			for(int j = 1; j < candidate.size(); ++j) {
				if(candidate.get(j).substring(candidate.get(j).indexOf('#')).compareTo(
						latest.substring(latest.indexOf('#'))) > 0) {
					user.addToProgressDisplay("发现匹配文件：" + candidate.get(j) + "，舍弃文件" + latest);
					if (new File(mainPath + latest).delete()) {
						user.addToProgressDisplay(latest + "删除成功");
					}
					latest = candidate.get(j);
				} else {
					user.addToProgressDisplay("舍弃文件" + candidate.get(j));
					if(new File(candidate.get(j) + latest).delete()) {
						user.addToProgressDisplay(candidate.get(j) + "删除成功");
					}
				}
			}
			
			user.addToProgressDisplay("开始发送" + latest);
			try {
				if(TaskType.PROGRAM == taskType) {
					SendMailTLS.send("面向对象建模方法：第" + homeworkIndex + "次作业的" + (isAboutPrinciple ? "原则" : "正误") +
							"测试任务", toAddress, announcement, mainPath + latest, "待测试程序" +
							latest.substring(latest.indexOf('.', (latest.indexOf('#') > 0) ? latest.indexOf('#') : 0)), user);
				} else if (TaskType.SENDBACK == taskType) {
					SendMailTLS.send("面向对象建模方法：第" + homeworkIndex + "次作业的原程序", toAddress, announcement,
							mainPath + latest, latest, user);
				} else {
					Scanner textScanner = new Scanner(new File(mainPath + latest));
					String text = new String();
					while(textScanner.hasNext()) {
						text = text + textScanner.nextLine();
						if(textScanner.hasNext()) {
							text = text + "\n";
						}
					}
					textScanner.close();
					
					if(text.equals("") || text.equals("\n")) {
						text = isAboutPrinciple ? "系统自动生成：没有发现原则违反" : "系统自动生成：木有发现错误";
					}
					
					SendMailTLS.send("面向对象建模方法：第" + homeworkIndex + "次作业的" + (isAboutPrinciple ? "原则" : "正误") +
							"测试结果", toAddress, text + "\n\n" + announcement, null, null, user);
				}
			} catch (UnsupportedEncodingException e) {
				user.addToProgressDisplay("发生错误，请检查文件路径及文件名，发送终止于学号：" + fromNum);
				user.setStartIndex(fromNum);
				e.printStackTrace();
				user.clearBusy();
				return;
			} catch (MessagingException e) {
				user.addToProgressDisplay("所有邮箱服务器出错，发送终止于学号" + fromNum);
				user.setStartIndex(fromNum);
				e.printStackTrace();
				user.clearBusy();
				return;
			} catch (FileNotFoundException e) {
				user.addToProgressDisplay("请不要在程序运行过程中修改主目录下的内容");
				e.printStackTrace();
				user.clearBusy();
				return;
			}
			if (sendRecorder != null) {
				try {
					sendRecorder.write((fromNum + "\n").getBytes());
				} catch (IOException e) { }
			}
			user.addToProgressDisplay(fromNum + "已发送完毕");
			System.out.println(fromNum + "");
		}
		
		user.addToProgressDisplay("所有邮件已发送成功");
		
		configScanner.close();
		
		user.clearBusy();
	}
}
