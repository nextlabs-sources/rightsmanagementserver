package com.nextlabs.rms.eval;

import org.apache.log4j.Logger;

import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.eval.APIEvalAdapter;
import com.nextlabs.rms.eval.EvaluationAdapterFactory;
import com.nextlabs.rms.eval.IEvalAdapter;
import com.nextlabs.rms.eval.RMIEvalAdapter;

public class EvaluationAdapterFactory {

	public static final String COMM_TYPE_RMI = "COMM_TYPE_RMI";
	
	public static final String COMM_TYPE_API = "COMM_TYPE_API";
	
	private static EvaluationAdapterFactory instance = new EvaluationAdapterFactory();
	
	private IEvalAdapter rmiEvalAdapter = null;

	private IEvalAdapter apiEvalAdapter = null;
	
	private static Logger logger = Logger.getLogger(EvaluationAdapterFactory.class);
	
	private EvaluationAdapterFactory(){
	}
	
	public static EvaluationAdapterFactory getInstance(){
		return instance;
	}
	
	//forceCreate is true only when the EvalPCHostName or RMIPortNumber is changed
	public IEvalAdapter getAdapter(boolean forceCreate){
		boolean isRemotePC=ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getBooleanProperty(ConfigManager.ENABLE_REMOTE_PC);
		if(!isRemotePC){
			if(apiEvalAdapter==null||forceCreate){
				apiEvalAdapter = new APIEvalAdapter();
			}
			return apiEvalAdapter;
		}else{
			if(rmiEvalAdapter==null||forceCreate){
				rmiEvalAdapter = new RMIEvalAdapter(getEvalPCHostName(), getRMIPortNumber());
			}
			return rmiEvalAdapter;
		}
	}
	
	public IEvalAdapter getTestAdapter(String commType,String evalPCHostName,int rmiPortNumber){
		if(COMM_TYPE_API.equalsIgnoreCase(commType)){
			IEvalAdapter tempApiEvalAdapter = new APIEvalAdapter();
			return tempApiEvalAdapter;
		}else if(COMM_TYPE_RMI.equalsIgnoreCase(commType)){
			IEvalAdapter tempRmiEvalAdapter = new RMIEvalAdapter(evalPCHostName, rmiPortNumber);
			return tempRmiEvalAdapter;
		}
		return null;
	}
	
	private int getRMIPortNumber() {
		String rmiPort = ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.EVAL_RMI_PORT_NUMBER);
		if(rmiPort!=null && rmiPort.length()>0){
			try{
				int port = Integer.parseInt(rmiPort);
				return port;
			}catch(NumberFormatException e){
				logger.error("Invalid port number specified. Using the default - 1099 ",e);
				return 1099;
			}
		}
		return 1099;
	}
	
	private String getEvalPCHostName() {
		String evalPcHostName = ConfigManager.getInstance(ConfigManager.KMS_DEFAULT_TENANT_ID).getStringProperty(ConfigManager.EVAL_POLICY_CONTROLLER_HOSTNAME);
		if(evalPcHostName!=null && evalPcHostName.length()>0){
			return evalPcHostName;
		}
		return "localhost";
	}

}
