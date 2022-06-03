import messages.FileHeader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class FileHeadersList implements Iterable<FileHeader> {
    private static ArrayList<FileHeader> fileHeaders = new ArrayList<>();
    private static List<Integer> list = List.of(1, 2, 3, 4, 5);

    @Override
    public Iterator<FileHeader> iterator() {
        return fileHeaders.iterator();
    }

    @Override
    public void forEach(Consumer action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<FileHeader> spliterator() {
        return Iterable.super.spliterator();
    }


    public void addFileHeader(long lastModifiedSeconds, String lastModifiedDate, String lastModifiedTime, String fileHash, String path) {
        fileHeaders.add(new FileHeader(lastModifiedSeconds, lastModifiedDate, lastModifiedTime, fileHash, path));
    }

    public FileHeader get(int i){
        return fileHeaders.get(i);
    }

//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(fileHeaders.size());
//        for (messages.FileHeader fileHeader : fileHeaders) {
//            out.writeObject(fileHeader);
//        }
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        fileHeaders=new ArrayList<>();
//        int count = (Integer) in.readObject();
//        for (int i = 0; i < count; i++) {
//            fileHeaders.add((messages.FileHeader) in.readObject());
//        }
//    }



}
