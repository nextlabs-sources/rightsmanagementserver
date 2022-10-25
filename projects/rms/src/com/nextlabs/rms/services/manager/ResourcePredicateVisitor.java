package com.nextlabs.rms.services.manager;

import java.util.HashMap;
import java.util.Stack;

import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IRelation;

import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.COMPONENTS.RESOURCES.RESOURCE;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY.CONDITION;
import noNamespace.PropertyType;

public class ResourcePredicateVisitor extends DefaultPredicateVisitor {

	/*This class contains a boolean value and an integer value. It helps store data that is being used to evaluate the 'not in' condition for composite predicates. 
	What I am basically trying to do is to evaluate expressions like (NOT (NOT (itar = "yes") AND (ear = "NO")). In the new XML policy bundle, the CONDITION tag has an
	exclude attribute which should be set to true when there is a NOT in the predicate. This becomes complicated as the composite predicates can have any number of levels
	and a NOT can be negated by another NOT in the child level.
	Since I don't have access to the data structure holding the predicates, I can only get every predicate in the composite predicate and the number of children that the composite predicate has.
	I am using this information to keep track of the nodes under the NOT rooted subtree.*/
	public class PredicateInfoNode{
		public PredicateInfoNode(int predicateCount) {
			this.numChildToProcess=predicateCount;
			exclude=true;
		}
		//Number of relations that are still to be processed to  
		int numChildToProcess;
		//Boolean indication if the condition should be excluded or not
		boolean exclude;
	}
	
	private RESOURCES ress;

	private int resourceId;
	
	private POLICY pol;
	
	private Stack<PredicateInfoNode> nodeCountStack= new Stack<PredicateInfoNode>();
	
	private HashMap<String, Integer> cache;
	
	public ResourcePredicateVisitor(POLICY pol, RESOURCES ress, int resourceId, HashMap<String,Integer>cache) {
		super();
		this.pol=pol;
		this.ress = ress;
		this.resourceId=resourceId;
		this.cache=cache;
	}

	public void visit(IRelation rel) {
		String key=rel.getLHS().toString().trim()+rel.getOp().toString().trim()+rel.getRHS();
		if(!cache.containsKey(key)){
			RESOURCE reso = ress.addNewRESOURCE();
			reso.setId(resourceId);
			PropertyType prop = reso.addNewPROPERTY();
			if(rel.getLHS().toString().trim().equalsIgnoreCase(PolicyTransformer.DATATYPE_PATH)){
				prop.setType(PolicyTransformer.DATATYPE_STRING);
				prop.setName(rel.getLHS().toString());
				prop.setValue(PolicyTransformer.escapeUnwantedQuotes(rel.getRHS().toString()));
				prop.setMethod(PolicyTransformer.getCompareMethod(rel.getOp().toString()));
			}
			else{
				prop.setType(PolicyTransformer.DATATYPE_TAG);
				prop.setName(rel.getLHS().toString());
				prop.setValue(PolicyTransformer.escapeUnwantedQuotes(rel.getRHS().toString()));
				prop.setMethod(PolicyTransformer.getCompareMethod(rel.getOp().toString()));
//				System.out.println(rel.getOp().getName() + "::: Type-" + rel.getOp().getType());
			}
		}
		CONDITION con = pol.addNewCONDITION();
		con.setType(PolicyTransformer.DATATYPE_RES);
		con.setExclude(false);
		if(!cache.containsKey(key)){
			cache.put(key, resourceId);
			con.newCursor().setTextValue(resourceId +"");
			resourceId++;
		}else{
			con.newCursor().setTextValue(cache.get(key)+"");
		}
		if(!nodeCountStack.isEmpty()){
			PredicateInfoNode poppedNode=nodeCountStack.pop();
			int current=poppedNode.numChildToProcess;
			if(current>1){
				--poppedNode.numChildToProcess;
				nodeCountStack.push(poppedNode);
			}
			if(poppedNode.exclude){
				con.setExclude(true);
			}
		}
	
	}
	public int getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}
	
	public void visit(ICompositePredicate pred, boolean preorder) {
		//This means that we are evaluating a subtree which has NOT operation in the root node
		if(!nodeCountStack.isEmpty()){
			PredicateInfoNode poppedNode=nodeCountStack.pop();
			//Check if there are more children to process
			if(poppedNode.numChildToProcess>1){
				--poppedNode.numChildToProcess;
				nodeCountStack.push(poppedNode);
			}
			PredicateInfoNode node=new PredicateInfoNode(pred.predicateCount());
			//The parent's NOT operation is being negated by the child's NOT operation
			if(pred.getOp().toString().equals(PolicyTransformer.DATATYPE_NOT)){
				node.exclude=!poppedNode.exclude;
			}else{
				node.exclude=poppedNode.exclude;
			}
			nodeCountStack.push(node);
		}
		//Add a node to the stack when the first NOT operation is encountered for a subtree of composite predicates
		if(pred.getOp().toString().equals(PolicyTransformer.DATATYPE_NOT) && nodeCountStack.isEmpty()){
			PredicateInfoNode node=new PredicateInfoNode(pred.predicateCount());
			nodeCountStack.add(node);
		}
    }
}
