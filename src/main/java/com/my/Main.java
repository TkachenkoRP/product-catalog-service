package com.my;

import com.my.app.ConsoleUI;
import com.my.app.PostgresqlServiceFactory;
import com.my.app.ServiceFactory;

public class Main {
    public static void main(String[] args) {
        ServiceFactory factory = new PostgresqlServiceFactory();
        ConsoleUI consoleUI = new ConsoleUI(factory);
        consoleUI.start();
    }
}