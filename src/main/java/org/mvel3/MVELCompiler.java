package org.mvel3;

import org.mvel3.grammar.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.*;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * MVEL Compiler - Handles parsing, transpilation, and compilation of MVEL expressions
 * This class orchestrates the compilation process but delegates transpilation to MVELTranspiler
 */
public class MVELCompiler {

    private final MVELTranspiler transpiler;

    public MVELCompiler() {
        this.transpiler = new MVELTranspiler();
    }

    /**
     * Generate and compile a Map-based evaluator for MVEL expressions
     */
    public <R> Evaluator<Map<String, Object>, Void, R> generateMapEvaluator(
            String expression, 
            Class<R> returnType, 
            Set<String> imports, 
            Map<String, Type<?>> types) {
        
        try {
            // Parse MVEL expression
            ANTLRInputStream input = new ANTLRInputStream(expression);
            Mvel3Lexer lexer = new Mvel3Lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Mvel3Parser parser = new Mvel3Parser(tokens);
            
            // Parse as expression
            ParseTree tree = parser.expression();
            
            // Transpile to Java code
            String javaExpression = transpiler.transpile(tree);
            
            // Generate complete evaluator class
            String className = "GeneratorEvaluator__" + System.currentTimeMillis();
            String fullClassName = "org.mvel3." + className;
            String javaCode = generateEvaluatorClass(className, javaExpression, returnType, imports, types);
            
            // Compile and load the class
            Class<?> evaluatorClass = compileAndLoadClass(fullClassName, javaCode);
            
            // Create instance and return
            @SuppressWarnings("unchecked")
            Evaluator<Map<String, Object>, Void, R> evaluator = 
                (Evaluator<Map<String, Object>, Void, R>) evaluatorClass.getDeclaredConstructor().newInstance();
            
            return evaluator;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile MVEL expression: " + expression, e);
        }
    }
    
    /**
     * Generate Java code from MVEL parse tree using the transpiler
     */
    public String generateJavaCode(ParseTree tree) {
        return transpiler.transpile(tree);
    }
    
    /**
     * Transpile MVEL expression to Java code using EvaluatorBuilder configuration
     */
    public TranspiledResult transpile(EvaluatorBuilder.EvaluatorInfo<?, ?, ?> evaluatorInfo) {
        try {
            // Parse MVEL expression
            ANTLRInputStream input = new ANTLRInputStream(evaluatorInfo.expression());
            Mvel3Lexer lexer = new Mvel3Lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Mvel3Parser parser = new Mvel3Parser(tokens);
            
            // Parse as expression or statement based on content
            ParseTree tree;
            if (evaluatorInfo.expression().trim().endsWith(";") || 
                evaluatorInfo.expression().contains("if(") || 
                evaluatorInfo.expression().contains("for(") ||
                evaluatorInfo.expression().contains("while(") ||
                evaluatorInfo.expression().contains("switch(") ||
                evaluatorInfo.expression().contains("modify(")) {
                tree = parser.start_();
            } else {
                tree = parser.expression();
            }
            
            // Configure transpiler with type information
            transpiler.setTypeInfo(evaluatorInfo.allVars());
            
            // Transpile to Java code
            String javaCode = transpiler.transpile(tree);
            
            // Extract variable names that are used
            Collection<String> inputs = evaluatorInfo.allVars().keySet();
            
            return new TranspiledResult(javaCode, inputs);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to transpile MVEL expression: " + evaluatorInfo.expression(), e);
        }
    }
    
    protected String generateEvaluatorClass(String className, String expression, Class<?> returnType, 
                                        Set<String> imports, Map<String, Type<?>> types) {
        StringBuilder code = new StringBuilder();
        
        // Package declaration
        code.append("package org.mvel3;\n\n");
        
        // Imports
        for (String importStr : imports) {
            code.append("import ").append(importStr).append(";\n");
        }
        code.append("\n");
        
        // Class declaration
        code.append("public class ").append(className)
            .append(" implements org.mvel3.Evaluator<java.util.Map<String, Object>, java.lang.Void, ")
            .append(returnType.getName()).append("> {\n\n");
        
        // Eval method
        code.append("    public ").append(returnType.getName())
            .append(" eval(java.util.Map<String, Object> __context) {\n");
        
        // Variable declarations for typed variables
        for (Map.Entry<String, Type<?>> entry : types.entrySet()) {
            String varName = entry.getKey();
            String typeName = entry.getValue().getType().getName();
            code.append("        ").append(typeName).append(" ").append(varName)
                .append(" = ((").append(typeName).append(") __context.get(\"").append(varName).append("\"));\n");
        }
        
        // Return statement
        code.append("        return ").append(expression).append(";\n");
        code.append("    }\n");
        code.append("}\n");
        
        return code.toString();
    }
    
    private Class<?> compileAndLoadClass(String className, String javaCode) throws Exception {
        // Create in-memory Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("No Java compiler available. Make sure you're running with JDK, not JRE.");
        }
        
        // Create file manager
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        InMemoryJavaFileManager inMemoryFileManager = new InMemoryJavaFileManager(fileManager);
        
        // Create source file object
        JavaSourceFromString sourceFile = new JavaSourceFromString(className, javaCode);
        
        // Compile
        JavaCompiler.CompilationTask task = compiler.getTask(
            null, inMemoryFileManager, null, null, null, Arrays.asList(sourceFile));
        
        boolean success = task.call();
        if (!success) {
            throw new RuntimeException("Compilation failed for generated code:\n" + javaCode);
        }
        
        // Load the compiled class
        ClassLoader classLoader = inMemoryFileManager.getClassLoader();
        return classLoader.loadClass(className);
    }
    
    // Inner classes for in-memory compilation
    private static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;
        
        public JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
    
    private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, ByteArrayOutputStream> classBytes = new HashMap<>();
        
        public InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }
        
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, 
                                                 JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new ClassFileObject(className, classBytes);
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
        
        public ClassLoader getClassLoader() {
            return new ByteArrayClassLoader(classBytes);
        }
    }
    
    private static class ClassFileObject extends SimpleJavaFileObject {
        private final Map<String, ByteArrayOutputStream> classBytes;
        private final String className;
        
        public ClassFileObject(String className, Map<String, ByteArrayOutputStream> classBytes) {
            super(URI.create("mem:///" + className + ".class"), Kind.CLASS);
            this.classBytes = classBytes;
            this.className = className;
        }
        
        @Override
        public OutputStream openOutputStream() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            classBytes.put(className, stream);
            return stream;
        }
    }
    
    private static class ByteArrayClassLoader extends ClassLoader {
        private final Map<String, ByteArrayOutputStream> classBytes;
        
        public ByteArrayClassLoader(Map<String, ByteArrayOutputStream> classBytes) {
            this.classBytes = classBytes;
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ByteArrayOutputStream byteStream = classBytes.get(name);
            if (byteStream == null) {
                throw new ClassNotFoundException(name);
            }
            
            byte[] bytes = byteStream.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}