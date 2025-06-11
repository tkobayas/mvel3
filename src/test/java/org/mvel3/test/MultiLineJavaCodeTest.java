package org.mvel3.test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class MultiLineJavaCodeTest {

    private ParseTree parseCode(String code) {
        ANTLRInputStream input = new ANTLRInputStream(code);
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
    public void testVariableDeclarations() {
        String code = """
            int x = 5;
            String name = "John";
            boolean flag = true;
            double price = 29.99;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testMultipleVariableDeclarations() {
        String code = """
            int a = 1, b = 2, c = 3;
            String firstName = "John", lastName = "Doe";
            final int MAX_SIZE = 100;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testIfElseStatements() {
        String code = """
            if (x > 0) {
                result = "positive";
            } else if (x < 0) {
                result = "negative";
            } else {
                result = "zero";
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testSimpleIfStatement() {
        String code = """
            if (condition) 
                doSomething();
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testWhileLoop() {
        String code = """
            int i = 0;
            while (i < 10) {
                sum += i;
                i++;
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testForLoop() {
        String code = """
            for (int i = 0; i < array.length; i++) {
                total += array[i];
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testEnhancedForLoop() {
        String code = """
            for (String item : collection) {
                process(item);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testDoWhileLoop() {
        String code = """
            int i = 0;
            do {
                process(i);
                i++;
            } while (i < 5);
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testNestedControlStructures() {
        String code = """
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    for (int j = 0; j < 5; j++) {
                        if (j > 2) {
                            break;
                        }
                        result += i * j;
                    }
                } else {
                    continue;
                }
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testTryCatchFinally() {
        String code = """
            try {
                riskyOperation();
            } catch (Exception e) {
                handleError(e);
            } finally {
                cleanup();
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testMultipleCatchBlocks() {
        String code = """
            try {
                parseNumber(input);
            } catch (NumberFormatException e) {
                handleNumberFormatError(e);
            } catch (IllegalArgumentException e) {
                handleIllegalArgument(e);
            } catch (Exception e) {
                handleGenericError(e);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testSwitchStatement() {
        String code = """
            switch (dayOfWeek) {
                case 1:
                    dayName = "Monday";
                    break;
                case 2:
                    dayName = "Tuesday";
                    break;
                case 3:
                    dayName = "Wednesday";
                    break;
                default:
                    dayName = "Unknown";
                    break;
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testReturnStatements() {
        String code = """
            if (value == null) {
                return null;
            }
            
            if (value < 0) {
                return -1;
            }
            
            return value * 2;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testBreakAndContinue() {
        String code = """
            for (int i = 0; i < 100; i++) {
                if (i < 10) {
                    continue;
                }
                if (i > 50) {
                    break;
                }
                process(i);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testLabeledStatements() {
        String code = """
            outer: for (int i = 0; i < 10; i++) {
                inner: for (int j = 0; j < 10; j++) {
                    if (i * j > 20) {
                        break outer;
                    }
                    if (j == 5) {
                        continue outer;
                    }
                }
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testComplexAssignments() {
        String code = """
            result = calculate(x, y);
            array[index] = newValue;
            obj.field = getValue();
            map.put(key, value);
            counter += increment;
            total *= factor;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testArrayDeclarations() {
        String code = """
            int[] numbers = new int[10];
            String[] names = {"John", "Jane", "Bob"};
            int[][] matrix = new int[5][5];
            Object[] objects = new Object[]{obj1, obj2, obj3};
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testMethodCalls() {
        String code = """
            result = method();
            obj.processData(input, flags);
            list.add(item);
            value = Math.max(a, b);
            System.out.println("Debug: " + value);
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testComplexExpressions() {
        String code = """
            result = (a + b) * (c - d) / e;
            condition = (x > 0) && (y < 100) || (z == 50);
            value = obj?.field?.method()?.result;
            isValid = name != null && name.length() > 0;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testMvelSpecificInMultiLine() {
        String code = """
            power = base ** exponent;
            similarity = name1 strsim name2;
            
            if (list contains item) {
                found = true;
            }
            
            if (isdef(variable)) {
                process(variable);
            }
            
            filtered = items.?(item.active == true);
            names = people.{person.name};
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testEmptyStatements() {
        String code = """
            int x = 5;
            ;
            ;
            y = x + 1;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testBlockStatements() {
        String code = """
            {
                int localVar = 10;
                process(localVar);
            }
            
            {
                String temp = "temporary";
                log(temp);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testRealWorldExample() {
        String code = """
            int sum = 0;
            int count = 0;
            
            for (int i = 0; i < data.length; i++) {
                if (data[i] != null && data[i] > threshold) {
                    sum += data[i];
                    count++;
                    
                    if (count >= maxItems) {
                        break;
                    }
                }
            }
            
            double average = count > 0 ? sum * 1.0 / count : 0.0;
            
            if (average > targetAverage) {
                result = "ABOVE_TARGET";
            } else if (average < minimumAverage) {
                result = "BELOW_MINIMUM";
            } else {
                result = "WITHIN_RANGE";
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }
}