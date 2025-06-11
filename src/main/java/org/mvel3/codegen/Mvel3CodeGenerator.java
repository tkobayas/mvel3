package org.mvel3.codegen;

import org.mvel3.grammar.*;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * MVEL3 Code Generator - Transforms MVEL expressions into equivalent Java code
 */
public class Mvel3CodeGenerator extends Mvel3ParserBaseVisitor<String> {

    private static final String INDENT = "    ";
    private int indentLevel = 0;
    private int tempVarCounter = 0;

    /**
     * Generate Java code from MVEL parse tree
     */
    public String generateJavaCode(ParseTree tree) {
        return visit(tree);
    }

    // ==== UTILITIES ====

    private String indent() {
        return INDENT.repeat(indentLevel);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String generateTempVar() {
        return "temp" + (++tempVarCounter);
    }

    /**
     * Generate safe navigation chain with temporary variables
     * For user?.address?.street generates:
     * (user != null ? (temp1 = user.getAddress()) != null ? temp1.getStreet() : null : null)
     */
    private String generateSafeNavigationChain(ParseTree ctx) {
        // This is a complex case - for now return a placeholder
        // The proper implementation would need to traverse the entire chain
        return "/* TODO: Complex safe navigation chain */";
    }

    // ==== BASIC EXPRESSIONS ====

    @Override
    public String visitStart_(Mvel3Parser.Start_Context ctx) {
        if (ctx.compilationUnit() != null) {
            return visit(ctx.compilationUnit());
        } else if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
        return "";
    }

    @Override
    public String visitCompilationUnit(Mvel3Parser.CompilationUnitContext ctx) {
        StringBuilder result = new StringBuilder();
        for (Mvel3Parser.StatementContext stmt : ctx.statement()) {
            result.append(indent()).append(visit(stmt));
            if (!visit(stmt).endsWith("\n")) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    // ==== LITERALS ====

    @Override
    public String visitIntegerLiteral(Mvel3Parser.IntegerLiteralContext ctx) {
        return ctx.INTEGER_LITERAL().getText();
    }

    @Override
    public String visitFloatingPointLiteral(Mvel3Parser.FloatingPointLiteralContext ctx) {
        return ctx.FLOATING_POINT_LITERAL().getText();
    }

    @Override
    public String visitStringLiteral(Mvel3Parser.StringLiteralContext ctx) {
        return ctx.STRING_LITERAL().getText();
    }

    @Override
    public String visitTextBlockLiteral(Mvel3Parser.TextBlockLiteralContext ctx) {
        return ctx.TEXT_BLOCK().getText();
    }

    @Override
    public String visitCharacterLiteral(Mvel3Parser.CharacterLiteralContext ctx) {
        return ctx.CHARACTER_LITERAL().getText();
    }

    @Override
    public String visitBooleanLiteral(Mvel3Parser.BooleanLiteralContext ctx) {
        return ctx.getText(); // true or false
    }

    @Override
    public String visitNullLiteral(Mvel3Parser.NullLiteralContext ctx) {
        return "null";
    }

    @Override
    public String visitRegexLiteral(Mvel3Parser.RegexLiteralContext ctx) {
        // Convert MVEL regex ~/pattern/ to Java Pattern.compile("pattern")
        String regex = ctx.REGEX_LITERAL().getText();
        String pattern = regex.substring(2, regex.length() - 1); // Remove ~/ and /
        // Escape backslashes for Java string literals
        pattern = pattern.replace("\\", "\\\\");
        return "Pattern.compile(\"" + pattern + "\")";
    }

    @Override
    public String visitUnitLiteral(Mvel3Parser.UnitLiteralContext ctx) {
        // For now, just return the literal - this may need clarification
        return ctx.UNIT_LITERAL().getText();
    }

    // ==== IDENTIFIERS ====

    @Override
    public String visitIdentifierExpression(Mvel3Parser.IdentifierExpressionContext ctx) {
        return ctx.IDENTIFIER().getText();
    }

    @Override
    public String visitThisExpression(Mvel3Parser.ThisExpressionContext ctx) {
        return "this";
    }

    // ==== ARITHMETIC OPERATIONS ====

    @Override
    public String visitAdditiveExpression(Mvel3Parser.AdditiveExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator = ctx.PLUS() != null ? "+" : "-";
        return "(" + left + " " + operator + " " + right + ")";
    }

    @Override
    public String visitMultiplicativeExpression(Mvel3Parser.MultiplicativeExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator;
        if (ctx.MULTIPLY() != null) operator = "*";
        else if (ctx.DIVIDE() != null) operator = "/";
        else operator = "%";
        return "(" + left + " " + operator + " " + right + ")";
    }

    @Override
    public String visitPowerExpression(Mvel3Parser.PowerExpressionContext ctx) {
        // Convert MVEL ** to Math.pow()
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        return "Math.pow(" + left + ", " + right + ")";
    }

    // ==== COMPARISON OPERATIONS ====

    @Override
    public String visitRelationalExpression(Mvel3Parser.RelationalExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator = ctx.getChild(1).getText(); // <, >, <=, >=
        return "(" + left + " " + operator + " " + right + ")";
    }

    @Override
    public String visitEqualityExpression(Mvel3Parser.EqualityExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator = ctx.EQ() != null ? "==" : "!=";
        return "(" + left + " " + operator + " " + right + ")";
    }

    // ==== LOGICAL OPERATIONS ====

    @Override
    public String visitLogicalAndExpression(Mvel3Parser.LogicalAndExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        return "(" + left + " && " + right + ")";
    }

    @Override
    public String visitLogicalOrExpression(Mvel3Parser.LogicalOrExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        return "(" + left + " || " + right + ")";
    }

    @Override
    public String visitUnaryExpression(Mvel3Parser.UnaryExpressionContext ctx) {
        String expr = visit(ctx.expression());
        String operator = ctx.getChild(0).getText(); // +, -, !, ~
        return "(" + operator + expr + ")";
    }

    // ==== MVEL-SPECIFIC: PROPERTY ACCESS ====

    @Override
    public String visitFieldAccess(Mvel3Parser.FieldAccessContext ctx) {
        String object = visit(ctx.expression());
        String field = ctx.IDENTIFIER().getText();
        
        // Convert property access to getter call
        // user.name → user.getName()
        // getUser().name → getUser().getName()
        String getter = "get" + capitalize(field);
        return object + "." + getter + "()";
    }

    // ==== MVEL-SPECIFIC: ARRAY/LIST ACCESS ====

    @Override
    public String visitArrayAccess(Mvel3Parser.ArrayAccessContext ctx) {
        String object = visit(ctx.expression(0));
        String index = visit(ctx.expression(1));
        
        // Convert array access to get() call
        // list[5] → list.get(5)
        return object + ".get(" + index + ")";
    }

    // ==== MVEL-SPECIFIC: SAFE NAVIGATION ====

    @Override
    public String visitSafeFieldAccess(Mvel3Parser.SafeFieldAccessContext ctx) {
        String object = visit(ctx.expression());
        String field = ctx.IDENTIFIER().getText();
        String getter = "get" + capitalize(field);
        
        // Convert safe navigation to null check
        // obj?.field → (obj != null ? obj.getField() : null)
        // For chained safe navigation, we need to check if object already contains safe navigation
        String objectVar = "temp";
        if (object.contains("!= null")) {
            // This is already a safe navigation chain, need more complex handling
            return "/* TODO: Complex safe navigation */ " + object + " != null ? " + object + "." + getter + "() : null";
        }
        return "(" + object + " != null ? " + object + "." + getter + "() : null)";
    }

    // ==== METHOD CALLS ====

    @Override
    public String visitMethodCall(Mvel3Parser.MethodCallContext ctx) {
        String args = "";
        if (ctx.expressionList() != null) {
            args = visit(ctx.expressionList());
        }
        
        // For method calls, we need to handle the expression differently
        // If the expression is a field access, we should treat it as a method call, not property access
        if (ctx.expression() instanceof Mvel3Parser.FieldAccessContext) {
            Mvel3Parser.FieldAccessContext fieldCtx = (Mvel3Parser.FieldAccessContext) ctx.expression();
            String object = visit(fieldCtx.expression());
            String method = fieldCtx.IDENTIFIER().getText();
            // obj.method(args) → obj.method(args) (no getter conversion)
            return object + "." + method + "(" + args + ")";
        } else {
            // For other expressions, visit normally and add parentheses
            String object = visit(ctx.expression());
            return object + "(" + args + ")";
        }
    }

    @Override
    public String visitExpressionList(Mvel3Parser.ExpressionListContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ctx.expression().size(); i++) {
            if (i > 0) result.append(", ");
            result.append(visit(ctx.expression(i)));
        }
        return result.toString();
    }

    // ==== PARENTHESES ====

    @Override
    public String visitParenthesizedExpression(Mvel3Parser.ParenthesizedExpressionContext ctx) {
        String expr = visit(ctx.expression());
        // Avoid double parentheses for already parenthesized expressions
        if (expr.startsWith("(") && expr.endsWith(")")) {
            return expr;
        }
        return "(" + expr + ")";
    }

    // ==== TERNARY OPERATOR ====

    @Override
    public String visitTernaryExpression(Mvel3Parser.TernaryExpressionContext ctx) {
        String condition = visit(ctx.expression(0));
        String trueExpr = visit(ctx.expression(1));
        String falseExpr = visit(ctx.expression(2));
        return "(" + condition + " ? " + trueExpr + " : " + falseExpr + ")";
    }

    // ==== ASSIGNMENT ====

    @Override
    public String visitAssignmentExpression(Mvel3Parser.AssignmentExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator = visit(ctx.assignmentOperator());
        return left + " " + operator + " " + right;
    }

    @Override
    public String visitAssignmentOperator(Mvel3Parser.AssignmentOperatorContext ctx) {
        if (ctx.POW_ASSIGN() != null) {
            // Special handling for **= - this needs clarification
            return "= Math.pow(" + /* left side needed */ ", " + /* right side needed */ ")";
        }
        return ctx.getText(); // =, +=, -=, *=, /=
    }

    // ==== PLACEHOLDER METHODS (to be implemented) ====

    @Override
    public String visitInlineListExpression(Mvel3Parser.InlineListExpressionContext ctx) {
        // Convert [1, 2, 3] to Arrays.asList(1, 2, 3) or List.of(1, 2, 3)
        if (ctx.inlineList().expressionList() == null) {
            // Empty list
            return "List.of()";
        }
        String elements = visit(ctx.inlineList().expressionList());
        return "List.of(" + elements + ")";
    }

    @Override
    public String visitInlineMapExpression(Mvel3Parser.InlineMapExpressionContext ctx) {
        // Convert {"key": "value", "key2": "value2"} to Map.of("key", "value", "key2", "value2")
        if (ctx.inlineMap().mapEntryList() == null) {
            // Empty map
            return "Map.of()";
        }
        
        StringBuilder result = new StringBuilder("Map.of(");
        Mvel3Parser.MapEntryListContext mapEntryList = ctx.inlineMap().mapEntryList();
        
        for (int i = 0; i < mapEntryList.mapEntry().size(); i++) {
            if (i > 0) result.append(", ");
            
            Mvel3Parser.MapEntryContext entry = mapEntryList.mapEntry(i);
            String key, value;
            
            if (entry.IDENTIFIER() != null) {
                // identifier: expression format
                key = "\"" + entry.IDENTIFIER().getText() + "\"";
                value = visit(entry.expression(0));
            } else {
                // expression: expression format
                key = visit(entry.expression(0));
                value = visit(entry.expression(1));
            }
            
            result.append(key).append(", ").append(value);
        }
        
        result.append(")");
        return result.toString();
    }

    @Override
    public String visitProjectionExpression(Mvel3Parser.ProjectionExpressionContext ctx) {
        // Convert list.{item.name} to list.stream().map(item -> item.getName()).collect(Collectors.toList())
        String collection = visit(ctx.expression(0));
        String projection = visit(ctx.expression(1));
        
        // For now, use a simple lambda approach
        // This assumes the projection expression uses 'item' as the variable name
        return collection + ".stream().map(item -> " + projection + ").collect(Collectors.toList())";
    }

    @Override
    public String visitSelectionExpression(Mvel3Parser.SelectionExpressionContext ctx) {
        // Convert list.?(item > 5) to list.stream().filter(item -> item > 5).collect(Collectors.toList())
        String collection = visit(ctx.expression(0));
        String condition = visit(ctx.expression(1));
        
        // For now, use a simple lambda approach
        // This assumes the selection expression uses 'item' as the variable name
        return collection + ".stream().filter(item -> " + condition + ").collect(Collectors.toList())";
    }

    @Override
    public String visitWithBlockExpression(Mvel3Parser.WithBlockExpressionContext ctx) {
        // Convert obj{field1 = value1, field2 = value2} to obj setter chain
        String object = visit(ctx.expression());
        
        if (ctx.withStatementList() == null) {
            // Empty with block - just return the object
            return object;
        }
        
        StringBuilder result = new StringBuilder();
        result.append("(").append(object);
        
        // Process each with statement
        for (Mvel3Parser.WithStatementContext stmt : ctx.withStatementList().withStatement()) {
            if (stmt.IDENTIFIER() != null) {
                // field = value assignment
                String field = stmt.IDENTIFIER().getText();
                String value = visit(stmt.expression());
                String setter = "set" + capitalize(field);
                result.append(".").append(setter).append("(").append(value).append(")");
            } else {
                // Expression statement - method call
                String expr = visit(stmt.expression());
                if (!expr.startsWith(object)) {
                    result.append(".").append(expr);
                } else {
                    // Expression already includes object, replace with continuation
                    result.append(".").append(expr.substring(object.length() + 1));
                }
            }
        }
        
        result.append(")");
        return result.toString();
    }

    @Override
    public String visitBooleanTestBlockExpression(Mvel3Parser.BooleanTestBlockExpressionContext ctx) {
        // Convert obj[test1, test2, test3] to (test1 && test2 && test3)
        String object = visit(ctx.expression());
        
        if (ctx.booleanTestList() == null) {
            // Empty boolean test block - just return true
            return "true";
        }
        
        StringBuilder result = new StringBuilder("(");
        
        for (int i = 0; i < ctx.booleanTestList().booleanTest().size(); i++) {
            if (i > 0) result.append(" && ");
            
            String test = visit(ctx.booleanTestList().booleanTest(i).expression());
            
            // If test doesn't reference the object, assume it's a property/method of the object
            if (!test.contains(object) && !test.startsWith("(") && !test.contains("\"")) {
                // Simple property access - convert to getter
                if (Character.isLowerCase(test.charAt(0)) && !test.contains("(")) {
                    test = object + ".get" + capitalize(test) + "()";
                } else {
                    test = object + "." + test;
                }
            }
            
            result.append(test);
        }
        
        result.append(")");
        return result.toString();
    }

    @Override
    public String visitCoercionExpression(Mvel3Parser.CoercionExpressionContext ctx) {
        // Convert obj#Type to ((Type) obj)
        String object = visit(ctx.expression());
        String type;
        
        if (ctx.IDENTIFIER() != null) {
            type = ctx.IDENTIFIER().getText();
        } else {
            // String literal type
            type = ctx.STRING_LITERAL().getText();
            // Remove quotes from string literal
            type = type.substring(1, type.length() - 1);
        }
        
        return "((" + type + ") " + object + ")";
    }

    // ==== MVEL-SPECIFIC MISSING METHODS ====
    
    @Override
    public String visitStringSimilarityExpression(Mvel3Parser.StringSimilarityExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        
        if (ctx.STRSIM() != null) {
            return "StringUtils.strsim(" + left + ", " + right + ")";
        } else { // SOUNDSLIKE
            return "StringUtils.soundslike(" + left + ", " + right + ")";
        }
    }
    
    @Override
    public String visitCollectionExpression(Mvel3Parser.CollectionExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        
        if (ctx.CONTAINS() != null) {
            return right + ".contains(" + left + ")";
        } else { // IN
            return right + ".contains(" + left + ")";
        }
    }
    
    @Override
    public String visitIsDefExpression(Mvel3Parser.IsDefExpressionContext ctx) {
        String expr = visit(ctx.expression());
        // Convert isdef(variable) to variable != null
        return "(" + expr + " != null)";
    }
    
    @Override
    public String visitEmptyExpression(Mvel3Parser.EmptyExpressionContext ctx) {
        return "Collections.emptyList()";
    }
    
    @Override
    public String visitNilExpression(Mvel3Parser.NilExpressionContext ctx) {
        return "null";
    }
    
    @Override
    public String visitUndefinedExpression(Mvel3Parser.UndefinedExpressionContext ctx) {
        return "null";
    }

    // ==== DEFAULT FALLBACK ====

    @Override
    protected String defaultResult() {
        return "";
    }

    @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        if (aggregate == null || aggregate.isEmpty()) return nextResult;
        if (nextResult == null || nextResult.isEmpty()) return aggregate;
        return aggregate + nextResult;
    }
}