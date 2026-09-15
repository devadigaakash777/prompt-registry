# prompt-storage-jdbc

`prompt-storage-jdbc` is the PostgreSQL/JDBC persistence module of the
Prompt Registry framework.

It provides a JDBC implementation of the core `PromptRepository`
abstraction, allowing prompts and their versions to be loaded from
PostgreSQL.

The module is an infrastructure integration. Database-specific concerns
such as JDBC, PostgreSQL, `DataSource`, SQL, and Flyway migrations
remain outside `prompt-core`.

## Responsibilities

The current `prompt-storage-jdbc` module is responsible for:

-   Providing `JdbcPromptRepository`
-   Loading prompts from PostgreSQL through JDBC
-   Mapping relational data to the core `Prompt` and `PromptVersion`
    domain objects
-   Managing JDBC resources safely
-   Providing the PostgreSQL database schema migration
-   Verifying repository behavior with PostgreSQL Testcontainers

The module is **not** responsible for:

-   Prompt resolution rules
-   Selecting the active version
-   Prompt management operations such as create, activate, or archive
-   Spring integration
-   Spring Boot auto-configuration
-   Redis or caching
-   Spring AI or LLM invocation
-   Template substitution
-   Canary or rollout strategies
-   Observability
-   Authorization

Those concerns belong to the core or to later integration modules.

## Architecture

The current runtime relationship is:

``` text
Java Application
       |
       | resolve(name, context)
       v
PromptRegistry
       |
       | depends on PromptRepository
       v
PromptRepository
       ^
       |
       | implements
       |
JdbcPromptRepository
       |
       | JDBC / DataSource
       v
PostgreSQL
```

The important dependency direction is:

``` text
prompt-storage-jdbc
        |
        v
   prompt-core
```

`JdbcPromptRepository` depends on the `PromptRepository` abstraction and
the core domain model. The core does not depend on JDBC or PostgreSQL.

## Module Structure

``` text
prompt-storage-jdbc
├── src
│   ├── main
│   │   ├── java
│   │   │   └── io.github.devadigaakash777.promptregistry.storage.jdbc
│   │   │       └── JdbcPromptRepository
│   │   │
│   │   └── resources
│   │       └── db
│   │           └── migration
│   │               └── V1__create_prompt_tables.sql
│   │
│   └── test
│       └── java
│           └── io.github.devadigaakash777.promptregistry.storage.jdbc
│               └── JdbcPromptRepositoryIntegrationTest
│
└── pom.xml
```

## Dependencies

The module currently depends on:

-   `prompt-core`
-   PostgreSQL JDBC Driver
-   Flyway Core
-   Flyway PostgreSQL database support

For integration testing it uses:

-   Testcontainers JUnit Jupiter
-   Testcontainers PostgreSQL
-   JUnit 5

The production implementation is based on standard JDBC APIs and
`javax.sql.DataSource`.

## JdbcPromptRepository

`JdbcPromptRepository` implements the core repository abstraction:

``` java
public final class JdbcPromptRepository
        implements PromptRepository {
```

Its constructor requires a `DataSource`:

``` java
public JdbcPromptRepository(final DataSource dataSource) {
    this.dataSource = Objects.requireNonNull(
            dataSource,
            "dataSource must not be null"
    );
}
```

The repository exposes the core operation:

``` java
Optional<Prompt> findByName(String name);
```

This keeps the application-facing repository contract independent of the
PostgreSQL implementation.

## Database Query

The repository loads a prompt and all of its versions using a single SQL
query:

``` sql
SELECT
    p.id AS prompt_id,
    p.name AS prompt_name,
    pv.id AS version_id,
    pv.version AS version,
    pv.content AS content,
    pv.status AS status
FROM prompts p
LEFT JOIN prompt_versions pv
    ON pv.prompt_id = p.id
WHERE p.name = ?
ORDER BY pv.version
```

The query uses a `LEFT JOIN`.

This is important because a prompt can exist without any versions. In
that case:

``` text
prompts
    |
    +---- prompt exists
             |
             +---- no prompt_versions rows
```

The repository returns:

``` java
Optional<Prompt>
```

with an empty version list rather than treating the prompt as missing.

For a prompt with versions, the rows are ordered by version number
before being mapped into the core domain model.

## JDBC Resource Management

The repository uses try-with-resources for:

-   `Connection`
-   `PreparedStatement`
-   `ResultSet`

This ensures JDBC resources are closed even when an operation fails.

The query parameter is supplied through `PreparedStatement`:

``` java
preparedStatement.setString(1, name);
```

This avoids constructing SQL by concatenating the supplied prompt name
into the query.

## Mapping to Core Domain

The JDBC module does not create JDBC-specific domain objects.

Database rows are mapped directly into the core types:

