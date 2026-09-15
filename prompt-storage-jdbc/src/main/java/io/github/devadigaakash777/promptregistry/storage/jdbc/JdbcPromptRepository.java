package io.github.devadigaakash777.promptregistry.storage.jdbc;

import io.github.devadigaakash777.promptregistry.core.domain.Prompt;
import io.github.devadigaakash777.promptregistry.core.domain.PromptStatus;
import io.github.devadigaakash777.promptregistry.core.domain.PromptVersion;
import io.github.devadigaakash777.promptregistry.core.repository.PromptRepository;

import javax.sql.DataSource;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public final class JdbcPromptRepository implements PromptRepository {
    private static final String FIND_BY_NAME_SQL = """
            SELECT 
                p.id AS prompt_id,
                p.name AS prompt_name,
                pv.id AS version_id,
                pv.version AS version,
                pv.content AS content,
                pv.status AS status
            FROM prompts p 
            LEFT JOIN
                prompt_versions pv
                ON pv.prompt_id = p.id
            WHERE
                p.name = ?
            ORDER BY
                pv.version
            """;

    private final DataSource dataSource;

    public JdbcPromptRepository(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");
    }

    @Override
    public Optional<Prompt> findByName(final String name) {
        Objects.requireNonNull(name, "name must not be null");

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_NAME_SQL);
                ){

            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                PromptData promptData = null;
                List<PromptVersion> promptVersions = new ArrayList<>();

                while (resultSet.next()) {

                    if (promptData == null) {
                        promptData = new PromptData(
                                resultSet.getString("prompt_name")
                        );
                    }

                    UUID versionId = resultSet.getObject("version_id", UUID.class);

                    if (versionId != null) {
                        promptVersions.add(new PromptVersion(
                                resultSet.getInt("version"),
                                resultSet.getString("content"),
                                PromptStatus.valueOf(
                                        resultSet.getString("status")
                                )
                        ));
                    }
                }

                if (promptData == null) {
                    return Optional.empty();
                }

                return Optional.of(
                        new Prompt(
                                promptData.name(),
                                promptVersions
                        )
                );
            }

        } catch (SQLException sqlException) {
            throw new IllegalStateException(
                    "Failed to load prompt: " + name,
                    sqlException
            );
        }
    }

    private record PromptData(
            String name
    ) {}
}
