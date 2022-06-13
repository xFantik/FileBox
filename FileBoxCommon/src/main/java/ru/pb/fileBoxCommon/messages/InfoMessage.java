package ru.pb.fileBoxCommon.messages;

public class InfoMessage extends AbstractMessage {
    public enum MessageType {ALL_FILES_SENT, DELETE_FILE}

    ;
    private MessageType messageType;
    private String message;


    public InfoMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public InfoMessage(MessageType messageType, String message) {
        this(messageType);
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (message != null) {
            return "InfoMessage{" +
                    "messageType=" + messageType +
                    ", message='" + message + '\'' +
                    '}';
        } else {
            return "InfoMessage{" +
                    "messageType=" + messageType + '\'' +
                    '}';
        }
    }
}
