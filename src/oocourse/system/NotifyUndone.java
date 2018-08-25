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
			user.addToProgressDisplay("找不到配置文件" + mainPath + "undone.txt，分发终止");
			e.printStackTrace();
			user.clearBusy();
			return;
		}
		
		File mainFolder = new File(mainPath);

		String number;
		
		while (undoneScanner.hasNext()) {
			number = undoneScanner.next();
			
			if (number.contains("班未上交作业的学生")) {
				continue;
			}
			
			if (abort) {
				user.addToProgressDisplay("终止发送任务于学号：" + number);
				user.setStartIndex(Integer.parseInt(number));
				break;
			}
			
			user.addToProgressDisplay("正在提醒" + number);
			if (addressMap.get(number) == null) {
				addressMap.put(number, "kyu_115s@126.com");
			}
			
			try {
				SendMailTLS.send("面向对象建模方法：第" + homeworkIndex + "次作业/测试未交作业通告",
						addressMap.get(number), "同学，我们发现你没有交作业/测试，请到我们的网站确认undone列表。" +
								"如果你的确已经提交了，请联系我们。"
						, null, null, user);
			} catch (AddressException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			
			user.addToProgressDisplay(number + "已提醒完毕");
		}
		
		user.addToProgressDisplay("所有邮件已发送成功");
		
		undoneScanner.close();
		
		user.clearBusy();
	}
}
