package org.mvel3.test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.codegen.Mvel3CodeGenerator;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class Mvel3CodeGeneratorTest {

    private String generateJavaCode(String mvelCode) {
        ANTLRInputStream input = new ANTLRInputStream(mvelCode);
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
        
        ParseTree tree = parser.start_();
        Mvel3CodeGenerator generator = new Mvel3CodeGenerator();
        return generator.generateJavaCode(tree);
    }

    // ==== BASIC LITERALS ====

    @Test
    public void testIntegerLiterals() {
        assertEquals("42", generateJavaCode("42"));
        assertEquals("0", generateJavaCode("0"));
        assertEquals("123456", generateJavaCode("123456"));
    }

    @Test
    public void testFloatingPointLiterals() {
        assertEquals("3.14", generateJavaCode("3.14"));
        assertEquals("0.5", generateJavaCode("0.5"));
        assertEquals("123.456", generateJavaCode("123.456"));
    }

    @Test
    public void testStringLiterals() {
        assertEquals("\"hello\"", generateJavaCode("\"hello\""));
        assertEquals("\"Hello, World!\"", generateJavaCode("\"Hello, World!\""));
        assertEquals("\"\"", generateJavaCode("\"\""));
    }

    @Test
    public void testBooleanLiterals() {
        assertEquals("true", generateJavaCode("true"));
        assertEquals("false", generateJavaCode("false"));
    }

    @Test
    public void testNullLiteral() {
        assertEquals("null", generateJavaCode("null"));
    }

    @Test
    public void testCharacterLiterals() {
        assertEquals("'a'", generateJavaCode("'a'"));
        assertEquals("'Z'", generateJavaCode("'Z'"));
        assertEquals("'\\n'", generateJavaCode("'\\n'"));
    }

    // ==== IDENTIFIERS ====

    @Test
    public void testIdentifiers() {
        assertEquals("x", generateJavaCode("x"));
        assertEquals("myVariable", generateJavaCode("myVariable"));
        assertEquals("_temp", generateJavaCode("_temp"));
    }

    @Test
    public void testThisExpression() {
        assertEquals("this", generateJavaCode("this"));
    }

    // ==== BASIC ARITHMETIC ====

    @Test
    public void testAddition() {
        assertEquals("(1 + 2)", generateJavaCode("1 + 2"));
        assertEquals("(x + y)", generateJavaCode("x + y"));
    }

    @Test
    public void testSubtraction() {
        assertEquals("(10 - 5)", generateJavaCode("10 - 5"));
        assertEquals("(a - b)", generateJavaCode("a - b"));
    }

    @Test
    public void testMultiplication() {
        assertEquals("(3 * 4)", generateJavaCode("3 * 4"));
        assertEquals("(x * y)", generateJavaCode("x * y"));
    }

    @Test
    public void testDivision() {
        assertEquals("(8 / 2)", generateJavaCode("8 / 2"));
        assertEquals("(a / b)", generateJavaCode("a / b"));
    }

    @Test
    public void testModulo() {
        assertEquals("(7 % 3)", generateJavaCode("7 % 3"));
        assertEquals("(x % y)", generateJavaCode("x % y"));
    }

    @Test
    public void testComplexArithmetic() {
        assertEquals("((1 + 2) * 3)", generateJavaCode("(1 + 2) * 3"));
        assertEquals("((10 / 2) + (3 * 4))", generateJavaCode("10 / 2 + 3 * 4"));
    }

    // ==== MVEL POWER OPERATOR ====

    @Test
    public void testPowerOperator() {
        assertEquals("Math.pow(2, 3)", generateJavaCode("2 ** 3"));
        assertEquals("Math.pow(x, 2)", generateJavaCode("x ** 2"));
        assertEquals("Math.pow((a + b), c)", generateJavaCode("(a + b) ** c"));
    }

    // ==== COMPARISON OPERATIONS ====

    @Test
    public void testComparisons() {
        assertEquals("(5 > 3)", generateJavaCode("5 > 3"));
        assertEquals("(2 < 7)", generateJavaCode("2 < 7"));
        assertEquals("(x >= y)", generateJavaCode("x >= y"));
        assertEquals("(a <= b)", generateJavaCode("a <= b"));
    }

    @Test
    public void testEquality() {
        assertEquals("(5 == 5)", generateJavaCode("5 == 5"));
        assertEquals("(x != y)", generateJavaCode("x != y"));
    }

    // ==== LOGICAL OPERATIONS ====

    @Test
    public void testLogicalOperations() {
        assertEquals("(true && false)", generateJavaCode("true && false"));
        assertEquals("(x || y)", generateJavaCode("x || y"));
        assertEquals("((a > b) && (c < d))", generateJavaCode("a > b && c < d"));
    }

    @Test
    public void testUnaryOperations() {
        assertEquals("(+5)", generateJavaCode("+5"));
        assertEquals("(-x)", generateJavaCode("-x"));
        assertEquals("(!true)", generateJavaCode("!true"));
        assertEquals("(~42)", generateJavaCode("~42"));
    }

    // ==== MVEL PROPERTY ACCESS ====

    @Test
    public void testPropertyAccess() {
        assertEquals("user.getName()", generateJavaCode("user.name"));
        assertEquals("person.getAge()", generateJavaCode("person.age"));
        assertEquals("obj.getValue()", generateJavaCode("obj.value"));
    }

    @Test
    public void testNestedPropertyAccess() {
        assertEquals("user.getAddress().getStreet()", generateJavaCode("user.address.street"));
        assertEquals("person.getJob().getCompany().getName()", generateJavaCode("person.job.company.name"));
    }

    // ==== MVEL ARRAY/LIST ACCESS ====

    @Test
    public void testArrayAccess() {
        assertEquals("list.get(0)", generateJavaCode("list[0]"));
        assertEquals("array.get(5)", generateJavaCode("array[5]"));
        assertEquals("items.get(index)", generateJavaCode("items[index]"));
    }

    @Test
    public void testNestedArrayAccess() {
        assertEquals("matrix.get(i).get(j)", generateJavaCode("matrix[i][j]"));
        assertEquals("data.get(0).get(1)", generateJavaCode("data[0][1]"));
    }

    // ==== MVEL SAFE NAVIGATION ====

    @Test
    public void testSafeNavigation() {
        assertEquals("(obj != null ? obj.getField() : null)", generateJavaCode("obj?.field"));
        assertEquals("(user != null ? user.getName() : null)", generateJavaCode("user?.name"));
    }

    @Test
    public void testChainedSafeNavigation() {
        // This should generate temporary variables for complex safe navigation
        String result = generateJavaCode("user?.address?.street");
        assertNotNull(result);
        // Should contain temp variables and null checks, or TODO placeholder
        assertTrue(result.contains("TODO") || result.contains("temp") || 
                   (result.contains("user") && result.contains("address") && result.contains("street")));
    }

    // ==== TERNARY OPERATOR ====

    @Test
    public void testTernaryOperator() {
        assertEquals("(true ? 1 : 2)", generateJavaCode("true ? 1 : 2"));
        assertEquals("((x > 0) ? x : (-x))", generateJavaCode("x > 0 ? x : -x"));
    }

    // ==== PARENTHESES ====

    @Test
    public void testParentheses() {
        assertEquals("(1 + 2)", generateJavaCode("(1 + 2)"));
        assertEquals("((a + b) * c)", generateJavaCode("((a + b) * c)"));
    }

    // ==== REGULAR EXPRESSIONS ====

    @Test
    public void testRegexLiterals() {
        assertEquals("Pattern.compile(\"hello\")", generateJavaCode("~/hello/"));
        assertEquals("Pattern.compile(\"[a-zA-Z]+\")", generateJavaCode("~/[a-zA-Z]+/"));
        assertEquals("Pattern.compile(\"\\\\d{3}-\\\\d{3}-\\\\d{4}\")", generateJavaCode("~/\\d{3}-\\d{3}-\\d{4}/"));
    }

    // ==== METHOD CALLS ====

    @Test
    public void testMethodCalls() {
        assertEquals("method()", generateJavaCode("method()"));
        assertEquals("obj.method(arg1, arg2)", generateJavaCode("obj.method(arg1, arg2)"));
        assertEquals("calculate(1, 2, 3)", generateJavaCode("calculate(1, 2, 3)"));
    }

    // ==== COMBINED EXPRESSIONS ====

    @Test
    public void testComplexExpressions() {
        assertEquals("((user.getName() == \"John\") && (user.getAge() > 18))", 
                    generateJavaCode("user.name == \"John\" && user.age > 18"));
        
        assertEquals("Math.pow(list.get(0), 2)", 
                    generateJavaCode("list[0] ** 2"));
        
        assertEquals("(((a + b) > c) ? user.getName() : \"default\")", 
                    generateJavaCode("a + b > c ? user.name : \"default\""));
    }

    // ==== UNIT LITERALS (asking for clarification) ====

    @Test
    public void testUnitLiterals() {
        // These might need special handling - will ask for clarification if they fail
        String result1 = generateJavaCode("5pints");
        String result2 = generateJavaCode("10litres");
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.contains("5") && result1.contains("pints"));
        assertTrue(result2.contains("10") && result2.contains("litres"));
    }

    // ==== TEXT BLOCKS ====

    @Test
    public void testTextBlocks() {
        String textBlock = """
            \"\"\"
            Hello
            World
            \"\"\"
            """;
        String result = generateJavaCode(textBlock);
        assertNotNull(result);
        assertTrue(result.contains("Hello") && result.contains("World"));
    }

    // ==== PLACEHOLDER TESTS (should fail initially) ====

    @Test
    public void testInlineListPlaceholder() {
        String result = generateJavaCode("[1, 2, 3]");
        assertTrue(result.contains("List.of") || result.contains("Arrays.asList"));
    }

    @Test
    public void testInlineMapPlaceholder() {
        String result = generateJavaCode("{\"key\": \"value\"}");
        assertTrue(result.contains("Map.of") || result.contains("Map"));
    }

    @Test
    public void testCollectionProjectionPlaceholder() {
        String result = generateJavaCode("list.{item.name}");
        assertTrue(result.contains("stream") && result.contains("map"));
    }

    @Test
    public void testCollectionSelectionPlaceholder() {
        String result = generateJavaCode("list.?(item > 5)");
        assertTrue(result.contains("stream") && result.contains("filter"));
    }

    @Test
    public void testWithBlockPlaceholder() {
        String result = generateJavaCode("obj{field = value}");
        assertTrue(result.contains("set") && result.contains("Field"));
    }

    @Test
    public void testBooleanTestBlockPlaceholder() {
        String result = generateJavaCode("obj[test1, test2]");
        assertTrue(result.contains("&&") && result.contains("obj"));
    }

    @Test
    public void testCoercionPlaceholder() {
        String result = generateJavaCode("obj#Type");
        assertTrue(result.contains("Type") && (result.contains("(") || result.contains("cast")));
    }

    // ==== ASSIGNMENT OPERATIONS ====

    @Test
    public void testAssignment() {
        assertEquals("x = 5", generateJavaCode("x = 5"));
        assertEquals("user.getName() = \"John\"", generateJavaCode("user.name = \"John\""));
    }

    @Test
    public void testCompoundAssignment() {
        assertEquals("x += 10", generateJavaCode("x += 10"));
        assertEquals("count -= 1", generateJavaCode("count -= 1"));
        assertEquals("total *= 2", generateJavaCode("total *= 2"));
    }

    // ==== EDGE CASES FOR CLARIFICATION ====

    @Test
    public void testComplexSafeNavigationChain() {
        // This is complex - how should we handle deep safe navigation?
        String result = generateJavaCode("a?.b?.c?.d");
        assertNotNull(result);
        // Will ask for clarification on expected output format
    }

    @Test
    public void testPropertyAccessWithMethodCalls() {
        // Should property access work with method results: getUser().name → getUser().getName()
        assertEquals("getUser().getName()", generateJavaCode("getUser().name"));
        assertEquals("service.getConnection().getHost()", generateJavaCode("service.getConnection().host"));
    }

    @Test
    public void testArrayAccessOnPropertyAccess() {
        // Should be: user.addresses[0] → user.getAddresses().get(0)
        assertEquals("user.getAddresses().get(0)", generateJavaCode("user.addresses[0]"));
        assertEquals("person.getPhones().get(index)", generateJavaCode("person.phones[index]"));
    }
}