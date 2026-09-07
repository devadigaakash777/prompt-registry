package io.github.devadigaakash777.promptregistry.core.resolution;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;
import io.github.devadigaakash777.promptregistry.core.domain.PromptContext;
import io.github.devadigaakash777.promptregistry.core.domain.PromptStatus;
import io.github.devadigaakash777.promptregistry.core.domain.PromptVersion;
import io.github.devadigaakash777.promptregistry.core.domain.ResolvedPrompt;
import io.github.devadigaakash777.promptregistry.core.exception.InvalidPromptStateException;
import io.github.devadigaakash777.promptregistry.core.exception.NoActivePromptVersionException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptResolverTest {

    private final PromptResolver resolver = new PromptResolver();

    @Test
    void shouldResolveSingleActiveVersion() {
        Prompt prompt = new Prompt(
                "customer-support",
                List.of(
                        new PromptVersion(
                                1,
                                "Old prompt",
                                PromptStatus.ARCHIVED
                        ),
                        new PromptVersion(
                                2,
                                "Active prompt",
                                PromptStatus.ACTIVE
                        )
                )
        );

        PromptContext context = new PromptContext(Map.of());

        ResolvedPrompt resolvedPrompt =
                resolver.resolve(prompt, context);

        assertEquals("customer-support", resolvedPrompt.name());
        assertEquals(2, resolvedPrompt.version());
        assertEquals("Active prompt", resolvedPrompt.content());
    }

    @Test
    void shouldThrowExceptionWhenNoActiveVersionExists() {
        Prompt prompt = new Prompt(
                "customer-support",
                List.of(
                        new PromptVersion(
                                1,
                                "Archived prompt",
                                PromptStatus.ARCHIVED
                        ),
                        new PromptVersion(
                                2,
                                "Draft prompt",
                                PromptStatus.DRAFT
                        )
                )
        );

        assertThrows(
                NoActivePromptVersionException.class,
                () -> resolver.resolve(
                        prompt,
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

        assertThrows(
                InvalidPromptStateException.class,
                () -> resolver.resolve(
                        prompt,
                        new PromptContext(Map.of())
                )
        );
    }
}