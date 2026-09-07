package io.github.devadigaakash777.promptregistry.core.domain;

import java.util.Objects;

public final class PromptVersion {
    private final int version;
    private final String content;
    private final PromptStatus status;

    public PromptVersion(int version, String content, PromptStatus status){
        if( version <= 0 ){
            throw new IllegalArgumentException("version must be greater than zero");
        }
        if( content == null || content.isBlank() ){
            throw new IllegalArgumentException("content must not be blank");
        }
        this.version = version;
        this.content = content;
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public int version(){
        return version;
    }

    public String content(){
        return content;
    }

    public PromptStatus status(){
        return status;
    }
}
