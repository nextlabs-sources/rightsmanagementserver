package com.nextlabs.rms.services.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

public class ExpressionEvaluator extends AbstractEvaluator<String> {

	  final static Operator EQ = new Operator("=", 2, Operator.Associativity.LEFT, 1);
	  final static Operator NE = new Operator("!=", 2, Operator.Associativity.LEFT, 2);
	  final static Operator GT = new Operator(">", 2, Operator.Associativity.LEFT, 3);
	  final static Operator GTE = new Operator(">=", 2, Operator.Associativity.LEFT, 4);
	  final static Operator LT = new Operator("<", 2, Operator.Associativity.LEFT, 5);
	  final static Operator LTE = new Operator("<=", 2, Operator.Associativity.LEFT, 6);
	  public final static Operator HAS = new Operator("~#", 2, Operator.Associativity.LEFT, 7);
	  
	  private static final Parameters PARAMETERS;

	  static {
	    PARAMETERS = new Parameters();
	    PARAMETERS.add(EQ);
	    PARAMETERS.add(NE);
	    PARAMETERS.add(GT);
	    PARAMETERS.add(GTE);
	    PARAMETERS.add(LT);
	    PARAMETERS.add(LTE);
	    PARAMETERS.add(HAS);
	    // Add the parentheses
//	    PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
	  }

	  public ExpressionEvaluator() {
	    super(PARAMETERS);
	  }

	  @Override
	  protected String toValue(String literal, Object evaluationContext) {
	    return literal;
	  }

	@Override
	protected Iterator<String> tokenize(String expression) {
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
		Matcher regexMatcher = regex.matcher(expression);
		int operatorPos = 0;
		int count = 0;
		String stringToAdd = "";
		while (regexMatcher.find()) {
			stringToAdd = regexMatcher.group();
			if (PolicyTransformer.getCompareMethod(stringToAdd) != null) {
				operatorPos = count;
			}
			matchList.add(stringToAdd);
			count++;
		}
		if (matchList.size() > 3) {
			for (int i = 1; i < operatorPos; i++) {
				matchList.set(0, matchList.get(0) + matchList.get(i));
				matchList.remove(i);
			}
			for (int i = operatorPos; i < matchList.size() - 1; i++) {
				matchList.set(2, matchList.get(2) + matchList.get(i));
				matchList.remove(i);
			}
		}

		return matchList.iterator();
	}

	  @Override
	  protected String evaluate(Operator operator, Iterator<String> operands,
	      Object evaluationContext) {
	    List<Component> tree = (List<Component>) evaluationContext;
	    String o1 = operands.next().trim();
	    String o2 = null;
	    if(operands.hasNext()){
	        o2 = operands.next().trim();    	
	    }
	    String eval = "";
	    if(o2!=null){
	    	eval = "("+o1+" "+operator.getSymbol()+" "+o2+")";
	    }else{
	    	eval = operator.getSymbol() + " " + o1;
	    }
	    Component comp = new Component();
	    comp.setOperand1(o1);
	    comp.setOperand2(o2);
	    comp.setOperator(operator.getSymbol().trim());
	    tree.add(comp);
	    return eval;
	  }

}
