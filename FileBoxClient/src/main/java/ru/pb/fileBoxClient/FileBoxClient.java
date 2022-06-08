package ru.pb.fileBoxClient;

import ru.pb.fileBoxCommon.messages.AbstractMessage;
import ru.pb.fileBoxCommon.messages.FileMessage;
import ru.pb.fileBoxCommon.messages.FileRequest;
import ru.pb.fileBoxCommon.messages.InfoMessage;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileBoxClient {
    private static String rootDirectory = "client_storage";
    static List<FileMessage> fileHeadersList = new ArrayList<>();

    public static void main(String[] args) {

        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    System.out.println("получено собщение" + am);
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Path localPath = Paths.get(rootDirectory, fm.getFilePath());
                        Files.write(localPath, fm.getData(), StandardOpenOption.CREATE);

                        //установка времени последнего изменения файла
                        FileTime time = FileTime.from(fm.getLastModifiedSeconds(), TimeUnit.SECONDS);
                        Files.setLastModifiedTime(localPath, time);
                    }
                    if (am instanceof FileRequest) {
                        FileMessage fm = new FileMessage(Paths.get(rootDirectory, ((FileRequest) am).getFilename()), true);
                        Network.sendMsg(fm);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
//        t.setDaemon(true);
        t.start();


        updateFileList(Paths.get(rootDirectory));


        Iterator<FileMessage> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            Network.sendMsg(it.next());
        }
        Network.sendMsg(new InfoMessage(InfoMessage.MessageType.ALL_FILES_SENT));

    }


    private static void updateFileList(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                    fileHeadersList.add(new FileMessage(attr.lastModifiedTime().to(TimeUnit.SECONDS),
                            (file.subpath(1, file.getNameCount()).toString())));

                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
