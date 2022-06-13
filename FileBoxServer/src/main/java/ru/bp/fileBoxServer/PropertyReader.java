package ru.bp.fileBoxServer;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static PropertyReader instance;
    private int port;
    private String dbConnectionString;
    private String dbUser;
    private String dbPassword;
    private int historySize;
    private String storagePath;

    public String getStoragePath() {
        return storagePath;
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
            dbConnectionString = (properties.getProperty("datasource.url"));
            storagePath= (properties.getProperty("server.storage.path"));
            dbUser = (properties.getProperty("datasource.user"));
            dbPassword = (properties.getProperty("datasource.password"));
        } catch (Exception e) {
            System.out.println("Не удалось считать настройки: " +e.getMessage());
        }
    }

    public int getPort() {
        return port;
    }


    public String  getDbConnectionName() {
        return dbConnectionString;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}