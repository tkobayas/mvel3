// MVEL3 Lexer Grammar - based on Java 20 with MVEL-specific extensions
lexer grammar Mvel3Lexer;

// MVEL-specific operators
EQ          : '==';
NE          : '!=';
LT          : '<';
GT          : '>';
LE          : '<=';
GE          : '>=';
POW         : '**';
STRSIM      : 'strsim';
SOUNDSLIKE  : 'soundslike';
CONTAINS    : 'contains';
INSTANCEOF  : 'instanceof';
ISDEF       : 'isdef';
IS          : 'is';
IN          : 'in';
EMPTY       : 'empty';
NIL         : 'nil';
UNDEFINED   : 'undefined';

// Safe navigation
SAFE_NAV    : '?.';

// Collection operators
PROJECTION  : '.{';
SELECTION   : '.?(';

// Assignment operators
ASSIGN      : '=';
PLUS_ASSIGN : '+=';
MINUS_ASSIGN: '-=';
MUL_ASSIGN  : '*=';
DIV_ASSIGN  : '/=';
POW_ASSIGN  : '**=';

// Arithmetic operators
PLUS        : '+';
MINUS       : '-';
MULTIPLY    : '*';
DIVIDE      : '/';
MODULO      : '%';

// Logical operators
AND         : '&&';
OR          : '||';
NOT         : '!';

// Bitwise operators
BIT_AND     : '&';
BIT_OR      : '|';
BIT_XOR     : '^';
BIT_NOT     : '~';

// Increment/decrement
INC         : '++';
DEC         : '--';

// Punctuation
LPAREN      : '(';
RPAREN      : ')';
LBRACE      : '{';
RBRACE      : '}';
LBRACKET    : '[';
RBRACKET    : ']';
SEMICOLON   : ';';
COMMA       : ',';
DOT         : '.';
COLON       : ':';
DOUBLE_COLON: '::';
QUESTION    : '?';
ARROW       : '->';
HASH        : '#';

// Java keywords that MVEL supports
ABSTRACT    : 'abstract';
ASSERT      : 'assert';
BOOLEAN     : 'boolean';
BREAK       : 'break';
BYTE        : 'byte';
CASE        : 'case';
CATCH       : 'catch';
CHAR        : 'char';
CLASS       : 'class';
CONST       : 'const';
CONTINUE    : 'continue';
DEFAULT     : 'default';
DO          : 'do';
DOUBLE      : 'double';
ELSE        : 'else';
ENUM        : 'enum';
EXTENDS     : 'extends';
FINAL       : 'final';
FINALLY     : 'finally';
FLOAT       : 'float';
FOR         : 'for';
GOTO        : 'goto';
IF          : 'if';
IMPLEMENTS  : 'implements';
IMPORT      : 'import';
INT         : 'int';
INTERFACE   : 'interface';
LONG        : 'long';
NATIVE      : 'native';
NEW         : 'new';
PACKAGE     : 'package';
PRIVATE     : 'private';
PROTECTED   : 'protected';
PUBLIC      : 'public';
RETURN      : 'return';
SHORT       : 'short';
STATIC      : 'static';
STRICTFP    : 'strictfp';
SUPER       : 'super';
SWITCH      : 'switch';
SYNCHRONIZED: 'synchronized';
THIS        : 'this';
THROW       : 'throw';
THROWS      : 'throws';
TRANSIENT   : 'transient';
TRY         : 'try';
VOID        : 'void';
VOLATILE    : 'volatile';
WHILE       : 'while';

// Java 17+ keywords
SEALED      : 'sealed';
PERMITS     : 'permits';
RECORD      : 'record';
YIELD       : 'yield';
VAR         : 'var';
NON_SEALED  : 'non-sealed';
WHEN        : 'when';
WITH        : 'with';

// Boolean literals
TRUE        : 'true';
FALSE       : 'false';
NULL        : 'null';

// Numeric literals
INTEGER_LITERAL
    : DecimalIntegerLiteral
    | HexIntegerLiteral
    | OctalIntegerLiteral
    | BinaryIntegerLiteral
    ;

fragment
DecimalIntegerLiteral
    : DecimalNumeral IntegerTypeSuffix?
    ;

fragment
HexIntegerLiteral
    : HexNumeral IntegerTypeSuffix?
    ;

fragment
OctalIntegerLiteral
    : OctalNumeral IntegerTypeSuffix?
    ;

fragment
BinaryIntegerLiteral
    : BinaryNumeral IntegerTypeSuffix?
    ;

fragment
IntegerTypeSuffix
    : [lL]
    ;

fragment
DecimalNumeral
    : '0'
    | NonZeroDigit (Digits? | Underscores Digits)
    ;

fragment
Digits
    : Digit (DigitsAndUnderscores? Digit)?
    ;

