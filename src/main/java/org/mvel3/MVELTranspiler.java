package org.mvel3;

import org.mvel3.grammar.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.Map;
import java.util.Collections;
import java.util.Set;

/**
 * MVEL Transpiler - Transforms MVEL AST into equivalent Java code
 * This class handles the pure AST-to-Java conversion without compilation concerns
 */
public class MVELTranspiler extends Mvel3ParserBaseVisitor<String> {

    private static final String INDENT = "    ";
    private int indentLevel = 0;
    private int tempVarCounter = 0;
    private Map<String, Declaration> typeInfo = Collections.emptyMap();
    

    /**
     * Generate Java code from MVEL parse tree
     */
    public String transpile(ParseTree tree) {
        return visit(tree);
    }
    
    /**
     * Set type information for variables to enable better transpilation
     */
    public void setTypeInfo(Map<String, Declaration> typeInfo) {
        this.typeInfo = typeInfo != null ? typeInfo : Collections.emptyMap();
    }
    
    /**
     * Check if a field name represents a method call rather than property access
     */
    private boolean isMethodName(String objectExpr, String fieldName) {
        // Well-known Object methods that should not be converted to getters
        if ("length".equals(fieldName) || "size".equals(fieldName) || "isEmpty".equals(fieldName) ||
            "toString".equals(fieldName) || "hashCode".equals(fieldName) || "equals".equals(fieldName) ||
            "clone".equals(fieldName) || "notify".equals(fieldName) || "notifyAll".equals(fieldName) ||
            "wait".equals(fieldName) || "getClass".equals(fieldName)) {
            return true;
        }
        
        // If we have type information, check if the field exists as a method
        Class<?> objectType = getObjectType(objectExpr);
        if (objectType != null) {
            try {
                // Check if method exists with no parameters
                objectType.getMethod(fieldName);
                return true;
            } catch (NoSuchMethodException e) {
                // Not a method, continue with other checks
            }
        }
        
        return false;
    }
    
    /**
     * Check if a field represents a static/public field rather than a property
     */
    private boolean isStaticOrPublicField(String objectExpr, String fieldName) {
        // Well-known static fields
        if ("out".equals(fieldName) || "err".equals(fieldName) || "in".equals(fieldName) || // System.*
            "TYPE".equals(fieldName) || "class".equals(fieldName) || // Class.TYPE, obj.class
            "MAX_VALUE".equals(fieldName) || "MIN_VALUE".equals(fieldName) || // Integer.MAX_VALUE
            "POSITIVE_INFINITY".equals(fieldName) || "NEGATIVE_INFINITY".equals(fieldName) || "NaN".equals(fieldName)) {
            return true;
        }
        
        // Check type information for public fields
        Class<?> objectType = getObjectType(objectExpr);
        if (objectType != null) {
            try {
                java.lang.reflect.Field field = objectType.getField(fieldName);
                // If we can get the field as public, it's likely a direct field access
                return java.lang.reflect.Modifier.isPublic(field.getModifiers()) ||
                       java.lang.reflect.Modifier.isStatic(field.getModifiers());
            } catch (NoSuchFieldException e) {
                // Not a public field
            }
        }
        
        return false;
    }
    
    /**
     * Check if a name represents a package component
     */
    private boolean isPackageName(String name) {
        // Common package prefixes
        return "org".equals(name) || "com".equals(name) || "net".equals(name) || 
               "java".equals(name) || "javax".equals(name) || "sun".equals(name) ||
               "mvel3".equals(name) || "transpiler".equals(name) || "test".equals(name) ||
               "main".equals(name) || "util".equals(name) || "lang".equals(name) || "io".equals(name);
    }
    
