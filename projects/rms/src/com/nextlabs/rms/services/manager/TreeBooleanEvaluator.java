package com.nextlabs.rms.services.manager;

import java.util.Iterator;
import java.util.List;

import com.fathzer.soft.javaluator.*;

public class TreeBooleanEvaluator extends AbstractEvaluator<String> {
	final static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
	final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);
	final static Operator NOT = new Operator("!!", 1, Operator.Associativity.LEFT, 3);

	private static final Parameters PARAMETERS;

	static {
		// Create the evaluator's parameters
		PARAMETERS = new Parameters();
		// Add the supported operators
		PARAMETERS.add(AND);
		PARAMETERS.add(OR);
		PARAMETERS.add(NOT);
		// Add the parentheses
		PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
	}

	public TreeBooleanEvaluator() {
		super(PARAMETERS);
	}

	@Override
	protected String toValue(String literal, Object evaluationContext) {
		return literal;
	}

	@Override
	protected String evaluate(Operator operator, Iterator<String> operands,
			Object evaluationContext) {
		List<Component> tree = (List<Component>) evaluationContext;
		String o1 = operands.next().trim();
		String o2 = null;
		if (operands.hasNext()) {
			o2 = operands.next().trim();
		}
		String equation = "";
		if (o2 != null) {
			equation = "(" + o1 + " " + operator.getSymbol() + " " + o2 + ")";
		} else {
			equation = operator.getSymbol() + " " + o1;
		}
		Component comp = new Component();
		comp.setOperand1(o1);
		comp.setOperand2(o2);
		comp.setOperator(operator.getSymbol().trim());
		comp.setEquation(equation);
		tree.add(comp);
		return equation;
	}


}