``` text
PostgreSQL
    |
    v
ResultSet
    |
    v
PromptVersion
    |
    v
Prompt
```

For each version row:

``` java
new PromptVersion(
        resultSet.getInt("version"),
        resultSet.getString("content"),
        PromptStatus.valueOf(
                resultSet.getString("status")
        )
);
```

The database status is converted into the core `PromptStatus` enum.

The repository then constructs:

``` java
new Prompt(
        promptName,
        promptVersions
);
```

This preserves the core domain model as the common representation
regardless of whether the data came from memory or PostgreSQL.

## Error Handling

JDBC failures are translated into an unchecked exception:

``` java
throw new IllegalStateException(
        "Failed to load prompt: " + name,
        sqlException
);
```

The original `SQLException` is retained as the cause.

This prevents JDBC-specific checked exceptions from leaking through the
core `PromptRepository` abstraction.

The repository also validates required constructor and method arguments.

A null `DataSource` is rejected during construction, and a null prompt
name is rejected before executing the database query.

## Database Schema

The PostgreSQL schema contains two tables:

``` text
prompts
   |
   | 1
   |
   | contains
   |
   | many
   v
prompt_versions
```

### `prompts`

  Column         Type           Constraint
  -------------- -------------- ------------------
  `id`           UUID           Primary Key
  `name`         VARCHAR(255)   NOT NULL, UNIQUE
  `created_at`   TIMESTAMP      NOT NULL
  `updated_at`   TIMESTAMP      NOT NULL

### `prompt_versions`

  Column         Type          Constraint
  -------------- ------------- -----------------------
  `id`           UUID          Primary Key
  `prompt_id`    UUID          NOT NULL, Foreign Key
  `version`      INTEGER       NOT NULL
  `content`      TEXT          NOT NULL
  `status`       VARCHAR(50)   NOT NULL
  `created_at`   TIMESTAMP     NOT NULL
  `updated_at`   TIMESTAMP     NOT NULL

Additional constraints:

``` text
FOREIGN KEY (prompt_id)
    REFERENCES prompts(id)

UNIQUE (prompt_id, version)

CHECK (
    status IN (
        'DRAFT',
        'ACTIVE',
        'ARCHIVED'
    )
)
```

The unique constraint ensures that the same prompt cannot contain the
same version number more than once.

The foreign key ensures that every prompt version belongs to an existing
prompt.

The status check constraint keeps persisted status values aligned with
the core `PromptStatus` values.

## Entity Relationship Diagram

![Prompt Storage JDBC ER
Diagram](docs/images/prompt-storage-jdbc/er-diagram.png)

The ER diagram represents the relationship between `prompts` and
`prompt_versions`.

A prompt can contain multiple versions, while each prompt version
belongs to exactly one prompt.

The database constraint:

``` sql
UNIQUE (prompt_id, version)
```

ensures version numbers are unique within a prompt.

## Class Diagram

![Prompt Storage JDBC Class
Diagram](docs/images/prompt-storage-jdbc/class-diagram.png)

The class diagram shows the separation between:

-   `PromptRegistry`
-   Core `PromptRepository`
-   `JdbcPromptRepository`
-   `DataSource`
-   PostgreSQL

The important dependency is that `PromptRegistry` depends on the core
`PromptRepository` interface, while `JdbcPromptRepository` provides the
JDBC implementation.

This follows dependency inversion:

``` text
PromptRegistry
      |
      v
PromptRepository
      ^
      |
JdbcPromptRepository
```

The core therefore remains independent of PostgreSQL.

## Sequence Diagram

![Prompt Storage JDBC Sequence
Diagram](docs/images/prompt-storage-jdbc/sequence-diagram.png)

The current repository interaction is:

``` text
Java Application
       |
       | resolve(name, context)
       v
PromptRegistry
       |
       | findByName(name)
       v
JdbcPromptRepository
       |
       | SELECT prompt and versions
       v
PostgreSQL
       |
       | ResultSet
       v
JdbcPromptRepository
       |
       | Map rows to Prompt
       v
PromptRegistry
       |
       | PromptResolver.resolve(prompt, context)
       v
ResolvedPrompt
       |
       v
Java Application
```

The JDBC repository is responsible only for persistence and mapping.

Version selection remains the responsibility of the core
`PromptResolver`.

## Flyway Migration

The initial database migration is:

``` text
src/main/resources/db/migration/
└── V1__create_prompt_tables.sql
```

`V1__create_prompt_tables.sql` creates:

1.  `prompts`
2.  `prompt_versions`
3.  The foreign-key relationship
4.  The unique prompt-version constraint
5.  The allowed-status check constraint

The migration does **not** silently create a database schema outside the
defined migration.

Flyway is responsible for tracking and applying the migration.

The module does not embed schema creation into `JdbcPromptRepository`.

