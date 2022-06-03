package messages;

import lombok.Getter;

import java.io.Serializable;
import java.nio.file.Path;

public class FileHeader implements Serializable {


    @Getter
    private long lastModifiedSeconds;
    @Getter
    private String lastModifiedDate;
    @Getter
    private String lastModifiedTime;
    @Getter
    private String hash;
    @Getter
    private String filePath;

    public FileHeader(long lastModifiedSeconds, String lastModifiedDate, String lastModifiedTime, String hash, String filePath) {
        this.lastModifiedSeconds = lastModifiedSeconds;
        this.lastModifiedDate = lastModifiedDate;
        this.lastModifiedTime = lastModifiedTime;
        this.hash = hash;
        this.filePath = filePath;
    }


    @Override
    public String toString() {
        return "messages.FileHeader{" +
                "lastModifiedSeconds=" + lastModifiedSeconds +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", lastModifiedTime='" + lastModifiedTime + '\'' +
                ", hash='" + hash + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
