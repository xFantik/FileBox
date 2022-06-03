import lombok.extern.log4j.Log4j2;
import messages.FileHeader;

import java.io.InputStream;
import java.sql.*;
import java.util.Iterator;

@Log4j2
public class MySQLService {

    private static int WAITING_FOR_FILE = 1;

    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:mysql://localhost:3306/filebox";
    private static final String user = "root";
    private static final String password = "123234";

    // JDBC variables for opening and managing connection
    private static Connection connection;
    private static Statement statement;

    private static PreparedStatement psAddFileHeader;
    private static final String statementAddFileHeader = "INSERT INTO files (owner, path, hash, last_modified_sec, status) values(?, ?, ?, ?, ?);";
    private static PreparedStatement psAddFileContent;
    private static final String statementAddFileContent = "UPDATE files set file = ? where owner = ? and path = ?";
    private static PreparedStatement psGetFileContent;
    private static final String statementGetFileContent = "SELECT file from files where owner = ? and path = ?;";
    private static PreparedStatement psGetUserFilesList;
    private static final String statementGetUserFilesList = "select path, hash, last_modified_sec from files where owner = ?;";

    private static PreparedStatement psCheckSHA;
    private static final String statementCheckSHA = "select sha(file), hash from files where owner = ? and path = ?;";




    public void start() throws MySQLConnectException {
        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLConnectException("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    private void connect() throws SQLException {
        log.info("Сервис подключается к БД.. ");
        connection = DriverManager.getConnection(url, user, password);
        statement = connection.createStatement();
        log.info("Сервис подключён к БД (" + url + ")");

    }

    private void disconnect() {
        log.info("Отключение от БД");
        try {
            if (statement != null) statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (psAddFileHeader != null) psAddFileHeader.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psAddFileContent != null) psAddFileContent.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psGetFileContent != null) psGetFileContent.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (psGetUserFilesList != null) psGetUserFilesList.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psCheckSHA != null) psCheckSHA.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }



        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info("Сервис отключен от БД");
    }

    public void stop() {
        disconnect();
    }

    public void addFileHeader(String owner, FileHeader fileHeader) {
        //log.debug("Добавляем заголовок файла в БД файл " + fileHeader.getFilePath());
        try {
            if (psAddFileHeader == null)
                psAddFileHeader = connection.prepareStatement(statementAddFileHeader);
//                                  " INSERT INTO files (owner, path, hash, last_modified_sec, status) values(?, ?, ?, ?, ?);";
            psAddFileHeader.setString(1, owner);
            psAddFileHeader.setString(2, fileHeader.getFilePath().toString());
            psAddFileHeader.setString(3, fileHeader.getHash());
            psAddFileHeader.setLong(4, fileHeader.getLastModifiedSeconds());
            psAddFileHeader.setInt(5, WAITING_FOR_FILE);
            psAddFileHeader.execute();
            log.debug("Добавлен заголовок файла (" + owner + ")" + fileHeader.getFilePath());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void compareFileList(String owner, FileHeadersList fileHeadersList){
        try {
            if (psGetUserFilesList == null)
                psGetUserFilesList = connection.prepareStatement(statementGetUserFilesList);
//                                 "select path, hash, last_modified_sec from files where owner = ?;";
            psGetUserFilesList.setString(1, owner);

            ResultSet rs= psGetUserFilesList.executeQuery();

            while (rs.next()){
                String path = rs.getString(1);
                String hash = rs.getString(2);
                Long lastModified = rs.getLong(3);
                if (!checkFileInHeadersList(owner, path, hash, lastModified, fileHeadersList)){
                     //добавить в список отправки клиенту
                }
            }

            for (FileHeader fileHeader : fileHeadersList) {
                addFileHeader(owner, fileHeader);
            }



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkFileInHeadersList(String owner, String path, String hash, Long lastModified, FileHeadersList fileHeadersList){

        Iterator<FileHeader> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            FileHeader fileHeader= it.next();
            if (path.equals(fileHeader.getFilePath())){             //если такой файл есть в присланном списке
                //todo сравнить хэши, время, актуализировать запись в БД или добавить в список отправки (плучения от) клиенту
                log.debug("Файл уже есть в БД ("+ owner+")"+ path);
                it.remove();
                return true;
            }
        }
        return false;

    }


    public void addFileContent(String owner, String path, InputStream file) {
        try {
            if (psAddFileContent == null)
                psAddFileContent = connection.prepareStatement(statementAddFileContent);
//                                  "UPDATE files set file = ? where owner = ? and path = ?";
            psAddFileContent.setBlob(1, file);
            psAddFileContent.setString(2, owner);
            psAddFileContent.setString(3, path);

            if (psAddFileContent.executeUpdate() == 1)
                log.debug("Добавлено тело файла (" + owner + ")" + path);
            else {
                log.debug("Ошибка добавления тела файла (" + owner + ")" + path);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (checkFileWithHash(owner,path)){
            //todo file status

        } else {
            //todo : запросить у клиента файл
        }

    }

    public InputStream getFileContent(String owner, String path) {
        try {
            if (psGetFileContent == null)
                psGetFileContent = connection.prepareStatement(statementGetFileContent);
//                                  "SELECT file from files where owner = ? and path = ?;";
            psGetFileContent.setString(1, owner);
            psGetFileContent.setString(2, path);
            ResultSet rs = psGetFileContent.executeQuery();

            if (rs.next()) {
                if (rs.getBlob(1) == null) {
                    log.warn("BLOB = NULL для файла (" + owner + ")" + path);
                    return null;
                }
                log.debug("Получено из БД тело файла (" + owner + ")" + path);
                return rs.getBlob(1).getBinaryStream();
            } else {
                log.warn("Ошибка получениния файла из БД. Запись (" + owner + ")" + path + " не найдена");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkFileWithHash(String owner, String path){
        log.debug("Проверка файла по ХЭШУ ("+owner+")"+path+" ...");
        try {
            if (psCheckSHA == null)
                psCheckSHA = connection.prepareStatement(statementCheckSHA);
//                                      "select sha(file), hash from files where owner = ? and path = ?;";
            psGetFileContent.setString(1, owner);
            psGetFileContent.setString(2, path);

            ResultSet rs = psGetFileContent.executeQuery();

            if (rs.next()) {
                if (rs.getString(1).equals(rs.getString(2))){
                    log.debug("Файл ("+owner+")"+path+" записан корректно");
                    return true;
                }
            } else {
                log.warn("Хэш файла (" + owner + ")" + path + " не совпадает с записанным!");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}