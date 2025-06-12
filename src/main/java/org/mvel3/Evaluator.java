package org.mvel3;

public interface Evaluator<T, K, R> {
    default R eval(T t) {
        throw new RuntimeException("Not Implemented");
    }

    default R eval(T t, K k) {
        throw new RuntimeException("Not Implemented");
    }

    default R evalRoot(K k) {
        throw new RuntimeException("Not Implemented");

    }
}
