package oocourse.system;

import java.io.FileOutputStream;

import oocourse.system.OOMailDelivery.TaskType;

public abstract class MyAbortableThread extends Thread {
	
	protected boolean abort;
	protected String mainPath;
	protected OOMailDelivery user;
	protected String homeworkIndex;
	protected int startIndex;
	protected String suffix;
	protected TaskType taskType;
	protected String announcement;
	protected boolean isAboutPrinciple;
	protected FileOutputStream sendRecorder = null;
	
	public MyAbortableThread(String mainPath, OOMailDelivery user, String homeworkIndex,
			TaskType taskType, boolean isAboutPrinciple) {
		abort = false;
		this.mainPath = mainPath;
		this.user = user;
		this.homeworkIndex = homeworkIndex;
		this.taskType = taskType;
		this.isAboutPrinciple = isAboutPrinciple;
	}
	
	public void abort() {
		abort = true;
	}
}
