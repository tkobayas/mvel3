package org.mvel3.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class SwitchExpressionImprovementTest {

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
    public void testArrowSyntaxWithMethodCall() {
        String code = """
            result = switch (action) {
                case "log" -> logger.info("message");
                case "warn" -> logger.warn("warning");
                default -> logger.error("unknown action");
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testArrowSyntaxWithThrowStatement() {
        String code = """
            status = switch (code) {
                case 200 -> "OK";
                case 404 -> "Not Found"; 
                case 500 -> "Internal Error";
                default -> throw new Exception("Unknown status code");
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testMixedColonAndArrowSyntax() {
        String code = """
            value = switch (type) {
                case "A": yield processA();
                case "B" -> processB();
                case "C" -> {
                    var result = processC();
                    yield result;
                }
                default -> throw new IllegalArgumentException("Invalid type");
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testArrowSyntaxWithComplexExpressions() {
        String code = """
            result = switch (operation) {
                case "ADD" -> a + b;
                case "SUBTRACT" -> a - b;
                case "MULTIPLY" -> a * b;
                case "DIVIDE" -> {
                    if (b != 0) {
                        yield a / b;
                    } else {
                        throw new ArithmeticException("Division by zero");
                    }
                }
                default -> throw new UnsupportedOperationException("Unknown operation");
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testThrowInSwitchStatement() {
        String code = """
            switch (status) {
                case "ACTIVE" -> processActive();
                case "INACTIVE" -> processInactive();
                case "SUSPENDED" -> processSuspended();
                default -> throw new IllegalStateException("Invalid status: " + status);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }
}