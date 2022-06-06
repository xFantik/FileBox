package ru.bp.fileBoxServer;

public class MySQLConnectException extends Exception {
    public MySQLConnectException() {
        super();
    }

    public MySQLConnectException(String message) {
        super(message);
    }
}
