package oocourse.system;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//TODO 优化界面，减少按钮数目
public class OOMailDelivery extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7428607180710820042L;
	
	private String version = "V2.5";
	
	private String homeworkIndex;
	private int startIndex;
	private String announcement = "";
	
	private boolean busy;
	
	private MyAbortableThread currentTask = null;
	
	public enum TaskType{PROGRAM, RESULT, APPEAL, SENDBACK}; 
	
	private String mainPath = "E:/OOHomework/";
	
	private HashMap<String, String> addressMap;
	
	private boolean isAboutPrinciple = false;
	
	private JTextField homeworkIndexInput = new JTextField();
	private JTextField homeworkIndexInputPrefix = new JTextField("第");
	private JTextField homeworkIndexInputSuffix = new JTextField("次作业");
	private JTextField mainPathDisplayPrefix = new JTextField("主路径：");
	private JTextField mainPathDisplay = new JTextField();
	private JTextField startIndexInputPrefix = new JTextField("开始的序号/学号：");
	private JTextField startIndexInput =  new JTextField("0");
	private JButton recieveProgram = new JButton("接收程序作业");
	private JButton recieveTestResult = new JButton("接收测试结果");
	private JButton recieveAppeal = new JButton("接收申诉");
	private JButton sendProgram = new JButton("分发程序");
	private JButton sendTestResult = new JButton("回发测试结果");
	private JButton selectMainPath = new JButton("选择主路径");
	private JButton abortTask = new JButton("中断当前任务");
	private JButton createProgramList = new JButton("生成作业名单");
	private JButton createResultList = new JButton("生成测试名单");
	private JButton deleteRepitition = new JButton("删除重复文件");
	private JButton notifyUndone = new JButton("提示未交");
	private JButton extractFiles = new JButton("提取java和txt文件");
	private JButton emergencyNotice = new JButton("紧急通知");
	private JButton sendBack = new JButton("回发程序");
	private JButton testFileNames = new JButton("检查文件名");
	private JButton tempFunction = new JButton("临时功能");
	private JTextArea progressDisplay = new JTextArea("准备就绪！");
	private JTextArea announcementInput = new JTextArea(announcement);
	private JScrollPane scrollPaneForProgressDisplay;
	private JScrollPane scrollPaneForAnnouncementInput;
	private JToggleButton bugOrPrincipleToggleButton = new JToggleButton("测试");
	
	private Scanner scanner;

	public OOMailDelivery() {
		setTitle("Object-Oriented Class Assignment System " + version + "");
		setBounds(300,100,920,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(false);
		
		busy = false;
		
		//作业次数号
		homeworkIndexInputPrefix.setBounds(10, 10, 20, 40);
		homeworkIndexInputPrefix.setEditable(false);
		homeworkIndexInputPrefix.setBorder(null);
		add(homeworkIndexInputPrefix);
		homeworkIndexInput.setBounds(30, 10, 40, 40);
		homeworkIndexInput.setEditable(true);
		add(homeworkIndexInput);
		homeworkIndexInputSuffix.setBounds(80, 10, 40, 40);
		homeworkIndexInputSuffix.setEditable(false);
		homeworkIndexInputSuffix.setBorder(null);
		add(homeworkIndexInputSuffix);
		
		//测试或原则检查
		bugOrPrincipleToggleButton.setBounds(130, 10, 80, 40);
		bugOrPrincipleToggleButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (isAboutPrinciple) {
					isAboutPrinciple = false;
					bugOrPrincipleToggleButton.setText("测试");
				} else {
					isAboutPrinciple = true;
					System.out.println("Toggled");
					bugOrPrincipleToggleButton.setText("原则");
				}
			}
		});
		add(bugOrPrincipleToggleButton);
		
		//开始检索的邮件序号
		startIndexInputPrefix.setBounds(230, 10, 120, 40);
		startIndexInputPrefix.setEditable(false);
		startIndexInputPrefix.setBorder(null);
		add(startIndexInputPrefix);
		startIndexInput.setBounds(350, 10, 40, 40);
		startIndexInput.setEditable(true);
		add(startIndexInput);
		
		//接收路径
		mainPathDisplayPrefix.setBounds(10, 60, 40, 40);
		mainPathDisplayPrefix.setEditable(false);
		mainPathDisplayPrefix.setBorder(null);
		add(mainPathDisplayPrefix);
		mainPathDisplay.setBounds(60, 60, 215, 40);
		mainPathDisplay.setEditable(false);
		mainPathDisplay.setText(mainPath);
		add(mainPathDisplay);
		selectMainPath.setBounds(280, 60, 115, 40);
		add(selectMainPath);
		selectMainPath.addActionListener(this);
		
		//接收程序作业按钮
		recieveProgram.setBounds(10, 110, 125, 40);
		add(recieveProgram);
		recieveProgram.addActionListener(this);
		
		//接收测试结果按钮
		recieveTestResult.setBounds(140, 110, 125, 40);
		add(recieveTestResult);
		recieveTestResult.addActionListener(this);
		
		//接收申诉按钮
		recieveAppeal.setBounds(270, 110, 125, 40);
		add(recieveAppeal);
		recieveAppeal.addActionListener(this);
		
		//分发程序按钮
		sendProgram.setBounds(10, 160, 125, 40);
		add(sendProgram);
		sendProgram.addActionListener(this);
		
		//回发测试结果按钮
		sendTestResult.setBounds(140, 160, 125, 40);
		add(sendTestResult);
		sendTestResult.addActionListener(this);
		
		//删除重复文件按钮
		deleteRepitition.setBounds(270, 160, 125, 40);
		add(deleteRepitition);
		deleteRepitition.addActionListener(this);
		
		//进度提示窗
		progressDisplay.setEditable(false);
		scrollPaneForProgressDisplay = new JScrollPane(progressDisplay);
		scrollPaneForProgressDisplay.setBounds(400, 10, 500, 550);
		add(scrollPaneForProgressDisplay);
		
		//生成程序作业名单
		createProgramList.setBounds(10, 210, 125, 40);
		add(createProgramList);
		createProgramList.addActionListener(this);
		
		//生成测试结果名单
		createResultList.setBounds(140, 210, 125, 40);
		add(createResultList);
		createResultList.addActionListener(this);
		
		//提示未交
		notifyUndone.setBounds(270, 210, 125, 40);
		add(notifyUndone);
		notifyUndone.addActionListener(this);
		
		//提取.java和.txt文件
		extractFiles.setBounds(10, 260, 125, 40);
		add(extractFiles);
		extractFiles.addActionListener(this);
		
		//紧急通知
		emergencyNotice.setBounds(140, 260, 125, 40);
		add(emergencyNotice);
		emergencyNotice.addActionListener(this);
		
		//中断任务
		abortTask.setBounds(270, 260, 125, 40);
		add(abortTask);
		abortTask.addActionListener(this);
		
		//测试文件名
		testFileNames.setBounds(10, 310, 125, 40);
		add(testFileNames);
		testFileNames.addActionListener(this);
		
		//回发程序
		sendBack.setBounds(140, 310, 125,40);
		add(sendBack);
		sendBack.addActionListener(this);
		
		//临时功能
		tempFunction.setBounds(270, 310, 125,40);
		add(tempFunction);
		tempFunction.addActionListener(this);
		
		//公告输入口
		try {
			scanner = new Scanner(new File(mainPath + "announcement.txt"));
			while (scanner.hasNext()) {
				announcement = announcement + scanner.nextLine() + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		announcementInput.setText(announcement);
		announcementInput.setEditable(true);
		scrollPaneForAnnouncementInput = new JScrollPane(announcementInput);
		scrollPaneForAnnouncementInput.setBounds(10, 360, 380, 180);
		announcementInput.setLineWrap(true);
		add(scrollPaneForAnnouncementInput);
		
		setVisible(true);
	}
	
	public static void main(String args[]) {
		new OOMailDelivery();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (selectMainPath == e.getSource()) {
			JFileChooser outputDirectorySelector = new JFileChooser();
			outputDirectorySelector.setCurrentDirectory(new File("C:/"));
			outputDirectorySelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = outputDirectorySelector.showOpenDialog(null);
			if(result == JFileChooser.APPROVE_OPTION) {
				mainPath = outputDirectorySelector.getSelectedFile().getAbsolutePath() + "\\";
				mainPathDisplay.setText(mainPath);
			}
			scanner = new Scanner(mainPath + "announcement");
			if (scanner != null) {
				while (scanner.hasNext()) {
					announcement = announcement + scanner.nextLine();
				}
			}
			announcementInput.setText(announcement);
			return;
		}
		
		if(abortTask == e.getSource()) {
			if(busy) {
				currentTask.abort();
				currentTask = null;
			}
			return;
		}
		
		if(busy) {
			addToProgressDisplay("上一次任务还未完成，请耐心等候");
			return;
		}
		
		if(startIndexInput.getText().isEmpty()) {
			setStartIndex(0);
		} else {
			startIndex = new Integer(startIndexInput.getText());
			if(startIndex < 0) {
				setStartIndex(0);
			}
		}
		
		mainPath = mainPathDisplay.getText();
		
		if (addressMap == null) {
			addressMap  = new HashMap<String, String>();
			String number;
			String address;
			try {
				Scanner addressScanner = new Scanner(new File(mainPath + "Email Address List.txt"));
				while(addressScanner.hasNext()) {
					number = addressScanner.next();
					if(!addressScanner.hasNext()) {
						break;
					}
					address = addressScanner.next();
					if(!addressMap.containsKey(number)) {
						addressMap.put(number, address);
					}
				}
				addressScanner.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				addToProgressDisplay("邮件列表" + mainPath + "Emain Address List.txt不存在");
				return;
			}
		}
		
		
		if(!announcement.equals(announcementInput.getText())) {
			announcement = announcementInput.getText();
			try {
				FileOutputStream announcementOutputStream = new FileOutputStream(new File(mainPath + "announcement.txt"));
				announcementOutputStream.write(announcement.getBytes());
				announcementOutputStream.close();
			} catch (IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(homeworkIndexInput.getText().isEmpty()) {
			addToProgressDisplay("请输入作业次数号");
			return;
		}
		homeworkIndex = homeworkIndexInput.getText();


		if(recieveAppeal == e.getSource()) {
			addToProgressDisplay("开始收集申诉结果，于目录" + mainPath + "申诉" + homeworkIndex + "/");
			ReadMailSSL readMail = new ReadMailSSL("inbox", mainPath + "申诉" + homeworkIndex + "/",
					"申诉" + homeworkIndex, this, TaskType.APPEAL, startIndex);
			currentTask = readMail;
			readMail.start();
		} else if(createProgramList == e.getSource()) {
			addToProgressDisplay("开始生成程序作业列表，于目录" + mainPath + "作业" + homeworkIndex + "/");
			CreateList createList = new CreateList(mainPath + "作业" + homeworkIndex + "/", this, homeworkIndex,
					TaskType.PROGRAM, addressMap, isAboutPrinciple);
			currentTask = createList;
			createList.start();
		} else if(createResultList == e.getSource()) {
			addToProgressDisplay("开始生成测试结果列表，于目录" + mainPath + "测试" + homeworkIndex + "/");
			CreateList createList = new CreateList(mainPath + "测试" + homeworkIndex + "/", this, homeworkIndex,
					TaskType.RESULT, addressMap, isAboutPrinciple);
			currentTask = createList;
			createList.start();
		} else if(recieveProgram == e.getSource()) {
			addToProgressDisplay("开始收集第" + homeworkIndex + "次程序作业，于目录" + mainPath + "作业" + homeworkIndex + "/");
			ReadMailSSL readMail = new ReadMailSSL("程序作业", mainPath + "作业" + homeworkIndex + "/",
					"作业" + homeworkIndex, this, TaskType.PROGRAM, startIndex);
			currentTask = readMail;
			readMail.start();
		} else if(recieveTestResult == e.getSource()) {
			addToProgressDisplay("开始收集第" + homeworkIndex + "次测试结果，于目录" + mainPath + "测试" + homeworkIndex + "/");
			ReadMailSSL readMail = new ReadMailSSL("测试作业", mainPath + "测试" + homeworkIndex + "/",
					"测试" + homeworkIndex, this, TaskType.RESULT, startIndex);
			currentTask = readMail;
			readMail.start();
		} else if(sendProgram == e.getSource()) {
			addToProgressDisplay("开始分发" + (isAboutPrinciple ? "原则" : "测试") + "作业，于目录" + mainPath + "作业" +
					homeworkIndex + "/");
			Delivery delivery = new Delivery(mainPath + "作业" + homeworkIndex + "/", homeworkIndex, TaskType.PROGRAM,
					startIndex, this, announcement, isAboutPrinciple);
			currentTask = delivery;
			delivery.start();
		} else if(sendTestResult == e.getSource()) {
			addToProgressDisplay("开始回发" + (isAboutPrinciple ? "原则结果" : "测试结果") + "，于目录" + mainPath + "测试" +
					homeworkIndex + "/");
			Delivery delivery = new Delivery(mainPath + "测试" + homeworkIndex + "/", homeworkIndex, TaskType.RESULT,
					startIndex, this, announcement, isAboutPrinciple);
			currentTask = delivery;
			delivery.start();
		} else if (deleteRepitition == e.getSource()) {
			addToProgressDisplay("开始删除重复文件，于目录" + mainPath + "测试" + homeworkIndex + "/");
			DeleteRepetition deleteRepetition = new DeleteRepetition(mainPath + "测试" + homeworkIndex + "/",
					homeworkIndex, TaskType.RESULT, startIndex, this, announcement, isAboutPrinciple);
			currentTask = deleteRepetition;
			deleteRepetition.start();
		} else if (notifyUndone == e.getSource()) {
			addToProgressDisplay("开始提示未交，于目录" + mainPath + "作业" + homeworkIndex + "/");
			NotifyUndone notifyUndone = new NotifyUndone(mainPath + "作业" + homeworkIndex + "/", this, homeworkIndex, addressMap);
			currentTask = notifyUndone;
			notifyUndone.start();
		} else if (extractFiles == e.getSource()) {
			addToProgressDisplay("开始提取.java和.txt文件，于目录" + mainPath + "作业" + homeworkIndex + "/");
			ExtractJavaAndText extractJavaAndText = new ExtractJavaAndText(mainPath + "作业" + homeworkIndex + "/",
					this, homeworkIndex);
			currentTask = extractJavaAndText;
			extractJavaAndText.start();
		} else if (emergencyNotice == e.getSource()) {
			addToProgressDisplay("开始发布紧急通知");
			EmergencyNoticer emegencyNoticer = new EmergencyNoticer(mainPath, this, announcement, startIndex);
			currentTask = emegencyNoticer;
			emegencyNoticer.start();
		} else if (testFileNames == e.getSource()) {
			TestFileNames tester = new TestFileNames(mainPath + "作业" + homeworkIndex + "/", this);
			currentTask = tester;
			tester.start();
		} else if (sendBack == e.getSource()) {
			addToProgressDisplay("开始回发作业，于目录" + mainPath + "作业" + homeworkIndex + "/");
			Delivery delivery = new Delivery(mainPath + "作业" + homeworkIndex + "/", homeworkIndex, TaskType.SENDBACK,
					startIndex, this, announcement, isAboutPrinciple);
			currentTask = delivery;
			delivery.start();
		} else if (tempFunction == e.getSource()) {
			addToProgressDisplay("开始执行临时功能，于目录" + mainPath + "作业" + homeworkIndex + "/");
			TempFunction tempFunction = new TempFunction(mainPath + "作业" + homeworkIndex + "/", this, homeworkIndex,
					null, false, addressMap);
			currentTask = tempFunction;
			tempFunction.start();
		}
	}
	
	protected void setBusy() {
		busy = true;
	}
	
	protected void clearBusy() {
		busy = false;
	}
	
	protected void setStartIndex(int newIndex) {
		startIndex = newIndex;
		startIndexInput.setText(String.valueOf(startIndex));
	}
	
	protected void addToProgressDisplay(String message) {
		progressDisplay.setText(progressDisplay.getText() + "\n" + message);
	    this.scrollPaneForProgressDisplay.getViewport()
	    	.setViewPosition(new Point(0, this.progressDisplay.getLineCount() * 100));
	}
}