import lombok.Getter;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileMessage extends AbstractMessage {
    @Getter
    private String filename;
    @Getter
    private byte[] data;

    @Getter
    private String hash;


    public FileMessage(Path path) throws IOException {
        hash =  HashUtil.getFileHash(path);

        filename = path.toString();
        System.out.println(filename);
        data = Files.readAllBytes(path);
    }
}
