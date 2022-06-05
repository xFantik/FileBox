import lombok.Getter;

@Getter
public class PrepareToFileList extends AbstractMessage{
    private int count;

    public PrepareToFileList(int count) {
        this.count = count;
    }

}
