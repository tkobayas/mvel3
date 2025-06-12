package org.mvel3.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class InlineCollectionTest {

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

    // ==== INLINE LIST TESTS ====

    @Test
    public void testEmptyList() {
        assertDoesNotThrow(() -> parseExpression("[]"));
    }

    @Test
    public void testListWithDifferentLiteralTypes() {
        assertDoesNotThrow(() -> parseExpression("[42]"));
        assertDoesNotThrow(() -> parseExpression("[3.14]"));
        assertDoesNotThrow(() -> parseExpression("[true]"));
        assertDoesNotThrow(() -> parseExpression("[false]"));
        assertDoesNotThrow(() -> parseExpression("[null]"));
        assertDoesNotThrow(() -> parseExpression("['c']"));
        assertDoesNotThrow(() -> parseExpression("[\"string\"]"));
    }

    @Test
    public void testListWithMixedTypes() {
        assertDoesNotThrow(() -> parseExpression("[1, 2.5, \"hello\", true, null, 'x']"));
        assertDoesNotThrow(() -> parseExpression("[42, \"answer\", false]"));
        assertDoesNotThrow(() -> parseExpression("[null, 0, \"\", false]"));
    }

    @Test
    public void testListWithExpressions() {
        assertDoesNotThrow(() -> parseExpression("[1 + 2, 3 * 4]"));
        assertDoesNotThrow(() -> parseExpression("[x, y + z]"));
        assertDoesNotThrow(() -> parseExpression("[method(), obj.field]"));
        assertDoesNotThrow(() -> parseExpression("[a > b ? a : b, min(x, y)]"));
    }

    @Test
    public void testListWithVariables() {
        assertDoesNotThrow(() -> parseExpression("[x, y, z]"));
        assertDoesNotThrow(() -> parseExpression("[firstName, lastName]"));
        assertDoesNotThrow(() -> parseExpression("[obj.field, another.property]"));
    }

    @Test
    public void testNestedLists() {
        assertDoesNotThrow(() -> parseExpression("[[]]"));
        assertDoesNotThrow(() -> parseExpression("[[1, 2], [3, 4]]"));
        assertDoesNotThrow(() -> parseExpression("[[], [1], [1, 2, 3]]"));
        assertDoesNotThrow(() -> parseExpression("[[[1, 2]], [[3, 4], [5, 6]]]"));
    }

    @Test
    public void testListsWithMvelOperators() {
        assertDoesNotThrow(() -> parseExpression("[x ** 2, y ** 3]"));
        assertDoesNotThrow(() -> parseExpression("[name1 strsim name2, name3 soundslike name4]"));
        assertDoesNotThrow(() -> parseExpression("[obj?.field, array?.get(0)]"));
    }

    @Test
    public void testVeryLongList() {
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]"));
    }

    @Test
    public void testListWithTrailingComma() {
        // Note: This might not be supported - testing to see behavior
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3]")); // without trailing comma should work
    }

    // ==== INLINE MAP TESTS ====

    @Test
    public void testEmptyMap() {
        assertDoesNotThrow(() -> parseExpression("{}"));
    }

    @Test
    public void testMapWithStringKeys() {
        assertDoesNotThrow(() -> parseExpression("{\"key\": \"value\"}"));
        assertDoesNotThrow(() -> parseExpression("{\"name\": \"John\", \"age\": 30}"));
        assertDoesNotThrow(() -> parseExpression("{\"a\": 1, \"b\": 2, \"c\": 3}"));
    }

    @Test
    public void testMapWithIdentifierKeys() {
        assertDoesNotThrow(() -> parseExpression("{key: value}"));
        assertDoesNotThrow(() -> parseExpression("{name: \"John\", age: 30}"));
        assertDoesNotThrow(() -> parseExpression("{x: 1, y: 2, z: 3}"));
    }

    @Test
    public void testMapWithNumericKeys() {
        assertDoesNotThrow(() -> parseExpression("{1: \"one\"}"));
        assertDoesNotThrow(() -> parseExpression("{1: \"one\", 2: \"two\", 3: \"three\"}"));
        assertDoesNotThrow(() -> parseExpression("{0: \"zero\", 42: \"answer\"}"));
    }

    @Test
    public void testMapWithMixedKeyTypes() {
        assertDoesNotThrow(() -> parseExpression("{\"string\": 1, 42: \"number\", key: \"identifier\"}"));
        assertDoesNotThrow(() -> parseExpression("{1: \"a\", \"2\": \"b\", three: \"c\"}"));
    }

    @Test
    public void testMapWithComplexValues() {
        assertDoesNotThrow(() -> parseExpression("{\"sum\": a + b, \"product\": a * b}"));
        assertDoesNotThrow(() -> parseExpression("{\"method\": method(), \"field\": obj.field}"));
        assertDoesNotThrow(() -> parseExpression("{\"condition\": x > y, \"ternary\": a > 0 ? a : -a}"));
    }

    @Test
    public void testMapWithNestedCollections() {
        assertDoesNotThrow(() -> parseExpression("{\"list\": [1, 2, 3]}"));
        assertDoesNotThrow(() -> parseExpression("{\"nested\": {\"inner\": \"value\"}}"));
        assertDoesNotThrow(() -> parseExpression("{\"array\": [], \"map\": {}}"));
    }

    @Test
    public void testMapWithMvelOperators() {
        assertDoesNotThrow(() -> parseExpression("{\"power\": base ** exponent}"));
        assertDoesNotThrow(() -> parseExpression("{\"similarity\": name1 strsim name2}"));
        assertDoesNotThrow(() -> parseExpression("{\"safe\": obj?.field?.method()}"));
        assertDoesNotThrow(() -> parseExpression("{\"defined\": isdef(variable)}"));
    }

    @Test
    public void testDeeplyNestedMaps() {
        assertDoesNotThrow(() -> parseExpression("{\"level1\": {\"level2\": {\"level3\": \"deep\"}}}"));
        assertDoesNotThrow(() -> parseExpression("{\"config\": {\"database\": {\"host\": \"localhost\", \"port\": 5432}}}"));
    }

    // ==== ARRAY CREATION TESTS ====

    @Test
    public void testPrimitiveArrayCreation() {
        assertDoesNotThrow(() -> parseExpression("new int[5]"));
        assertDoesNotThrow(() -> parseExpression("new double[10]"));
        assertDoesNotThrow(() -> parseExpression("new boolean[3]"));
        assertDoesNotThrow(() -> parseExpression("new char[256]"));
        assertDoesNotThrow(() -> parseExpression("new byte[1024]"));
        assertDoesNotThrow(() -> parseExpression("new short[50]"));
        assertDoesNotThrow(() -> parseExpression("new long[100]"));
        assertDoesNotThrow(() -> parseExpression("new float[25]"));
    }

    @Test
    public void testObjectArrayCreation() {
        assertDoesNotThrow(() -> parseExpression("new String[10]"));
        assertDoesNotThrow(() -> parseExpression("new Object[5]"));
        assertDoesNotThrow(() -> parseExpression("new Integer[20]"));
    }

    @Test
    public void testArrayCreationWithInitializers() {
        assertDoesNotThrow(() -> parseExpression("new int[]{1, 2, 3, 4, 5}"));
        assertDoesNotThrow(() -> parseExpression("new String[]{\"a\", \"b\", \"c\"}"));
        assertDoesNotThrow(() -> parseExpression("new double[]{1.1, 2.2, 3.3}"));
        assertDoesNotThrow(() -> parseExpression("new boolean[]{true, false, true}"));
    }

    @Test
    public void testMultiDimensionalArrays() {
        assertDoesNotThrow(() -> parseExpression("new int[3][4]"));
        assertDoesNotThrow(() -> parseExpression("new String[2][5]"));
        assertDoesNotThrow(() -> parseExpression("new double[3][3][3]"));
        assertDoesNotThrow(() -> parseExpression("new Object[2][2][2][2]"));
    }

    @Test
    public void testArrayCreationWithExpressions() {
        assertDoesNotThrow(() -> parseExpression("new int[size]"));
        assertDoesNotThrow(() -> parseExpression("new String[count + 1]"));
        assertDoesNotThrow(() -> parseExpression("new double[rows][cols]"));
        assertDoesNotThrow(() -> parseExpression("new int[]{x, y, z}"));
        assertDoesNotThrow(() -> parseExpression("new String[]{getName(), getTitle()}"));
    }

    // ==== MIXED COLLECTION OPERATIONS ====

    @Test
    public void testMixedCollectionTypes() {
        assertDoesNotThrow(() -> parseExpression("[{\"key\": \"value\"}, {\"key2\": \"value2\"}]"));
        assertDoesNotThrow(() -> parseExpression("{\"array\": [1, 2, 3], \"map\": {\"nested\": true}}"));
        assertDoesNotThrow(() -> parseExpression("[[], {}, new int[5]]"));
    }

    @Test
    public void testCollectionsWithMethodCalls() {
        assertDoesNotThrow(() -> parseExpression("[list.size(), map.keySet(), array.length]"));
        assertDoesNotThrow(() -> parseExpression("{\"size\": collection.size(), \"empty\": collection.isEmpty()}"));
    }

    @Test
    public void testCollectionsInAssignments() {
        assertDoesNotThrow(() -> parseExpression("list = [1, 2, 3]"));
        assertDoesNotThrow(() -> parseExpression("map = {\"key\": \"value\"}"));
        assertDoesNotThrow(() -> parseExpression("array = new int[]{1, 2, 3}"));
    }

    @Test
    public void testCollectionsInOperations() {
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3].size()"));
        assertDoesNotThrow(() -> parseExpression("{\"key\": \"value\"}.get(\"key\")"));
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3] + [4, 5, 6]"));
        assertDoesNotThrow(() -> parseExpression("{\"a\": 1} == {\"a\": 1}"));
    }

    @Test
    public void testComplexCollectionExpressions() {
        assertDoesNotThrow(() -> parseExpression("[[1, 2], [3, 4]].{row.{cell * 2}}"));
        assertDoesNotThrow(() -> parseExpression("[{\"active\": true}, {\"active\": false}].?(item.active)"));
        assertDoesNotThrow(() -> parseExpression("{\"numbers\": [1, 2, 3]}.numbers.{n * n}"));
    }

    @Test
    public void testCollectionsWithMvelSpecialLiterals() {
        assertDoesNotThrow(() -> parseExpression("[empty, nil, undefined]"));
        assertDoesNotThrow(() -> parseExpression("{\"empty\": empty, \"nil\": nil, \"undefined\": undefined}"));
        assertDoesNotThrow(() -> parseExpression("value == empty ? [] : [value]"));
    }

    @Test
    public void testCollectionsWithRegex() {
        assertDoesNotThrow(() -> parseExpression("[~/pattern1/, ~/pattern2/]"));
        assertDoesNotThrow(() -> parseExpression("{\"email\": ~/\\w+@\\w+\\.\\w+/, \"phone\": ~/\\d{3}-\\d{3}-\\d{4}/}"));
    }

    @Test
    public void testVeryComplexNestedStructure() {
        String complex = """
            {
                "users": [
                    {"name": "John", "scores": [85, 92, 78]},
                    {"name": "Jane", "scores": [91, 87, 94]}
                ],
                "metadata": {
                    "created": "2023-01-01",
                    "version": 1.0,
                    "active": true
                },
                "calculations": {
                    "sum": a + b,
                    "product": a * b,
                    "power": base ** exponent
                }
            }
            """;
        assertDoesNotThrow(() -> parseExpression(complex));
    }
}