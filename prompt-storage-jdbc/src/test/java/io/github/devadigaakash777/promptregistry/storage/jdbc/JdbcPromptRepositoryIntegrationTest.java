package io.github.devadigaakash777.promptregistry.storage.jdbc;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;
import io.github.devadigaakash777.promptregistry.core.domain.PromptStatus;
import io.github.devadigaakash777.promptregistry.core.domain.PromptVersion;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Connection;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Testcontainers
public class JdbcPromptRepositoryIntegrationTest {

    @Container
    public static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("prompt_registry")
                    .withUsername("postgres")
                    .withPassword("postgres");

    private DataSource dataSource;
    private JdbcPromptRepository jdbcPromptRepository;

    @BeforeEach
    void setUp() {

        dataSource = createDataSource();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        jdbcPromptRepository = new JdbcPromptRepository(dataSource);

        cleanDatabase();
    }

    @Test
    void shouldFindExistingPromptWithVersions() {

        UUID promptId = UUID.randomUUID();

        insertPrompt(promptId, "customer-support");

        insertPromptVersions(
                UUID.randomUUID(),
                promptId,
                1,
                "Old prompt",
                "ARCHIVED"
        );

        insertPromptVersions(
                UUID.randomUUID(),
                promptId,
                2,
                "Active Prompt",
                "ACTIVE"
        );

        Optional<Prompt> result = jdbcPromptRepository.findByName("customer-support");

        assertTrue(result.isPresent());

        Prompt prompt = result.orElseThrow();

        assertEquals("customer-support", prompt.name());
        assertEquals(2, prompt.versions().size());

        List<PromptVersion> versions = prompt.versions();

        assertEquals(1, versions.getFirst().version());
        assertEquals("Old prompt", versions.getFirst().content());
        assertEquals(PromptStatus.ARCHIVED, versions.getFirst().status());

        assertEquals(2, versions.getLast().version());
        assertEquals("Active Prompt", versions.getLast().content());
        assertEquals(PromptStatus.ACTIVE, versions.getLast().status());
    }

    @Test
    void shouldReturnEmptyWhenPromptDoesNotExist() {

        Optional<Prompt> result =
                jdbcPromptRepository.findByName("unknown-prompt");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindPromptWithNoVersions() {

        UUID promptId = UUID.randomUUID();

        insertPrompt(
                promptId,
                "doctor-support"
        );

        Optional<Prompt> result =
                jdbcPromptRepository.findByName("doctor-support");

        assertTrue(result.isPresent());

        Prompt prompt = result.orElseThrow();

        assertEquals("doctor-support", prompt.name());
        assertTrue(prompt.versions().isEmpty());
    }

    private void insertPrompt(
            UUID id,
            String name
    ) {

        String sql = """
                INSERT INTO prompts (
                    id,
                    name,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
                ) {

            preparedStatement.setObject(1, id);
            preparedStatement.setString(2, name);

            preparedStatement.executeUpdate();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to insert test prompt with id " + id,
                    exception
            );
        }
    }

    private void insertPromptVersions(
            UUID id,
            UUID promptId,
            int version,
            String content,
            String status
    ) {

        String sql = """
                INSERT INTO prompt_versions(
                    id,
                    prompt_id,
                    version,
                    content,
                    status,
                    created_at,
                    updated_at
                ) VALUES (
                    ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                )
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
                ){

            preparedStatement.setObject(1, id);
            preparedStatement.setObject(2, promptId);
            preparedStatement.setInt(3, version);
            preparedStatement.setString(4, content);
            preparedStatement.setString(5, status);

            preparedStatement.executeUpdate();

        } catch (Exception exception){
            throw new IllegalStateException(
                    "Failed to insert test prompt version with id " + id,
                    exception
            );
        }
    }

    private DataSource createDataSource() {

        PGSimpleDataSource simpleDataSource = new PGSimpleDataSource();
        simpleDataSource.setURL(POSTGRES.getJdbcUrl());
        simpleDataSource.setUser(POSTGRES.getUsername());
        simpleDataSource.setPassword(POSTGRES.getPassword());

        return simpleDataSource;
    }

    private void cleanDatabase() {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "TRUNCATE TABLE prompt_versions, prompts CASCADE"
                )
        ) {

            preparedStatement.executeUpdate();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to clean test database",
                    exception
            );
        }
    }
}
