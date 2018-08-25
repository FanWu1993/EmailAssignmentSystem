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
			user.addToProgressDisplay("找不到邮箱列表" + mainPath + "Email Address List.txt，提醒终止");
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
				user.addToProgressDisplay("终止发送任务于学号：" + number);
				user.setStartIndex(number);
				break;
			}
			
			if (addressListScanner.hasNext()) {
				address = addressListScanner.next();
			} else {
				user.addToProgressDisplay("邮件列表有误，请检查。终止发送任务于学号：" + number);
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
			
			user.addToProgressDisplay("正在通知" + number);
			
			try {
				SendMailTLS.send("面向对象建模方法：紧急通告", address, message , null, null, user);
			} catch (AddressException e) {
				user.addToProgressDisplay("邮箱格式有误。终止发送任务于学号：" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			} catch (MessagingException e) {
				user.addToProgressDisplay("邮箱服务暂时不可用。终止发送任务于学号：" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			} catch (Exception e) {
				user.addToProgressDisplay("未知错误。终止发送任务于学号：" + number);
				user.setStartIndex(number);
				addressListScanner.close();
				user.clearBusy();
				return;
			}
			
			user.addToProgressDisplay(number + "已通知完毕");
		}
		
		user.addToProgressDisplay("所有邮件已发送成功");
		
		addressListScanner.close();
		
		user.clearBusy();
	}
}
