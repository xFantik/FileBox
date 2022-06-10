package ru.pb.fileBoxClient;

import ru.pb.fileBoxCommon.messages.AbstractMessage;
import ru.pb.fileBoxCommon.messages.FileMessage;
import ru.pb.fileBoxCommon.messages.FileRequest;
import ru.pb.fileBoxCommon.messages.InfoMessage;
import ru.pb.fileBoxCommon.utils.FileUtil;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import static java.nio.file.StandardWatchEventKinds.*;

public class FileBoxClient {
    private static WatchService watchService;
    private static String rootDirectory = "client_storage";
    static List<FileMessage> fileHeadersList = new ArrayList<>();

    public static void main(String[] args) throws IOException {

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

                        FileUtil.setFileTime(localPath, fm.getLastModifiedSeconds());
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
        t.setDaemon(true);
        t.start();


        updateFileList();

        Iterator<FileMessage> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            Network.sendMsg(it.next());
        }
        Network.sendMsg(new InfoMessage(InfoMessage.MessageType.ALL_FILES_SENT));
        Path p = Paths.get(rootDirectory);
        watchService = FileSystems.getDefault().newWatchService();

        registerRecursive(p);


        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }


                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                System.out.println(ev.kind() + " " + filename.toString());
                if ((!filename.toFile().isFile()) && ev.kind() == ENTRY_CREATE) {
                    registerRecursive(Paths.get(rootDirectory));
                }

                updateFileList();
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            key.reset();
        }

    }

    private static void registerRecursive(final Path root) throws IOException {
        if (root.toFile().exists()) {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    System.out.println("поставили папку на мониторинг: " + dir.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    private static void updateFileList() {
        Path path = Paths.get(rootDirectory);
        for (FileMessage fileMessage : fileHeadersList) {
            fileMessage.setDeleted(true);
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

                    addHeaderToList(new FileMessage(FileUtil.getFileTime(file),
                            (file.subpath(1, file.getNameCount()).toString())));

                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<FileMessage> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            FileMessage fileMessage = it.next();
            if (fileMessage.isDeleted()) {
                Network.sendMsg(new InfoMessage(InfoMessage.MessageType.DELETE_FILE, fileMessage.getFilePath()));
                it.remove();
            }
        }
    }

    private static void addHeaderToList(FileMessage newFM) throws IOException {
        Iterator<FileMessage> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            FileMessage oldEntry = it.next();
            if (newFM.equals(oldEntry)) {
                if (newFM.getLastModifiedSeconds() > oldEntry.getLastModifiedSeconds()) {
                    it.remove();
                    break;
                } else {
                    oldEntry.setDeleted(false);
                    return;
                }
            }
        }
        fileHeadersList.add(newFM);
        FileMessage fm = new FileMessage(Paths.get(rootDirectory, newFM.getFilePath()), true);
        Network.sendMsg(fm);
    }
}
