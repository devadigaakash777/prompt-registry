package io.github.devadigaakash777.promptregistry.core.management;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;
import io.github.devadigaakash777.promptregistry.core.domain.PromptVersion;

public interface PromptManagement {

    Prompt create(String name, String content);

    PromptVersion createVersion(String name, String content);

    void activate(String name, int version);

    void archive(String name, int version);
}
