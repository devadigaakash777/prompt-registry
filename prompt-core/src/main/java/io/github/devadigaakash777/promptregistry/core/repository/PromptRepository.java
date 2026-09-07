package io.github.devadigaakash777.promptregistry.core.repository;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;

import java.util.Optional;

public interface PromptRepository {
    Optional<Prompt> findByName(String name);
}
