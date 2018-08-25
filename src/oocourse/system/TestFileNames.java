package oocourse.system;

import java.io.File;
import java.io.UnsupportedEncodingException;



public class TestFileNames extends MyAbortableThread {
	String mainPath;
	OOMailDelivery user;

	public TestFileNames(String mainPath, OOMailDelivery user) {
		// TODO 自动生成的构造函数存根
		super(mainPath, user, null, null, false);
		
		this.mainPath = mainPath;
		this.user = user;
	}
	
	@Override
	public void run() {
		user.setBusy();
		File directory = new File(mainPath);
		if (!directory.isDirectory()) {
			user.addToProgressDisplay("Not a directory");
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			try {
				user.addToProgressDisplay(new String(file.getName().getBytes("utf-8"), "GB2312"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (abort) {
				break;
			}
		}
		user.clearBusy();
	}
}
