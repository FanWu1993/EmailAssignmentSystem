/**
 * 
 */
package oocourse.system;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import de.innosystec.unrar.exception.RarException;


public class ExtractJavaAndText extends MyAbortableThread {
	String index;
	
	public ExtractJavaAndText(String mainPath, OOMailDelivery user, String index) {
		super(mainPath, user, null, null, false);
		this.index = index;
	}
	
	public void run() {
		System.out.println(mainPath);
		File mainFolder = new File(mainPath);
		mainFolder.mkdirs();
		
		IntegratedUncompressor uncompressor = new IntegratedUncompressor();

		File[] rarFiles = mainFolder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("rar");
			}
		});

		File[] zipFiles = mainFolder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("zip");
			}
		});
		
		String fileName;
		ArrayList<String> failedList = new ArrayList<String>();
		
		for (File file : rarFiles) {
			fileName = file.getName();
			
			int numberStartIndex = fileName.indexOf("1");
			if (numberStartIndex < 0 || numberStartIndex + 8 > fileName.length()) {
				continue;
			}
			boolean notANumber = false;
			for (int j = 1; j < 8; ++j) {
				if (fileName.charAt(numberStartIndex + j) > '9' ||
						fileName.charAt(numberStartIndex + j) <'0') {
					notANumber = true;
					break;
				}
			}
			if (notANumber) {
				user.addToProgressDisplay(fileName + "����������ѧ�ţ��Թ�");
				continue;
			}
			
			user.addToProgressDisplay("������ȡ" + fileName);
			try {
				uncompressor.unrar(file.getAbsolutePath(), mainPath);
			} catch (RarException e) {
				user.addToProgressDisplay("��ѹ���󣬵�ǰ��ѹ���ļ�Ϊ" + file.getName());
				failedList.add(file.getName());
				//e.printStackTrace();
				//user.clearBusy();
				//return;
			} catch (IOException e) {
				user.addToProgressDisplay("�ļ���ȡ/������󣬵�ǰ��ѹ���ļ�Ϊ" + file.getName());
				e.printStackTrace();
				user.clearBusy();
				return;
			} catch (Exception e) {
				user.addToProgressDisplay("δ֪����:" + e.getMessage());
				user.clearBusy();
				return;
			}
		}

		for (File file : zipFiles) {
			fileName = file.getName();
			
			int numberStartIndex = fileName.indexOf("1");
			if (numberStartIndex < 0 || numberStartIndex + 8 > fileName.length()) {
				continue;
			}
			boolean notANumber = false;
			for (int j = 1; j < 8; ++j) {
				if (fileName.charAt(numberStartIndex + j) > '9' ||
						fileName.charAt(numberStartIndex + j) <'0') {
					notANumber = true;
					break;
				}
			}
			if (notANumber) {
				user.addToProgressDisplay(fileName + "����������ѧ�ţ��Թ�");
				continue;
			}
			
			user.addToProgressDisplay("������ȡ" + fileName);
			try {
				uncompressor.unzipFileThenRenameToZipName(file.getAbsolutePath());
			} catch (IOException e) {
				user.addToProgressDisplay("�ļ���ȡ/������󣬵�ǰ��ѹ���ļ�Ϊ" + file.getName());
				failedList.add(file.getName());
			} catch (Exception e) {
				user.addToProgressDisplay("δ֪����:" + e.getMessage() + ",��������:" + e.getClass().toString());
				e.printStackTrace();
				user.clearBusy();
				return;
			}
		}
		
		user.addToProgressDisplay("����ȡ���ļ����Ѿ���ȡ����ˡ�");
		if (failedList.size() > 0) {
			user.addToProgressDisplay("��ȡʧ�ܵ��ļ���");
			for(String name : failedList) {
				user.addToProgressDisplay(name);
			}
		}
	}
}
