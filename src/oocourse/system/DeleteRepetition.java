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
		
		this.suffix = ((taskType == TaskType.PROGRAM) ? "作业" : (isAboutPrinciple ? "原则" : "测试")) + homeworkIndex;
		this.startIndex = startIndex;
		this.announcement = announcement;
		this.isAboutPrinciple = isAboutPrinciple;
	}

	@SuppressWarnings({ "unused" })
	public void run() {
		
		user.setBusy();
		
		Scanner inScanner;
		try {
			inScanner = new Scanner(new File(mainPath + (isAboutPrinciple ? "原则" : "测试") + "in.txt"));
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			user.addToProgressDisplay("找不到配置文件" + mainPath + (isAboutPrinciple ? "原则" : "测试") + "in.txt，分发终止");
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
			
			user.addToProgressDisplay("正在检查学号为：" + number + "的文件");
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
				user.addToProgressDisplay(number + "未发现匹配文件！");
				continue;
			}
			
			String latest = candidate.get(0);
			user.addToProgressDisplay("发现匹配文件：" + latest);
			
			for(int j = 1; j < candidate.size(); ++j) {
				if(candidate.get(j).compareTo(latest) > 0) {
					user.addToProgressDisplay("发现匹配文件：" + candidate.get(j) + "，舍弃文件" + latest);
					if (new File(mainPath + latest).delete()) {
						user.addToProgressDisplay(latest + "删除成功");
					}
					latest = candidate.get(j);
				} else {
					user.addToProgressDisplay("舍弃文件" + candidate.get(j));
					if(new File(candidate.get(j) + latest).delete()) {
						user.addToProgressDisplay(candidate.get(j) + "删除成功");
					}
				}
			}
			
			user.addToProgressDisplay(number + "已检查完毕");
		}
		
		user.addToProgressDisplay("检查完成");
		
		inScanner.close();
		
		user.clearBusy();
	}
}