fragment
Digit
    : '0'
    | NonZeroDigit
    ;

fragment
NonZeroDigit
    : [1-9]
    ;

fragment
DigitsAndUnderscores
    : DigitOrUnderscore+
    ;

fragment
DigitOrUnderscore
    : Digit
    | '_'
    ;

fragment
Underscores
    : '_'+
    ;

fragment
HexNumeral
    : '0' [xX] HexDigits
    ;

fragment
HexDigits
    : HexDigit (HexDigitsAndUnderscores? HexDigit)?
    ;

fragment
HexDigit
    : [0-9a-fA-F]
    ;

fragment
HexDigitsAndUnderscores
    : HexDigitOrUnderscore+
    ;

fragment
HexDigitOrUnderscore
    : HexDigit
    | '_'
    ;

fragment
OctalNumeral
    : '0' Underscores? OctalDigits
    ;

fragment
OctalDigits
    : OctalDigit (OctalDigitsAndUnderscores? OctalDigit)?
    ;

fragment
OctalDigit
    : [0-7]
    ;

fragment
OctalDigitsAndUnderscores
    : OctalDigitOrUnderscore+
    ;

fragment
OctalDigitOrUnderscore
    : OctalDigit
    | '_'
    ;

fragment
BinaryNumeral
    : '0' [bB] BinaryDigits
    ;

fragment
BinaryDigits
    : BinaryDigit (BinaryDigitsAndUnderscores? BinaryDigit)?
    ;

fragment
BinaryDigit
    : [01]
    ;

fragment
BinaryDigitsAndUnderscores
    : BinaryDigitOrUnderscore+
    ;

fragment
BinaryDigitOrUnderscore
    : BinaryDigit
    | '_'
    ;

// Floating point literals
FLOATING_POINT_LITERAL
    : DecimalFloatingPointLiteral
    | HexadecimalFloatingPointLiteral
    ;

fragment
DecimalFloatingPointLiteral
    : Digits '.' Digits? ExponentPart? FloatTypeSuffix?
    | '.' Digits ExponentPart? FloatTypeSuffix?
    | Digits ExponentPart FloatTypeSuffix?
    | Digits FloatTypeSuffix
    ;

fragment
ExponentPart
    : ExponentIndicator SignedInteger
    ;

fragment
ExponentIndicator
    : [eE]
    ;

fragment
SignedInteger
    : Sign? Digits
    ;

fragment
Sign
    : [+-]
    ;

fragment
FloatTypeSuffix
    : [fFdD]
    ;

fragment
HexadecimalFloatingPointLiteral
    : HexSignificand BinaryExponent FloatTypeSuffix?
    ;

fragment
HexSignificand
    : HexNumeral '.'?
    | '0' [xX] HexDigits? '.' HexDigits
    ;

fragment
BinaryExponent
    : BinaryExponentIndicator SignedInteger
    ;

fragment
BinaryExponentIndicator
    : [pP]
    ;

// Character literals
CHARACTER_LITERAL
    : '\'' SingleCharacter '\''
    | '\'' EscapeSequence '\''
    ;

fragment
SingleCharacter
    : ~['\\]
    ;

fragment
EscapeSequence
    : '\\' [btnfr"'\\]
    | OctalEscape
    | UnicodeEscape
    ;

fragment
OctalEscape
    : '\\' OctalDigit
    | '\\' OctalDigit OctalDigit
    | '\\' ZeroToThree OctalDigit OctalDigit
    ;

fragment
ZeroToThree
    : [0-3]
    ;

fragment
UnicodeEscape
    : '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

// String literals
STRING_LITERAL
    : '"' StringCharacters? '"'
    ;

// Text block literals (Java 15+)
TEXT_BLOCK
    : '"""' [ \t]* [\r\n] TextBlockContent? '"""'
    ;

fragment
TextBlockContent
    : TextBlockCharacter*
    ;

fragment
TextBlockCharacter
    : ~["\\]
    | EscapeSequence
    | '"' ~["]
    | '""' ~["]
    ;

fragment
StringCharacters
    : StringCharacter+
    ;

fragment
StringCharacter
    : ~["\\]
    | EscapeSequence
    ;

// Regular expression literals
REGEX_LITERAL
    : '~/' (~[/\r\n] | '\\' .)+ '/'
    ;

// Unit literals (like "5pints", "10litres")
UNIT_LITERAL
    : [0-9]+ [a-zA-Z]+
    ;

// Identifiers
IDENTIFIER
    : JavaLetter JavaLetterOrDigit*
    ;

fragment
JavaLetter
    : [a-zA-Z$_]
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

fragment
JavaLetterOrDigit
    : [a-zA-Z0-9$_]
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

// Whitespace and comments
WS
    : [ \t\r\n\u000C]+ -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;