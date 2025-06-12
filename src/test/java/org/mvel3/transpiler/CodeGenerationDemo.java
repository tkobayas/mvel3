package org.mvel3.transpiler;

import org.junit.jupiter.api.Test;
import org.mvel3.*;
import org.mvel3.MVELCompiler;
import org.mvel3.MVELTranspiler;
import org.mvel3.grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.*;

public class CodeGenerationDemo {

    @Test
    public void demonstrateCodeGeneration() {
        // Show what Java code gets generated for the MVEL expression
        String expression = "foo.getName() + bar.getName()";
        
        System.out.println("MVEL Expression: " + expression);
        System.out.println("\nGenerated Java Expression:");
        
        try {
            // Parse the expression
            ANTLRInputStream input = new ANTLRInputStream(expression);
            Mvel3Lexer lexer = new Mvel3Lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Mvel3Parser parser = new Mvel3Parser(tokens);
            ParseTree tree = parser.expression();
            
            // Generate Java code
            MVELTranspiler transpiler = new MVELTranspiler();
            String javaCode = transpiler.transpile(tree);
            System.out.println(javaCode);
            
            // Show the complete evaluator class
            Map<String, Type<?>> types = new HashMap<>();
            types.put("foo", Type.type(Foo.class));
            types.put("bar", Type.type(Bar.class));
            
            Set<String> imports = Set.of(
                "java.util.Map",
                "org.mvel3.Foo",
                "org.mvel3.Bar"
            );
            
            TestCompiler testCompiler = new TestCompiler();
            String evaluatorClass = testCompiler.generateEvaluatorClass(
                "DemoEvaluator", 
                javaCode, 
                String.class, 
                imports, 
                types
            );
            
            System.out.println("\nGenerated Evaluator Class:");
            System.out.println(evaluatorClass);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Helper method to make generateEvaluatorClass accessible for demo
    private static class TestCompiler extends MVELCompiler {
        public String generateEvaluatorClass(String className, String expression, Class<?> returnType, 
                                           Set<String> imports, Map<String, Type<?>> types) {
            return super.generateEvaluatorClass(className, expression, returnType, imports, types);
        }
    }
}