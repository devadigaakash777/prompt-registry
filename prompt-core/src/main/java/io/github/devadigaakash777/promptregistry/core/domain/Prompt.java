package io.github.devadigaakash777.promptregistry.core.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Prompt {
    private final String name;
    private final List<PromptVersion> versions;

    public Prompt(final String name, final List<PromptVersion> versions) {
        if( name == null || name.isBlank() ){
            throw new IllegalArgumentException("name must not be blank");
        }

        Objects.requireNonNull(versions, "versions must not be null");

        this.name = name;

        this.versions = List.copyOf(versions);
        validateUniqueVersions(this.versions);
    }

    public String name(){
        return this.name;
    }

    public List<PromptVersion> versions(){
        return this.versions;
    }

    private void validateUniqueVersions(List<PromptVersion> versions) {
        Set<Integer> uniqueVersions = new HashSet<>();

        for(PromptVersion version : versions) {
            Objects.requireNonNull(
                    version,
                    "version must not be null"
            );

            if(!uniqueVersions.add(version.version())) {
                throw new IllegalArgumentException(
                        "Duplicate version found for version " + version.version()
                );
            }
        }
    }
}
