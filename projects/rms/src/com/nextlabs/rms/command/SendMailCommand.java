package com.nextlabs.rms.command;

import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.wrapper.util.SMTPClient;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

public class SendMailCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(SendMailCommand.class);

	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
        String firstName=request.getParameter("firstName");
        String lastName=request.getParameter("lastName");
        String emailId=request.getParameter("emailId");
        String company=request.getParameter("company");
        String sponsor= request.getParameter("sponsor");
        String sponsorEmail=request.getParameter("sponsorEmail");
        String reason= request.getParameter("reason");
		SMTPClient mailClient = SMTPClient.getInstance();
		String mailSubject=ConfigManager.getInstance(GlobalConfigManager.DEFAULT_TENANT_ID).getStringProperty(ConfigManager.REGN_EMAIL_SUBJECT);
		String[] toMailArr = {ConfigManager.getInstance(GlobalConfigManager.DEFAULT_TENANT_ID).getStringProperty(ConfigManager.RMS_ADMIN_EMAILID)};
		String[] ccMailArr = {sponsorEmail.trim()};
		logger.debug("Sending registration email for user:"+firstName+ " " +lastName+". Sponsor mail id:"+sponsorEmail);
		String mailText = getMailText(firstName, lastName, emailId, company, sponsor, sponsorEmail, reason);
		Properties smtpProps = ConfigManager.getInstance(GlobalConfigManager.DEFAULT_TENANT_ID).getSmtpServerProps();
		boolean res = mailClient.sendEmail(toMailArr, ccMailArr, mailSubject, mailText,smtpProps);
		if(res){
            logger.info("Registration email for user: " + firstName + " " + lastName +" sent to administrator");
			response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/register.jsp?success="+RMSMessageHandler.getClientString("regnMailSuccess"));
		}else{
            logger.info("Error occurred while sending Registration email for user: " + firstName + " " + lastName);
			response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/register.jsp?error="+RMSMessageHandler.getClientString("regnMailErr"));
		}
	}
	
	private String getMailText(String firstName, String lastName,
			String emailId, String company, String sponsor,
			String sponsorEmail, String justification) {
		String fullName = firstName + " " + lastName;
		StringBuffer sb = new StringBuffer();
        sb.append("Dear RMS Administrator,\n");
        sb.append("'" + fullName);
        sb.append("' has requested access to RMS. Here are the details of the request:\n\n First Name: ");
        sb.append(firstName);
        sb.append("\n Last Name: ");
        sb.append(lastName);
        sb.append("\n Email id: ");
        sb.append(emailId);
        sb.append("\n Company: ");
        sb.append(company);
        sb.append("\n Sponsor: ");
        sb.append(sponsor);
        sb.append("\n Sponsor Email id: ");
        sb.append(sponsorEmail);
        sb.append("\n Justification: ");
        sb.append(justification);
        sb.append("\n\n Kindly validate the request and do the needful.");
        sb.append("\n\n\n PS: This is an automated email from RMS. Do not reply to this email.");
        String mailText = sb.toString();
		return mailText;
	}

}