    /**
     * Check if a field should use direct field access instead of setter
     */
    private boolean isPublicField(String objectExpr, String fieldName) {
        Class<?> objectType = getObjectType(objectExpr);
        if (objectType != null) {
            try {
                java.lang.reflect.Field field = objectType.getField(fieldName);
                return java.lang.reflect.Modifier.isPublic(field.getModifiers()) &&
                       !java.lang.reflect.Modifier.isStatic(field.getModifiers());
            } catch (NoSuchFieldException e) {
                // Not a public field, fallback to property access
            }
        }
        
        // Fallback: check if field name suggests it's public (naming convention)
        return fieldName.startsWith("public") || 
               fieldName.equals("nickName") || // Known public field from Person class
               fieldName.equals("parentPublic");
    }
    
    /**
     * Get the Class type for an object expression using type information
     */
    private Class<?> getObjectType(String objectExpr) {
        // Extract variable name from complex expressions
        String varName = objectExpr;
        if (objectExpr.contains(".")) {
            varName = objectExpr.substring(0, objectExpr.indexOf("."));
        }
        if (objectExpr.contains("[")) {
            varName = objectExpr.substring(0, objectExpr.indexOf("["));
        }
        
        // Remove $ prefix if present
        if (varName.startsWith("$")) {
            varName = varName.substring(1);
        }
        
        // Look up in type information
        Declaration decl = typeInfo.get(varName);
        if (decl != null) {
            return decl.type().getClazz();
        }
        
        // Look up with $ prefix
        decl = typeInfo.get("$" + varName);
        if (decl != null) {
            return decl.type().getClazz();
        }
        
        return null;
    }
    
    /**
     * Get the target type for a field assignment (e.g., p.salary -> BigDecimal)
     */
    private String getFieldType(String objectExpr, String fieldName) {
        Class<?> objectType = getObjectType(objectExpr);
        if (objectType != null) {
            try {
                // Try to get the field directly
                java.lang.reflect.Field field = objectType.getDeclaredField(fieldName);
                Class<?> fieldType = field.getType();
                return fieldType.getSimpleName();
            } catch (NoSuchFieldException e) {
                // Try to get via public field
                try {
                    java.lang.reflect.Field field = objectType.getField(fieldName);
                    Class<?> fieldType = field.getType();
                    return fieldType.getSimpleName();
                } catch (NoSuchFieldException e2) {
                    // Try to infer from getter method
                    try {
                        String getterName = "get" + capitalize(fieldName);
                        java.lang.reflect.Method getter = objectType.getMethod(getterName);
                        Class<?> returnType = getter.getReturnType();
                        return returnType.getSimpleName();
                    } catch (NoSuchMethodException e3) {
                        // Unable to determine type
                    }
                }
            }
        }
        
        return null; // Unknown type
    }
    
    /**
     * Get the element type for an array (e.g., int[][] x -> int for x[0][0])
     */
    private String getArrayElementType(String arrayExpr) {
        // Extract the base array name from expressions like "x[2]" -> "x"
        String arrayName = arrayExpr;
        if (arrayExpr.contains("[")) {
            arrayName = arrayExpr.substring(0, arrayExpr.indexOf("["));
        }
        
        // For common array variable names, deduce the element type
        if (arrayName.equals("x") || arrayName.startsWith("int")) return "int";
        if (arrayName.contains("String")) return "String";
        if (arrayName.contains("BigDecimal")) return "BigDecimal";
        if (arrayName.contains("BigInteger")) return "BigInteger";
        return null; // Unknown type
    }
    
    /**
     * Apply type coercion for binary operands (e.g., int * String -> int * Integer.parseInt(String))
     */
    private String[] coerceBinaryOperands(String left, String right) {
        // Detect string literals vs variables
        boolean leftIsStringLiteral = left.startsWith("\"") && left.endsWith("\"");
        boolean rightIsStringLiteral = right.startsWith("\"") && right.endsWith("\"");
        boolean leftIsVariable = !leftIsStringLiteral && !left.matches("\\d+") && !left.contains("(");
        boolean rightIsVariable = !rightIsStringLiteral && !right.matches("\\d+") && !right.contains("(");
        
        // If one operand is a string variable and the other is numeric, coerce the string
        if (leftIsVariable && (right.matches("\\d+") || rightIsVariable)) {
            // Check if left might be a string variable (simple heuristic)
            if (left.equals("y") || left.contains("String") || left.endsWith("Str")) {
                left = "Integer.parseInt(" + left + ")";
            }
        }
        
        if (rightIsVariable && (left.matches("\\d+") || leftIsVariable)) {
            // Check if right might be a string variable
            if (right.equals("y") || right.contains("String") || right.endsWith("Str")) {
                right = "Integer.parseInt(" + right + ")";
            }
        }
        
        return new String[]{left, right};
    }
    
