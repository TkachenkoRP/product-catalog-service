package com.my.servlet;

import com.my.service.JdbcDataService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import liquibase.exception.LiquibaseException;

@WebListener
public class LiquibaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            JdbcDataService jdbcDataService = new JdbcDataService();
            jdbcDataService.initDb();
        } catch (LiquibaseException e) {
            System.out.println(e);
        }
    }
}
