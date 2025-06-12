// MVEL3 Parser Grammar - based on Java 20 with MVEL-specific features
parser grammar Mvel3Parser;

options {
    tokenVocab = Mvel3Lexer;
}

// Start rule for MVEL expressions or statements
start_
    : compilationUnit EOF
    | expression EOF
    ;

// Compilation unit for multi-line code
compilationUnit
    : statement*
    ;

// Statement types
statement
    : block                                                     # BlockStatement
    | IF LPAREN expression RPAREN statement (ELSE statement)?   # IfStatement
    | WHILE LPAREN expression RPAREN statement                  # WhileStatement
    | FOR LPAREN forControl RPAREN statement                   # ForStatement
    | DO statement WHILE LPAREN expression RPAREN SEMICOLON    # DoWhileStatement
    | TRY block (catchClause+ finallyBlock? | finallyBlock)     # TryStatement
    | SWITCH LPAREN expression RPAREN LBRACE switchBlockStatementGroup* RBRACE # SwitchStatement
    | RETURN expression? SEMICOLON                              # ReturnStatement
    | THROW expression SEMICOLON                                # ThrowStatement
    | YIELD expression SEMICOLON                                # YieldStmt
    | BREAK IDENTIFIER? SEMICOLON                               # BreakStatement
    | CONTINUE IDENTIFIER? SEMICOLON                            # ContinueStatement
    | SEMICOLON                                                 # EmptyStatement
    | statementExpression SEMICOLON                             # ExpressionStatement
    | localVariableDeclarationStatement                         # LocalVarDeclStatement
    | IDENTIFIER COLON statement                                # LabeledStatement
    ;

// Block statement
block
    : LBRACE statement* RBRACE
    ;

// For loop control
forControl
    : forInit? SEMICOLON expression? SEMICOLON forUpdate?       # BasicForControl
    | localVariableDeclaration COLON expression                # EnhancedForControl
    ;

forInit
    : localVariableDeclaration
    | expressionList
    ;

forUpdate
    : expressionList
    ;

// Variable declarations
localVariableDeclarationStatement
    : localVariableDeclaration SEMICOLON
    ;

localVariableDeclaration
    : variableModifier* type variableDeclarators
    | variableModifier* VAR variableDeclarators
    ;

variableModifier
    : FINAL
    ;

variableDeclarators
    : variableDeclarator (COMMA variableDeclarator)*
    ;

variableDeclarator
    : variableDeclaratorId (ASSIGN variableInitializer)?
    ;

variableDeclaratorId
    : IDENTIFIER (LBRACKET RBRACKET)*
    ;

// Try-catch constructs
catchClause
    : CATCH LPAREN variableModifier* catchType IDENTIFIER RPAREN block
    ;

catchType
    : qualifiedName (BIT_OR qualifiedName)*
    ;

finallyBlock
    : FINALLY block
    ;

// Switch constructs
switchBlockStatementGroup
    : switchLabel+ statement*
    ;

switchLabel
    : CASE constantExpression COLON
    | CASE enumConstantName COLON
    | DEFAULT COLON
    ;

constantExpression
    : expression
    ;

enumConstantName
    : IDENTIFIER
    ;

// Statement expressions (expressions that can be statements)
statementExpression
    : expression
    ;

// Qualified names
qualifiedName
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

