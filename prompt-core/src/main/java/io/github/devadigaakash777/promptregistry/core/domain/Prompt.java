package io.github.devadigaakash777.promptregistry.core.domain;

import java.util.Objects;
import java.util.UUID;

public record Prompt(
        UUID id,
        String name
) {

    public Prompt {
        Objects.requireNonNull(id, "id must not be null");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
