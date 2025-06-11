package org.mvel3.test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionOperationTest {

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
    public void testInlineListCreation() {
        assertDoesNotThrow(() -> parseExpression("[]"));
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3]"));
        assertDoesNotThrow(() -> parseExpression("[\"a\", \"b\", \"c\"]"));
        assertDoesNotThrow(() -> parseExpression("[1, \"hello\", true, null]"));
        assertDoesNotThrow(() -> parseExpression("[x, y, z]"));
    }

    @Test
    public void testInlineMapCreation() {
        assertDoesNotThrow(() -> parseExpression("{}"));
        assertDoesNotThrow(() -> parseExpression("{key: value}"));
        assertDoesNotThrow(() -> parseExpression("{\"name\": \"John\", \"age\": 30}"));
        assertDoesNotThrow(() -> parseExpression("{1: \"one\", 2: \"two\", 3: \"three\"}"));
        assertDoesNotThrow(() -> parseExpression("{key1: value1, key2: value2}"));
    }

    @Test
    public void testNestedCollections() {
        assertDoesNotThrow(() -> parseExpression("[[1, 2], [3, 4]]"));
        assertDoesNotThrow(() -> parseExpression("[{\"name\": \"John\"}, {\"name\": \"Jane\"}]"));
        assertDoesNotThrow(() -> parseExpression("{\"users\": [\"John\", \"Jane\"], \"count\": 2}"));
        assertDoesNotThrow(() -> parseExpression("{\"matrix\": [[1, 2], [3, 4]]}"));
    }

    @Test
    public void testCollectionProjection() {
        assertDoesNotThrow(() -> parseExpression("list.{name}"));
        assertDoesNotThrow(() -> parseExpression("people.{person.name}"));
        assertDoesNotThrow(() -> parseExpression("items.{item.price * 1.1}"));
        assertDoesNotThrow(() -> parseExpression("users.{user.firstName + \" \" + user.lastName}"));
    }

    @Test
    public void testCollectionSelection() {
        assertDoesNotThrow(() -> parseExpression("list.?(item > 5)"));
        assertDoesNotThrow(() -> parseExpression("people.?(person.age >= 18)"));
        assertDoesNotThrow(() -> parseExpression("items.?(item.price < 100)"));
        assertDoesNotThrow(() -> parseExpression("users.?(user.active == true)"));
    }

    @Test
    public void testComplexCollectionOperations() {
        assertDoesNotThrow(() -> parseExpression("list.?(item > 0).{item * 2}"));
        assertDoesNotThrow(() -> parseExpression("people.?(person.age >= 21).{person.name}"));
        assertDoesNotThrow(() -> parseExpression("products.?(product.inStock).{product.name + \": $\" + product.price}"));
    }

    @Test
    public void testArrayAccess() {
        assertDoesNotThrow(() -> parseExpression("array[0]"));
        assertDoesNotThrow(() -> parseExpression("matrix[i][j]"));
        assertDoesNotThrow(() -> parseExpression("map[\"key\"]"));
        assertDoesNotThrow(() -> parseExpression("list[index]"));
        assertDoesNotThrow(() -> parseExpression("data[key1][key2]"));
    }

    @Test
    public void testSafeArrayAccess() {
        assertDoesNotThrow(() -> parseExpression("array?.get(0)"));
        assertDoesNotThrow(() -> parseExpression("map?.get(\"key\")"));
        assertDoesNotThrow(() -> parseExpression("matrix?.get(i)?.get(j)"));
        assertDoesNotThrow(() -> parseExpression("data?.get(key1)?.get(key2)"));
    }

    @Test
    public void testCollectionWithExpressions() {
        assertDoesNotThrow(() -> parseExpression("[1 + 2, 3 * 4, 5 - 1]"));
        assertDoesNotThrow(() -> parseExpression("{\"sum\": a + b, \"product\": a * b}"));
        assertDoesNotThrow(() -> parseExpression("[method(), obj.field, variable]"));
        assertDoesNotThrow(() -> parseExpression("{getName(): getValue(), \"static\": 42}"));
    }

    @Test
    public void testCollectionOperatorsWithCollections() {
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3] contains 2"));
        assertDoesNotThrow(() -> parseExpression("5 in [1, 2, 3, 4, 5]"));
        assertDoesNotThrow(() -> parseExpression("\"key\" in {\"key\": \"value\"}"));
        assertDoesNotThrow(() -> parseExpression("{\"a\": 1, \"b\": 2} contains \"a\""));
    }

    @Test
    public void testArrayCreation() {
        assertDoesNotThrow(() -> parseExpression("new int[5]"));
        assertDoesNotThrow(() -> parseExpression("new String[10]"));
        assertDoesNotThrow(() -> parseExpression("new int[]{1, 2, 3}"));
        assertDoesNotThrow(() -> parseExpression("new String[]{\"a\", \"b\", \"c\"}"));
        assertDoesNotThrow(() -> parseExpression("new int[3][4]"));
    }

    @Test
    public void testCollectionMixedWithOtherOperations() {
        assertDoesNotThrow(() -> parseExpression("list.size() > 0"));
        assertDoesNotThrow(() -> parseExpression("[1, 2, 3].length"));
        assertDoesNotThrow(() -> parseExpression("map.get(\"key\") != null"));
        assertDoesNotThrow(() -> parseExpression("array[0] + array[1]"));
        assertDoesNotThrow(() -> parseExpression("{\"result\": x + y}.result"));
    }
}