// Primary expression hierarchy
expression
    : primary                                                           # PrimaryExpression
    | expression DOT IDENTIFIER                                         # FieldAccess
    | expression SAFE_NAV IDENTIFIER                                    # SafeFieldAccess
    | expression LBRACKET expression RBRACKET                          # ArrayAccess
    | expression SAFE_NAV LBRACKET expression RBRACKET                 # SafeArrayAccess
    | expression LPAREN expressionList? RPAREN                         # MethodCall
    | expression SAFE_NAV IDENTIFIER LPAREN expressionList? RPAREN     # SafeMethodCall
    | expression (INC | DEC)                                           # PostfixExpression
    | (INC | DEC) expression                                           # PrefixExpression
    | (PLUS | MINUS | NOT | BIT_NOT) expression                        # UnaryExpression
    | expression POW expression                                         # PowerExpression
    | expression (MULTIPLY | DIVIDE | MODULO) expression               # MultiplicativeExpression
    | expression (PLUS | MINUS) expression                             # AdditiveExpression
    | expression (LT | GT | LE | GE) expression                        # RelationalExpression
    | expression (INSTANCEOF | IS) type IDENTIFIER?                    # InstanceofExpression
    | expression (EQ | NE) expression                                  # EqualityExpression
    | expression (STRSIM | SOUNDSLIKE) expression                      # StringSimilarityExpression
    | expression (CONTAINS | IN) expression                            # CollectionExpression
    | expression BIT_AND expression                                    # BitwiseAndExpression
    | expression BIT_XOR expression                                    # BitwiseXorExpression
    | expression BIT_OR expression                                     # BitwiseOrExpression
    | expression AND expression                                        # LogicalAndExpression
    | expression OR expression                                         # LogicalOrExpression
    | <assoc=right> expression QUESTION expression COLON expression    # TernaryExpression
    | <assoc=right> expression assignmentOperator expression           # AssignmentExpression
    | expression PROJECTION expression RBRACE                          # ProjectionExpression
    | expression SELECTION expression RPAREN                           # SelectionExpression
    | ISDEF LPAREN expression RPAREN                                   # IsDefExpression
    | expression BIT_NOT REGEX_LITERAL                                 # RegexMatchExpression
    | expression HASH IDENTIFIER                                       # CoercionExpression
    | expression HASH STRING_LITERAL                                   # CoercionExpression
    | expression LBRACE withStatementList? RBRACE                      # WithBlockExpression
    | expression LBRACKET booleanTestList? RBRACKET                    # BooleanTestBlockExpression
    | expression DOUBLE_COLON IDENTIFIER                               # MethodReferenceExpression
    | expression DOUBLE_COLON NEW                                      # ConstructorReferenceExpression
    | expressionName DOUBLE_COLON IDENTIFIER                           # ExpressionMethodReference
    | referenceType DOUBLE_COLON IDENTIFIER                            # TypeMethodReference
    | SUPER DOUBLE_COLON IDENTIFIER                                    # SuperMethodReference
    | classType DOUBLE_COLON NEW                                       # ConstructorReference
    ;

// Primary expressions
primary
    : LPAREN expression RPAREN      # ParenthesizedExpression
    | literal                       # LiteralExpression
    | IDENTIFIER                    # IdentifierExpression
    | THIS                          # ThisExpression
    | SUPER DOT IDENTIFIER          # SuperFieldAccess
    | NEW creator                   # NewExpression
    | inlineList                    # InlineListExpression
    | inlineMap                     # InlineMapExpression
    | EMPTY                         # EmptyExpression
    | NIL                           # NilExpression
    | UNDEFINED                     # UndefinedExpression
    | switchExpression              # SwitchExpressionPrimary
    | lambdaExpression              # LambdaExpressionPrimary
    ;

// Literals
literal
    : INTEGER_LITERAL               # IntegerLiteral
    | FLOATING_POINT_LITERAL        # FloatingPointLiteral
    | CHARACTER_LITERAL             # CharacterLiteral
    | STRING_LITERAL                # StringLiteral
    | TEXT_BLOCK                    # TextBlockLiteral
    | TRUE                          # BooleanLiteral
    | FALSE                         # BooleanLiteral
    | NULL                          # NullLiteral
    | REGEX_LITERAL                 # RegexLiteral
    | UNIT_LITERAL                  # UnitLiteral
    ;


// Collection literals
inlineList
    : LBRACKET (expressionList)? RBRACKET
    ;

inlineMap
    : LBRACE (mapEntryList)? RBRACE
    ;

mapEntryList
    : mapEntry (COMMA mapEntry)*
    ;

mapEntry
    : expression COLON expression
    | IDENTIFIER COLON expression
    ;

// Expression list
expressionList
    : expression (COMMA expression)*
    ;

// Assignment operators
assignmentOperator
    : ASSIGN
    | PLUS_ASSIGN
    | MINUS_ASSIGN
    | MUL_ASSIGN
    | DIV_ASSIGN
    | POW_ASSIGN
    ;

// Object creation
creator
    : createdName (arrayCreatorRest | classCreatorRest)
    ;

createdName
    : IDENTIFIER (DOT IDENTIFIER)* typeArguments?
    | primitiveType
    ;

arrayCreatorRest
    : LBRACKET (RBRACKET (LBRACKET RBRACKET)* arrayInitializer
              | expression RBRACKET (LBRACKET expression RBRACKET)* (LBRACKET RBRACKET)*)
    ;

