package com.nextlabs.rms.services.manager;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.APPLICATIONS.APPLICATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.LOCATIONS.LOCATION;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.USERS.USER;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.CONDITION;
import noNamespace.PropertyType;

import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

public class SubjectPredicateVisitor extends DefaultPredicateVisitor {

	private int userId;

	private POLICY pol;

	private USERS users;

	private APPLICATIONS app;

	private LOCATIONS loc;
	
	private int locId;

	public int getLocId() {
		return locId;
	}

	public void setLocId(int locId) {
		this.locId = locId;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	private int appId;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public SubjectPredicateVisitor(POLICY pol, USERS user, int userId, APPLICATIONS app, int appId, LOCATIONS loc, int locId) {
		super();
		this.userId = userId;
		this.pol = pol;
		this.users = user;
		this.app=app;
		this.appId=appId;
		this.loc=loc;
		this.locId=locId;
	}

	@Override
	public void visit(IRelation rel) {
		if(((SubjectAttribute)rel.getLHS()).getSubjectType() == SubjectType.USER){
			if(rel.getLHS().toString().equalsIgnoreCase(PolicyTransformer.DATATYPE_GROUP)){
				pol.setUsergroup(rel.getRHS().toString());
			}else{
				USER user = users.addNewUSER();
				user.setId(userId);
				PropertyType prop = user.addNewPROPERTY();
				prop.setName(rel.getLHS().toString());
				prop.setValue(PolicyTransformer.escapeUnwantedQuotes(rel.getRHS().toString()));
				prop.setMethod(PolicyTransformer.getCompareMethod(rel.getOp().toString()));
				CONDITION con = pol.addNewCONDITION();
				con.setType(PolicyTransformer.DATATYPE_USR);
				con.setExclude(false);
				con.newCursor().setTextValue(""+userId++);
			}
		}else if(((SubjectAttribute)rel.getLHS()).getSubjectType() == SubjectType.APP){
			/*if(rel.getLHS().toString().equalsIgnoreCase("GROUP")){
				//TODO: Can I add new attribute for appgroup???
//				pol.setUsergroup(Integer.parseInt(rel.getRHS().toString()));
			}*/
			if(!rel.getLHS().toString().equalsIgnoreCase(PolicyTransformer.DATATYPE_GROUP)){
				APPLICATION application = app.addNewAPPLICATION();
				PropertyType prop = application.addNewPROPERTY();
				application.setId(appId);
				prop.setName(rel.getLHS().toString());
				prop.setValue(PolicyTransformer.escapeUnwantedQuotes(rel.getRHS().toString()));
				prop.setMethod(PolicyTransformer.getCompareMethod(rel.getOp().toString()));
				prop.setType(PolicyTransformer.DATATYPE_STRING);
				CONDITION con = pol.addNewCONDITION();
				con.setType(PolicyTransformer.DATATYPE_APP);
				con.setExclude(false);
				con.newCursor().setTextValue(""+appId++);
			}
		}else if(((SubjectAttribute)rel.getLHS()).getSubjectType() == SubjectType.HOST){
			if(rel.getLHS().toString().equalsIgnoreCase(PolicyTransformer.DATATYPE_INET_ADDRESS)){
				LOCATION location= loc.addNewLOCATION();
				PropertyType prop = location.addNewPROPERTY();
				location.setId(locId);
				prop.setName(PolicyTransformer.PROPERTYNAME_IPV4);
				prop.setValue(PolicyTransformer.escapeUnwantedQuotes(rel.getRHS().toString()));
				prop.setMethod(PolicyTransformer.getCompareMethod(rel.getOp().toString()));
				prop.setType(PolicyTransformer.DATATYPE_STRING);
				CONDITION con = pol.addNewCONDITION();
				con.setType(PolicyTransformer.DATATYPE_LOC);
				con.setExclude(false);
				con.newCursor().setTextValue(""+locId++);
			}
		}
	}
}
