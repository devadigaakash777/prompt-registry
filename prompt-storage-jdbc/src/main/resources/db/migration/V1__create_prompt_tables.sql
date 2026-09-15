CREATE TABLE prompts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE prompt_versions (
    id UUID PRIMARY KEY,
    prompt_id UUID NOT NULL,
    version INTEGER NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_prompt_versions_prompt
        FOREIGN KEY (prompt_id)
        REFERENCES prompts(id),

    CONSTRAINT uk_prompt_versions_prompt_version
        UNIQUE (prompt_id, version),

    CONSTRAINT chk_prompt_versions_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'))
);