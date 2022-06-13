package ru.pb.fileBoxClient;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static PropertyReader instance;
    private int port;
    private String host;
    private String syncPath;
    private String nick;
    private String pass;

    public String getNick() {
        return nick;
    }

    public String getPass() {
        return pass;
    }

    public String getSyncPath() {
        return syncPath;
    }

    private PropertyReader() {
        getPropValues();
    }

    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }
        return instance;
    }

    public void getPropValues() {
//        var propFileName = "./config/application.properties";
        var propFileName = "application.properties";
//        try (InputStream inputStream = new FileInputStream(propFileName)) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            var properties = new Properties();
            properties.load(inputStream);
            port = Integer.parseInt(properties.getProperty("server.port"));
            host = (properties.getProperty("server.host"));
            syncPath = (properties.getProperty("client.sync.path"));
            nick = (properties.getProperty("client.user"));
            pass = (properties.getProperty("client.password"));

        } catch (Exception e) {
            System.out.println("Не удалось считать настройки: " +e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

}