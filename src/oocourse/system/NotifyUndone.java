package oocourse.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Scanner;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class NotifyUndone extends MyAbortableThread {
	HashMap<String, String> addressMap;
	
	public NotifyUndone(String mainPath, OOMailDelivery user, String homeworkIndex, HashMap<String, String> addressMap) {
		super(mainPath, user, homeworkIndex, null, false);
		this.addressMap = addressMap;
	}

	@SuppressWarnings({ "unused" })
	public void run() {
		user.setBusy();
		Scanner undoneScanner;
		try {
			undoneScanner = new Scanner(new File(mainPath + "undone.txt"));
		} catch (FileNotFoundException e) {
			user.addToProgressDisplay("�Ҳ��������ļ�" + mainPath + "undone.txt���ַ���ֹ");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		File mainFolder = new File(mainPath);

		String number;
		
		while (undoneScanner.hasNext()) {
			number = undoneScanner.next();
			
			if (number.contains("��δ�Ͻ���ҵ��ѧ��")) {
				continue;
			}
			
			if (abort) {
				user.addToProgressDisplay("��ֹ����������ѧ�ţ�" + number);
				user.setStartIndex(Integer.parseInt(number));
				break;
			}
			
			user.addToProgressDisplay("��������" + number);
			if (addressMap.get(number) == null) {
				addressMap.put(number, "kyu_115s@126.com");
			}
			
			try {
				SendMailTLS.send("�������ģ��������" + homeworkIndex + "����ҵ/����δ����ҵͨ��",
						addressMap.get(number), "ͬѧ�����Ƿ�����û�н���ҵ/���ԣ��뵽���ǵ���վȷ��undone�б�" +
								"������ȷ�Ѿ��ύ�ˣ�����ϵ���ǡ�"
						, null, null, user);
			} catch (AddressException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
			
			user.addToProgressDisplay(number + "���������");
		}
		
		user.addToProgressDisplay("�����ʼ��ѷ��ͳɹ�");
		
		undoneScanner.close();
		
		user.clearBusy();
	}
}
