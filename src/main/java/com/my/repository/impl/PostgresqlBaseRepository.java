package com.my.repository.impl;

import com.my.configuration.AppConfiguration;
import com.my.exception.DataAccessException;
import com.my.util.ConnectionProviderFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class PostgresqlBaseRepository {

    protected final Connection connection;
    protected final String schema;

    protected PostgresqlBaseRepository() {
        this(ConnectionProviderFactory.getDefaultProvider().getConnection());
    }

    protected PostgresqlBaseRepository(Connection connection) {
        this.connection = connection;
        schema = AppConfiguration.getProperty("database.schema");
    }

    protected Long getNextSequenceValue(String sequenceName) throws SQLException {
        String sql = String.format("SELECT nextval('%s.%s')", schema, sequenceName);
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new DataAccessException("Sequence returned no value for: " + sequenceName);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get next sequence value for: " + sequenceName, e);
        }
    }
}
