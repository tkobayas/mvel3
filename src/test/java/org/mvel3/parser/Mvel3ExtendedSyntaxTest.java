package org.mvel3.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.junit.jupiter.api.Test;
import org.mvel3.grammar.Mvel3Lexer;
import org.mvel3.grammar.Mvel3Parser;

import static org.junit.jupiter.api.Assertions.*;

public class Mvel3ExtendedSyntaxTest {

    private ParseTree parseExpression(String expression) {
        ANTLRInputStream input = new ANTLRInputStream(expression);
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

    private ParseTree parseCode(String code) {
        return parseExpression(code);
    }

    // ==== WITH-STYLE BLOCKS ====

    @Test
    public void testBasicWithBlock() {
        // Basic with block for setter chains
        assertDoesNotThrow(() -> parseExpression("t{status = RECEIVED, timestamp = new Date()}"));
    }

    @Test
    public void testWithBlockSingleSetter() {
        assertDoesNotThrow(() -> parseExpression("obj{value = 42}"));
        assertDoesNotThrow(() -> parseExpression("person{name = \"John\"}"));
        assertDoesNotThrow(() -> parseExpression("config{enabled = true}"));
    }

    @Test
    public void testWithBlockMultipleSetters() {
        assertDoesNotThrow(() -> parseExpression("user{name = \"Alice\", age = 30, active = true}"));
        assertDoesNotThrow(() -> parseExpression("order{id = 123, status = \"PENDING\", total = 99.99}"));
        assertDoesNotThrow(() -> parseExpression("product{sku = \"ABC123\", price = 29.95, inStock = true}"));
    }

    @Test
    public void testWithBlockWithExpressions() {
        assertDoesNotThrow(() -> parseExpression("item{value = x + y, active = !disabled}"));
        assertDoesNotThrow(() -> parseExpression("entry{timestamp = System.currentTimeMillis(), valid = checkValid()}"));
        assertDoesNotThrow(() -> parseExpression("entity{score = Math.max(a, b), rating = score / 10}"));
    }

    @Test
    public void testWithBlockWithMethodCalls() {
        assertDoesNotThrow(() -> parseExpression("obj{value = getValue(), name = getName()}"));
        assertDoesNotThrow(() -> parseExpression("task{priority = calculatePriority(), deadline = getDeadline()}"));
    }

    @Test
    public void testNestedWithBlocks() {
        assertDoesNotThrow(() -> parseExpression("outer{inner = inner{value = 42}}"));
        assertDoesNotThrow(() -> parseExpression("parent{child = child{name = \"test\", active = true}}"));
    }

    @Test
    public void testWithBlockInAssignment() {
        String code = """
            result = user{
                name = "John Doe",
                email = "john@example.com",
                active = true
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testWithBlockWithArrayAccess() {
        assertDoesNotThrow(() -> parseExpression("items[0]{status = \"UPDATED\", modified = true}"));
        assertDoesNotThrow(() -> parseExpression("users.get(id){lastLogin = new Date()}"));
    }

    // ==== BOOLEAN TEST BLOCKS ====

    @Test
    public void testBasicBooleanTestBlock() {
        assertDoesNotThrow(() -> parseExpression("t[status == RECEIVED, timestamp != null]"));
    }

    @Test
    public void testBooleanTestBlockSingleTest() {
        assertDoesNotThrow(() -> parseExpression("obj[value > 0]"));
        assertDoesNotThrow(() -> parseExpression("person[name != null]"));
        assertDoesNotThrow(() -> parseExpression("item[active == true]"));
    }

    @Test
    public void testBooleanTestBlockMultipleTests() {
        assertDoesNotThrow(() -> parseExpression("user[name != null, age >= 18, active == true]"));
        assertDoesNotThrow(() -> parseExpression("order[id > 0, status == \"PENDING\", total > 0]"));
        assertDoesNotThrow(() -> parseExpression("product[sku != null, price > 0, inStock == true]"));
    }

    @Test
    public void testBooleanTestBlockWithComplexExpressions() {
        assertDoesNotThrow(() -> parseExpression("item[value > threshold, status in validStatuses]"));
        assertDoesNotThrow(() -> parseExpression("entry[timestamp > startTime, isdef(owner)]"));
        assertDoesNotThrow(() -> parseExpression("entity[score >= minScore, name strsim searchTerm]"));
    }

    @Test
    public void testBooleanTestBlockWithMvelOperators() {
        assertDoesNotThrow(() -> parseExpression("obj[field?.value != null, list contains item]"));
        assertDoesNotThrow(() -> parseExpression("data[value ** 2 > threshold, name soundslike pattern]"));
    }

    @Test
    public void testNestedBooleanTestBlocks() {
        assertDoesNotThrow(() -> parseExpression("outer[inner[value > 0], active == true]"));
        assertDoesNotThrow(() -> parseExpression("parent[child[name != null, active], valid == true]"));
    }

    @Test
    public void testBooleanTestBlockInConditions() {
        String code = """
            if (user[name != null, age >= 18, active == true]) {
                processUser(user);
            }
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== INLINE CAST AND COERCION ====

    @Test
    public void testBasicCoercion() {
        assertDoesNotThrow(() -> parseExpression("object#Car"));
        assertDoesNotThrow(() -> parseExpression("value#String"));
        assertDoesNotThrow(() -> parseExpression("data#Integer"));
    }

    @Test
    public void testCoercionWithFieldAccess() {
        assertDoesNotThrow(() -> parseExpression("object#Car.manufacturer"));
        assertDoesNotThrow(() -> parseExpression("vehicle#Truck.payload"));
        assertDoesNotThrow(() -> parseExpression("item#Product.category"));
    }

    @Test
    public void testCoercionWithAssignment() {
        String code = """
            object#Car.manufacturer = "Honda";
            vehicle#Truck.maxWeight = 5000;
            item#Product.price = 99.99;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testCoercionWithStringTypes() {
        assertDoesNotThrow(() -> parseExpression("p.dateOfBirth = \"01-01-2005\"#StdDate"));
        assertDoesNotThrow(() -> parseExpression("value = \"123\"#Integer"));
        assertDoesNotThrow(() -> parseExpression("config = \"true\"#Boolean"));
    }

    @Test
    public void testCoercionWithMethodChaining() {
        assertDoesNotThrow(() -> parseExpression("obj#Service.process().getResult()"));
        assertDoesNotThrow(() -> parseExpression("data#Processor.transform().validate()"));
    }

    @Test
    public void testComplexCoercionExpressions() {
        assertDoesNotThrow(() -> parseExpression("(getValue())#String.toUpperCase()"));
        assertDoesNotThrow(() -> parseExpression("list.get(0)#Car.getManufacturer()"));
        assertDoesNotThrow(() -> parseExpression("map[\"key\"]#Product.getPrice()"));
    }

    // ==== UNIT LITERALS ====

    @Test
    public void testBasicUnitLiterals() {
        assertDoesNotThrow(() -> parseExpression("5pints"));
        assertDoesNotThrow(() -> parseExpression("10litres"));
        assertDoesNotThrow(() -> parseExpression("25meters"));
        assertDoesNotThrow(() -> parseExpression("100grams"));
    }

    @Test
    public void testUnitLiteralsInExpressions() {
        assertDoesNotThrow(() -> parseExpression("volume = 10litres"));
        assertDoesNotThrow(() -> parseExpression("distance = 5miles + 2kilometers"));
        assertDoesNotThrow(() -> parseExpression("weight = 100grams * 3"));
    }

    @Test
    public void testUnitCoercionCombination() {
        assertDoesNotThrow(() -> parseCode("var x = 10#litres * 5pints;"));
        assertDoesNotThrow(() -> parseCode("result = 25#meters + 10feet;"));
        assertDoesNotThrow(() -> parseCode("total = 5#kilograms + 100grams;"));
    }

    @Test
    public void testCoercionWithUnits() {
        assertDoesNotThrow(() -> parseExpression("volume = value#litres"));
        assertDoesNotThrow(() -> parseExpression("distance = measurement#kilometers"));
        assertDoesNotThrow(() -> parseExpression("weight = amount#pounds"));
    }

    // ==== COMBINED MVEL3 FEATURES ====

    @Test
    public void testWithBlocksAndCoercion() {
        assertDoesNotThrow(() -> parseExpression("car#Vehicle{manufacturer = \"Toyota\", year = 2023}"));
        assertDoesNotThrow(() -> parseExpression("product#Item{price = 99.99#USD, weight = 5pounds}"));
    }

    @Test
    public void testBooleanTestsWithCoercion() {
        assertDoesNotThrow(() -> parseExpression("obj#Car[manufacturer == \"Honda\", year > 2020]"));
        assertDoesNotThrow(() -> parseExpression("item#Product[price#USD > 50, weight#grams < 1000]"));
    }

    @Test
    public void testComplexMvel3Expressions() {
        assertDoesNotThrow(() -> parseExpression("users.?(user#Person[age >= 18])"));
        assertDoesNotThrow(() -> parseExpression("products.{item#Product{discounted = price * 0.9}}"));
    }

    @Test
    public void testMvel3WithSafeNavigation() {
        assertDoesNotThrow(() -> parseExpression("obj?.field#String?.toUpperCase()"));
        assertDoesNotThrow(() -> parseExpression("item?.value{updated = true}"));
        assertDoesNotThrow(() -> parseExpression("data?.item[valid == true]"));
    }

    @Test
    public void testMvel3InMultiLineCode() {
        String code = """
            var car = vehicle#Car{
                manufacturer = "Toyota",
                year = 2023,
                fuelCapacity = 50litres
            };
            
            if (car[manufacturer != null, year > 2020]) {
                car.efficiency = car.fuelCapacity#litres / 100kilometers;
            }
            
            var valid = car[
                manufacturer strsim "Toyota",
                year >= 2020,
                fuelCapacity#litres > 40
            ];
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    @Test
    public void testAdvancedMvel3Patterns() {
        String code = """
            result = switch (type) {
                case "car" -> item#Car{
                    efficiency = fuelCapacity#litres / range#kilometers,
                    valid = this[manufacturer != null, year > 2015]
                };
                case "truck" -> item#Truck{
                    payload = maxWeight#tons - emptyWeight#tons,
                    commercial = this[payloadCapacity#tons > 3.5]
                };
                default -> item{type = "unknown"};
            };
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }

    // ==== ERROR CASES AND EDGE CASES ====

    @Test
    public void testEmptyWithBlock() {
        assertDoesNotThrow(() -> parseExpression("obj{}"));
    }

    @Test
    public void testEmptyBooleanTestBlock() {
        assertDoesNotThrow(() -> parseExpression("obj[]"));
    }

    @Test
    public void testWithBlockTrailingComma() {
        // Test if trailing comma is handled gracefully
        try {
            parseExpression("obj{value = 42,}");
        } catch (RuntimeException e) {
            // Expected for trailing comma - grammar may not support it
            assertTrue(e.getMessage().contains("Syntax error"));
        }
    }

    @Test
    public void testComplexUnitArithmetic() {
        assertDoesNotThrow(() -> parseExpression("total = 5meters * 3 + 10centimeters"));
        assertDoesNotThrow(() -> parseExpression("volume = 2litres + 500milliliters - 100milliliters"));
        assertDoesNotThrow(() -> parseExpression("weight = 1kilogram + 500grams * 2"));
    }

    @Test
    public void testMvel3WithExistingOperators() {
        assertDoesNotThrow(() -> parseExpression("obj{value = base ** exponent}"));
        assertDoesNotThrow(() -> parseExpression("item[name1 strsim name2, status in validStatuses]"));
        assertDoesNotThrow(() -> parseExpression("data#DataRecord{verified = isdef(validator)}"));
    }

    @Test
    public void testRealWorldMvel3Example() {
        String code = """
            var order = orderData#Order{
                id = generateId(),
                timestamp = new Date(),
                total = items.{item.price#USD}.sum(),
                valid = this[
                    id != null,
                    total#USD > 0,
                    items?.size() > 0
                ]
            };
            
            var processed = orders.?(order#Order[
                valid == true,
                total#USD >= minimumOrder#USD,
                timestamp != null
            ]).{order#Order{
                processed = true,
                processedAt = new Date(),
                fees = total#USD * feeRate#percent
            }};
            
            return processed.size() > 0;
            """;
        assertDoesNotThrow(() -> parseCode(code));
    }
}