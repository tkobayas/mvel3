# MVEL3 Grammar Development Notes

This document explains the differences between the reference Java 20 grammar files and the MVEL3 grammar implementation, providing guidance for future grammar development and maintenance.

## Overview

The MVEL3 grammar is based on Java 20 syntax but extends it with MVEL-specific expression language features. Two sets of grammar files exist in this project:

- **Reference Grammar**: `docs/Java20Lexer.g4` and `docs/Java20Parser.g4` - Complete Java 20 language specification
- **MVEL3 Grammar**: `src/main/antlr4/org/mvel3/grammar/Mvel3Lexer.g4` and `src/main/antlr4/org/mvel3/grammar/Mvel3Parser.g4` - MVEL3 expression language

## Key Differences

### 1. Language Scope and Purpose

#### Java 20 Grammar (Reference)
- **Full Programming Language**: Complete Java language specification including modules, packages, classes, interfaces, methods, etc.
- **Statement-Oriented**: Supports full program structure with class declarations, method definitions, control flow
- **Compilation Units**: Designed for parsing complete Java source files with package declarations and imports

#### MVEL3 Grammar  
- **Expression Language**: Focused on expressions, simple statements, and scripting constructs
- **Expression-Oriented**: Optimized for evaluating expressions within a runtime context
- **Embedded Usage**: Designed to be embedded in applications for dynamic expression evaluation

### 2. Lexer Differences (`Mvel3Lexer.g4` vs `Java20Lexer.g4`)

#### MVEL-Specific Operators (Added in MVEL3)
```antlr
// MVEL-specific comparison and collection operators
STRSIM      : 'strsim';         // String similarity operator
SOUNDSLIKE  : 'soundslike';     // Phonetic similarity operator  
CONTAINS    : 'contains';       // Collection membership
IS          : 'is';             // Alternative instanceof
IN          : 'in';             // Collection membership (alternative)

// MVEL-specific literals
EMPTY       : 'empty';          // Empty collection literal
NIL         : 'nil';            // Null alternative
UNDEFINED   : 'undefined';      // Undefined value

// MVEL-specific navigation
SAFE_NAV    : '?.';             // Safe navigation operator
PROJECTION  : '.{';             // Collection projection
SELECTION   : '.?(';            // Collection selection

// MVEL-specific operators
POW         : '**';             // Power operator
POW_ASSIGN  : '**=';            // Power assignment
ISDEF       : 'isdef';          // Variable definition check
```

#### Simplified Token Set
- **Reduced Keywords**: MVEL3 omits many Java keywords not relevant for expressions (`package`, `import`, `class`, `interface`, `enum`, etc.)
- **Focused Tokens**: Only includes tokens necessary for expression evaluation and simple statements
- **MVEL Extensions**: Adds tokens for MVEL-specific syntax not present in Java

### 3. Parser Differences (`Mvel3Parser.g4` vs `Java20Parser.g4`)

#### Simplified Start Rules
```antlr
// Java 20: Full compilation unit
start_: compilationUnit EOF;

// MVEL3: Expression or simple statements
start_: compilationUnit EOF | expression EOF;
```

#### Expression-Centric Design

**Java 20 Parser Structure:**
- Compilation units → Type declarations → Class bodies → Method declarations → Statements → Expressions
- Complex inheritance hierarchies, generics, annotations, modules
- Full object-oriented programming constructs

**MVEL3 Parser Structure:**  
- Direct expression evaluation with optional simple statements
- Flattened hierarchy focused on runtime expression evaluation
- Streamlined for embedded usage scenarios

#### MVEL-Specific Expression Extensions

```antlr
// Safe navigation
expression SAFE_NAV IDENTIFIER                          # SafeFieldAccess
expression SAFE_NAV LBRACKET expression RBRACKET       # SafeArrayAccess

// Collection operations  
expression PROJECTION expression RBRACE                 # ProjectionExpression
expression SELECTION expression RPAREN                  # SelectionExpression

// MVEL-specific operators
expression (STRSIM | SOUNDSLIKE) expression            # StringSimilarityExpression
expression (CONTAINS | IN) expression                  # CollectionExpression
expression POW expression                               # PowerExpression

// MVEL-specific constructs
ISDEF LPAREN expression RPAREN                         # IsDefExpression
expression LBRACE withStatementList? RBRACE            # WithBlockExpression
expression LBRACKET booleanTestList? RBRACKET          # BooleanTestBlockExpression
```

#### Simplified Type System

**Java 20**: Full generics with variance, intersection types, union types, etc.
```antlr
typeParameter: typeParameterModifier* typeIdentifier typeBound?;
wildcardBounds: 'extends' referenceType | 'super' referenceType;
```

