package io.github.devadigaakash777.promptregistry.core.repository.memory;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;
import io.github.devadigaakash777.promptregistry.core.repository.PromptRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPromptRepository implements PromptRepository {

    private final Map<String, Prompt> prompts;

    public InMemoryPromptRepository(Collection<Prompt> prompts) {
        Objects.requireNonNull(prompts, "prompts must not be null");

        this.prompts = new ConcurrentHashMap<>();

        for (Prompt prompt : prompts) {
            Objects.requireNonNull(prompt, "prompt must not be null");

            Prompt existingPrompt = this.prompts.putIfAbsent(prompt.name(), prompt);

            if( existingPrompt != null) {
                throw new IllegalArgumentException("Duplicate prompt name: " + prompt.name());
            }
        }
    }

    @Override
    public Optional<Prompt> findByName(String name) {
        return Optional.ofNullable(prompts.get(name));
    }
}
