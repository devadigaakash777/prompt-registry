package io.github.devadigaakash777.promptregistry.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptVersionTest {

    @Test
    void shouldCreatePromptVersion() {
        PromptVersion promptVersion = new PromptVersion(
                1,
                "You are a helpful assistant.",
                PromptStatus.ACTIVE
        );

        assertEquals(1, promptVersion.version());
        assertEquals("You are a helpful assistant.", promptVersion.content());
        assertEquals(PromptStatus.ACTIVE, promptVersion.status());
    }

    @Test
    void shouldRejectVersionLessThanOrEqualToZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PromptVersion(
                        0,
                        "You are a helpful assistant.",
                        PromptStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldRejectBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> new PromptVersion(
                1,
                " ",
                PromptStatus.ACTIVE
        ));
    }

    @Test
    void shouldRejectNullStatus() {
        assertThrows(NullPointerException.class, () -> new PromptVersion(
                2,
                "You are a helpful assistant.",
                null
        ));
    }
}
