package ru.pb.fileBoxCommon.utils;

import ru.pb.fileBoxCommon.messages.FileMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FileUtil {
    public static String getFileHash(Path path) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, md)) {

            int d;
            while ((d = dis.read()) != -1) {
//                System.out.print((char) d);
            }
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void setFileTime(Path localPath, long timeSeconds) throws IOException {
        FileTime time = FileTime.from(timeSeconds, TimeUnit.SECONDS);
        Files.setLastModifiedTime(localPath, time);
    }

    public static long getFileTime(Path path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        return attr.lastModifiedTime().to(TimeUnit.SECONDS);
    }

    public static ArrayList<FileMessage> getFileList(Path path){
        int root_count  = (path.getNameCount());
        ArrayList<FileMessage> fms = new ArrayList<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    fms.add(new FileMessage(file.subpath(0,root_count), file.subpath(root_count, file.getNameCount()), false));
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fms;
    }

}
