package io.github.devadigaakash777.promptregistry.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolvedPromptTest {

    @Test
    void shouldCreateResolvedPrompt() {
        ResolvedPrompt resolvedPrompt = new ResolvedPrompt(
                "customer-support",
                2,
                "You are an expert customer support assistant."
        );

        assertEquals(
                "customer-support",
                resolvedPrompt.name()
        );

        assertEquals(
                2,
                resolvedPrompt.version()
        );

        assertEquals(
                "You are an expert customer support assistant.",
                resolvedPrompt.content()
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResolvedPrompt(
                        " ",
                        1,
                        "Prompt content"
                )
        );
    }

    @Test
    void shouldRejectInvalidVersion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResolvedPrompt(
                        "customer-support",
                        0,
                        "Prompt content"
                )
        );
    }

    @Test
    void shouldRejectNullContent() {
        assertThrows(
                NullPointerException.class,
                () -> new ResolvedPrompt(
                        "customer-support",
                        1,
                        null
                )
        );
    }
}