package org.mvel3.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class LiteralAndFunctionTest {

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
    public void testIntegerLiterals() {
        assertDoesNotThrow(() -> parseExpression("42"));
        assertDoesNotThrow(() -> parseExpression("0"));
        assertDoesNotThrow(() -> parseExpression("123456789"));
        assertDoesNotThrow(() -> parseExpression("42L"));
        assertDoesNotThrow(() -> parseExpression("42l"));
    }

    @Test
    public void testHexadecimalLiterals() {
        assertDoesNotThrow(() -> parseExpression("0x1F"));
        assertDoesNotThrow(() -> parseExpression("0xFF"));
        assertDoesNotThrow(() -> parseExpression("0x0"));
        assertDoesNotThrow(() -> parseExpression("0XABCDEF"));
    }

    @Test
    public void testOctalLiterals() {
        assertDoesNotThrow(() -> parseExpression("0777"));
        assertDoesNotThrow(() -> parseExpression("0123"));
        assertDoesNotThrow(() -> parseExpression("0_123"));
    }

    @Test
    public void testBinaryLiterals() {
        assertDoesNotThrow(() -> parseExpression("0b1010"));
        assertDoesNotThrow(() -> parseExpression("0B1111"));
        assertDoesNotThrow(() -> parseExpression("0b0000"));
        assertDoesNotThrow(() -> parseExpression("0b1100_1010"));
    }

    @Test
    public void testFloatingPointLiterals() {
        assertDoesNotThrow(() -> parseExpression("3.14"));
        assertDoesNotThrow(() -> parseExpression("0.5"));
        assertDoesNotThrow(() -> parseExpression("123.456"));
        assertDoesNotThrow(() -> parseExpression("3.14f"));
        assertDoesNotThrow(() -> parseExpression("3.14F"));
        assertDoesNotThrow(() -> parseExpression("3.14d"));
        assertDoesNotThrow(() -> parseExpression("3.14D"));
    }

    @Test
    public void testScientificNotation() {
        assertDoesNotThrow(() -> parseExpression("1.23e4"));
        assertDoesNotThrow(() -> parseExpression("1.23E4"));
        assertDoesNotThrow(() -> parseExpression("1.23e-4"));
        assertDoesNotThrow(() -> parseExpression("1.23E+4"));
        assertDoesNotThrow(() -> parseExpression("2e10"));
    }

    @Test
    public void testHexFloatingPoint() {
        assertDoesNotThrow(() -> parseExpression("0x1.8p3"));
        assertDoesNotThrow(() -> parseExpression("0X1.8P3"));
        assertDoesNotThrow(() -> parseExpression("0x1.0p-3"));
    }

    @Test
    public void testCharacterLiterals() {
        assertDoesNotThrow(() -> parseExpression("'a'"));
        assertDoesNotThrow(() -> parseExpression("'Z'"));
        assertDoesNotThrow(() -> parseExpression("'1'"));
        assertDoesNotThrow(() -> parseExpression("' '"));
        assertDoesNotThrow(() -> parseExpression("'\\n'"));
        assertDoesNotThrow(() -> parseExpression("'\\t'"));
        assertDoesNotThrow(() -> parseExpression("'\\''"));
        assertDoesNotThrow(() -> parseExpression("'\\\"'"));
        assertDoesNotThrow(() -> parseExpression("'\\\\'"));
    }

    @Test
    public void testUnicodeCharacters() {
        assertDoesNotThrow(() -> parseExpression("'\\u0041'"));
        assertDoesNotThrow(() -> parseExpression("'\\u00A9'"));
        assertDoesNotThrow(() -> parseExpression("'\\u1234'"));
    }

    @Test
    public void testStringLiterals() {
        assertDoesNotThrow(() -> parseExpression("\"hello\""));
        assertDoesNotThrow(() -> parseExpression("\"Hello, World!\""));
        assertDoesNotThrow(() -> parseExpression("\"\""));
        assertDoesNotThrow(() -> parseExpression("\"Line 1\\nLine 2\""));
        assertDoesNotThrow(() -> parseExpression("\"Tab\\tSeparated\""));
        assertDoesNotThrow(() -> parseExpression("\"Quote: \\\"Hello\\\"\""));
    }

    @Test
    public void testBooleanLiterals() {
        assertDoesNotThrow(() -> parseExpression("true"));
        assertDoesNotThrow(() -> parseExpression("false"));
    }

    @Test
    public void testNullLiteral() {
        assertDoesNotThrow(() -> parseExpression("null"));
    }

    @Test
    public void testRegexLiterals() {
        assertDoesNotThrow(() -> parseExpression("~/hello/"));
        assertDoesNotThrow(() -> parseExpression("~/[a-zA-Z]+/"));
        assertDoesNotThrow(() -> parseExpression("~/\\d{3}-\\d{3}-\\d{4}/"));
        assertDoesNotThrow(() -> parseExpression("~/^\\w+@\\w+\\.\\w+$/"));
        assertDoesNotThrow(() -> parseExpression("~/pattern\\/with\\/slashes/"));
    }

    @Test
    public void testMvelSpecialLiterals() {
        assertDoesNotThrow(() -> parseExpression("empty"));
        assertDoesNotThrow(() -> parseExpression("nil"));
        assertDoesNotThrow(() -> parseExpression("undefined"));
    }

    @Test
    public void testThisExpression() {
        assertDoesNotThrow(() -> parseExpression("this"));
        assertDoesNotThrow(() -> parseExpression("this.field"));
        assertDoesNotThrow(() -> parseExpression("this.method()"));
    }

    @Test
    public void testSuperExpression() {
        assertDoesNotThrow(() -> parseExpression("super.field"));
        assertDoesNotThrow(() -> parseExpression("super.method"));
    }

    @Test
    public void testObjectCreation() {
        assertDoesNotThrow(() -> parseExpression("new Object()"));
        assertDoesNotThrow(() -> parseExpression("new String(\"hello\")"));
        assertDoesNotThrow(() -> parseExpression("new java.util.ArrayList()"));
        assertDoesNotThrow(() -> parseExpression("new MyClass(arg1, arg2)"));
    }

    @Test
    public void testPrimitiveTypes() {
        assertDoesNotThrow(() -> parseExpression("obj instanceof boolean"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof char"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof byte"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof short"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof int"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof long"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof float"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof double"));
    }

    @Test
    public void testArrayTypes() {
        assertDoesNotThrow(() -> parseExpression("obj instanceof int[]"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof String[]"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof int[][]"));
        assertDoesNotThrow(() -> parseExpression("obj instanceof java.util.List[]"));
    }

    @Test
    public void testComplexLiteralExpressions() {
        assertDoesNotThrow(() -> parseExpression("42 + 3.14"));
        assertDoesNotThrow(() -> parseExpression("\"Hello\" + \" \" + \"World\""));
        assertDoesNotThrow(() -> parseExpression("'a' < 'z'"));
        assertDoesNotThrow(() -> parseExpression("true && !false"));
        assertDoesNotThrow(() -> parseExpression("null == nil"));
        assertDoesNotThrow(() -> parseExpression("empty != undefined"));
    }

    @Test
    public void testLiteralsInComplexExpressions() {
        assertDoesNotThrow(() -> parseExpression("x > 0 ? \"positive\" : \"non-positive\""));
        assertDoesNotThrow(() -> parseExpression("items[0] == null ? empty : items[0]"));
        assertDoesNotThrow(() -> parseExpression("name != null && name != \"\""));
        assertDoesNotThrow(() -> parseExpression("age >= 18 && status == 'A'"));
    }
}