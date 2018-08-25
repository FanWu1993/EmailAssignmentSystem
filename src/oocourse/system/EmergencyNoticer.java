/**
 * 
 */
package oocourse.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;


public class EmergencyNoticer extends MyAbortableThread {
	String message;
	int startIndex;
	
	public EmergencyNoticer(String mainPath, OOMailDelivery user, String message, int startIndex) {
		super(mainPath, user, null, null, false);
		
		this.message = message;
		this.startIndex = startIndex;
	}

	@SuppressWarnings({ "unused" })
	public void run() {
		user.setBusy();
		Scanner addressListScanner;
		try {
			addressListScanner = new Scanner(new File(mainPath + "Email Address List.txt"));
		} catch (FileNotFoundException e) {
			user.addToProgressDisplay("�Ҳ��������б�" + mainPath + "Email Address List.txt��������ֹ");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		File mainFolder = new File(mainPath);

		int number;
		String address;
		
		boolean flag = (startIndex != 0);
		
		while (addressListScanner.hasNext()) {
			number = Integer.parseInt(addressListScanner.next());

			if (abort) {
				user.addToProgressDisplay("��ֹ����������ѧ�ţ�" + number);
				user.setStartIndex(number);
				break;
			}
			
			if (addressListScanner.hasNext()) {
				address = addressListScanner.next();
			} else {
				user.addToProgressDisplay("�ʼ��б��������顣��ֹ����������ѧ�ţ�" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			}
			
			if (flag) {
				if(number != startIndex) {
					continue;
				} else {
					flag = false;
				}
			}
			
			user.addToProgressDisplay("����֪ͨ" + number);
			
			try {
				SendMailTLS.send("�������ģ����������ͨ��", address, message , null, null, user);
			} catch (AddressException e) {
				user.addToProgressDisplay("�����ʽ������ֹ����������ѧ�ţ�" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			} catch (MessagingException e) {
				user.addToProgressDisplay("���������ʱ�����á���ֹ����������ѧ�ţ�" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			} catch (Exception e) {
				user.addToProgressDisplay("δ֪������ֹ����������ѧ�ţ�" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			}
			
			user.addToProgressDisplay(number + "��֪ͨ���");
		}
		
		user.addToProgressDisplay("�����ʼ��ѷ��ͳɹ�");
		
		addressListScanner.close();
		
		user.clearBusy();
	}
}
