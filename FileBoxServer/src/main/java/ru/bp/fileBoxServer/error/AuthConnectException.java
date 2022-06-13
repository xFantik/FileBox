package ru.bp.fileBoxServer.error;

public class AuthConnectException extends Exception{
    public AuthConnectException() {
    }

    public AuthConnectException(String message) {
        super(message);
    }
}
