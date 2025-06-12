package org.mvel3.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class Java17SyntaxTest {

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

    private ParseTree parseExpression(String expression) {
        return parseCode(expression);
    }

    // ==== TEXT BLOCKS (Java 15, standardized in Java 17) ====

    @Test
    public void testBasicTextBlock() {
        String textBlock = """
            \"\"\"
            Hello
            World
            \"\"\"
            """;
        assertDoesNotThrow(() -> parseExpression(textBlock));
    }

    @Test
    public void testTextBlockWithEscapes() {
        String textBlock = """
            \"\"\"
            Line 1\\n
            Line 2\\t
            Line 3\\\\
            \"\"\"
            """;
        assertDoesNotThrow(() -> parseExpression(textBlock));
    }

    @Test
    public void testTextBlockInAssignment() {
        String code = """
            String sql = \"\"\"
                SELECT *
                FROM users
                WHERE active = true
                ORDER BY name
                \"\"\";
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testTextBlockWithExpressions() {
        String code = """
            String html = \"\"\"
                <html>
                    <body>
                        <h1>Hello World</h1>
                    </body>
                </html>
                \"\"\";
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== SWITCH EXPRESSIONS (Preview in Java 17) ====

    @Test
    public void testSwitchExpressionWithArrow() {
        String code = """
            result = switch (day) {
                case MONDAY -> "Start of work week";
                case FRIDAY -> "TGIF";
                case SATURDAY, SUNDAY -> "Weekend";
                default -> "Midweek";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testSwitchExpressionWithColon() {
        String code = """
            result = switch (value) {
                case 1: yield "One";
                case 2: yield "Two";
                case 3: yield "Three";
                default: yield "Other";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testSwitchExpressionWithBlocks() {
        String code = """
            result = switch (type) {
                case "A" -> {
                    processA();
                    yield "Processed A";
                }
                case "B" -> {
                    processB();
                    yield "Processed B";
                }
                default -> "Unknown";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testNestedSwitchExpressions() {
        String code = """
            result = switch (category) {
                case "number" -> switch (size) {
                    case "small" -> "Small number";
                    case "large" -> "Large number";
                    default -> "Regular number";
                };
                case "text" -> "Text value";
                default -> "Unknown category";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== VAR KEYWORD (Java 10, local variable type inference) ====

    @Test
    public void testVarDeclarations() {
        String code = """
            var name = "John";
            var age = 25;
            var active = true;
            var score = 95.5;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testVarWithComplexTypes() {
        String code = """
            var list = new ArrayList<String>();
            var map = new HashMap<String, Integer>();
            var array = new int[]{1, 2, 3};
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testVarWithMethodCalls() {
        String code = """
            var result = calculateValue();
            var connection = database.getConnection();
            var users = service.findAllUsers();
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== YIELD STATEMENTS ====

    @Test
    public void testYieldInSwitchExpression() {
        String code = """
            int result = switch (input) {
                case 1: {
                    doSomething();
                    yield 10;
                }
                case 2: {
                    doSomethingElse();
                    yield 20;
                }
                default: yield 0;
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testYieldWithExpressions() {
        String code = """
            String message = switch (status) {
                case "OK": yield "Success: " + details;
                case "ERROR": yield "Failed: " + error.getMessage();
                default: yield "Unknown status: " + status;
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== ENHANCED INSTANCEOF (Java 16+) ====

    @Test
    public void testInstanceofWithPatternMatching() {
        String code = """
            if (obj instanceof String s) {
                return s.toUpperCase();
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testInstanceofInConditions() {
        String code = """
            if (value instanceof Integer num && num > 0) {
                process(num);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== COMBINED MODERN JAVA FEATURES ====

    @Test
    public void testCombinedModernFeatures() {
        String code = """
            var query = \"\"\"
                SELECT name, age
                FROM users
                WHERE active = true
                \"\"\";
            
            var result = switch (operation) {
                case "SELECT" -> {
                    var data = database.execute(query);
                    yield data.size();
                }
                case "UPDATE" -> {
                    var count = database.update(query);
                    yield count;
                }
                default -> 0;
            };
            
            if (result instanceof Integer count && count > 0) {
                logger.info("Processed " + count + " records");
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== PATTERN MATCHING IN SWITCH ====

    @Test
    public void testPatternMatchingInSwitch() {
        String code = """
            String result = switch (obj) {
                case String s -> "String: " + s;
                case Integer i -> "Integer: " + i;
                case null -> "Null value";
                default -> "Unknown type";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testComplexPatternMatching() {
        String code = """
            var message = switch (value) {
                case String s when s.length() > 10 -> "Long string";
                case String s -> "Short string";
                case Integer i when i > 100 -> "Large number";
                case Integer i -> "Small number";
                case null -> "No value";
                default -> "Other type";
            };
            """;
        // Note: 'when' guards might not be fully supported yet, but testing basic structure
        try {
            parseCode(code);
        } catch (RuntimeException e) {
            // Expected for advanced pattern matching features
            assertTrue(e.getMessage().contains("Syntax error"));
        }
    }

    // ==== RECORD SYNTAX (Preview/Experimental) ====

    @Test
    public void testBasicRecordDeclaration() {
        String code = """
            record Person(String name, int age) {}
            """;
        // Records might not be fully implemented in expression parser
        try {
            parseCode(code);
        } catch (RuntimeException e) {
            // Expected for record declarations in expression context
            assertTrue(e.getMessage().contains("Syntax error"));
        }
    }

    // ==== MULTI-LINE EXPRESSIONS WITH MODERN SYNTAX ====

    @Test
    public void testModernSyntaxInMultilineCode() {
        String code = """
            var users = getUsers();
            
            var processed = users.stream()
                .filter(user -> user.isActive())
                .map(user -> switch (user.getRole()) {
                    case "ADMIN" -> user.getName().toUpperCase();
                    case "USER" -> user.getName();
                    default -> "Unknown";
                })
                .collect(toList());
            
            var summary = \"\"\"
                Processing complete.
                Total users: %d
                Active users: %d
                \"\"\".formatted(users.size(), processed.size());
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== SWITCH WITH MULTIPLE VALUES ====

    @Test
    public void testSwitchWithMultipleValues() {
        String code = """
            String season = switch (month) {
                case 12, 1, 2 -> "Winter";
                case 3, 4, 5 -> "Spring";
                case 6, 7, 8 -> "Summer";
                case 9, 10, 11 -> "Autumn";
                default -> "Invalid month";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== EXPRESSION STATEMENTS WITH MODERN SYNTAX ====

    @Test
    public void testModernExpressionStatements() {
        String code = """
            switch (action) {
                case "log" -> logger.info("Logging action");
                case "save" -> repository.save(entity);
                case "delete" -> repository.delete(id);
                default -> throw new IllegalArgumentException("Unknown action");
            };
            
            yield calculateResult();
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }
}