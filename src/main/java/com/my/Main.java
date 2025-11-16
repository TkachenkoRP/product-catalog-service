package com.my;

import com.my.app.ConsoleUI;
import com.my.app.PostgresqlServiceFactory;
import com.my.app.ServiceFactory;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            ServiceFactory factory = new PostgresqlServiceFactory();
            ConsoleUI consoleUI = new ConsoleUI(factory);
            consoleUI.start();
        } catch (
                SQLException e) {
            System.err.println("Ошибка соединения с БД. " + e.getMessage());
        }
    }
}