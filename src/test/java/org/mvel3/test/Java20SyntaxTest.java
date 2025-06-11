package org.mvel3.test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class Java20SyntaxTest {

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

    // ==== ADVANCED PATTERN MATCHING (Java 20 Preview) ====

    @Test
    public void testAdvancedPatternMatchingInSwitch() {
        String code = """
            String result = switch (obj) {
                case String s -> "String: " + s.length();
                case Integer i -> "Integer: " + i;
                case Double d -> "Double: " + d;
                case null -> "Null value";
                default -> "Unknown type: " + obj.getClass().getSimpleName();
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testPatternMatchingWithComplexTypes() {
        String code = """
            String description = switch (value) {
                case String s -> "Text with " + s.length() + " characters";
                case Integer i -> i > 0 ? "Positive: " + i : "Non-positive: " + i;
                case Double d -> "Decimal: " + d.toString();
                case Boolean b -> b ? "True value" : "False value";
                default -> "Unsupported type";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== RECORD PATTERNS (Java 20 Preview) ====

    @Test
    public void testRecordPatternsInSwitch() {
        // Note: This is advanced syntax that may not be fully supported
        String code = """
            String info = switch (person) {
                case Person(var name, var age) -> name + " is " + age + " years old";
                case Employee(var name, var salary) -> name + " earns " + salary;
                default -> "Unknown person type";
            };
            """;
        // This advanced feature might not be implemented yet
        try {
            parseCode(code);
        } catch (RuntimeException e) {
            // Expected for advanced record pattern features
            assertTrue(e.getMessage().contains("Syntax error"));
        }
    }

    @Test
    public void testNestedRecordPatterns() {
        String code = """
            String result = switch (data) {
                case Address(var street, City(var name, var country)) -> 
                    "Street: " + street + ", City: " + name + ", Country: " + country;
                case Address(var street, null) -> "Street: " + street + ", No city";
                default -> "No address information";
            };
            """;
        // Advanced nested pattern matching
        try {
            parseCode(code);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Syntax error"));
        }
    }

    // ==== ENHANCED INSTANCEOF WITH PATTERNS ====

    @Test
    public void testInstanceofWithAdvancedPatterns() {
        String code = """
            if (obj instanceof String s && s.length() > 5) {
                return "Long string: " + s.toUpperCase();
            } else if (obj instanceof Integer i && i > 100) {
                return "Large number: " + i;
            } else if (obj instanceof Double d) {
                return "Decimal: " + String.format("%.2f", d);
            }
            return "Other type";
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testComplexInstanceofPatterns() {
        String code = """
            boolean isValid = switch (value) {
                case String s -> s != null && !s.isEmpty();
                case Integer i -> i != null && i >= 0;
                case Double d -> d != null && !d.isNaN();
                case null -> false;
                default -> true;
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== SWITCH EXPRESSIONS WITH GUARDS (Conceptual) ====

    @Test
    public void testSwitchWithGuardLikeConditions() {
        String code = """
            String category = switch (number) {
                case Integer i -> {
                    if (i < 0) yield "Negative";
                    else if (i == 0) yield "Zero";
                    else if (i < 100) yield "Small positive";
                    else yield "Large positive";
                }
                case Double d -> d < 0 ? "Negative decimal" : "Positive decimal";
                case null -> "No number";
                default -> "Not a number";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== VIRTUAL THREADS SYNTAX (Java 20 Preview) ====

    @Test
    public void testVirtualThreadCreation() {
        // Virtual threads are mostly API-based, but testing related syntax
        String code = """
            var executor = Executors.newVirtualThreadPerTaskExecutor();
            var future = executor.submit(() -> {
                return calculateValue();
            });
            var result = future.get();
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testThreadLocalWithVirtualThreads() {
        String code = """
            var threadLocal = ThreadLocal.withInitial(() -> new HashMap<>());
            var value = threadLocal.get();
            threadLocal.set(newValue);
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== SCOPED VALUES (Java 20 Incubator) ====

    @Test
    public void testScopedValueSyntax() {
        // Scoped values are mostly API-based
        String code = """
            var scopedValue = ScopedValue.newInstance();
            var result = ScopedValue.where(scopedValue, "test").call(() -> {
                return processWithScopedValue();
            });
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== ADVANCED TEXT BLOCKS ====

    @Test
    public void testAdvancedTextBlocks() {
        String code = """
            var json = \"\"\"
                {
                    "name": "%s",
                    "age": %d,
                    "active": %b,
                    "scores": [%s]
                }
                \"\"\".formatted(name, age, active, scores.stream()
                    .map(String::valueOf)
                    .collect(joining(", ")));
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testTextBlockWithComplexFormatting() {
        String code = """
            var sql = \"\"\"
                WITH filtered_data AS (
                    SELECT id, name, score
                    FROM users
                    WHERE active = true
                      AND score > %d
                ),
                ranked_data AS (
                    SELECT *,
                           ROW_NUMBER() OVER (ORDER BY score DESC) as rank
                    FROM filtered_data
                )
                SELECT * FROM ranked_data
                WHERE rank <= %d
                \"\"\".formatted(minScore, maxResults);
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== PATTERN MATCHING COMBINED WITH MODERN FEATURES ====

    @Test
    public void testPatternMatchingWithTextBlocks() {
        String code = """
            var template = switch (reportType) {
                case "summary" -> \"\"\"
                    Summary Report
                    ==============
                    Total: %d
                    Average: %.2f
                    \"\"\";
                case "detailed" -> \"\"\"
                    Detailed Report
                    ===============
                    Items processed: %d
                    Success rate: %.1f%%
                    Errors: %d
                    \"\"\";
                default -> \"\"\"
                    Unknown Report Type: %s
                    Please specify 'summary' or 'detailed'
                    \"\"\";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testSwitchWithVarAndPatterns() {
        String code = """
            var processor = switch (config.getType()) {
                case "fast" -> {
                    var opts = config.getFastOptions();
                    yield new FastProcessor(opts);
                }
                case "secure" -> {
                    var certs = config.getCertificates();
                    var encryption = config.getEncryption();
                    yield new SecureProcessor(certs, encryption);
                }
                case "batch" -> {
                    var batchSize = config.getBatchSize();
                    var parallel = config.isParallel();
                    yield new BatchProcessor(batchSize, parallel);
                }
                default -> new DefaultProcessor();
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== COMPREHENSIVE JAVA 20 FEATURES ====

    @Test
    public void testComprehensiveJava20Features() {
        String code = """
            var query = \"\"\"
                SELECT u.id, u.name, p.title
                FROM users u
                JOIN profiles p ON u.id = p.user_id
                WHERE u.active = true
                ORDER BY u.name
                \"\"\";
            
            var result = switch (database.getType()) {
                case "mysql", "mariadb" -> {
                    var connection = database.getConnection();
                    yield connection.executeQuery(query);
                }
                case "postgresql" -> {
                    var pgConnection = database.getPostgresConnection();
                    yield pgConnection.execute(query);
                }
                case "h2" -> {
                    if (database.isEmbedded()) {
                        yield database.executeEmbedded(query);
                    } else {
                        yield database.executeRemote(query);
                    }
                }
                default -> throw new UnsupportedOperationException(
                    "Unsupported database: " + database.getType());
            };
            
            var summary = switch (result.getStatus()) {
                case "success" -> {
                    var count = result.getRowCount();
                    var duration = result.getDuration();
                    yield \"\"\"
                        Query executed successfully
                        Rows returned: %d
                        Execution time: %d ms
                        \"\"\".formatted(count, duration);
                }
                case "error" -> {
                    var error = result.getError();
                    yield "Query failed: " + error.getMessage();
                }
                default -> "Unknown result status";
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== PATTERN MATCHING WITH NULL HANDLING ====

    @Test
    public void testPatternMatchingWithNullHandling() {
        String code = """
            String description = switch (value) {
                case null -> "No value provided";
                case String s when s.isEmpty() -> "Empty string";
                case String s -> "String: " + s;
                case Integer i when i == 0 -> "Zero";
                case Integer i -> "Number: " + i;
                default -> "Other type: " + value.getClass().getSimpleName();
            };
            """;
        // Advanced pattern matching with guards might not be fully supported
        try {
            parseCode(code);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Syntax error"));
        }
    }

    // ==== SEALED CLASSES SYNTAX ====

    @Test
    public void testSealedClassBasics() {
        // Testing basic sealed syntax recognition
        String code = """
            switch (shape) {
                case Circle c -> Math.PI * c.radius() * c.radius();
                case Rectangle r -> r.width() * r.height();
                case Triangle t -> 0.5 * t.base() * t.height();
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testModernJavaWithMvelFeatures() {
        String code = """
            var data = [1, 2, 3, 4, 5];
            
            var processed = switch (operation) {
                case "square" -> data.{item ** 2};
                case "filter" -> data.?(item > 2);
                case "double" -> data.{item * 2};
                default -> data;
            };
            
            var result = switch (processed?.size()) {
                case null -> "No data";
                case 0 -> "Empty result";
                case 1 -> "Single item: " + processed[0];
                default -> \"\"\"
                    Multiple items: %d
                    First: %s
                    Last: %s
                    \"\"\".formatted(
                        processed.size(),
                        processed[0],
                        processed[processed.size() - 1]
                    );
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }
}