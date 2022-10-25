package com.nextlabs.rms.eval;

public interface IEvalAdapter {
	
	String CONN_ERR_CONNECT_EXCEPTION = "ConnectException";
	
	String CONN_ERR_UNKNOWN_HOST = "Unknown Host";

	public EvalResponse evaluate(EvalRequest req);

	public String getConnStatus();

	public void initializeSDK();
	
}
