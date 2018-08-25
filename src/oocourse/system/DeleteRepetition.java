package oocourse.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import oocourse.system.OOMailDelivery.TaskType;


public class DeleteRepetition extends MyAbortableThread {

	int startIndex;
	String suffix;
	String announcement;
	
	public DeleteRepetition(String mainPath, String homeworkIndex, TaskType taskType,
			int startIndex, OOMailDelivery user, String announcement, boolean isAboutPrinciple) {
		super(mainPath, user, homeworkIndex, taskType, isAboutPrinciple);
		
		this.suffix = ((taskType == TaskType.PROGRAM) ? "��ҵ" : (isAboutPrinciple ? "ԭ��" : "����")) + homeworkIndex;
		this.startIndex = startIndex;
		this.announcement = announcement;
		this.isAboutPrinciple = isAboutPrinciple;
	}

	@SuppressWarnings({ "unused" })
	public void run() {
		
		user.setBusy();
		
		Scanner inScanner;
		try {
			inScanner = new Scanner(new File(mainPath + (isAboutPrinciple ? "ԭ��" : "����") + "in.txt"));
		} catch (FileNotFoundException e) {
			// TODO �Զ����ɵ� catch ��
			user.addToProgressDisplay("�Ҳ��������ļ�" + mainPath + (isAboutPrinciple ? "ԭ��" : "����") + "in.txt���ַ���ֹ");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		File mainFolder = new File(mainPath);
		
		while (inScanner.hasNext()) {
			int number;
			String address;
			number = inScanner.nextInt();
			address = inScanner.next();
			
			user.addToProgressDisplay("���ڼ��ѧ��Ϊ��" + number + "���ļ�");
			String[] name = mainFolder.list();
			boolean foundFlag = false;
			ArrayList<String> candidate = new ArrayList<String>();
			
			for(int i = 0; i < name.length; ++i) {
				if(name[i].contains(String.valueOf(number)) && name[i].contains(suffix) &&
						name[i].contains(".")) {
					candidate.add(name[i]);
				}
			}
			
			if(candidate.size() == 0) {
				user.addToProgressDisplay(number + "δ����ƥ���ļ���");
				continue;
			}
			
			String latest = candidate.get(0);
			user.addToProgressDisplay("����ƥ���ļ���" + latest);
			
			for(int j = 1; j < candidate.size(); ++j) {
				if(candidate.get(j).compareTo(latest) > 0) {
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
			
			user.addToProgressDisplay(number + "�Ѽ�����");
		}
		
		user.addToProgressDisplay("������");
		
		inScanner.close();
		
		user.clearBusy();
	}
}
