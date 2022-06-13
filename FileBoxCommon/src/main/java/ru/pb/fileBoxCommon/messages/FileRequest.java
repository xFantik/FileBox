package ru.pb.fileBoxCommon.messages;

public class FileRequest extends AbstractMessage {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public FileRequest(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "FileRequest{" +
                "filename='" + filename + '\'' +
                '}';
    }
}
