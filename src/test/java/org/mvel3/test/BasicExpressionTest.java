package org.mvel3.test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class BasicExpressionTest {

    private ParseTree parseExpression(String expression) {
        ANTLRInputStream input = new ANTLRInputStream(expression);
        Mvel3Lexer lexer = new Mvel3Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Mvel3Parser parser = new Mvel3Parser(tokens);
        
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException("Syntax error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        });
        
        return parser.start_();
    }

    @Test
    public void testLiterals() {
        assertDoesNotThrow(() -> parseExpression("42"));
        assertDoesNotThrow(() -> parseExpression("3.14"));
        assertDoesNotThrow(() -> parseExpression("'c'"));
        assertDoesNotThrow(() -> parseExpression("\"hello\""));
        assertDoesNotThrow(() -> parseExpression("true"));
        assertDoesNotThrow(() -> parseExpression("false"));
        assertDoesNotThrow(() -> parseExpression("null"));
    }

    @Test
    public void testBasicArithmetic() {
        assertDoesNotThrow(() -> parseExpression("1 + 2"));
        assertDoesNotThrow(() -> parseExpression("10 - 5"));
        assertDoesNotThrow(() -> parseExpression("3 * 4"));
        assertDoesNotThrow(() -> parseExpression("8 / 2"));
        assertDoesNotThrow(() -> parseExpression("7 % 3"));
    }

    @Test
    public void testComplexArithmetic() {
        assertDoesNotThrow(() -> parseExpression("1 + 2 * 3"));
        assertDoesNotThrow(() -> parseExpression("(1 + 2) * 3"));
        assertDoesNotThrow(() -> parseExpression("10 / 2 + 3 * 4"));
        assertDoesNotThrow(() -> parseExpression("((10 + 5) * 2) - 3"));
    }

    @Test
    public void testUnaryOperations() {
        assertDoesNotThrow(() -> parseExpression("+5"));
        assertDoesNotThrow(() -> parseExpression("-10"));
        assertDoesNotThrow(() -> parseExpression("!true"));
        assertDoesNotThrow(() -> parseExpression("~42"));
    }

    @Test
    public void testComparison() {
        assertDoesNotThrow(() -> parseExpression("5 > 3"));
        assertDoesNotThrow(() -> parseExpression("2 < 7"));
        assertDoesNotThrow(() -> parseExpression("4 >= 4"));
        assertDoesNotThrow(() -> parseExpression("6 <= 8"));
        assertDoesNotThrow(() -> parseExpression("5 == 5"));
        assertDoesNotThrow(() -> parseExpression("3 != 4"));
    }

    @Test
    public void testLogicalOperations() {
        assertDoesNotThrow(() -> parseExpression("true && false"));
        assertDoesNotThrow(() -> parseExpression("true || false"));
        assertDoesNotThrow(() -> parseExpression("!true"));
        assertDoesNotThrow(() -> parseExpression("5 > 3 && 2 < 7"));
        assertDoesNotThrow(() -> parseExpression("false || (3 == 3)"));
    }

    @Test
    public void testBitwiseOperations() {
        assertDoesNotThrow(() -> parseExpression("5 & 3"));
        assertDoesNotThrow(() -> parseExpression("5 | 3"));
        assertDoesNotThrow(() -> parseExpression("5 ^ 3"));
        assertDoesNotThrow(() -> parseExpression("~5"));
    }

    @Test
    public void testTernaryOperator() {
        assertDoesNotThrow(() -> parseExpression("true ? 1 : 2"));
        assertDoesNotThrow(() -> parseExpression("5 > 3 ? \"yes\" : \"no\""));
        assertDoesNotThrow(() -> parseExpression("x > 0 ? x : -x"));
    }

    @Test
    public void testIdentifiers() {
        assertDoesNotThrow(() -> parseExpression("x"));
        assertDoesNotThrow(() -> parseExpression("myVariable"));
        assertDoesNotThrow(() -> parseExpression("_value"));
        assertDoesNotThrow(() -> parseExpression("$temp"));
    }

    @Test
    public void testFieldAccess() {
        assertDoesNotThrow(() -> parseExpression("obj.field"));
        assertDoesNotThrow(() -> parseExpression("person.name"));
        assertDoesNotThrow(() -> parseExpression("obj.field.subfield"));
    }

    @Test
    public void testArrayAccess() {
        assertDoesNotThrow(() -> parseExpression("array[0]"));
        assertDoesNotThrow(() -> parseExpression("matrix[i][j]"));
        assertDoesNotThrow(() -> parseExpression("list[index]"));
    }

    @Test
    public void testMethodCalls() {
        assertDoesNotThrow(() -> parseExpression("method()"));
        assertDoesNotThrow(() -> parseExpression("obj.method()"));
        assertDoesNotThrow(() -> parseExpression("method(arg1, arg2)"));
        assertDoesNotThrow(() -> parseExpression("obj.method(1, \"test\")"));
    }

    @Test
    public void testAssignment() {
        assertDoesNotThrow(() -> parseExpression("x = 5"));
        assertDoesNotThrow(() -> parseExpression("x += 10"));
        assertDoesNotThrow(() -> parseExpression("x -= 3"));
        assertDoesNotThrow(() -> parseExpression("x *= 2"));
        assertDoesNotThrow(() -> parseExpression("x /= 4"));
    }

    @Test
    public void testIncrementDecrement() {
        assertDoesNotThrow(() -> parseExpression("++x"));
        assertDoesNotThrow(() -> parseExpression("--x"));
        assertDoesNotThrow(() -> parseExpression("x++"));
        assertDoesNotThrow(() -> parseExpression("x--"));
    }
}