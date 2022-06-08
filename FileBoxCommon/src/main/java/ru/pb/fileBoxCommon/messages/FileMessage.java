package ru.pb.fileBoxCommon.messages;

import lombok.Getter;
import lombok.Setter;
import ru.pb.fileBoxCommon.utils.HashUtil;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;


public class FileMessage extends AbstractMessage {
    @Getter
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

    public FileMessage(Path path, boolean withData) throws IOException {
        filePath = path.toString();
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        lastModifiedSeconds = attr.lastModifiedTime().to(TimeUnit.SECONDS);
        System.out.println(filePath);
        if (withData) {
            hash = HashUtil.getFileHash(path);
            data = Files.readAllBytes(path);
        }
    }

    public void updateFile(long lastModifiedSeconds, String hash, Path pathOnServer) throws IOException{
        this.lastModifiedSeconds = lastModifiedSeconds;
        this.hash= hash;
        data = Files.readAllBytes(pathOnServer);


    }



    public FileMessage(long lastModifiedSeconds, String filePath) {
        this.lastModifiedSeconds = lastModifiedSeconds;
        this.filePath = filePath;
    }

    public FileMessage(long lastModifiedSeconds, String filePath, boolean deleted) {
        this(lastModifiedSeconds, filePath);
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "ru.pb.fileBoxCommon.messages.FileMessage{" +
                "filePath='" + filePath + '\'' +
                ", lastModifiedSeconds=" + lastModifiedSeconds +
                '}';
    }


    public boolean equals(FileMessage obj) {
        return filePath.equals(obj.getFilePath());
    }
}
