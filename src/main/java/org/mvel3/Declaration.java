package org.mvel3;

import java.util.Map;
import java.util.stream.Collectors;

public class Declaration<T> {
    private final String name;

    private final Type<T> type;

    public static <Y> Declaration of(String name, Class<Y> clazz) {
        return new Declaration(name, clazz);
    }

    public static <Y> Declaration of(String name, Type<Y> type) {
        return new Declaration(name, type);
    }

    public static Declaration[] from(Map<String, Type<?>> types) {
        Declaration[]  declrs = types.entrySet().stream().map(e -> Declaration.of(e.getKey(), e.getValue()))
                                     .collect(Collectors.toList()).toArray(new Declaration[0]);
        return declrs;
    }

    public Declaration(String name, Type<T> type) {
        this.name = name;
        this.type = type;
    }

    public static <T> Declaration of(String name, Class<T> clazz, String generics) {
        return new Declaration(name, clazz, generics);
    }

    public Declaration(String name, Class<T> clazz) {
        this(name, clazz,null);
    }

    public Declaration(String name, Class<T> clazz, String generics) {
        this.name = name;
        this.type = Type.type(clazz, generics);
    }

    public String name() {
        return name;
    }
    public Type<T> type() { return type; }

    @Deprecated
    public Class<T> clazz() {
        return type.getClazz();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Declaration that = (Declaration) o;

        if (!name.equals(that.name)) {
            return false;
        }
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Declaration{" +
               "name='" + name + '\'' +
               ", type=" + type +
               '}';
    }

}