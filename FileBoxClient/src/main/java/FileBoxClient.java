import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileBoxClient {
    static List<FileHeader> fileHeadersList = new ArrayList<>();

    public static void main(String[] args) {

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


        updateFileList(new File("client_storage/"));

        Network.sendMsg(new PrepareToFileList(fileHeadersList.size()));


        Iterator<FileHeader> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            Network.sendMsg(it.next());
            //System.out.println(it.next());
        }


    }


    private static void updateFileList(File file) {
        if (file.isFile()) {
            try {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);


                fileHeadersList.add(new FileHeader(attr.lastModifiedTime().to(TimeUnit.SECONDS),
                          file.toString()));
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

//    private static void serializeToFile() {
//        try (var oos = new ObjectOutputStream(new FileOutputStream("filelist"))) {
////            oos.writeObject(fileHeadersList.get(1));
//            oos.writeObject(fileHeadersList);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private static void deserializeFromFile() {
//        try (var ois = new ObjectInputStream(new FileInputStream("filelist"))) {
//            fileHeadersList = (FileHeadersList) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }




}
