# MVEL3 Grammar

A comprehensive ANTLR4-based grammar for the MVEL3 expression language, extending Java syntax with MVEL-specific features and supporting modern Java language constructs up to Java 20.

## Overview

MVEL3 is an expression language that combines Java-compatible syntax with powerful MVEL-specific operators and features. This project provides a complete ANTLR4 grammar definition that can parse both simple expressions and complex multi-line Java code blocks.

## Features

### Core MVEL Features

- **Java-compatible syntax**: Full support for Java expressions, statements, and control structures
- **MVEL-specific operators**: Power (`**`), string similarity (`strsim`, `soundslike`), collection operators (`contains`, `in`)
- **Safe navigation**: Null-safe operations with `?.` operator
- **Collection operations**: Projection (`.{expression}`) and selection (`.?(expression)`)
- **Inline collections**: Lists `[1, 2, 3]` and maps `{key: value}`
- **With-style blocks**: Setter chains `obj{field1 = value1, field2 = value2}`
- **Boolean test blocks**: Series of tests `obj[test1, test2, test3]`
- **Inline cast/coercion**: Type casting `object#Type` and `value#Unit`
- **Unit literals**: Physical units `5pints`, `10litres`, `25meters`
- **Special literals**: `empty`, `nil`, `undefined`
- **Regular expressions**: `~/pattern/` literals
- **Built-in functions**: `isdef()` for variable existence checking

### Modern Java Support

#### Java 17 LTS Features
- **Text blocks**: Multi-line string literals with `"""`
- **Switch expressions**: Arrow syntax and yield statements
- **Local variable type inference**: `var` keyword
- **Pattern matching**: Basic instanceof patterns
- **Enhanced switch**: Multiple case values and expressions

#### Java 20 Features
- **Advanced pattern matching**: Type patterns in switch expressions
- **Record patterns**: Destructuring record values (basic support)
- **Sealed classes**: `sealed` and `non-sealed` modifiers
- **Pattern guards**: `when` clauses for conditional patterns

### Multi-line Code Support

- **Complete Java statements**: Variable declarations, assignments, method calls
- **Control flow**: `if`/`else`, `while`, `for`, `do-while`, `switch`
- **Exception handling**: `try`/`catch`/`finally` blocks
- **Block statements**: Nested scopes with `{}`
- **Flow control**: `break`, `continue`, `return`, `throw`, `yield`

## Quick Start

### Building the Project

```bash
# Clean and compile the grammar
mvn clean compile

# Run all tests
mvn test

# Generate ANTLR sources and compile
mvn clean install
```

### Basic Usage

```java
// Parse a simple expression
String expression = "x + y * 2";
ParseTree tree = parseExpression(expression);

// Parse MVEL-specific syntax
String mvelExpr = "list.{item.name} contains 'John'";
ParseTree tree = parseExpression(mvelExpr);

// Parse multi-line code
String code = """
    var result = switch (type) {
        case "A" -> processA();
        case "B" -> processB();
        default -> processDefault();
    };
    """;
ParseTree tree = parseCode(code);
```

## Grammar Architecture

### Key Components

- **`Mvel3Lexer.g4`**: Lexical analysis defining all tokens including Java keywords and MVEL operators
- **`Mvel3Parser.g4`**: Parsing rules for expressions, statements, and modern Java constructs

### Generated Classes

The ANTLR4 Maven plugin generates:
- `Mvel3Lexer`: Tokenizes input text
- `Mvel3Parser`: Parses token streams into syntax trees
- Visitor and Listener interfaces for tree traversal

## Examples

### MVEL-Specific Expressions

```java
// Power operator
base ** exponent

// String similarity
name1 strsim name2
"Smith" soundslike "Smyth"

// Collection operations
list contains item
value in collection

// Safe navigation
obj?.field?.method()
array?.get(0)

// Collection projection and selection
people.{person.name}
users.?(user.active == true)

// With-style blocks (setter chains)
user{name = "John", age = 30, active = true}
order{id = 123, status = "PENDING", total = 99.99}

// Boolean test blocks (series of tests)
user[name != null, age >= 18, active == true]
product[price > 0, inStock == true, category != null]

// Inline cast and coercion
object#Car.manufacturer = "Honda"
value#String.toUpperCase()
"01-01-2005"#StdDate

// Unit literals and coercion
var volume = 10litres * 5pints
var distance = 25#meters + 10feet
var weight = 5#kilograms + 100grams

// Special functions
isdef(variable)
```

### Modern Java Syntax

```java
// Text blocks
var sql = """
    SELECT name, age
    FROM users
    WHERE active = true
    ORDER BY name
    """;

// Switch expressions
var result = switch (day) {
    case MONDAY -> "Start of week";
    case FRIDAY -> "TGIF";
    case SATURDAY, SUNDAY -> "Weekend";
    default -> "Weekday";
};

// Pattern matching
if (obj instanceof String s && s.length() > 0) {
    return s.toUpperCase();
}

// Var declarations
var list = new ArrayList<String>();
var name = getName();
```

