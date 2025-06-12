package org.mvel3;

import java.util.Map;
import java.util.Set;

public class MVEL {
    
    public <R> Evaluator<Map<String, Object>, Void, R> compileMapEvaluator(
            String expression, 
            Class<R> returnType, 
            Set<String> imports, 
            Map<String, Type<?>> types) {
        
        MVELCompiler compiler = new MVELCompiler();
        return compiler.generateMapEvaluator(expression, returnType, imports, types);
    }
}