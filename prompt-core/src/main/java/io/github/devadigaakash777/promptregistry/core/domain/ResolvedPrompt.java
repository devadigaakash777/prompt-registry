package io.github.devadigaakash777.promptregistry.core.domain;

import java.util.Objects;

public record ResolvedPrompt(
        String name,
        int version,
        String content
) {
    public ResolvedPrompt {
        if ( name == null || name.isBlank() ) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if ( version <= 0 ) {
            throw new IllegalArgumentException("version must not be less than 1");
        }

        Objects.requireNonNull( content, "content must not be null");
    }
}
