package ru.pb.fileBoxCommon.messages;

import lombok.Getter;
import lombok.Setter;
import ru.pb.fileBoxCommon.utils.FileUtil;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;


public class FileMessage extends AbstractMessage {

    private String filePath;

    @Getter
    @Setter
    private long lastModifiedSeconds;

    @Getter
    private boolean deleted = false;

    @Getter
    private byte[] data;

    @Getter
    private String hash;

    public FileMessage(Path storage, Path file, boolean withData) throws IOException {
        filePath = file.toString();
        BasicFileAttributes attr = Files.readAttributes(storage.resolve(file), BasicFileAttributes.class);
        lastModifiedSeconds = attr.lastModifiedTime().to(TimeUnit.SECONDS);
        if (withData) {
            hash = FileUtil.getFileHash(storage.resolve(file));
            data = Files.readAllBytes(storage.resolve(file));
        }
    }

    public FileMessage(String filePath, long lastModifiedSeconds) {
        this.filePath = filePath;
        this.lastModifiedSeconds = lastModifiedSeconds;
    }

    @Override
    public String toString() {
        if (hash == null)
            return "Заголовок файла {" +
                    "filePath='" + filePath + '\'' +
                    ", lastModifiedSeconds=" + lastModifiedSeconds +
                    '}';
        return "FileMessage {" +
                "filePath='" + filePath + '\'' +
                ", lastModifiedSeconds=" + lastModifiedSeconds +
                ", hash=" + hash +
                '}';
    }


    public boolean equals(FileMessage obj) {
        return filePath.equals(obj.getFilePath().toString());
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Path getFilePath() {
        return Paths.get(filePath);
    }
}
