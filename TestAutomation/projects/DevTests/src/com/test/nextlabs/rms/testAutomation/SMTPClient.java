package com.test.nextlabs.rms.testAutomation;

import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.MailSSLSocketFactory;

public class SMTPClient {

	private static SMTPClient instance=new  SMTPClient();

	private SMTPClient() {		
	}
	
	public static SMTPClient getInstance(){
		return instance;
	}
	
	public boolean sendEmail(String[] toMailIds, String[] copyMailIds, String mailSubject, String mailText, Properties props) {		
        String from=props.getProperty("mail.smtp.user");
        String host=props.getProperty("mail.smtp.host");
        String pass=props.getProperty("mail.smtp.password");
        String tlsEnabled = (String)props.get("mail.smtp.starttls.enable"); 
        if(tlsEnabled!=null&&tlsEnabled.equalsIgnoreCase("true")){
//            props.setProperty("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.ssl.checkserveridentity", "false");
            MailSSLSocketFactory sf = null;
    		try {
    			sf = new MailSSLSocketFactory();
    		} catch (GeneralSecurityException e) {
    			System.out.println("Error occurred while creating MailSSLSocketFactory");
    		}
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.socketFactory", sf);        	
        }
        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from));
            if(toMailIds!=null){
	            InternetAddress[] toAddress = new InternetAddress[toMailIds.length];
	            // To get the array of addresses
	            for( int i = 0; i < toMailIds.length; i++ ) {
	                toAddress[i] = new InternetAddress(toMailIds[i]);
	            }
	            for( int i = 0; i < toAddress.length; i++) {
	                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
	            }
            }
            if(copyMailIds!=null){
	            InternetAddress[] ccAddress = new InternetAddress[copyMailIds.length];
	            // To get the array of addresses
	            for( int i = 0; i < copyMailIds.length; i++ ) {
	                ccAddress[i] = new InternetAddress(copyMailIds[i]);
	            }
	            for( int i = 0; i < ccAddress.length; i++) {
	                message.addRecipient(Message.RecipientType.CC, ccAddress[i]);
	            }
            }
            message.setSubject(mailSubject,"UTF-8");
            message.setHeader("Content-Type", "text/html; charset=\"utf-8\"");  
            message.setText(mailText, "UTF-8");
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        }
        catch (Exception e) {
        	System.out.println("Error occurred while sending email");
            return false;
        }
		
	}
	
}