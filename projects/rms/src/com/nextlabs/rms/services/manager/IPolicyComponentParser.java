package com.nextlabs.rms.services.manager;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;

public interface IPolicyComponentParser {
	public void processPolicy(POLICY pol, String resourceStr, String subjectStr, String envStr);
}
