package io.github.devadigaakash777.promptregistry.core.repository.memory;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

class InMemoryPromptRepositoryTest {

    @Test
    void shouldFindExistingPromptByName() {
        Prompt prompt = new Prompt(
                "customer-support",
                List.of()
        );

        InMemoryPromptRepository inMemoryPromptRepository = new InMemoryPromptRepository(List.of(prompt));

        assertTrue(inMemoryPromptRepository
                .findByName("customer-support")
                .isPresent()
        );

        assertEquals(prompt, inMemoryPromptRepository
                .findByName("customer-support")
                .orElseThrow()
        );
    }

    @Test
    void shouldReturnEmptyWhenPromptDoesNotExist() {
        InMemoryPromptRepository inMemoryPromptRepository = new InMemoryPromptRepository(List.of());

        assertTrue(inMemoryPromptRepository.findByName("unknown-prompt").isEmpty());
    }

    @Test
    void shouldRejectDuplicatePromptNames() {
        Prompt firstPrompt = new Prompt(
                "customer-support",
                List.of()
        );

        Prompt secondPrompt = new Prompt(
                "customer-support",
                List.of()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new InMemoryPromptRepository(List.of(firstPrompt, secondPrompt))
        );
    }
}
