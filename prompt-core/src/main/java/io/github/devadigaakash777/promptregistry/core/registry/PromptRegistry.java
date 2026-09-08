package io.github.devadigaakash777.promptregistry.core.registry;

import io.github.devadigaakash777.promptregistry.core.domain.PromptContext;
import io.github.devadigaakash777.promptregistry.core.domain.ResolvedPrompt;
import io.github.devadigaakash777.promptregistry.core.exception.InvalidPromptNameException;
import io.github.devadigaakash777.promptregistry.core.exception.PromptNotFoundException;
import io.github.devadigaakash777.promptregistry.core.repository.PromptRepository;
import io.github.devadigaakash777.promptregistry.core.resolution.PromptResolver;

import java.util.Objects;

public final class PromptRegistry {
    private final PromptRepository promptRepository;
    private final PromptResolver promptResolver;

    public PromptRegistry(
            PromptRepository  promptRepository,
            PromptResolver promptResolver
    ) {
        this.promptRepository = Objects.requireNonNull(
                promptRepository,
                "promptRepository cannot be null"
        );
        this.promptResolver = Objects.requireNonNull(
                promptResolver,
                "promptResolver cannot be null"
        );
    }

    public ResolvedPrompt resolve(
            String name,
            PromptContext promptContext
    ) {
        validateName(name);

        Objects.requireNonNull(promptContext, "promptContext cannot be null");

        return promptRepository.findByName( name )
                .map(prompt -> promptResolver.resolve( prompt, promptContext ))
                .orElseThrow( () -> new PromptNotFoundException(
                        "Prompt not found: " + name
                ));
    }

    private void validateName( String name ) {
        if( name == null || name.isBlank() ) {
            throw new InvalidPromptNameException(
                    "Prompt name cannot be blank"
            );
        }
    }
}
