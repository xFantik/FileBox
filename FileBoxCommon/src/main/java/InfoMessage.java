public class InfoMessage extends AbstractMessage{
    enum MessageType {ALL_FILES_SENT};
    private MessageType messageType;


    public InfoMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
