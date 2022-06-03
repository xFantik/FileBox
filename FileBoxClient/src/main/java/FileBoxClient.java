import messages.AbstractMessage;
import messages.FileHeader;
import messages.FileMessage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class FileBoxClient {
    static FileHeadersList fileHeadersList = new FileHeadersList();

    public static void main(String[] args) throws IOException {

        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);

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



        updateFileList(new File("./"));

        Iterator<FileHeader> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            Network.sendMsg(new FileMessage(Paths.get(it.next().getFilePath())));
            //System.out.println(it.next());
        }

//        fileHeadersList.prepareToSend();

//        Network.sendMsg(fileHeadersList);

//        serializeToFile();
//        for (messages.FileHeader fh: fileHeadersList) {
//            System.out.println(fh);
//        }
//        deserializeFromFile();
    }


    private static void updateFileList(File file) {
        if (file.isFile()) {
            try {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                String dateTime = attr.lastModifiedTime().toString();

                fileHeadersList.addFileHeader(attr.lastModifiedTime().to(TimeUnit.SECONDS),
                        dateTime.substring(0, 10), dateTime.substring(11, 19), HashUtil.getFileHash(file.getPath()), file.toString());
                // System.out.println(dateTime.substring(0, 10) + " " + dateTime.substring(11, 19) + " " + attr.lastModifiedTime().to(TimeUnit.SECONDS) + " sha256:" + getFileHash(file.getPath()) + " " + file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //System.out.println("Catalog -->> " + file.getPath());
            var files = file.listFiles();
            for (File innerFile : files) {
                updateFileList(innerFile);
            }
        }
    }

    private static void serializeToFile() {
        try (var oos = new ObjectOutputStream(new FileOutputStream("filelist"))) {
//            oos.writeObject(fileHeadersList.get(1));
            oos.writeObject(fileHeadersList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void deserializeFromFile() {
        try (var ois = new ObjectInputStream(new FileInputStream("filelist"))) {
            fileHeadersList = (FileHeadersList) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }




}