**MVEL3**: Essential generics for expression evaluation
```antlr
typeParameter: IDENTIFIER typeBound?;
wildcardBounds: EXTENDS referenceType | SUPER referenceType;
```

### 4. Modern Java Feature Support

Both grammars support Java 17+ and Java 20+ features, but with different scopes:

#### Shared Modern Features
- **Generic Types**: `List<String>`, `Map<K,V>`, wildcards `<?>`
- **Lambda Expressions**: `x -> x * 2`, `(a, b) -> a + b`
- **Method References**: `String::valueOf`, `Object::toString`
- **Switch Expressions**: `case A -> result;`, `default -> throw ex;`
- **Pattern Matching**: `instanceof String s`
- **var Declarations**: `var list = new ArrayList<>();`
- **Text Blocks**: Multi-line string literals

#### Java 20 Only (Not in MVEL3)
- **Modules**: `module`, `exports`, `requires` declarations
- **Records**: `record Person(String name, int age)`
- **Sealed Classes**: `sealed class Shape permits Circle, Rectangle`
- **Advanced Pattern Matching**: Complex patterns with guards
- **Full Class/Interface Declarations**: Complete OOP constructs

## Development Guidelines

### When to Update MVEL3 Grammar

1. **New Java Expression Syntax**: When new Java versions introduce expression-level syntax
2. **MVEL Feature Requests**: When new MVEL-specific operators or constructs are needed
3. **Bug Fixes**: When parsing issues are discovered in existing features

### How to Update MVEL3 Grammar

1. **Reference Java Grammar**: Check `docs/Java20Parser.g4` for the canonical Java implementation
2. **Adapt for MVEL**: Simplify rules to focus on expression context
3. **Add MVEL Extensions**: Include any MVEL-specific modifications
4. **Test Thoroughly**: Ensure both Java compatibility tests and MVEL-specific tests pass

### Maintaining Compatibility

#### With Java
- Monitor new Java language features in each release
- Update grammar to support new expression syntax
- Maintain compatibility with existing Java expressions

#### With MVEL Legacy
- Preserve all existing MVEL-specific operators and syntax
- Ensure backward compatibility with existing MVEL expressions
- Document any breaking changes clearly

## Testing Strategy

### Grammar Validation Tests
- **Java17SyntaxTest**: Validates Java 17+ language features
- **Java20SyntaxTest**: Validates Java 20+ language features  
- **MVELTranspilerTest**: Validates MVEL-specific syntax and transpilation
- **MVELCompilerTest**: Validates end-to-end compilation and execution

### Coverage Areas
1. **Core Java Expressions**: Arithmetic, logical, comparison operations
2. **Modern Java Features**: Generics, lambdas, method references, switch expressions
3. **MVEL Extensions**: Safe navigation, collection operations, MVEL-specific operators
4. **Edge Cases**: Complex nested expressions, operator precedence, type inference

## Architecture Notes

### Parser Generation
Both grammars use ANTLR 4.13.1 for parser generation:
```xml
<plugin>
    <groupId>org.antlr</groupId>
    <artifactId>antlr4-maven-plugin</artifactId>
    <version>4.13.1</version>
</plugin>
```

### Integration Points
- **MVELTranspiler**: Converts MVEL3 AST to Java code using visitor pattern
- **MVELCompiler**: Orchestrates parsing, transpilation, and compilation
- **Runtime Evaluation**: Generated evaluators execute with type safety

### Performance Considerations
- **Expression Focus**: MVEL3 grammar optimized for expression parsing performance
- **Reduced Complexity**: Simpler rule structure compared to full Java grammar
- **Runtime Efficiency**: Generated parsers optimized for embedded usage scenarios

## Future Enhancements

### Potential Java Features to Add
- **Pattern Matching Enhancements**: As Java continues to evolve pattern matching
- **Virtual Threads Syntax**: Any new syntax related to Project Loom
- **Value Types**: When Project Valhalla introduces new syntax
- **Foreign Function Interface**: If FFI adds expression-level syntax

### MVEL-Specific Improvements
- **Enhanced Collection Operations**: More sophisticated projection/selection syntax
- **Type Inference**: Improved type inference for MVEL expressions
- **Performance Optimizations**: Grammar rules optimized for common expression patterns
- **IDE Support**: Better tooling support through grammar annotations

## Conclusion

The MVEL3 grammar represents a carefully balanced implementation that provides:
- **Java Compatibility**: Support for modern Java expression syntax
- **MVEL Extensions**: Powerful expression language features
- **Performance**: Optimized for embedded evaluation scenarios
- **Maintainability**: Clear separation from reference Java grammar

By maintaining both reference and implementation grammars, we ensure MVEL3 stays current with Java evolution while preserving its unique expression language capabilities.