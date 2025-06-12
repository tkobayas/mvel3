package org.mvel3;

import java.util.Collection;
import java.util.Collections;

public class TranspiledResult {
    private String methodBody;
    private Collection<String> inputs;

    public TranspiledResult(String methodBody) {
        this.methodBody = methodBody;
        this.inputs = Collections.emptyList();
    }

    public TranspiledResult(String methodBody, Collection<String> inputs) {
        this.methodBody = methodBody;
        this.inputs = inputs != null ? inputs : Collections.emptyList();
    }

    public String methodBodyAsString() {
        return methodBody;
    }

    public Collection<String> getInputs() {
        return inputs;
    }
}