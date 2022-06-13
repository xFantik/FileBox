package ru.pb.fileBoxClient;

import ru.pb.fileBoxCommon.messages.*;
import ru.pb.fileBoxCommon.utils.FileUtil;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


import static java.nio.file.StandardWatchEventKinds.*;

public class FileBoxClient {
    private static WatchService watchService;
    private static String rootDirectory;
    private static Path rootPath;
    private static List<FileMessage> fileHeadersList = new ArrayList<>();
    private static ArrayList<FileMessage> newFileList;

    public static void main(String[] args) throws IOException {
        rootDirectory = PropertyReader.getInstance().getSyncPath();
        rootPath = Paths.get(rootDirectory);


        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();


                    if (am instanceof FileMessage) {

                        FileMessage fm = (FileMessage) am;
                        Path localPath = rootPath.resolve(fm.getFilePath());
                        Files.write(localPath, fm.getData(), StandardOpenOption.CREATE);
                        FileUtil.setFileTime(localPath, fm.getLastModifiedSeconds());
                        System.out.println("Сохранили файл " + fm);
                        updateHeaderInList(fm);
                    }
                    if (am instanceof FileRequest) {
                        Path filePath = Paths.get(((FileRequest) am).getFilename());
                        FileMessage fm = new FileMessage(rootPath, filePath, true);
                        Network.sendMsg(fm);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();


        fileHeadersList = FileUtil.getFileList(rootPath);

        Network.sendMsg(new FileHeaderList(fileHeadersList));
//        Iterator<FileMessage> it = fileHeadersList.iterator();
//        while (it.hasNext()) {
//            Network.sendMsg(it.next());
//        }
//        Network.sendMsg(new InfoMessage(InfoMessage.MessageType.ALL_FILES_SENT));


        while (true) {
            try {
                Thread.sleep(500);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            System.out.println("обновляем список файлов по расписанию");

            updateFileList();

        }


        //watchService = FileSystems.getDefault().newWatchService();
//      registerRecursive(rootPath);


//        while (true) {
//            WatchKey key;
//            try {
//                key = watchService.take();
//            } catch (InterruptedException x) {
//                return;
//            }
//
//            for (WatchEvent<?> event : key.pollEvents()) {
//                WatchEvent.Kind<?> kind = event.kind();
//                if (kind == OVERFLOW) {
//                    continue;
//                }
//
//                WatchEvent<Path> ev = (WatchEvent<Path>) event;
//                Path filename = ev.context();
//
//                System.out.println(ev.kind() + " " + filename.toString());
//                if ((!filename.toFile().isFile()) && ev.kind() == ENTRY_CREATE) {
//                    registerRecursive(rootPath);
//                }
//                updateFileList();
//            }
//            key.reset();
//        }
    }

    private static void updateHeaderInList(FileMessage fm) {
        for (FileMessage fileMessage : fileHeadersList) {
            if (fm.equals(fileMessage)) {
                fileHeadersList.remove(fileMessage);
            }
            break;
        }
        fileHeadersList.add(fm);
    }
//
//    private static void registerRecursive(final Path root) throws IOException {
//        if (root.toFile().exists()) {
//            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
//                    System.out.println("поставили папку на мониторинг: " + dir.toString());
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        }
//    }


    private static boolean findHeaderInOldFileList(FileMessage newFileMessage) throws IOException {
        for (FileMessage oldFileMessage : fileHeadersList) {
            if (oldFileMessage.equals(newFileMessage)) {

                if (oldFileMessage.getLastModifiedSeconds() < newFileMessage.getLastModifiedSeconds()) {
                    readAndSendFile(rootPath, newFileMessage.getFilePath());
                }
                fileHeadersList.remove(oldFileMessage);
                return true;
            }
        }
        return false;

    }


    private static void updateFileList() throws IOException {
        newFileList = FileUtil.getFileList(rootPath);


        for (FileMessage newFileMessage : newFileList) {
            if (!findHeaderInOldFileList(newFileMessage)) {
                readAndSendFile(rootPath, newFileMessage.getFilePath());
            }
        }


        for (FileMessage fileMessage : fileHeadersList) {
            Network.sendMsg(new InfoMessage(InfoMessage.MessageType.DELETE_FILE, fileMessage.getFilePath().toString()));
        }
        fileHeadersList = newFileList;
    }


    private static void readAndSendFile(Path storage, Path file) throws IOException {
        FileMessage fm = new FileMessage(storage, file, true);
        Network.sendMsg(fm);
    }

    private static void print() {
        System.out.println("\nСохраненный список файлов");
        for (FileMessage o : fileHeadersList) {
            System.out.println(o);
        }
        System.out.println("\nНовый список файлов");

        for (FileMessage o : newFileList) {
            System.out.println(o);
        }


    }
}
