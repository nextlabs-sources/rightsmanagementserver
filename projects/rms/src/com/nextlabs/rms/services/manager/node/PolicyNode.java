package com.nextlabs.rms.services.manager.node;

import java.io.Serializable;

import com.fathzer.soft.javaluator.Operator;

public class PolicyNode implements Serializable {
	private static final long serialVersionUID = 4838822340290382839L;
	private final String operand1;
	private final String operand2;
	private final Operator operator;
	private final String equation;

	public PolicyNode(String operand1, String operand2, Operator operator, String equation) {
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operator = operator;
		this.equation = equation;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof PolicyNode) {
			PolicyNode oth = (PolicyNode) obj;
			return getEquation().equalsIgnoreCase(oth.getEquation());
		}
		return false;
	}

	public String getEquation() {
		return equation;
	}

	public String getOperand1() {
		return operand1;
	}

	public String getOperand2() {
		return operand2;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public int hashCode() {
		int hash = getEquation().hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "{operand1: " + getOperand1() + ", operand2: " + getOperand2() + ", operator: "
				+ (operator != null ? operator.getSymbol() : "<empty>") + ", equation: " + equation + "}";
	}
}
