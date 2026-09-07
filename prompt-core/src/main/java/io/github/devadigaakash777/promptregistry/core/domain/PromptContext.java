package io.github.devadigaakash777.promptregistry.core.domain;

import java.util.Map;
import java.util.Objects;

public final class PromptContext {
    private final Map<String, Object> values;
    public PromptContext(Map<String, Object> values) {
        Objects.requireNonNull(values, "values must not be null");
        this.values = Map.copyOf(values);
    }

    public Map<String, Object> values() {
        return values;
    }
}
