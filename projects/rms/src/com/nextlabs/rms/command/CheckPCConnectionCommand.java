package com.nextlabs.rms.command;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import com.nextlabs.nxl.exception.NXRTERROR;
import com.nextlabs.nxl.pojos.ConnectionResult;
import com.nextlabs.nxl.pojos.PolicyControllerDetails;
import com.nextlabs.rms.eval.EvaluationAdapterFactory;
import com.nextlabs.rms.eval.IEvalAdapter;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.nxl.decrypt.CryptManager;
import com.nextlabs.rms.sharedutil.OperationResult;

public class CheckPCConnectionCommand extends AbstractCommand {

	private static Logger logger = Logger.getLogger(CheckPCConnectionCommand.class);
	public String errorMessage="";
	
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String keyStoreFilePath=request.getParameter("km_rmi_keystoreFile");
		String trustStoreFilePath=request.getParameter("km_rmi_truststoreFile");
		String kmpcHostName=request.getParameter("km_policy_controller_hostname");
		String kmRMIPort= request.getParameter("km_rmi_port_number");
		String keyStorePass=request.getParameter("km_rmi_keystorePass");
		String trustStorePass=request.getParameter("km_rmi_truststorePass");
		String evalpcHostName=request.getParameter("eval_policy_controller_hostname");
		String evalRMIPort= request.getParameter("eval_rmi_port_number");
		
		if(kmRMIPort==null || kmRMIPort.length()<=0)
			kmRMIPort="1099";
		if(evalRMIPort==null || evalRMIPort.length()<=0)
			evalRMIPort="1099";
		
		OperationResult result = new OperationResult();
		boolean isCertPresent = false;
		try{
			isCertPresent=checkCertFiles(keyStoreFilePath,trustStoreFilePath);
		} catch(Exception e){	
			errorMessage=e.getMessage();
			logger.error(errorMessage);
			sendErrorResponse(result, response);
		}
		if(isCertPresent){
			IEvalAdapter adapter=EvaluationAdapterFactory.getInstance().getTestAdapter(EvaluationAdapterFactory.COMM_TYPE_RMI,evalpcHostName,Integer.parseInt(evalRMIPort));
			if(adapter.getConnStatus()!=null)
				errorMessage=adapter.getConnStatus();
			if(errorMessage==""){
				logger.info("Policy Controller Connection for Policy Evaluation is Successful");
				PolicyControllerDetails pcDetails=new PolicyControllerDetails();
				pcDetails.setPcHostName(kmpcHostName);
				pcDetails.setRmiPortNum(Integer.parseInt(kmRMIPort));
				pcDetails.setKeyStoreName(keyStoreFilePath);
				pcDetails.setKeyStorePassword(keyStorePass);
				pcDetails.setTrustStoreName(trustStoreFilePath);
				pcDetails.setTrustStorePasswd(trustStorePass);
				if(checkKeyManagement(pcDetails)){
					result.setResult(true);
					result.setMessage("");
					JsonUtil.writeJsonToResponse(result, response);
					return;
				}
				else {
					logger.error("Key Management Connection Failed");
					sendErrorResponse(result, response);
				}
			} else if (errorMessage.equals(IEvalAdapter.CONN_ERR_UNKNOWN_HOST)){
				logger.error(errorMessage);
				errorMessage="Rights Management Server could not connect to a Policy Controller with the specified Hostname. Verify that your Policy Controller Hostname is correct.";
				sendErrorResponse(result, response);
			} else {
				logger.error(errorMessage);
				errorMessage="Rights Management Server could not connect to the Policy Controller. Verify that the port number is correct and ensure that the Policy Controller is running.";
				sendErrorResponse(result, response);
			}
			
		}
	}
	
	private void sendErrorResponse(OperationResult result, HttpServletResponse response) {
		result.setResult(false);
		result.setMessage(errorMessage);
		JsonUtil.writeJsonToResponse(result, response);
		return;
	}
	
	private boolean checkKeyManagement(PolicyControllerDetails pcDetails) throws NXRTERROR{
		ConnectionResult kmConnection = CryptManager.testConnection(pcDetails);		
		switch(kmConnection){
			case SUCCESS:
				errorMessage="";
				return true;
			case MISSING_CONFIGURATION_ERROR:
				errorMessage="Please verify your configuration settings.";
				break;
			case CONNECTION_ERROR:
				errorMessage="Rights Management Server could not connect to the Key Management Service. Verify that the port number is correct.";
				break;
			case CERTIFICATE_ERROR:
				errorMessage="The passwords specified for your certificate files (KeyStore or TrustStore or both) are incorrect.";
				break;
			case GENERAL_ERROR: default:
				errorMessage="Rights Management Server could not connect to the Key Management Service. Verify your configuration settings";
				break;
		}
		return false;
	}
	
	private boolean checkCertFiles(String keyStoreFilePath, String trustStoreFilePath) throws RMSException  {
		if(keyStoreFilePath==null||trustStoreFilePath==null){
			throw new RMSException("You must specify the file paths for the KeyStore and TrustStore files.");
		}
		File keyStoreFile=new File(keyStoreFilePath);
		if(!keyStoreFile.exists()){
			throw new RMSException("The KeyStore file does not exist at the specified file path.");
		}
		File trustStoreFile=new File(trustStoreFilePath);
		if(!trustStoreFile.exists()){
			throw new RMSException("The TrustStore file does not exist at the specified file path.");
		}
		return true;
	}	
}
