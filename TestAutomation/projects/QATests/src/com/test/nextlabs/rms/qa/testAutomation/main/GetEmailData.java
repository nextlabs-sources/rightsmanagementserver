package com.test.nextlabs.rms.qa.testAutomation.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.sun.mail.util.MailSSLSocketFactory;
import com.test.nextlabs.rms.qa.testAutomation.common.RMSUtility;

public class GetEmailData {
	public static String smtpHost;
	public static String smtpUserName;
	public static String smtpPassword;
	public static String smtpPort;
	public static String smtpAuth;
	public static String smtpStarttlsEnable;
	public static String emailTo;
	public static String emailCC;
	public static String emailSubject;
	public static String report;
	public static String rmsServer;
	
	public static void getData()
	{
		try
		{
			//create Document object to read xml file
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File("config/emailConfig.xml"));
			
			//get root element
			Element root = document.getRootElement();
			
			//get all chile elements
			List<Element> childElemements = root.elements();
			
			//get element value
			for(Element child : childElemements)
			{
				smtpHost = child.elementText("SMTPHost");
				smtpUserName = child.elementText("SMTPUserName");
				smtpPassword = child.elementText("SMTPPassword");
				smtpPort = child.elementText("SMTPPort");
				smtpAuth = child.elementText("SMTPAuth");
				smtpStarttlsEnable = child.elementText("SMTPStarttlsEnable");
				emailTo = child.elementText("EmailTo");
				emailCC = child.elementText("EmailCC");
				//emailSubject = child.elementText("EmailSubject")+RMSUtility.getRMSVersionNumber();
				emailSubject = child.elementText("EmailSubject");
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sendEmail()
	{
		
		try
		{
			
			Properties props = new Properties();
			
			//using properties to store SMTP server information
			props.put("mail.smtp.host",smtpHost);
			props.put("mail.smtp.user", smtpUserName);
			props.put("mail.smtp.password", smtpPassword);
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.smtp.auth", smtpAuth);
			props.put("mail.smtp.starttls.enable", smtpStarttlsEnable);

			String host = props.getProperty("mail.smtp.host");
			String userName = props.getProperty("mail.smtp.user");
			String password = props.getProperty("mail.smtp.password");
			String tlsEnabled = (String)props.get("mail.smtp.starttls.enable"); 
			
			if(tlsEnabled!=null && tlsEnabled.equalsIgnoreCase("true"))
			{
				props.put("mail.smtp.ssl.checkserveridentity", "false");
				
				MailSSLSocketFactory msf = new MailSSLSocketFactory();
				msf.setTrustAllHosts(true);
				props.put("mail.smtp.ssl.socketFactory", msf);
			}
			
			//Session session = Session.getInstance(props);
			Session session = Session.getDefaultInstance(props,new Authenticator(){
				
				protected PasswordAuthentication getPasswordAuthentication(){
					return new PasswordAuthentication("nextlabs@163.com","123blue!");
				}
			});
		
			//create message, contain email subject and text
			MimeMessage message = new MimeMessage(session);
			
			//set the user who send email
			message.setFrom(new InternetAddress(userName));
			
			String[] toEmails = getEmails(emailTo);
			String[] ccEmails = getEmails(emailCC);
			
			//add users who receive email
			if(toEmails!=null)
			{
				InternetAddress[] toAddress = new InternetAddress[toEmails.length];
				for(int i=0; i<toEmails.length; i++)
				{
					toAddress[i] = new InternetAddress(toEmails[i]);
				}
				for(int i=0; i<toAddress.length; i++)
				{
					message.addRecipient(Message.RecipientType.TO, toAddress[i]);
				}
			}
			
			//add cc user who receive email
			if(ccEmails!=null)
			{
				InternetAddress[] ccAddress = new InternetAddress[ccEmails.length];
				for(int i=0; i<ccEmails.length; i++)
				{
					ccAddress[i] = new InternetAddress(ccEmails[i]);
				}
				for(int i=0; i<ccAddress.length; i++)
				{
					message.addRecipient(Message.RecipientType.CC, ccAddress[i]);
				}
			}
			
			//set email subject
			message.setSubject(emailSubject, "UTF-8");
			
			//create multipart object, set email text and attachment
			Multipart m = new MimeMultipart();
			
			//set email text content
			BodyPart contentPart = new MimeBodyPart();
			
			//"Hi, all\n This is a testing email, ignore it\n you can view the test result index.html from report.zip\n Below is test rms service result" +"\n"+ 
			contentPart.setText("Hi, all\n This is a testing email, ignore it\n"+getRMSServiceResult());
			m.addBodyPart(contentPart);
			
			//add attachment
			BodyPart attachmentFile = new MimeBodyPart();
			DataSource source = new FileDataSource("test-output/report.zip");
			attachmentFile.setDataHandler(new DataHandler(source));
			
			//add attachment title
			attachmentFile.setFileName(source.getName());
			m.addBodyPart(attachmentFile);
			
			//add multipart to message object
			message.setContent(m);
			
			//save email
			message.saveChanges();
			
			//create transport object to send email
			Transport transport = session.getTransport("smtp");
			
			//connect to server
			transport.connect(host, userName, password);
			
			//send email
			transport.send(message, message.getAllRecipients());
			
			transport.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String[] getEmails(String email)
	{
		String[] emails = null;
		
		if(email!=null && !(email.equals("")))
		{
			emails = email.split(";");
		}
		return emails;
	}
	
	public static String getRMSServiceResult()
	{
		String content = "";
		StringBuilder sb = new StringBuilder();
		try
		{
			//get rms server address
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File("config/Config.xml"));
			
			//get root element
			Element root = document.getRootElement();
			
			//get all chile elements
			List<Element> childElemements = root.elements();
			
			//get element value
			for(Element child : childElemements)
			{
				rmsServer = child.elementText("RMSServer");
			}
			
			System.out.println(rmsServer);
			
			// get result to string variable
			File file = new File("\\\\"+rmsServer+"\\RMSTestAutomation\\results\\devTestResult.txt");
			
			BufferedReader bf = new BufferedReader(new FileReader(file));	
			
			while(content!=null)
			{
				content = bf.readLine();
				
				if(content == null)
				{
					break;
				}
				
				sb.append(content.trim()+"\n");
			}
			
			bf.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
}
