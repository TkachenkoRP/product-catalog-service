package com.my.servlet;

import com.my.configuration.AppConfiguration;
import com.my.util.ConnectionProviderFactory;
import com.my.util.LiquibaseManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class LiquibaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LiquibaseManager.updateDatabase(ConnectionProviderFactory.getDefaultProvider()
                .getConnection(AppConfiguration.getProperty("liquibase.liquibase-schema")));
    }
}