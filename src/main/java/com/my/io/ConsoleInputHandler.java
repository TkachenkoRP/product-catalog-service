package com.my.io;

import java.util.Scanner;

public class ConsoleInputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleInputHandler() {
    }

    public static String getUserTextInput(String request) {
        ConsoleOutputHandler.displayMsg(request);
        return scanner.nextLine().trim();
    }

    public static int getUserIntegerInput(String request) {
        while (true) {
            ConsoleOutputHandler.displayMsg(request);
            if (scanner.hasNextLong()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } else {
                ConsoleOutputHandler.displayMsg("Неверное значение! Введите число.\n");
                scanner.next();
            }
        }
    }

    public static long getUserLongInput(String request) {
        while (true) {
            ConsoleOutputHandler.displayMsg(request);
            if (scanner.hasNextLong()) {
                long value = scanner.nextLong();
                scanner.nextLine();
                return value;
            } else {
                ConsoleOutputHandler.displayMsg("Неверное значение! Введите число.\n");
                scanner.next();
            }
        }
    }

    public static double getUserDoubleInput(String request) {
        while (true) {
            ConsoleOutputHandler.displayMsg(request);
            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            } else {
                ConsoleOutputHandler.displayMsg("Неверное значение! Введите число.\n");
                scanner.next();
            }
        }
    }

    public static boolean getUserConfirmation(String request) {
        ConsoleOutputHandler.displayMsg(request + " (y/n)");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes") || input.equals("да");
    }
}
