package ru.bp.fileBoxServer;

public class DBFileDeletedException extends Exception {
    long time;

    public DBFileDeletedException() {
        super();
    }

    public DBFileDeletedException(long time) {
        super();
        this.time=time;
    }

    public long getTime() {
        return time;
    }
}
