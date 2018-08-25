package oocourse.system;
 
import java.io.UnsupportedEncodingException;
import java.util.Properties;
 
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
 
//TODO ���̷߳���
public class SendMailTLS {
	final static int serverNumber = 3;
	 
	final static String[] username = {"@outlook.com", "@126.com", "@qq.com"};
	final static String[] password = {"", "", ""};
	final static String[] server = {"smtp.live.com", "smtp.126.com", "smtp.qq.com"};
	final static String[] port = {"25", "25", "465"};
	
	public static void send(String subject, String destination, String text, String filePath, String fileName, OOMailDelivery user)
			throws AddressException, MessagingException, UnsupportedEncodingException {
		
		int serverIndex = 0;
		int retry = 0;
		boolean flag = true;
		while(flag) {
			user.addToProgressDisplay("ͨ��������" + server[serverIndex] + "���ͣ��û�����" + username[serverIndex]);
			try {
				send(subject, destination, text, filePath, fileName, serverIndex);
				flag = false;
			} catch(MessagingException e) {
				if(serverIndex == serverNumber) {
					throw e;
				} else if(retry == 2){
					user.addToProgressDisplay("������" + server[serverIndex] + "����ʧ�ܣ��Զ�������һ��������");
					++serverIndex;
					retry = 0;
				} else {
					user.addToProgressDisplay("������" + server[serverIndex] + "�����Զ�����");
					++retry;
				}
			} 
		}
	}
 
	private static void send(String subject, String destination, String text, String filePath, String fileName, final int serverIndex)
					throws AddressException, MessagingException, UnsupportedEncodingException {
 
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", server[serverIndex]);
		props.put("mail.smtp.port", port[serverIndex]);
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username[serverIndex], password[serverIndex]);
			}
		  });
		
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(username[serverIndex]));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));
		message.setSubject(subject);
		
		MimeMultipart mimeMultipart = new MimeMultipart();
		
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		
		mimeBodyPart.setText(text);
		
		mimeMultipart.addBodyPart(mimeBodyPart);
		
		mimeBodyPart = new MimeBodyPart();
		
		if(filePath != null) {
			FileDataSource fds = new FileDataSource(filePath);

			mimeBodyPart.setFileName(MimeUtility.encodeText(fileName, "utf-8", "B"));
			mimeBodyPart.setDataHandler(new DataHandler(fds));
			
			mimeMultipart.addBodyPart(mimeBodyPart);
		}
		
		message.setContent(mimeMultipart, "multipart/*");

		Transport.send(message);
	}
}