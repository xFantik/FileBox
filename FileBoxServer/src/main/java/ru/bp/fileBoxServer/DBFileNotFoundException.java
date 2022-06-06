package ru.bp.fileBoxServer;

public class DBFileNotFoundException extends Exception {
    public DBFileNotFoundException() {
        super();
    }

    public DBFileNotFoundException(String message) {
        super(message);
    }


}
