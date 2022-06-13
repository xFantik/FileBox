package ru.pb.fileBoxClient;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.pb.fileBoxCommon.messages.AbstractMessage;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private static int maxObjectSize = 50 * 1024 * 1024;

    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;

    public static void start() {
        try {
            socket = new Socket(PropertyReader.getInstance().getHost(), PropertyReader.getInstance().getPort());
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), maxObjectSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            System.out.println("отправили на сервер: " + msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();
        System.out.println("Входящие данные: " + obj);
        return (AbstractMessage) obj;
    }
}