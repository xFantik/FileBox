import lombok.Getter;

import java.io.Serializable;

public class FileHeader extends AbstractMessage implements Serializable {


    @Getter
    private long lastModifiedSeconds;

    @Getter
    private String filePath;

    @Getter
    private boolean deleted =false;

    public FileHeader(long lastModifiedSeconds,  String filePath) {
        this.lastModifiedSeconds = lastModifiedSeconds;
        this.filePath = filePath;
    }

    public FileHeader(long lastModifiedSeconds, String filePath, boolean deleted) {
        this(lastModifiedSeconds, filePath);
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "FileHeader{" +
                "lastModifiedSeconds=" + lastModifiedSeconds +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
