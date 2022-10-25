package com.nextlabs.rms.services.manager;


import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;

import java.util.*;

public class TreeBooleanEvaluatorV2 extends AbstractEvaluator<String> {
    /** The logical NOT operator.*/
    final static Operator NOT = new Operator("!!", 1, Operator.Associativity.LEFT, 3);
    /** The logical AND operator.*/
    final static Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
    /** The logical OR operator.*/
    final static Operator OR = new Operator("~~", 2, Operator.Associativity.LEFT, 1);

    private static final Parameters PARAMETERS;

    private static ExpressionEvaluatorV2 ev;

    public Map<String, String> operandMap;
    private String lastEvalOpen;

    static {
        // Create the evaluator's parameters
        PARAMETERS = new Parameters();
        // Add the supported operators
        PARAMETERS.add(NOT);
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        // Add the parentheses
        PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
    }

    public TreeBooleanEvaluatorV2() {
        super(PARAMETERS);
        operandMap = new HashMap<String, String>();

        ev = new ExpressionEvaluatorV2(NOT.getSymbol(), AND.getSymbol(), OR.getSymbol());
    }

    public String getLastEvalOpen() {
        return lastEvalOpen;
    }

    @Override
    protected String toValue(String literal, Object evaluationContext) {
        return literal;
    }

    @Override
    protected String evaluate(Operator operator, Iterator<String> operands,
                              Object evaluationContext) {
        List<String> tree = (List<String>) evaluationContext;
        String o1 = operands.next();
        String o2 = operands.hasNext() ? operands.next() : "";
        String evalOpen = "", evalClosed = "";
        if (operator == NOT) {
            String o1Open = operandMap.containsKey(o1) ? operandMap.get(o1) : o1;

            evalOpen = ev.NOT(o1Open);
            evalClosed = "(" + NOT.getSymbol() + o1 + ")";
            operandMap.put(evalClosed, evalOpen);
        }
        else if (operator == AND) {
            String o1Open = operandMap.containsKey(o1) ? operandMap.get(o1) : o1;
            String o2Open = operandMap.containsKey(o2) ? operandMap.get(o2) : o2;

            evalOpen = ev.AND(o1Open, o2Open);
            evalClosed = "(" + o1 + AND.getSymbol() + o2 + ")";
            operandMap.put(evalClosed, evalOpen);
        }
        else if (operator == OR) {
            String o1Open = operandMap.containsKey(o1) ? operandMap.get(o1) : o1;
            String o2Open = operandMap.containsKey(o2) ? operandMap.get(o2) : o2;

            evalOpen = ev.OR(o1Open, o2Open);
            evalClosed = "(" + o1 + OR.getSymbol() + o2 + ")";
            operandMap.put(evalClosed, evalOpen);
        }
        else {
            System.out.println("Unknown operator detected: " + operator.getSymbol());
        }

        lastEvalOpen = evalOpen;
        tree.add(evalClosed);

        return evalClosed;
    }

    public static String doIt(String expression) {
    	TreeBooleanEvaluatorV2 evaluator = new TreeBooleanEvaluatorV2();
        List<String> sequence = new ArrayList<String>();
        evaluator.evaluate(expression, sequence);
        expression = "(" + expression + ")";
        String result = evaluator.getLastEvalOpen();
        if(result==null && expression.length()>2){
        	return expression.substring(1,expression.length()-1);
        }
        expression = expression.replaceAll("\\s+", "");
        return result;
    }
}