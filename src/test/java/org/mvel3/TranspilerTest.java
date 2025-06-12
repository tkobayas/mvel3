package org.mvel3;

import org.mvel3.EvaluatorBuilder.ContextInfoBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public interface TranspilerTest {

    default void test(Consumer<EvaluatorBuilder<Map, Void,Object>> contextUpdater,
                                        String inputExpression,
                                        String expectedResult,
                                        Consumer<TranspiledResult> resultAssert) {
        EvaluatorBuilder<Map, Void, Object> builder = new EvaluatorBuilder<>();
        builder.setExpression(inputExpression);
        builder.addImport(java.util.List.class.getCanonicalName());
        builder.addImport(java.util.ArrayList.class.getCanonicalName());
        builder.addImport(java.util.HashMap.class.getCanonicalName());
        builder.addImport(java.util.Map.class.getCanonicalName());
        builder.addImport(BigDecimal.class.getCanonicalName());
        builder.addImport(BigInteger.class.getCanonicalName());
        builder.addImport(Address.class.getCanonicalName());
        builder.addImport(Person.class.getCanonicalName());
        builder.addImport(Gender.class.getCanonicalName());

        builder.setVariableInfo(ContextInfoBuilder.create(Type.type(Map.class)));
        builder.setOutType(Type.type(Void.class));

        contextUpdater.accept(builder);

        TranspiledResult compiled = new MVELCompiler().transpile(builder.build());

        verifyBodyWithBetterDiff(expectedResult, compiled.methodBodyAsString());
        resultAssert.accept(compiled);
    }

    default void verifyBodyWithBetterDiff(Object expected, Object actual) {
        try {
            assertEquals(expected.toString().replaceAll("\\s+", " ").trim(), 
                        actual.toString().replaceAll("\\s+", " ").trim());
        } catch (AssertionError e) {
            assertEquals(expected, actual);
        }
    }

    default void test(String inputExpression,
                      String expectedResult,
                      Consumer<TranspiledResult> resultAssert) {
        test(id -> {
        }, inputExpression, expectedResult, resultAssert);
    }

    default <K,R> void test(Consumer<EvaluatorBuilder<Map, Void, Object>> testFunction,
                      String inputExpression,
                      String expectedResult) {
        test(testFunction, inputExpression, expectedResult, t -> {
        });
    }

    default void test(String inputExpression,
                      String expectedResult) {
        test(d -> {
        }, inputExpression, expectedResult, t -> {
        });
    }

    default Collection<String> allUsedBindings(TranspiledResult result) {
        return new ArrayList<>(result.getInputs());
    }
}