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
		
		this.suffix = ((taskType == TaskType.PROGRAM || taskType == TaskType.SENDBACK) ? "��ҵ" :
			(isAboutPrinciple ? "ԭ��" : "����")) + homeworkIndex;
		this.startIndex = startIndex;
		this.announcement = announcement;
		try {
			sendRecorder = new FileOutputStream(new File(mainPath +
					"sendingrecord" + (isAboutPrinciple ? "ԭ��" : "����") + ".txt"));
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
			user.addToProgressDisplay("�Ҳ��������ļ�" + mainPath + "match.txt���ַ���ֹ");
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
				user.addToProgressDisplay("��ֹ����������ѧ�ţ�" + fromNum);
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
			
			user.addToProgressDisplay("���ڽ�ѧ��Ϊ��" + fromNum + ((TaskType.PROGRAM == taskType) ? "�ĳ�����" : "�Ľ������") +
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
				user.addToProgressDisplay(fromNum + "δ����ƥ���ļ���");
				notFoundList.add(String.valueOf(fromNum));
				continue;
			}
			
			String latest = candidate.get(0);
			user.addToProgressDisplay("����ƥ���ļ���" + latest);
			
			for(int j = 1; j < candidate.size(); ++j) {
				if(candidate.get(j).substring(candidate.get(j).indexOf('#')).compareTo(
						latest.substring(latest.indexOf('#'))) > 0) {
					user.addToProgressDisplay("����ƥ���ļ���" + candidate.get(j) + "�������ļ�" + latest);
					if (new File(mainPath + latest).delete()) {
						user.addToProgressDisplay(latest + "ɾ���ɹ�");
					}
					latest = candidate.get(j);
				} else {
					user.addToProgressDisplay("�����ļ�" + candidate.get(j));
					if(new File(candidate.get(j) + latest).delete()) {
						user.addToProgressDisplay(candidate.get(j) + "ɾ���ɹ�");
					}
				}
			}
			
			user.addToProgressDisplay("��ʼ����" + latest);
			try {
				if(TaskType.PROGRAM == taskType) {
					SendMailTLS.send("�������ģ��������" + homeworkIndex + "����ҵ��" + (isAboutPrinciple ? "ԭ��" : "����") +
							"��������", toAddress, announcement, mainPath + latest, "�����Գ���" +
							latest.substring(latest.indexOf('.', (latest.indexOf('#') > 0) ? latest.indexOf('#') : 0)), user);
				} else if (TaskType.SENDBACK == taskType) {
					SendMailTLS.send("�������ģ��������" + homeworkIndex + "����ҵ��ԭ����", toAddress, announcement,
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
						text = isAboutPrinciple ? "ϵͳ�Զ����ɣ�û�з���ԭ��Υ��" : "ϵͳ�Զ����ɣ�ľ�з��ִ���";
					}
					
					SendMailTLS.send("�������ģ��������" + homeworkIndex + "����ҵ��" + (isAboutPrinciple ? "ԭ��" : "����") +
							"���Խ��", toAddress, text + "\n\n" + announcement, null, null, user);
				}
			} catch (UnsupportedEncodingException e) {
				user.addToProgressDisplay("�������������ļ�·�����ļ�����������ֹ��ѧ�ţ�" + fromNum);
				user.setStartIndex(fromNum);
				e.printStackTrace();
				user.clearBusy();
				return;
			} catch (MessagingException e) {
				user.addToProgressDisplay("�����������������������ֹ��ѧ��" + fromNum);
				user.setStartIndex(fromNum);
				e.printStackTrace();
				user.clearBusy();
				return;
			} catch (FileNotFoundException e) {
				user.addToProgressDisplay("�벻Ҫ�ڳ������й������޸���Ŀ¼�µ�����");
				e.printStackTrace();
				user.clearBusy();
				return;
			}
			if (sendRecorder != null) {
				try {
					sendRecorder.write((fromNum + "\n").getBytes());
				} catch (IOException e) { }
			}
			user.addToProgressDisplay(fromNum + "�ѷ������");
			System.out.println(fromNum + "");
		}
		
		user.addToProgressDisplay("�����ʼ��ѷ��ͳɹ�");
		
		configScanner.close();
		
		user.clearBusy();
	}
}
