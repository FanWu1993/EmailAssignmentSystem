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
				user.addToProgressDisplay(fileName + "不包含正常学号，略过");
				continue;
			}
			
			user.addToProgressDisplay("正在提取" + fileName);
			try {
				uncompressor.unrar(file.getAbsolutePath(), mainPath);
			} catch (RarException e) {
				user.addToProgressDisplay("解压错误，当前解压中文件为" + file.getName());
				failedList.add(file.getName());
				//e.printStackTrace();
				//user.clearBusy();
				//return;
			} catch (IOException e) {
				user.addToProgressDisplay("文件读取/输出错误，当前解压中文件为" + file.getName());
				e.printStackTrace();
				user.clearBusy();
				return;
			} catch (Exception e) {
				user.addToProgressDisplay("未知错误:" + e.getMessage());
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
				user.addToProgressDisplay(fileName + "不包含正常学号，略过");
				continue;
			}
			
			user.addToProgressDisplay("正在提取" + fileName);
			try {
				uncompressor.unzipFileThenRenameToZipName(file.getAbsolutePath());
			} catch (IOException e) {
				user.addToProgressDisplay("文件读取/输出错误，当前解压中文件为" + file.getName());
				failedList.add(file.getName());
			} catch (Exception e) {
				user.addToProgressDisplay("未知错误:" + e.getMessage() + ",错误类型:" + e.getClass().toString());
				e.printStackTrace();
				user.clearBusy();
				return;
			}
		}
		
		user.addToProgressDisplay("能提取的文件都已经提取完成了。");
		if (failedList.size() > 0) {
			user.addToProgressDisplay("提取失败的文件：");
			for(String name : failedList) {
				user.addToProgressDisplay(name);
			}
		}
	}
}
