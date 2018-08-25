package oocourse.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import oocourse.system.OOMailDelivery.TaskType;

public class CreateList extends MyAbortableThread {
	
	final static boolean STUDENTIDIDENTIFICATIONONLY = false;
	
	String suffix;
	HashMap<String, String> addressMap;
	
	public CreateList(String mainPath, OOMailDelivery user, String homeworkIndex, TaskType taskType,
			HashMap<String, String> addressMap, boolean isAboutPrinciple) {
		super(mainPath, user, homeworkIndex, taskType, isAboutPrinciple);
		this.suffix = ((taskType == TaskType.PROGRAM) ? "��ҵ" : (isAboutPrinciple ? "ԭ��" : "����")) + homeworkIndex; 
		this.addressMap = addressMap;
	}

	@SuppressWarnings({ "resource" })
	public void run() {
		user.setBusy();
		
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(new File(mainPath +
					((taskType == TaskType.RESULT) ? (isAboutPrinciple ? "ԭ��" : "����") : "") + "in.txt"));
			user.addToProgressDisplay("�б���: " +
					((taskType == TaskType.RESULT) ? (isAboutPrinciple ? "ԭ��" : "����") : "") + "in.txt");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			user.addToProgressDisplay("�޷��½�" + mainPath + (isAboutPrinciple ? "ԭ��" : "����") + "in.txt");
			user.clearBusy();
			return;
		}
		
		String[] fileNameList = new File(mainPath).list();
		int length = fileNameList.length;
		HashSet<Integer> added = new HashSet<Integer>();
		for(int i = 0; i < length; ++i) {
			if(abort) {
				user.addToProgressDisplay("��ֹ�ڼ��" + fileNameList[i]);
				break;
			} else {
				user.addToProgressDisplay("��ʼ���" + fileNameList[i]);
			}

			int numberStartIndex = fileNameList[i].indexOf('1');
			if(numberStartIndex >= 0 && isStudentID(fileNameList[i].substring(numberStartIndex)) &&
					(STUDENTIDIDENTIFICATIONONLY || fileNameList[i].contains(suffix))) {
				String numberInString = fileNameList[i].substring(numberStartIndex, numberStartIndex + 8);
				int numberInInteger = Integer.parseInt(numberInString);
				
				if(!added.contains(numberInInteger)) {
					user.addToProgressDisplay("������ѧ��" + numberInString + "����ʼд��");
					try {
						if (addressMap.containsKey(numberInString)) {
							fileOutputStream.write((numberInString + " " +
									addressMap.get(numberInString) + "\r\n").getBytes());
						} else {
							fileOutputStream.write((numberInString + " kyu_115s@126.com\r\n").getBytes());
						}
					} catch (IOException e) {
						e.printStackTrace();
						user.addToProgressDisplay("�ļ�д�����");
						user.clearBusy();
						return;
					}
					added.add(numberInInteger);
				}
			}
		}
		
		user.addToProgressDisplay("������");
		
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		user.clearBusy();
	}

	private boolean isStudentID(String substr) {
		if (substr.length() < 8) {
			return false;
		}
		for (int i = 0; i < 8; ++i) {
			if (!isDigit(substr.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private final boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}
}
