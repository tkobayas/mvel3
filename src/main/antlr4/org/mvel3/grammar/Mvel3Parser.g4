// MVEL3 Parser Grammar - based on Java 20 with MVEL-specific features
parser grammar Mvel3Parser;

options {
    tokenVocab = Mvel3Lexer;
}

// Start rule for MVEL expressions
start_
    : expression EOF
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
    | expression (INSTANCEOF | IS) type                                # InstanceofExpression
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
    ;

// Literals
literal
    : INTEGER_LITERAL               # IntegerLiteral
    | FLOATING_POINT_LITERAL        # FloatingPointLiteral
    | CHARACTER_LITERAL             # CharacterLiteral
    | STRING_LITERAL                # StringLiteral
    | TRUE                          # BooleanLiteral
    | FALSE                         # BooleanLiteral
    | NULL                          # NullLiteral
    | REGEX_LITERAL                 # RegexLiteral
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
    : IDENTIFIER (DOT IDENTIFIER)*
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

// Types
type
    : primitiveType (LBRACKET RBRACKET)*
    | classType (LBRACKET RBRACKET)*
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

classType
    : IDENTIFIER (DOT IDENTIFIER)*
    ;