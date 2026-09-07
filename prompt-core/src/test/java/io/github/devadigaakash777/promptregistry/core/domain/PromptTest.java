package io.github.devadigaakash777.promptregistry.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;


class PromptTest {

    @Test
    void shouldCreatePrompt() {
        PromptVersion promptVersion = new PromptVersion(
                1,
                "You are a helpful assistant.",
                PromptStatus.ACTIVE
        );

        Prompt prompt = new Prompt(
                "customer-support",
                List.of(promptVersion)
        );

        assertEquals("customer-support", prompt.name());
        assertEquals(List.of(promptVersion), prompt.versions());
    }

    @Test
    void shouldRejectBlankName() {
        List<PromptVersion> promptVersions = List.of();
        assertThrows(
                IllegalArgumentException.class,
                () -> new Prompt(" ", promptVersions)
        );
    }

    @Test
    void shouldRejectNullVersions() {
        assertThrows(
                NullPointerException.class,
                () -> new Prompt("customer-support", null)
        );
    }

    @Test
    void shouldAllowPromptWithNoVersions() {
        List<PromptVersion> promptVersions = List.of();
        Prompt prompt = new Prompt("customer-support", promptVersions);
        assertEquals(promptVersions, prompt.versions());
    }
}
