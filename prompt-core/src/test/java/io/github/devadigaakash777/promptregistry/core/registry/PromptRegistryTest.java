package io.github.devadigaakash777.promptregistry.core.registry;

import io.github.devadigaakash777.promptregistry.core.domain.*;

import io.github.devadigaakash777.promptregistry.core.exception.InvalidPromptNameException;
import io.github.devadigaakash777.promptregistry.core.exception.PromptNotFoundException;
import io.github.devadigaakash777.promptregistry.core.repository.PromptRepository;
import io.github.devadigaakash777.promptregistry.core.resolution.PromptResolver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class PromptRegistryTest {

    @Test
    void shouldResolveExistingPrompt() {
        Prompt prompt = new Prompt(
                "customer-support",
                List.of(
                        new PromptVersion(
                                1,
                                "You are a helpful assistant.",
                                PromptStatus.ACTIVE
                        ),
                        new PromptVersion(
                                2,
                                "Old content",
                                PromptStatus.ARCHIVED
                        )
                )
        );

        PromptRepository promptRepository = name -> Optional.of(prompt);

        PromptRegistry promptRegistry = new PromptRegistry(promptRepository, new PromptResolver());

        ResolvedPrompt resolvedPrompt = promptRegistry.resolve(
                "customer-support",
                new PromptContext(Map.of())
        );

        assertEquals("customer-support", resolvedPrompt.name());
        assertEquals(1, resolvedPrompt.version());
        assertEquals("You are a helpful assistant.", resolvedPrompt.content());
    }

    @Test
    void shouldThrowExceptionForBlankPromptName() {
        PromptRepository promptRepository = name -> Optional.empty();

        PromptRegistry promptRegistry = new PromptRegistry(
                promptRepository, new PromptResolver()
        );

        assertThrows(
                InvalidPromptNameException.class,
                () -> promptRegistry.resolve(
                        " ",
                        new PromptContext(Map.of())
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenPromptDoesNotExist() {
        PromptRepository promptRepository = name -> Optional.empty();

        PromptRegistry promptRegistry = new PromptRegistry(
                promptRepository, new PromptResolver()
        );

        assertThrows(
                PromptNotFoundException.class,
                () -> promptRegistry.resolve(
                        "unknown-prompt",
                        new PromptContext(Map.of())
                )
        );
    }

    @Test
    void shouldRejectNullContext() {
        PromptRepository promptRepository = name -> Optional.empty();

        PromptRegistry promptRegistry = new PromptRegistry(
                promptRepository,
                new PromptResolver()
        );

        assertThrows(
                NullPointerException.class,
                () -> promptRegistry.resolve("customer-support", null)
        );
    }
}