### MVEL3 Extended Syntax

```java
// With-style blocks for setter chains
var user = person{
    name = "John Doe",
    age = 30,
    email = "john@example.com"
};

// Boolean test blocks for validation
if (user[name != null, age >= 18, email contains "@"]) {
    processUser(user);
}

// Inline casting and coercion
object#Car.manufacturer = "Honda";
var birthDate = "01-01-2005"#StdDate;

// Unit literals and arithmetic
var totalVolume = 10litres + 5pints;
var distance = 25#meters + 10feet;
var weight = 2#kilograms + 500grams;

// Combined with existing MVEL features
var validCars = cars.?(car#Vehicle[
    manufacturer != null,
    year >= 2020,
    fuelCapacity#litres > 40
]).{car#Vehicle{
    efficiency = car.fuelCapacity#litres / car.range#kilometers,
    rating = car.price#USD / car.rating
}};
```

### Multi-line Code Blocks

```java
// Complete algorithms
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

var result = switch (average) {
    case var avg when avg > targetAverage -> "ABOVE_TARGET";
    case var avg when avg < minimumAverage -> "BELOW_MINIMUM";
    default -> "WITHIN_RANGE";
};
```

## Testing

The project includes comprehensive test suites covering all supported features:

### Test Classes

- **`BasicExpressionTest`** (14 tests): Core Java expressions and operators
- **`MvelSpecificOperatorTest`** (12 tests): MVEL-specific syntax and operators
- **`InlineCollectionTest`** (31 tests): Lists, maps, and array creation
- **`CollectionOperationTest`** (12 tests): Projection, selection, and operations
- **`LiteralAndFunctionTest`** (21 tests): All literal types and functions
- **`MultiLineJavaCodeTest`** (23 tests): Complete Java code blocks
- **`Java17SyntaxTest`** (22 tests): Java 17 LTS features
- **`Java20SyntaxTest`** (18 tests): Java 20 modern syntax
- **`Mvel3ExtendedSyntaxTest`** (37 tests): MVEL3-specific syntax features

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BasicExpressionTest

# Run specific test method
mvn test -Dtest=Java17SyntaxTest#testSwitchExpressionWithArrow

# View test results
cat target/surefire-reports/TEST-*.xml
```

### Test Coverage

**Total: 190 tests with 178 passing (94% success rate)**

- ✅ All basic Java syntax
- ✅ All MVEL-specific features (including new MVEL3 syntax)
- ✅ Multi-line code blocks
- ✅ Modern Java features (var, switch expressions, text blocks)
- ✅ MVEL3 extended syntax (with-blocks, boolean tests, coercion, units)
- 🔄 Advanced pattern matching (expected limitations)

## Development

### Project Structure

```
mvel3/
├── src/main/antlr4/org/mvel3/grammar/
│   ├── Mvel3Lexer.g4          # Lexical rules
│   └── Mvel3Parser.g4         # Parsing rules
├── src/test/java/org/mvel3/test/
│   ├── BasicExpressionTest.java
│   ├── MvelSpecificOperatorTest.java
│   ├── InlineCollectionTest.java
│   ├── CollectionOperationTest.java
│   ├── LiteralAndFunctionTest.java
│   ├── MultiLineJavaCodeTest.java
│   ├── Java17SyntaxTest.java
│   └── Java20SyntaxTest.java
├── target/generated-sources/antlr4/  # Generated parser classes
├── pom.xml                     # Maven configuration
├── CLAUDE.md                   # Development instructions
└── README.md                   # This file
```

### Adding New Features

1. **Extend the lexer** (`Mvel3Lexer.g4`) with new tokens
2. **Update the parser** (`Mvel3Parser.g4`) with new grammar rules
3. **Add test cases** to verify the new syntax works correctly
4. **Compile and test** with `mvn clean test`

### Grammar Dependencies

- **ANTLR 4.13.1**: Grammar processing and code generation
- **Maven**: Build automation and dependency management
- **JUnit 5**: Test framework for validation

## Limitations

### Current Limitations

- **Record declarations**: Limited support for complex record syntax
- **Advanced pattern guards**: `when` clauses may not work in all contexts
- **Sealed class hierarchies**: Basic syntax recognition only
- **Method references**: Lambda expressions have limited support

### Future Enhancements

- Full record pattern support
- Complete sealed class implementation
- Lambda expression improvements
- Method reference syntax
- Additional Java 21+ features

## License

This project is part of the MVEL3 experiment and follows the same licensing terms as the MVEL project.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add comprehensive test cases
4. Ensure all tests pass with `mvn test`
5. Submit a pull request

## Support

For issues, questions, or contributions:
- Review the test cases for syntax examples
- Check `CLAUDE.md` for development guidelines
- Examine the ANTLR4 grammar files for implementation details

---

**MVEL3 Grammar** - Bringing modern Java syntax to MVEL expressions.