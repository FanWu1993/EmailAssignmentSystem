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

//TODO �Ż����棬���ٰ�ť��Ŀ
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
	private JTextField homeworkIndexInputPrefix = new JTextField("��");
	private JTextField homeworkIndexInputSuffix = new JTextField("����ҵ");
	private JTextField mainPathDisplayPrefix = new JTextField("��·����");
	private JTextField mainPathDisplay = new JTextField();
	private JTextField startIndexInputPrefix = new JTextField("��ʼ�����/ѧ�ţ�");
	private JTextField startIndexInput =  new JTextField("0");
	private JButton recieveProgram = new JButton("���ճ�����ҵ");
	private JButton recieveTestResult = new JButton("���ղ��Խ��");
	private JButton recieveAppeal = new JButton("��������");
	private JButton sendProgram = new JButton("�ַ�����");
	private JButton sendTestResult = new JButton("�ط����Խ��");
	private JButton selectMainPath = new JButton("ѡ����·��");
	private JButton abortTask = new JButton("�жϵ�ǰ����");
	private JButton createProgramList = new JButton("������ҵ����");
	private JButton createResultList = new JButton("���ɲ�������");
	private JButton deleteRepitition = new JButton("ɾ���ظ��ļ�");
	private JButton notifyUndone = new JButton("��ʾδ��");
	private JButton extractFiles = new JButton("��ȡjava��txt�ļ�");
	private JButton emergencyNotice = new JButton("����֪ͨ");
	private JButton sendBack = new JButton("�ط�����");
	private JButton testFileNames = new JButton("����ļ���");
	private JButton tempFunction = new JButton("��ʱ����");
	private JTextArea progressDisplay = new JTextArea("׼��������");
	private JTextArea announcementInput = new JTextArea(announcement);
	private JScrollPane scrollPaneForProgressDisplay;
	private JScrollPane scrollPaneForAnnouncementInput;
	private JToggleButton bugOrPrincipleToggleButton = new JToggleButton("����");
	
	private Scanner scanner;

	public OOMailDelivery() {
		setTitle("Object-Oriented Class Assignment System " + version + "");
		setBounds(300,100,920,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setResizable(false);
		
		busy = false;
		
		//��ҵ������
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
		
		//���Ի�ԭ����
		bugOrPrincipleToggleButton.setBounds(130, 10, 80, 40);
		bugOrPrincipleToggleButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (isAboutPrinciple) {
					isAboutPrinciple = false;
					bugOrPrincipleToggleButton.setText("����");
				} else {
					isAboutPrinciple = true;
					System.out.println("Toggled");
					bugOrPrincipleToggleButton.setText("ԭ��");
				}
			}
		});
		add(bugOrPrincipleToggleButton);
		
		//��ʼ�������ʼ����
		startIndexInputPrefix.setBounds(230, 10, 120, 40);
		startIndexInputPrefix.setEditable(false);
		startIndexInputPrefix.setBorder(null);
		add(startIndexInputPrefix);
		startIndexInput.setBounds(350, 10, 40, 40);
		startIndexInput.setEditable(true);
		add(startIndexInput);
		
		//����·��
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
		
		//���ճ�����ҵ��ť
		recieveProgram.setBounds(10, 110, 125, 40);
		add(recieveProgram);
		recieveProgram.addActionListener(this);
		
		//���ղ��Խ����ť
		recieveTestResult.setBounds(140, 110, 125, 40);
		add(recieveTestResult);
		recieveTestResult.addActionListener(this);
		
		//�������߰�ť
		recieveAppeal.setBounds(270, 110, 125, 40);
		add(recieveAppeal);
		recieveAppeal.addActionListener(this);
		
		//�ַ�����ť
		sendProgram.setBounds(10, 160, 125, 40);
		add(sendProgram);
		sendProgram.addActionListener(this);
		
		//�ط����Խ����ť
		sendTestResult.setBounds(140, 160, 125, 40);
		add(sendTestResult);
		sendTestResult.addActionListener(this);
		
		//ɾ���ظ��ļ���ť
		deleteRepitition.setBounds(270, 160, 125, 40);
		add(deleteRepitition);
		deleteRepitition.addActionListener(this);
		
		//������ʾ��
		progressDisplay.setEditable(false);
		scrollPaneForProgressDisplay = new JScrollPane(progressDisplay);
		scrollPaneForProgressDisplay.setBounds(400, 10, 500, 550);
		add(scrollPaneForProgressDisplay);
		
		//���ɳ�����ҵ����
		createProgramList.setBounds(10, 210, 125, 40);
		add(createProgramList);
		createProgramList.addActionListener(this);
		
		//���ɲ��Խ������
		createResultList.setBounds(140, 210, 125, 40);
		add(createResultList);
		createResultList.addActionListener(this);
		
		//��ʾδ��
		notifyUndone.setBounds(270, 210, 125, 40);
		add(notifyUndone);
		notifyUndone.addActionListener(this);
		
		//��ȡ.java��.txt�ļ�
		extractFiles.setBounds(10, 260, 125, 40);
		add(extractFiles);
		extractFiles.addActionListener(this);
		
		//����֪ͨ
		emergencyNotice.setBounds(140, 260, 125, 40);
		add(emergencyNotice);
		emergencyNotice.addActionListener(this);
		
		//�ж�����
		abortTask.setBounds(270, 260, 125, 40);
		add(abortTask);
		abortTask.addActionListener(this);
		
		//�����ļ���
		testFileNames.setBounds(10, 310, 125, 40);
		add(testFileNames);
		testFileNames.addActionListener(this);
		
		//�ط�����
		sendBack.setBounds(140, 310, 125,40);
		add(sendBack);
		sendBack.addActionListener(this);
		
		//��ʱ����
		tempFunction.setBounds(270, 310, 125,40);
		add(tempFunction);
		tempFunction.addActionListener(this);
		
		//���������
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
			addToProgressDisplay("��һ������δ��ɣ������ĵȺ�");
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
				addToProgressDisplay("�ʼ��б�" + mainPath + "Emain Address List.txt������");
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
				// TODO �Զ����ɵ� catch ��
				e1.printStackTrace();
			}
		}
		
		if(homeworkIndexInput.getText().isEmpty()) {
			addToProgressDisplay("��������ҵ������");
			return;
		}
		homeworkIndex = homeworkIndexInput.getText();


		if(recieveAppeal == e.getSource()) {
			addToProgressDisplay("��ʼ�ռ����߽������Ŀ¼" + mainPath + "����" + homeworkIndex + "/");
			ReadMailSSL readMail = new ReadMailSSL("inbox", mainPath + "����" + homeworkIndex + "/",
					"����" + homeworkIndex, this, TaskType.APPEAL, startIndex);
			currentTask = readMail;
			readMail.start();
		} else if(createProgramList == e.getSource()) {
			addToProgressDisplay("��ʼ���ɳ�����ҵ�б���Ŀ¼" + mainPath + "��ҵ" + homeworkIndex + "/");
			CreateList createList = new CreateList(mainPath + "��ҵ" + homeworkIndex + "/", this, homeworkIndex,
					TaskType.PROGRAM, addressMap, isAboutPrinciple);
			currentTask = createList;
			createList.start();
		} else if(createResultList == e.getSource()) {
			addToProgressDisplay("��ʼ���ɲ��Խ���б���Ŀ¼" + mainPath + "����" + homeworkIndex + "/");
			CreateList createList = new CreateList(mainPath + "����" + homeworkIndex + "/", this, homeworkIndex,
					TaskType.RESULT, addressMap, isAboutPrinciple);
			currentTask = createList;
			createList.start();
		} else if(recieveProgram == e.getSource()) {
			addToProgressDisplay("��ʼ�ռ���" + homeworkIndex + "�γ�����ҵ����Ŀ¼" + mainPath + "��ҵ" + homeworkIndex + "/");
			ReadMailSSL readMail = new ReadMailSSL("������ҵ", mainPath + "��ҵ" + homeworkIndex + "/",
					"��ҵ" + homeworkIndex, this, TaskType.PROGRAM, startIndex);
			currentTask = readMail;
			readMail.start();
		} else if(recieveTestResult == e.getSource()) {
			addToProgressDisplay("��ʼ�ռ���" + homeworkIndex + "�β��Խ������Ŀ¼" + mainPath + "����" + homeworkIndex + "/");
			ReadMailSSL readMail = new ReadMailSSL("������ҵ", mainPath + "����" + homeworkIndex + "/",
					"����" + homeworkIndex, this, TaskType.RESULT, startIndex);
			currentTask = readMail;
			readMail.start();
		} else if(sendProgram == e.getSource()) {
			addToProgressDisplay("��ʼ�ַ�" + (isAboutPrinciple ? "ԭ��" : "����") + "��ҵ����Ŀ¼" + mainPath + "��ҵ" +
					homeworkIndex + "/");
			Delivery delivery = new Delivery(mainPath + "��ҵ" + homeworkIndex + "/", homeworkIndex, TaskType.PROGRAM,
					startIndex, this, announcement, isAboutPrinciple);
			currentTask = delivery;
			delivery.start();
		} else if(sendTestResult == e.getSource()) {
			addToProgressDisplay("��ʼ�ط�" + (isAboutPrinciple ? "ԭ����" : "���Խ��") + "����Ŀ¼" + mainPath + "����" +
					homeworkIndex + "/");
			Delivery delivery = new Delivery(mainPath + "����" + homeworkIndex + "/", homeworkIndex, TaskType.RESULT,
					startIndex, this, announcement, isAboutPrinciple);
			currentTask = delivery;
			delivery.start();
		} else if (deleteRepitition == e.getSource()) {
			addToProgressDisplay("��ʼɾ���ظ��ļ�����Ŀ¼" + mainPath + "����" + homeworkIndex + "/");
			DeleteRepetition deleteRepetition = new DeleteRepetition(mainPath + "����" + homeworkIndex + "/",
					homeworkIndex, TaskType.RESULT, startIndex, this, announcement, isAboutPrinciple);
			currentTask = deleteRepetition;
			deleteRepetition.start();
		} else if (notifyUndone == e.getSource()) {
			addToProgressDisplay("��ʼ��ʾδ������Ŀ¼" + mainPath + "��ҵ" + homeworkIndex + "/");
			NotifyUndone notifyUndone = new NotifyUndone(mainPath + "��ҵ" + homeworkIndex + "/", this, homeworkIndex, addressMap);
			currentTask = notifyUndone;
			notifyUndone.start();
		} else if (extractFiles == e.getSource()) {
			addToProgressDisplay("��ʼ��ȡ.java��.txt�ļ�����Ŀ¼" + mainPath + "��ҵ" + homeworkIndex + "/");
			ExtractJavaAndText extractJavaAndText = new ExtractJavaAndText(mainPath + "��ҵ" + homeworkIndex + "/",
					this, homeworkIndex);
			currentTask = extractJavaAndText;
			extractJavaAndText.start();
		} else if (emergencyNotice == e.getSource()) {
			addToProgressDisplay("��ʼ��������֪ͨ");
			EmergencyNoticer emegencyNoticer = new EmergencyNoticer(mainPath, this, announcement, startIndex);
			currentTask = emegencyNoticer;
			emegencyNoticer.start();
		} else if (testFileNames == e.getSource()) {
			TestFileNames tester = new TestFileNames(mainPath + "��ҵ" + homeworkIndex + "/", this);
			currentTask = tester;
			tester.start();
		} else if (sendBack == e.getSource()) {
			addToProgressDisplay("��ʼ�ط���ҵ����Ŀ¼" + mainPath + "��ҵ" + homeworkIndex + "/");
			Delivery delivery = new Delivery(mainPath + "��ҵ" + homeworkIndex + "/", homeworkIndex, TaskType.SENDBACK,
					startIndex, this, announcement, isAboutPrinciple);
			currentTask = delivery;
			delivery.start();
		} else if (tempFunction == e.getSource()) {
			addToProgressDisplay("��ʼִ����ʱ���ܣ���Ŀ¼" + mainPath + "��ҵ" + homeworkIndex + "/");
			TempFunction tempFunction = new TempFunction(mainPath + "��ҵ" + homeworkIndex + "/", this, homeworkIndex,
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