classCreatorRest
    : LPAREN expressionList? RPAREN
    ;

arrayInitializer
    : LBRACE (variableInitializer (COMMA variableInitializer)* (COMMA)?)? RBRACE
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

// Types with generic support
type
    : primitiveType (LBRACKET RBRACKET)*
    | referenceType (LBRACKET RBRACKET)*
    ;

primitiveType
    : BOOLEAN
    | CHAR
    | BYTE
    | SHORT
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    ;

referenceType
    : classType typeArguments?
    | typeVariable
    ;

classType
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

typeVariable
    : IDENTIFIER
    ;

// Generic type support
typeArguments
    : LT typeArgumentList? GT
    ;

typeArgumentList
    : typeArgument (COMMA typeArgument)*
    ;

typeArgument
    : referenceType
    | wildcard
    ;

wildcard
    : QUESTION wildcardBounds?
    ;

wildcardBounds
    : EXTENDS referenceType
    | SUPER referenceType
    ;

// Type parameters for declarations
typeParameters
    : LT typeParameterList GT
    ;

typeParameterList
    : typeParameter (COMMA typeParameter)*
    ;

typeParameter
    : IDENTIFIER typeBound?
    ;

typeBound
    : EXTENDS (referenceType | classType) (BIT_AND (referenceType | classType))*
    ;

// Modern Java syntax (Java 17+)

// Switch expressions
switchExpression
    : SWITCH LPAREN expression RPAREN LBRACE switchExpressionCase* RBRACE
    ;

switchExpressionCase
    : CASE casePattern (COMMA casePattern)* COLON switchBlockStatements       # ColonSwitchExpressionCase
    | CASE casePattern (COMMA casePattern)* ARROW switchExpressionResult      # ArrowSwitchExpressionCase
    | DEFAULT COLON switchBlockStatements                                     # DefaultColonSwitchExpressionCase
    | DEFAULT ARROW switchExpressionResult                                    # DefaultArrowSwitchExpressionCase
    ;

casePattern
    : expression
    | pattern
    ;

pattern
    : typePattern
    | expression
    ;

typePattern
    : type IDENTIFIER
    ;

switchExpressionResult
    : expression SEMICOLON?                                                   # ExpressionSwitchResult
    | block                                                                   # BlockSwitchResult
    | YIELD expression SEMICOLON                                              # YieldSwitchResult
    | THROW expression SEMICOLON                                              # ThrowSwitchResult
    ;

switchBlockStatements
    : statement*
    ;

// Yield statement
yieldStatement
    : YIELD expression SEMICOLON
    ;

// Record declaration (basic support)
recordDeclaration
    : RECORD IDENTIFIER LPAREN recordComponentList? RPAREN recordBody?
    ;

recordComponentList
    : recordComponent (COMMA recordComponent)*
    ;

recordComponent
    : type IDENTIFIER
    ;

recordBody
    : LBRACE recordBodyDeclaration* RBRACE
    ;

recordBodyDeclaration
    : statement
    ;

// Sealed class modifiers
sealedModifier
    : SEALED
    | NON_SEALED
    ;

// Pattern guards (for advanced pattern matching)
guardedPattern
    : pattern WHEN expression
    ;

// MVEL3-specific syntax

// Lambda expressions
lambdaExpression
    : lambdaParameters ARROW lambdaBody
    ;

lambdaParameters
    : IDENTIFIER                                                    # SingleParameterLambda
    | LPAREN RPAREN                                                 # NoParameterLambda
    | LPAREN lambdaParameterList RPAREN                             # MultiParameterLambda
    ;

lambdaParameterList
    : lambdaParameter (COMMA lambdaParameter)*
    ;

lambdaParameter
    : type? IDENTIFIER
    | VAR IDENTIFIER
    ;

lambdaBody
    : expression                                                    # ExpressionLambdaBody
    | block                                                         # BlockLambdaBody
    ;

// Method references are now handled in the expression rules above

expressionName
    : IDENTIFIER (DOT IDENTIFIER)*
    ;

// With-style block statement lists
withStatementList
    : withStatement (COMMA withStatement)*
    ;

withStatement
    : IDENTIFIER ASSIGN expression
    | expression
    ;

// Boolean test block lists
booleanTestList
    : booleanTest (COMMA booleanTest)*
    ;

booleanTest
    : expression
    ;