package io.github.devadigaakash777.promptregistry.core;

import io.github.devadigaakash777.promptregistry.core.domain.*;
import io.github.devadigaakash777.promptregistry.core.exception.NoActivePromptVersionException;
import io.github.devadigaakash777.promptregistry.core.exception.InvalidPromptNameException;
import io.github.devadigaakash777.promptregistry.core.exception.InvalidPromptStateException;
import io.github.devadigaakash777.promptregistry.core.exception.PromptNotFoundException;
import io.github.devadigaakash777.promptregistry.core.registry.PromptRegistry;
import io.github.devadigaakash777.promptregistry.core.repository.memory.InMemoryPromptRepository;
import io.github.devadigaakash777.promptregistry.core.resolution.PromptResolver;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class InMemoryPromptRegistryTest {
    @Test
    void shouldResolveExistingPromptWithActiveVersion() {
        Prompt prompts = new Prompt(
                "customer-support",
                List.of(
                        new PromptVersion(
                                1,
                                "Old Prompt",
                                PromptStatus.ARCHIVED
                        ),
                        new PromptVersion(
                                2,
                                "Active Prompt",
                                PromptStatus.ACTIVE
                        )
                )
        );

        PromptRegistry promptRegistry = createRegistry(prompts);

        ResolvedPrompt resolvedPrompt = promptRegistry.resolve(
                "customer-support",
                new PromptContext(Map.of())
        );

        assertEquals("customer-support", resolvedPrompt.name());
        assertEquals(2, resolvedPrompt.version());
        assertEquals("Active Prompt", resolvedPrompt.content());
    }

    @Test
    void shouldThrowExceptionForMissingPrompt() {
        PromptRegistry promptRegistry = createRegistry();

        assertThrows(
                PromptNotFoundException.class,
                () -> promptRegistry.resolve(
                        "unknown-prompt",
                        new PromptContext(Map.of())
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenNoActiveVersionExists() {
        Prompt prompt = new Prompt(
                "customer-support",
                List.of(
                        new PromptVersion(
                                1,
                                "Draft prompt",
                                PromptStatus.DRAFT
                        ),
                        new PromptVersion(
                                2,
                                "Archived prompt",
                                PromptStatus.ARCHIVED
                        )
                )
        );

        PromptRegistry registry = createRegistry(prompt);

        assertThrows(
                NoActivePromptVersionException.class,
                () -> registry.resolve(
                        "customer-support",
                        new PromptContext(Map.of())
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenMultipleActiveVersionsExist() {
        Prompt prompt = new Prompt(
                "customer-support",
                List.of(
                        new PromptVersion(
                                1,
                                "First active prompt",
                                PromptStatus.ACTIVE
                        ),
                        new PromptVersion(
                                2,
                                "Second active prompt",
                                PromptStatus.ACTIVE
                        )
                )
        );

        PromptRegistry registry = createRegistry(prompt);

        assertThrows(
                InvalidPromptStateException.class,
                () -> registry.resolve(
                        "customer-support",
                        new PromptContext(Map.of())
                )
        );
    }

    @Test
    void shouldThrowExceptionForInvalidPromptName() {
        PromptRegistry registry = createRegistry();

        assertThrows(
                InvalidPromptNameException.class,
                () -> registry.resolve(
                        " ",
                        new PromptContext(Map.of())
                )
        );
    }

    private PromptRegistry createRegistry(Prompt... prompts) {
        return new PromptRegistry(
                new InMemoryPromptRepository(List.of(prompts)),
                new PromptResolver()
        );
    }
}