## Running Migrations

Flyway can be configured against the application's `DataSource`.

For example:

``` java
Flyway.configure()
        .dataSource(dataSource)
        .load()
        .migrate();
```

In production, migration execution should be treated as part of the
application's deployment/database lifecycle rather than being hidden
inside repository construction or query execution.

## Repository Usage

An application can construct the JDBC repository with a configured
`DataSource`:

``` java
DataSource dataSource = ...;

PromptRepository repository =
        new JdbcPromptRepository(dataSource);

PromptResolver resolver =
        new PromptResolver();

PromptRegistry registry =
        new PromptRegistry(
                repository,
                resolver
        );
```

The application then continues to use the same core API:

``` java
ResolvedPrompt resolved =
        registry.resolve(
                "customer-support",
                context
        );
```

The application does not need to know whether the repository is backed
by memory or PostgreSQL.

## Resolution Boundary

The JDBC module retrieves prompt data; it does not resolve the active
version.

For example, PostgreSQL may return:

``` text
customer-support

v1 -> ARCHIVED
v2 -> ACTIVE
v3 -> DRAFT
```

The JDBC repository constructs:

``` text
Prompt
 ├── PromptVersion 1 (ARCHIVED)
 ├── PromptVersion 2 (ACTIVE)
 └── PromptVersion 3 (DRAFT)
```

Then the core resolver determines that version 2 is the active version.

This separation is intentional:

``` text
JDBC Repository
    |
    | persistence
    v
Prompt

PromptResolver
    |
    | resolution
    v
ResolvedPrompt
```

Database access and business resolution therefore remain separate
responsibilities.

## Testing

The module uses a real PostgreSQL database for repository integration
tests through Testcontainers.

The current integration test verifies:

### Existing prompt with versions

A prompt containing:

``` text
v1 -> ARCHIVED
v2 -> ACTIVE
```

is loaded correctly with both versions.

The test verifies:

-   Prompt name
-   Number of versions
-   Version number
-   Version content
-   Version status
-   Version ordering

### Missing prompt

A lookup for a prompt that does not exist returns:

``` java
Optional.empty()
```

### Prompt without versions

A prompt can exist without any associated versions.

The repository returns:

``` java
Optional.of(prompt)
```

with:

``` java
prompt.versions().isEmpty()
```

This behavior is important because prompt existence and prompt version
availability are separate concerns.

## Test Database Lifecycle

The integration test:

1.  Starts a PostgreSQL 16 Alpine container.
2.  Creates a PostgreSQL `DataSource`.
3.  Runs Flyway migrations.
4.  Creates a `JdbcPromptRepository`.
5.  Cleans the tables before each test.
6.  Executes repository integration tests against PostgreSQL.

The tests therefore verify the repository against an actual PostgreSQL
database rather than a mocked JDBC layer.

## PostgreSQL Version

The current integration tests use:

``` text
postgres:16-alpine
```

The production PostgreSQL version is ultimately an
application/deployment decision. The repository uses standard PostgreSQL
JDBC access and the migration defines the database objects required by
this module.

## Current Limitations

The current `prompt-storage-jdbc` implementation intentionally provides
only the repository read operation:

``` java
Optional<Prompt> findByName(String name);
```

It does not currently provide:

-   Prompt creation
-   Prompt version creation
-   Version activation
-   Version archiving
-   Update management APIs
-   Optimistic locking
-   Transactional management operations
-   Cache integration
-   Redis
-   Distributed cache invalidation
-   Canary selection
-   Spring integration
-   Spring Boot auto-configuration
-   Spring AI integration
-   Observability
-   Authorization

These capabilities should be introduced in their appropriate milestones
rather than being added to the current read-only repository
implementation prematurely.

## Architectural Boundary

The module follows this boundary:

``` text
prompt-core
    ^
    |
    | depends on core abstractions
    |
prompt-storage-jdbc
    |
    +---- JDBC
    |
    +---- DataSource
    |
    +---- PostgreSQL
    |
    +---- Flyway
```

The dependency direction must not be reversed.

`prompt-core` must never acquire a dependency on:

``` text
JDBC
PostgreSQL
Flyway
DataSource
```

This keeps the core reusable with any future persistence implementation.

## Next Development Step

The next storage-related work should focus on making persistence
production-ready as management operations are introduced.

Important concerns for the later management milestone include:

-   Transactions
-   Concurrent activation
-   Optimistic locking
-   Consistency of prompt/version state
-   Appropriate database indexes
-   Management APIs separate from runtime resolution
-   Preserving immutable historical versions

Those concerns should be implemented when the corresponding
write/management functionality is introduced, rather than adding unused
complexity to the current repository read implementation.

## License

License information will be added when the project's licensing decision
is finalized.
