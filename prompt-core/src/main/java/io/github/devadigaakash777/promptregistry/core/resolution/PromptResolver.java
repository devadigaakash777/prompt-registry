package io.github.devadigaakash777.promptregistry.core.resolution;

import io.github.devadigaakash777.promptregistry.core.domain.*;
import io.github.devadigaakash777.promptregistry.core.exception.InvalidPromptStateException;
import io.github.devadigaakash777.promptregistry.core.exception.NoActivePromptVersionException;

import java.util.List;
import java.util.Objects;

public final class PromptResolver {
    public ResolvedPrompt resolve(
            Prompt prompt,
            PromptContext context
    ) {
        Objects.requireNonNull(prompt, "prompt cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        List<PromptVersion> activeVersions = prompt.versions()
                .stream()
                .filter( version -> version.status() == PromptStatus.ACTIVE )
                .toList();

        if (activeVersions.isEmpty()) {
            throw new NoActivePromptVersionException(
                    "No active version found for prompt: " + prompt.name()
            );
        }

        if (activeVersions.size() > 1) {
            throw new InvalidPromptStateException(
                    "Multiple active versions found for prompt: "
                            + prompt.name()
            );
        }

        PromptVersion activeVersion = activeVersions.getFirst();

        return new ResolvedPrompt(
                prompt.name(),
                activeVersion.version(),
                activeVersion.content()
        );
    }
}
