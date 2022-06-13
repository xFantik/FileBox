package ru.pb.fileBoxCommon.messages;

import java.util.ArrayList;
import java.util.List;

public class FileHeaderList extends AbstractMessage{
    private String fileList;
    private static final String delimeter = "::";

    public FileHeaderList(List<FileMessage> list) {
        StringBuilder sb = new StringBuilder();
        for (FileMessage fileMessage : list) {
            sb.append(fileMessage.getFilePath().toString()).append(delimeter).append(fileMessage.getLastModifiedSeconds()).append(delimeter);
        }
        this.fileList = sb.toString();
    }

    public ArrayList<FileMessage> getFileList(){
        ArrayList<FileMessage> fileMessages= new ArrayList<>();
        String array[]= fileList.split(delimeter);
        for (int i = 0; i < array.length; i+=2) {
            fileMessages.add(new FileMessage(array[i], Long.parseLong(array[i+1])));
        }
        return fileMessages;
    }
}
