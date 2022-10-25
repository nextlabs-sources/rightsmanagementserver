package com.nextlabs.rms.services.manager;

import com.fathzer.soft.javaluator.Operator;

public class PolicyComponentHelper {
	public final static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2) {
		public String toString() {
			return "&&";
		};
	};
	public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1) {
		public String toString() {
			return "||";
		};
	};
	public final static Operator NOT = new Operator("!!", 1, Operator.Associativity.LEFT, 3) {
		public String toString() {
			return "!!";
		};
	};

	public static String negateRelationalOperator(String operator) {
		if ("EQ".equalsIgnoreCase(operator)) {
			return "NE";
		} else if ("NE".equalsIgnoreCase(operator)) {
			return "EQ";
		} else if ("LT".equalsIgnoreCase(operator)) {
			return "GE";
		} else if ("LE".equalsIgnoreCase(operator)) {
			return "GT";
		} else if ("GE".equalsIgnoreCase(operator)) {
			return "LT";
		} else if ("GT".equalsIgnoreCase(operator)) {
			return "LE";
		}
		return operator;
	}

	public static Operator negateLogicalOperator(Operator operator) {
		if (AND.equals(operator)) {
			return OR;
		} else if (OR.equals(operator)) {
			return AND;
		}
		return operator;
	}
}
