package org.mvel3.compiler;

import org.junit.jupiter.api.Test;
import org.mvel3.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MVELCompilerTest {

    @Test
    public void testMapEvaluator() {
        Map<String, Type<?>> types = new HashMap<>();
        types.put("foo", Type.type(Foo.class));
        types.put("bar", Type.type(Bar.class));

        Map<String, Object> vars = new HashMap<>();
        Foo foo = new Foo();
        foo.setName("xxx");
        vars.put("foo", foo);

        Bar bar = new Bar();
        bar.setName("yyy");
        vars.put("bar", bar);

        MVEL mvel = new MVEL();
        Evaluator<Map<String, Object>, Void, String> evaluator = mvel.compileMapEvaluator("foo.getName() + bar.getName()", String.class, getImports(), types);
        assertEquals("xxxyyy", evaluator.eval(vars));
    }

    public static Set<String> getImports() {
        Set<String> imports = new HashSet<>();
        imports.add("java.util.List");
        imports.add("java.util.ArrayList");
        imports.add("java.util.HashMap");
        imports.add("java.util.Map");
        imports.add("java.math.BigDecimal");
        imports.add(Address.class.getCanonicalName());
        imports.add(Foo.class.getCanonicalName());
        imports.add(Bar.class.getCanonicalName());
        return imports;
    }
}