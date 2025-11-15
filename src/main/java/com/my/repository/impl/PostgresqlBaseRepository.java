package com.my.repository.impl;

import com.my.configuration.AppConfiguration;
import com.my.util.DBUtil;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class PostgresqlBaseRepository {
    protected final String schema;

    protected PostgresqlBaseRepository() {
        schema = AppConfiguration.getProperty("database.schema");
    }

    protected Connection getConnection() throws SQLException {
        return DBUtil.getConnection();
    }

    protected Long getNextSequenceValue(String sequenceName) throws SQLException {
        String sql = String.format("SELECT nextval('%s.%s')", schema, sequenceName);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get next sequence value for: " + sequenceName);
        }
    }

    @Getter
    protected enum Sequences {
        BRAND("brand_seq"),
        CATEGORY("category_seq"),
        PRODUCT("product_seq"),
        USER("user_seq");

        private final String sequenceName;

        Sequences(String sequenceName) {
            this.sequenceName = sequenceName;
        }
    }
}
