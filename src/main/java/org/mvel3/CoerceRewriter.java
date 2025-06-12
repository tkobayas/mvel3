package org.mvel3;

public class CoerceRewriter {
    public enum Primitive {
        CHAR, SHORT, INT, LONG, FLOAT, DOUBLE;
        
        public String toBoxedType() {
            switch (this) {
                case CHAR: return "Character";
                case SHORT: return "Short";
                case INT: return "Integer";
                case LONG: return "Long";
                case FLOAT: return "Float";
                case DOUBLE: return "Double";
                default: throw new IllegalArgumentException("Unknown primitive: " + this);
            }
        }
    }

    public static final Primitive[] INTEGER_PRIMITIVES = new Primitive[] {
        Primitive.CHAR,
        Primitive.SHORT,
        Primitive.INT,
        Primitive.LONG
    };

    public static final Primitive[] FLOAT_PRIMITIVES = new Primitive[] {
        Primitive.CHAR,
        Primitive.SHORT,
        Primitive.INT,
        Primitive.LONG,
        Primitive.FLOAT,
        Primitive.DOUBLE
    };
}