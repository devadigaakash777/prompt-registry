package io.github.devadigaakash777.promptregistry.core.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptContextTest {
    @Test
    void shouldCreatePromptContext() {
        PromptContext promptContext = new PromptContext(
                Map.of(
                        "customer", "Joe",
                        "question", "How can I reset my password?"
                )
        );

        assertEquals("Joe", promptContext.values().get("customer"));
        assertEquals(2, promptContext.values().size());
        assertEquals("How can I reset my password?", promptContext.values().get("question"));
    }

    @Test
    void shouldRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new PromptContext(null));
    }

    @Test
    void shouldCreateDefensiveCopyOfValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("customer", "Joe");
        PromptContext promptContext = new PromptContext(values);
        values.put("customer", "James");

        assertEquals("Joe", promptContext.values().get("customer"));
    }
}
