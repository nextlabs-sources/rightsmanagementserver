package com.nextlabs.rms.services.manager;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;

public class ActionPredicateVisitor extends DefaultPredicateVisitor {

	private String rightsPrefix;
	
	private Set<String> rightSet;

	public ActionPredicateVisitor() {
		super();
		rightsPrefix=GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.RM_ACTION_PREFIX);
		if(rightsPrefix==null || rightsPrefix.length()==0){
			rightsPrefix="RIGHT_";
		}
		rightSet = new HashSet<String>(); 
	}

	@Override
	public void visit(IPredicate pred) {
		if (!pred.toString().equalsIgnoreCase(PolicyTransformer.PREDICATE_FALSE) && !pred.toString().equalsIgnoreCase(PolicyTransformer.PREDICATE_TRUE)) {
			if(pred.toString().startsWith(rightsPrefix)){	
				rightSet.add(pred.toString());
			}
		}
	}
	
	public Set<String> getRightSet() {
		return rightSet;
	}
	
	public void setRightSet(Set<String> rightSet) {
		this.rightSet = rightSet;
	}
	
}