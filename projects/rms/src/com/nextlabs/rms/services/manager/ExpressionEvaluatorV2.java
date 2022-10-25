package com.nextlabs.rms.services.manager;


/**
 * Created by IntelliJ IDEA.
 * User: tbiegeleisen
 * Date: 9/21/15
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionEvaluatorV2 {
    private String NOT;
    private String AND;
    private String OR;

    public ExpressionEvaluatorV2(String NOT, String AND, String OR) {
        this.NOT = NOT;
        this.AND = AND;
        this.OR = OR;
    }

    public String NOT(String operand) {
        String[] ORterms = operand.split(OR);
        String operandLeft = productNOT(ORterms[0]);

        for (int i=1; i < ORterms.length; ++i) {
            String operandRight = productNOT(ORterms[i]);
            operandLeft = AND(operandLeft, operandRight);
        }

        return operandLeft;
    }

    private String productNOT(String operand) {
        String[] terms = operand.split(AND);
        String result = "";

        for (int i=0; i < terms.length; ++i) {
            if (i > 0) {
                result += OR;
            }

            if (terms[i].length() > NOT.length() && terms[i].substring(0, NOT.length()).equals(NOT)) {
                terms[i] = terms[i].substring(NOT.length());
            }
            else {
                terms[i] = NOT + terms[i];
            }

            result += terms[i];
        }

        return result;
    }

    public String OR(String operand1, String operand2) {
        if (operand1 == null || operand2 == null)
            return null;

        String result = operand1 + OR + operand2;
        return result;
    }

    public String AND (String operand1, String operand2) {
        if (operand1 == null || operand2 == null)
            return null;

        String[] terms1 = operand1.split(OR);
        String[] terms2 = operand2.split(OR);
        String result = "";

        for (int i=0; i < terms1.length; ++i) {
            for (int j=0; j < terms2.length; ++j) {
                if (i != 0 || j != 0) {
                    result += OR;
                }
                result += terms1[i] + AND + terms2[j];
            }
        }

        return result;
    }
}