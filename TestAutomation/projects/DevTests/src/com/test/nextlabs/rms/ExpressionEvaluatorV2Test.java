package com.test.nextlabs.rms;

import com.nextlabs.rms.services.manager.ExpressionEvaluatorV2;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExpressionEvaluatorV2Test {
    @Test
    public void testEnvironment() throws IOException {
        String NOT = "NOT", AND = "AND", OR = "OR";
        ExpressionEvaluatorV2 test = new ExpressionEvaluatorV2(NOT, AND, OR);

        // simple NOT
        // !A = !A
        String operand1 = "A";
        String operand2 = "";
        String result = test.NOT(operand1);
        Assert.assertEquals(NOT+"A", result);

        // simple AND
        // A (BC) = ABC
        operand1 = "A";
        operand2 = "B" + AND + "C";
        result = test.AND(operand1, operand2);
        Assert.assertEquals("A"+AND+"B"+AND+"C", result);

        // simple OR
        // A + (B + C) = A + B + C
        operand1 = "A";
        operand2 = "B" + OR + "C";
        result = test.OR(operand1, operand2);
        Assert.assertEquals("A"+OR+"B"+OR+"C", result);

        // ORs over AND
        // (!A + B)(C + !E + F) = !AC + !A!E + !AF + BC + B!E + BF
        operand1 = NOT + "A" + OR + "B";
        operand2 = "C" + OR + NOT + "E" + OR + "F";
        result = test.AND(operand1, operand2);
        Assert.assertEquals(NOT + "A" + AND + "C" + OR + NOT + "A" + AND + NOT + "E" + OR +
                            NOT + "A" + AND + "F" + OR + "B" + AND + "C" + OR +
                            "B" + AND + NOT + "E" + OR + "B" + AND + "F", result);

        // ANDs over OR
        // (!AB) + (C!EF) = !AB + C!EF
        operand1 = NOT + "A" + AND + "B";
        operand2 = "C" + AND + NOT + "E" + AND + "F";
        result = test.OR(operand1, operand2);
        Assert.assertEquals(NOT + "A" + AND + "B" + OR + "C" + AND + NOT + "E" + AND + "F", result);

        // complex NOT
        // !(A + !B + CD + !E) = !AB!(CD)E = !AB(!C + !D)E = (!AB!C + !AB!D)E = !AB!CE + !AB!DE
        operand1 = "A" + OR + NOT + "B" + OR + "C" + AND + "D" + OR + NOT + "E";
        result = test.NOT(operand1);
        Assert.assertEquals(NOT+"A"+AND+"B"+AND+NOT+"C"+AND+"E"+OR+NOT+"A"+AND+"B"+AND+NOT+"D"+AND+"E", result);
   }
}