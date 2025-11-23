package com.my.service;

import com.my.configuration.AppConfiguration;
import com.my.util.DBUtil;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public class JdbcDataService {
    private final Connection connection;

    public JdbcDataService() {
        connection = DBUtil.getConnection(AppConfiguration.getProperty("liquibase.liquibase-schema"));
    }

    public void initDb() throws LiquibaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(AppConfiguration.getProperty("liquibase.change-log"), new ClassLoaderResourceAccessor(), database);
        liquibase.update(AppConfiguration.getProperty("liquibase.contexts"));
    }
}
