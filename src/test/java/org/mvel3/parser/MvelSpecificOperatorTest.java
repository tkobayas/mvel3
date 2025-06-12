package org.mvel3.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class MvelSpecificOperatorTest {

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
    public void testPowerOperator() {
        assertDoesNotThrow(() -> parseExpression("2 ** 3"));
        assertDoesNotThrow(() -> parseExpression("x ** 2"));
        assertDoesNotThrow(() -> parseExpression("(2 + 3) ** 4"));
        assertDoesNotThrow(() -> parseExpression("x **= 2"));
    }

    @Test
    public void testStringSimilarityOperators() {
        assertDoesNotThrow(() -> parseExpression("\"hello\" strsim \"helo\""));
        assertDoesNotThrow(() -> parseExpression("name1 strsim name2"));
        assertDoesNotThrow(() -> parseExpression("\"Smith\" soundslike \"Smyth\""));
        assertDoesNotThrow(() -> parseExpression("lastName soundslike searchName"));
    }

    @Test
    public void testCollectionOperators() {
        assertDoesNotThrow(() -> parseExpression("list contains item"));
        assertDoesNotThrow(() -> parseExpression("\"hello\" contains \"ell\""));
        assertDoesNotThrow(() -> parseExpression("item in collection"));
        assertDoesNotThrow(() -> parseExpression("5 in numbers"));
        assertDoesNotThrow(() -> parseExpression("\"key\" in map"));
    }

    @Test
    public void testSafeNavigation() {
        assertDoesNotThrow(() -> parseExpression("obj?.field"));
        assertDoesNotThrow(() -> parseExpression("person?.address?.street"));
        assertDoesNotThrow(() -> parseExpression("obj?.method()"));
        assertDoesNotThrow(() -> parseExpression("array?.get(0)"));
        assertDoesNotThrow(() -> parseExpression("map?.get(\"key\")"));
    }

    @Test
    public void testIsDefFunction() {
        assertDoesNotThrow(() -> parseExpression("isdef(variable)"));
        assertDoesNotThrow(() -> parseExpression("isdef(obj.field)"));
        assertDoesNotThrow(() -> parseExpression("isdef(array[0])"));
    }

    @Test
    public void testInstanceofOperator() {
        assertDoesNotThrow(() -> parseExpression("obj instanceof String"));
        assertDoesNotThrow(() -> parseExpression("value instanceof java.util.List"));
        assertDoesNotThrow(() -> parseExpression("item instanceof MyClass"));
    }

    @Test
    public void testIsOperator() {
        assertDoesNotThrow(() -> parseExpression("obj is String"));
        assertDoesNotThrow(() -> parseExpression("value is List"));
        assertDoesNotThrow(() -> parseExpression("item is MyClass"));
    }

    @Test
    public void testRegexOperations() {
        assertDoesNotThrow(() -> parseExpression("~/hello/"));
        assertDoesNotThrow(() -> parseExpression("text ~ ~/pattern/"));
        assertDoesNotThrow(() -> parseExpression("email ~ ~/\\w+@\\w+\\.\\w+/"));
    }

    @Test
    public void testSpecialLiterals() {
        assertDoesNotThrow(() -> parseExpression("empty"));
        assertDoesNotThrow(() -> parseExpression("nil"));
        assertDoesNotThrow(() -> parseExpression("undefined"));
        assertDoesNotThrow(() -> parseExpression("x == empty"));
        assertDoesNotThrow(() -> parseExpression("value != nil"));
        assertDoesNotThrow(() -> parseExpression("result == undefined"));
    }

    @Test
    public void testComplexMvelExpressions() {
        assertDoesNotThrow(() -> parseExpression("obj?.field ** 2"));
        assertDoesNotThrow(() -> parseExpression("list contains item && item != nil"));
        assertDoesNotThrow(() -> parseExpression("name1 strsim name2 ? \"similar\" : \"different\""));
        assertDoesNotThrow(() -> parseExpression("isdef(obj?.field) && obj.field > 0"));
        assertDoesNotThrow(() -> parseExpression("value in collection || value == empty"));
    }

    @Test
    public void testAssignmentWithMvelOperators() {
        assertDoesNotThrow(() -> parseExpression("x **= 2"));
        assertDoesNotThrow(() -> parseExpression("result = name1 strsim name2"));
        assertDoesNotThrow(() -> parseExpression("found = list contains item"));
        assertDoesNotThrow(() -> parseExpression("safe = obj?.field"));
    }

    @Test
    public void testChainedOperations() {
        assertDoesNotThrow(() -> parseExpression("obj?.field?.method()?.result"));
        assertDoesNotThrow(() -> parseExpression("a ** b ** c"));
        assertDoesNotThrow(() -> parseExpression("str1 strsim str2 && str2 soundslike str3"));
        assertDoesNotThrow(() -> parseExpression("list contains item && item in otherList"));
    }
}