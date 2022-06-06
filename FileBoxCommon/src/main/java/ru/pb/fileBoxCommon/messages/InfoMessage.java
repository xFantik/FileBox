package ru.pb.fileBoxCommon.messages;

public class InfoMessage extends AbstractMessage{
    public enum MessageType {ALL_FILES_SENT};
    private MessageType messageType;


    public InfoMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