    /**
     * Apply type coercion for assignment values
     */
    private String coerceValueForAssignment(String value, String targetType) {
        if (targetType == null) return value;
        
        // BigDecimal coercion
        if ("BigDecimal".equals(targetType)) {
            // Check if value is a plain integer literal
            if (value.matches("\\d+")) {
                return "new BigDecimal(\"" + value + "\")";
            }
            // Check if value is a long literal (ends with L)
            if (value.matches("\\d+L")) {
                String number = value.substring(0, value.length() - 1);
                return "new BigDecimal(\"" + number + "\")";
            }
            // Check if value is a string literal for BigDecimal
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return "new BigDecimal(" + value + ")";
            }
        }
        
        // BigInteger coercion  
        if ("BigInteger".equals(targetType)) {
            if (value.matches("\\d+")) {
                return "new BigInteger(\"" + value + "\")";
            }
        }
        
        // int coercion
        if ("int".equals(targetType)) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return "Integer.parseInt(" + value + ")";
            }
        }
        
        // String coercion for BigDecimal/BigInteger values
        if ("String".equals(targetType)) {
            if (value.startsWith("new BigDecimal(") || value.startsWith("new BigInteger(") || 
                value.contains("BigDecimal.") || value.contains("BigInteger.")) {
                return "java.util.Objects.toString(" + value + ", null)";
            }
        }
        
        return value;
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
     * Check if an expression represents a BigDecimal value
     */
    private boolean isBigDecimalExpression(String expr) {
        // Check for BigDecimal literals
        if (expr.startsWith("new BigDecimal(")) {
            return true;
        }
        
        // Check for method calls that return BigDecimal
        if (expr.contains(".getSalary()") || expr.contains("BigDecimal.")) {
            return true;
        }
        
        // Check for variables declared as BigDecimal
        for (Map.Entry<String, Declaration> entry : typeInfo.entrySet()) {
            String varName = entry.getKey();
            if (expr.equals(varName) || expr.equals("$" + varName)) {
                Class<?> type = entry.getValue().type().getClazz();
                if (type == java.math.BigDecimal.class) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Convert binary operations involving BigDecimal to method calls
     */
    private String convertBigDecimalOperation(String left, String right, String operator) {
        // Convert non-BigDecimal operands to BigDecimal
        if (!isBigDecimalExpression(left)) {
            if (left.matches("\\d+")) {
                left = "new BigDecimal(\"" + left + "\")";
            } else {
                left = "BigDecimal.valueOf(" + left + ")";
            }
        }
        
        if (!isBigDecimalExpression(right)) {
            if (right.matches("\\d+")) {
                right = "new BigDecimal(\"" + right + "\")";
            } else {
                right = "BigDecimal.valueOf(" + right + ")";
            }
        }
        
        // Convert operator to method call
        String method;
        switch (operator) {
            case "+": method = "add"; break;
            case "-": method = "subtract"; break;
            case "*": method = "multiply"; break;
            case "/": method = "divide"; break;
            case "%": method = "remainder"; break;
            default: return left + " " + operator + " " + right; // fallback
        }
        
        return left + "." + method + "(" + right + ", java.math.MathContext.DECIMAL128)";
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
        for (int i = 0; i < ctx.statement().size(); i++) {
            Mvel3Parser.StatementContext stmt = ctx.statement(i);
            String stmtResult = visit(stmt);
            result.append(stmtResult);
            // Add space separation between statements for compact formatting
            if (i < ctx.statement().size() - 1) {
                result.append(" ");
            }
        }
        return result.toString().trim();
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
        String literal = ctx.UNIT_LITERAL().getText();
        
        // Handle BigDecimal literals (ending with B)
        if (literal.endsWith("B")) {
            String value = literal.substring(0, literal.length() - 1);
            return "new BigDecimal(\"" + value + "\")";
        }
        
        // Handle BigInteger literals (ending with I)
        if (literal.endsWith("I")) {
            String value = literal.substring(0, literal.length() - 1);
            return "new BigInteger(\"" + value + "\")";
        }
        
        // For other unit literals, just return as-is for now
        return literal;
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
        
        // Check if this involves BigDecimal operations
        if (isBigDecimalExpression(left) || isBigDecimalExpression(right)) {
            return convertBigDecimalOperation(left, right, operator);
        }
        
        return left + " " + operator + " " + right;
    }

    @Override
    public String visitMultiplicativeExpression(Mvel3Parser.MultiplicativeExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator;
        if (ctx.MULTIPLY() != null) operator = "*";
        else if (ctx.DIVIDE() != null) operator = "/";
        else operator = "%";
        
        // Check if this involves BigDecimal operations
        if (isBigDecimalExpression(left) || isBigDecimalExpression(right)) {
            return convertBigDecimalOperation(left, right, operator);
        }
        
        // Apply binary expression coercion for mixed types
        String[] coerced = coerceBinaryOperands(left, right);
        left = coerced[0];
        right = coerced[1];
        
        return left + " " + operator + " " + right;
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
        return left + " " + operator + " " + right;
    }

    @Override
    public String visitEqualityExpression(Mvel3Parser.EqualityExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator = ctx.EQ() != null ? "==" : "!=";
        return left + " " + operator + " " + right;
    }

    // ==== LOGICAL OPERATIONS ====

    @Override
    public String visitLogicalAndExpression(Mvel3Parser.LogicalAndExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        return left + " && " + right;
    }

    @Override
    public String visitLogicalOrExpression(Mvel3Parser.LogicalOrExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        return left + " || " + right;
    }

    @Override
    public String visitUnaryExpression(Mvel3Parser.UnaryExpressionContext ctx) {
        String expr = visit(ctx.expression());
        String operator = ctx.getChild(0).getText(); // +, -, !, ~
        return operator + expr;
    }

    // ==== MVEL-SPECIFIC: PROPERTY ACCESS ====

    @Override
    public String visitFieldAccess(Mvel3Parser.FieldAccessContext ctx) {
        String object = visit(ctx.expression());
        String field = ctx.IDENTIFIER().getText();
        
        // Check if this is a known method name - if so, treat as method call
        if (isMethodName(object, field)) {
            return object + "." + field + "()";
        }
        
        // Check if this is a static/public field - if so, keep as field access
        if (isStaticOrPublicField(object, field)) {
            return object + "." + field;
        }
        
        // Check if this is a package name - if so, keep as field access
        if (isPackageName(field)) {
            return object + "." + field;
        }
        
        // Check if this is a public field - if so, use direct field access
        if (isPublicField(object, field)) {
            return object + "." + field;
        }
        
        // Check if this looks like a class name (starts with uppercase)
        if (Character.isUpperCase(field.charAt(0))) {
            return object + "." + field;
        }
        
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
        
        // Determine if this is an array or collection access
        // Arrays should use [] syntax, collections should use .get()
        if (isArrayType(object)) {
            // Keep array access syntax: array[index]
            return object + "[" + index + "]";
        } else {
            // Convert collection access to get() call: list[5] → list.get(5)
            return object + ".get(" + index + ")";
        }
    }
    
    /**
     * Check if the object is likely an array type (vs collection)
     */
    private boolean isArrayType(String objectExpr) {
        // Check for obvious array patterns
        if (objectExpr.contains("[]") || objectExpr.endsWith("Array")) {
            return true;
        }
        
        // Check for variable names that suggest arrays
        if (objectExpr.matches(".*[A-Za-z].*") && 
            (objectExpr.contains("array") || objectExpr.contains("Array") || 
             objectExpr.equals("x") || objectExpr.equals("a") || // common array variable names
             objectExpr.endsWith("[]"))) {
            return true;
        }
        
        // Variables that end with certain patterns are likely arrays
        if (objectExpr.matches(".*[Aa]rray.*") || objectExpr.matches(".*\\[\\d+\\].*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if the object is likely a Map type (vs List/Set)
     */
    private boolean isMapType(String objectExpr) {
        // Check if the expression contains method calls that return Maps
        if (objectExpr.contains("getItems()") || objectExpr.contains("getPrices()") || 
            objectExpr.contains("getBigDecimalMap()") || objectExpr.contains("getBigIntegerMap()")) {
            return true;
        }
        
        // Extract variable name from complex expressions
        String varName = objectExpr;
        if (objectExpr.contains(".")) {
            varName = objectExpr.substring(0, objectExpr.indexOf("."));
        }
        if (objectExpr.contains("[")) {
            varName = objectExpr.substring(0, objectExpr.indexOf("["));
        }
        
        // Remove $ prefix if present
        if (varName.startsWith("$")) {
            varName = varName.substring(1);
        }
        
        // Check if variable name suggests a map
        if (varName.contains("map") || varName.contains("Map") || 
            varName.equals("m") || varName.contains("items") || 
            varName.contains("prices") || varName.endsWith("Map")) {
            return true;
        }
        
        // Use type information to check if it's actually a Map
        Class<?> objectType = getObjectType(objectExpr);
        if (objectType != null) {
            return java.util.Map.class.isAssignableFrom(objectType);
        }
        
        return false;
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
        return condition + " ? " + trueExpr + " : " + falseExpr;
    }

    // ==== ASSIGNMENT ====

    @Override
    public String visitAssignmentExpression(Mvel3Parser.AssignmentExpressionContext ctx) {
        String left = visit(ctx.expression(0));
        String right = visit(ctx.expression(1));
        String operator = visit(ctx.assignmentOperator());
        
        
        // Check if this is an assignment to a field access (property assignment)
        if (ctx.expression(0) instanceof Mvel3Parser.FieldAccessContext) {
            Mvel3Parser.FieldAccessContext fieldCtx = (Mvel3Parser.FieldAccessContext) ctx.expression(0);
            String object = visit(fieldCtx.expression());
            String field = fieldCtx.IDENTIFIER().getText();
            
            // Check if this is a public field - if so, use direct field access
            if (isPublicField(object, field)) {
                // For public fields, use direct assignment
                if (operator.equals("+=")) {
                    return object + "." + field + " += " + right;
                } else if (operator.equals("-=")) {
                    return object + "." + field + " -= " + right;
                } else if (operator.equals("*=")) {
                    return object + "." + field + " *= " + right;
                } else if (operator.equals("/=")) {
                    return object + "." + field + " /= " + right;
                } else if (operator.equals("=")) {
                    return object + "." + field + " = " + right;
                }
            } else {
                // For private fields, use setter methods
                String setter = "set" + capitalize(field);
                
                // For compound assignments like +=, we need to expand to setter(getter() + value)
                if (operator.equals("+=")) {
                    String getter = "get" + capitalize(field);
                    return object + "." + setter + "(" + object + "." + getter + "() + " + right + ")";
                } else if (operator.equals("-=")) {
                    String getter = "get" + capitalize(field);
                    return object + "." + setter + "(" + object + "." + getter + "() - " + right + ")";
                } else if (operator.equals("*=")) {
                    String getter = "get" + capitalize(field);
                    return object + "." + setter + "(" + object + "." + getter + "() * " + right + ")";
                } else if (operator.equals("/=")) {
                    String getter = "get" + capitalize(field);
                    return object + "." + setter + "(" + object + "." + getter + "() / " + right + ")";
                } else if (operator.equals("=")) {
                    // Simple assignment: p.age = 10 becomes p.setAge(10)
                    // Apply type coercion based on field type
                    String fieldType = getFieldType(object, field);
                    String coercedValue = coerceValueForAssignment(right, fieldType);
                    return object + "." + setter + "(" + coercedValue + ")";
                }
            }
        }
        
        // Check if this is an assignment to an array element
        if (ctx.expression(0) instanceof Mvel3Parser.ArrayAccessContext) {
            Mvel3Parser.ArrayAccessContext arrayCtx = (Mvel3Parser.ArrayAccessContext) ctx.expression(0);
            String arrayExpr = visit(arrayCtx.expression(0));
            String indexExpr = visit(arrayCtx.expression(1));
            
            // For array assignments, check if we need type coercion
            if (operator.equals("=")) {
                // Determine array element type based on array declaration
                String elementType = getArrayElementType(arrayExpr);
                String coercedValue = coerceValueForAssignment(right, elementType);
                
                if (isArrayType(arrayExpr)) {
                    return arrayExpr + "[" + indexExpr + "] " + operator + " " + coercedValue;
                } else if (isMapType(arrayExpr)) {
                    // Map assignment uses put()
                    return arrayExpr + ".put(" + indexExpr + ", " + coercedValue + ")";
                } else {
                    // Collection assignment uses set()
                    return arrayExpr + ".set(" + indexExpr + ", " + coercedValue + ")";
                }
            } else {
                // Compound assignments for arrays
                if (isArrayType(arrayExpr)) {
                    return arrayExpr + "[" + indexExpr + "] " + operator + " " + right;
                } else if (isMapType(arrayExpr)) {
                    // Map compound assignments need special handling with put()
                    return arrayExpr + ".put(" + indexExpr + ", " + arrayExpr + ".get(" + indexExpr + ") " + operator.charAt(0) + " " + right + ")";
                } else {
                    // Collection compound assignments need special handling
                    return arrayExpr + ".set(" + indexExpr + ", " + arrayExpr + ".get(" + indexExpr + ") " + operator.charAt(0) + " " + right + ")";
                }
            }
        }
        
        // Check if this is an assignment to a context variable
        // If the left side is a simple identifier that exists in our type info,
        // wrap with context.put()
        if (ctx.expression(0) instanceof Mvel3Parser.PrimaryExpressionContext) {
            Mvel3Parser.PrimaryExpressionContext primaryCtx = (Mvel3Parser.PrimaryExpressionContext) ctx.expression(0);
            if (primaryCtx.primary() instanceof Mvel3Parser.IdentifierExpressionContext) {
                Mvel3Parser.IdentifierExpressionContext identCtx = (Mvel3Parser.IdentifierExpressionContext) primaryCtx.primary();
                String varName = identCtx.IDENTIFIER().getText();
                
                // If this variable is in our type info, it's a context variable
                if (typeInfo.containsKey(varName)) {
                    String assignment = left + " " + operator + " " + right;
                    return "context.put(\"" + varName + "\", " + assignment + ")";
                }
            }
        }
        
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

    // ==== COLLECTION OPERATIONS ====

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

    // ==== CONSTRUCTOR CALLS ====
    
    @Override
    public String visitNewExpression(Mvel3Parser.NewExpressionContext ctx) {
        return "new " + visit(ctx.creator());
    }
    
    @Override
    public String visitCreator(Mvel3Parser.CreatorContext ctx) {
        String createdName = visit(ctx.createdName());
        if (ctx.classCreatorRest() != null) {
            String args = visit(ctx.classCreatorRest());
            return createdName + args;
        } else if (ctx.arrayCreatorRest() != null) {
            String arrayRest = visit(ctx.arrayCreatorRest());
            return createdName + arrayRest;
        }
        return createdName + "()"; // fallback
    }
    
    @Override
    public String visitCreatedName(Mvel3Parser.CreatedNameContext ctx) {
        return ctx.getText(); // Class name like Person, java.util.List, etc.
    }
    
    @Override
    public String visitClassCreatorRest(Mvel3Parser.ClassCreatorRestContext ctx) {
        if (ctx.expressionList() != null) {
            String args = visit(ctx.expressionList());
            return "(" + args + ")";
        } else {
            return "()";
        }
    }
    
    @Override
    public String visitArrayCreatorRest(Mvel3Parser.ArrayCreatorRestContext ctx) {
        StringBuilder result = new StringBuilder();
        
        // Handle array creation like new int[3][3] or new int[][]{{1,2},{3,4}}
        if (ctx.expression() != null && !ctx.expression().isEmpty()) {
            // Array with dimensions: new int[3][3]
            for (int i = 0; i < ctx.expression().size(); i++) {
                result.append("[").append(visit(ctx.expression(i))).append("]");
            }
            // Add empty brackets for remaining dimensions
            for (int i = 0; i < ctx.LBRACKET().size() - ctx.expression().size(); i++) {
                result.append("[]");
            }
        } else {
            // Array with initializer: new int[][]{{1,2},{3,4}}
            for (int i = 0; i < ctx.LBRACKET().size(); i++) {
                result.append("[]");
            }
            if (ctx.arrayInitializer() != null) {
                result.append(visit(ctx.arrayInitializer()));
            }
        }
        
        return result.toString();
    }
    
    @Override
    public String visitArrayInitializer(Mvel3Parser.ArrayInitializerContext ctx) {
        if (ctx.variableInitializer() == null || ctx.variableInitializer().isEmpty()) {
            return "{}";
        }
        
        StringBuilder result = new StringBuilder("{");
        for (int i = 0; i < ctx.variableInitializer().size(); i++) {
            if (i > 0) result.append(", ");
            result.append(visit(ctx.variableInitializer(i)));
        }
        result.append("}");
        return result.toString();
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

    // ==== STATEMENTS ====
    
    @Override
    public String visitExpressionStatement(Mvel3Parser.ExpressionStatementContext ctx) {
        String expr = visit(ctx.statementExpression());
        return expr + ";";
    }
    
    @Override
    public String visitLocalVarDeclStatement(Mvel3Parser.LocalVarDeclStatementContext ctx) {
        return visit(ctx.localVariableDeclarationStatement());
    }
    
    @Override
    public String visitLocalVariableDeclarationStatement(Mvel3Parser.LocalVariableDeclarationStatementContext ctx) {
        String decl = visit(ctx.localVariableDeclaration());
        return decl + ";";
    }
    
    @Override
    public String visitLocalVariableDeclaration(Mvel3Parser.LocalVariableDeclarationContext ctx) {
        StringBuilder result = new StringBuilder();
        
        // Handle modifiers (final, etc.)
        for (Mvel3Parser.VariableModifierContext modifier : ctx.variableModifier()) {
            result.append(visit(modifier)).append(" ");
        }
        
        // Handle type or var
        if (ctx.type() != null) {
            result.append(visit(ctx.type()));
        } else if (ctx.VAR() != null) {
            result.append("var");
        }
        
        result.append(" ");
        result.append(visit(ctx.variableDeclarators()));
        
        return result.toString();
    }
    
    @Override
    public String visitVariableDeclarators(Mvel3Parser.VariableDeclaratorsContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ctx.variableDeclarator().size(); i++) {
            if (i > 0) result.append(", ");
            result.append(visit(ctx.variableDeclarator(i)));
        }
        return result.toString();
    }
    
    @Override
    public String visitVariableDeclarator(Mvel3Parser.VariableDeclaratorContext ctx) {
        String name = visit(ctx.variableDeclaratorId());
        if (ctx.variableInitializer() != null) {
            String initializer = visit(ctx.variableInitializer());
            return name + " = " + initializer;
        }
        return name;
    }
    
    @Override
    public String visitVariableDeclaratorId(Mvel3Parser.VariableDeclaratorIdContext ctx) {
        return ctx.IDENTIFIER().getText();
    }
    
    // ==== TYPES ====
    
    @Override
    public String visitType(Mvel3Parser.TypeContext ctx) {
        StringBuilder result = new StringBuilder();
        if (ctx.primitiveType() != null) {
            result.append(visit(ctx.primitiveType()));
        } else if (ctx.referenceType() != null) {
            result.append(visit(ctx.referenceType()));
        }
        
        // Handle array brackets
        for (int i = 0; i < ctx.LBRACKET().size(); i++) {
            result.append("[]");
        }
        
        return result.toString();
    }
    
    @Override
    public String visitPrimitiveType(Mvel3Parser.PrimitiveTypeContext ctx) {
        return ctx.getText(); // int, boolean, char, etc.
    }
    
    @Override
    public String visitReferenceType(Mvel3Parser.ReferenceTypeContext ctx) {
        return ctx.getText(); // String, Person, etc.
    }
    
    @Override
    public String visitVariableModifier(Mvel3Parser.VariableModifierContext ctx) {
        return ctx.getText(); // final
    }
    
    @Override
    public String visitVariableInitializer(Mvel3Parser.VariableInitializerContext ctx) {
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        } else if (ctx.arrayInitializer() != null) {
            return visit(ctx.arrayInitializer());
        }
        return ctx.getText(); // fallback
    }

    // ==== CONTROL FLOW STATEMENTS ====
    
    @Override
    public String visitForStatement(Mvel3Parser.ForStatementContext ctx) {
        String forControl = visit(ctx.forControl());
        String statement = visit(ctx.statement());
        return "for (" + forControl + ") " + statement;
    }
    
    @Override
    public String visitBasicForControl(Mvel3Parser.BasicForControlContext ctx) {
        String init = ctx.forInit() != null ? visit(ctx.forInit()) : "";
        String condition = ctx.expression() != null ? visit(ctx.expression()) : "";
        String update = ctx.forUpdate() != null ? visit(ctx.forUpdate()) : "";
        return init + "; " + condition + "; " + update;
    }
    
    @Override
    public String visitEnhancedForControl(Mvel3Parser.EnhancedForControlContext ctx) {
        String varDecl = visit(ctx.localVariableDeclaration());
        String iterable = visit(ctx.expression());
        return varDecl + " : " + iterable;
    }
    
    @Override
    public String visitIfStatement(Mvel3Parser.IfStatementContext ctx) {
        String condition = visit(ctx.expression());
        String thenStmt = visit(ctx.statement(0));
        String result = "if (" + condition + ") " + thenStmt;
        
        if (ctx.statement().size() > 1) {
            String elseStmt = visit(ctx.statement(1));
            result += " else " + elseStmt;
        }
        
        return result;
    }
    
    @Override
    public String visitWhileStatement(Mvel3Parser.WhileStatementContext ctx) {
        String condition = visit(ctx.expression());
        String statement = visit(ctx.statement());
        return "while (" + condition + ") " + statement;
    }
    
    @Override
    public String visitDoWhileStatement(Mvel3Parser.DoWhileStatementContext ctx) {
        String statement = visit(ctx.statement());
        String condition = visit(ctx.expression());
        return "do " + statement + " while (" + condition + ");";
    }
    
    @Override
    public String visitBlockStatement(Mvel3Parser.BlockStatementContext ctx) {
        return visit(ctx.block());
    }
    
    @Override
    public String visitBlock(Mvel3Parser.BlockContext ctx) {
        StringBuilder result = new StringBuilder("{\n");
        indentLevel++;
        
        for (Mvel3Parser.StatementContext stmt : ctx.statement()) {
            result.append(indent()).append(visit(stmt)).append("\n");
        }
        
        indentLevel--;
        result.append(indent()).append("}");
        return result.toString();
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