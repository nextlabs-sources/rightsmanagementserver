package com.nextlabs.rms.eval;

import java.util.ArrayList;
import java.util.List;

public class EvalRequest {
	
	private Resource srcResource = null;
	
	private Resource targetResource = null;

	private List<String> rights = null;
	
	private User user = null;

	private Application application = null;
	
	private Host host = null;
	
	private NamedAttributes[] environmentAttributes= null;
	
	private String otfPolicy = null;

	private boolean ignoreBuiltInPolicies = false;

	private boolean performObligations = true;
	
	//TODO: Value is hardcoded here. Check if there is a better way to handle this.
	private int noiseLevel = 3;//PDPSDK.NOISE_LEVEL_USER_ACTION;
	
	//TODO: Value is hardcoded here. Check if there is a better way to handle this.
	private int timeoutInMins = -1;//PDPSDK.WAIT_FOREVER;
	
	public static final String ATTRIBVAL_RES_ID_FSO = "fso";

	public final static String ATTRIBVAL_ACTION_OPEN = "RIGHT_VIEW";
	
	public final static String ATTRIBVAL_ACTION_PRINT = "RIGHT_PRINT";
	
	public final static String ATTRIBVAL_ACTION_PMI = "RIGHT_VIEW_CAD_PMI";
	
	public static final String ATTRIBVAL_RMS_APP_NAME = "RMS";
	
	public static final String ATTRIBVAL_RES_DIMENSION_FROM = "from";
	
	public static final String ATTRIBVAL_RES_DIMENSION_TO = "to";
	public static final String ATTRIBVAL_RES_DONT_CARE_ACCEPTABLE = "dont-care-acceptable";
	public static final String ENVIRONMENT_ATTRIBUTE_NAME = "environment";
	
	public static final String POLICY_USER_LOCATION_IDENTIFIER_DEFAULT = "USER_LOCATION_CODE";
	public static final String OBLIGATION_WATERMARK = "OB_OVERLAY";
	public static final String OBLIGATION_NOTIFY = "CE::NOTIFY";
	
	public static final String OBLIGATION_COUNT = "CE_ATTR_OBLIGATION_COUNT";
	public static final String OBLIGATION_NAME = "CE_ATTR_OBLIGATION_NAME";
	public static final String OBLIGATION_POLICY = "CE_ATTR_OBLIGATION_POLICY";
	public static final String OBLIGATION_VALUE = "CE_ATTR_OBLIGATION_VALUE";
	public static final String OBLIGATION_VALUE_COUNT = "CE_ATTR_OBLIGATION_NUMVALUES";

	public static final String[] ALL_RIGHTS = {ATTRIBVAL_ACTION_OPEN, ATTRIBVAL_ACTION_PRINT, ATTRIBVAL_ACTION_PMI};
	
	public EvalRequest(){
		rights = new ArrayList<>();
	}
	
	public Resource getSrcResource() {
		return srcResource;
	}

	public void setSrcResource(Resource srcResource) {
		this.srcResource = srcResource;
	}

	public Resource getTargetResource() {
		return targetResource;
	}

	public void setTargetResource(Resource targetResource) {
		this.targetResource = targetResource;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public boolean isPerformObligations() {
		return performObligations;
	}

	public void setPerformObligations(boolean performObligations) {
		this.performObligations = performObligations;
	}

	public int getNoiseLevel() {
		return noiseLevel;
	}

	public void setNoiseLevel(int noiseLevel) {
		this.noiseLevel = noiseLevel;
	}

	public int getTimeoutInMins() {
		return timeoutInMins;
	}

	public void setTimeoutInMins(int timeoutInMins) {
		this.timeoutInMins = timeoutInMins;
	}

	public NamedAttributes[] getEnvironmentAttributes() {
		return environmentAttributes;
	}

	public void setEnvironmentAttributes(NamedAttributes[] environmentAttributes) {
		this.environmentAttributes = environmentAttributes;
	}

	public String getOtfPolicy() {
		return otfPolicy;
	}

	public void setOtfPolicy(String otfPolicy) {
		this.otfPolicy = otfPolicy;
	}

	public boolean isIgnoreBuiltInPolicies() {
		return ignoreBuiltInPolicies;
	}

	public void setIgnoreBuiltInPolicies(boolean ignoreBuiltInPolicies) {
		this.ignoreBuiltInPolicies = ignoreBuiltInPolicies;
	}

	public List<String> getRights() {
		return rights;
	}

	public void setRights(List<String> rights) {
		this.rights = rights;
	}
}
