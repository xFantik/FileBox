package ru.bp.fileBoxServer;

import lombok.extern.log4j.Log4j2;
import ru.pb.fileBoxCommon.messages.FileMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

@Log4j2
public class MySQLService {

    public enum Status {WAITING_FOR_FILE, DELETED, OK}


    // JDBC URL, username and password of MySQL server
    private static String url;
    private static String user;
    private static String password;

    // JDBC variables for opening and managing connection
    private static Connection connection;
    private static Statement statement;

    private static PreparedStatement psAddFileHeader;
    private static final String statementAddFileHeader = "INSERT INTO files (owner, path, last_modified_sec, status) values(?, ?, ?, ?);";

    private static PreparedStatement psAddFileSuccess;
    private static final String statementAddedFileSuccess = "UPDATE files set hash = ?, last_modified_sec = ?, status = ? where id = ?;";

    private static PreparedStatement psGetFileTime;
    private static final String statementGetFileTime = "SELECT last_modified_sec, status from files where owner = ? and path = ?;";

    private static PreparedStatement psGetFileID;
    private static final String statementGetFileID = "SELECT ID from files where owner = ? and path = ?;";


    private static PreparedStatement psGetUserFilesList;
    private static final String statementGetUserFilesList = "select path, last_modified_sec, status from files where owner = ?;";

    private static PreparedStatement psGetHash;
    private static final String statementGetHash = "select hash from files where owner = ? and path = ?;";


    public static void start() throws MySQLConnectException {
        url = PropertyReader.getInstance().getDbConnectionName();
        user = PropertyReader.getInstance().getDbUser();
        password = PropertyReader.getInstance().getDbPassword();

        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLConnectException("???????????? ?????????????????????? ?? ???????? ????????????: " + e.getMessage());
        }
    }

    private static void connect() throws SQLException {
        log.info("???????????? ???????????????????????? ?? ????.. ");
        connection = DriverManager.getConnection(url, user, password);
        statement = connection.createStatement();
        log.info("???????????? ?????????????????? ?? ???? (" + url + ")");

    }

    private static void disconnect() {
        log.info("???????????????????? ???? ????");
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
            if (psAddFileSuccess != null) psAddFileSuccess.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psGetFileTime != null) psGetFileTime.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (psGetUserFilesList != null) psGetUserFilesList.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psGetHash != null) psGetHash.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info("???????????? ???????????????? ???? ????");
    }

    public static void stop() {
        disconnect();
    }

    public static void addFileHeader(String owner, FileMessage fileHeader) {
        //log.debug("?????????????????? ?????????????????? ?????????? ?? ???? ???????? " + fileHeader.getFilePath());
        try {
            if (psAddFileHeader == null)
                psAddFileHeader = connection.prepareStatement(statementAddFileHeader);
//                                  " INSERT INTO files (owner, path, last_modified_sec, status) values(?, ?, ?, ?);";
            psAddFileHeader.setString(1, owner);
            psAddFileHeader.setString(2, fileHeader.getFilePath().toString());
            psAddFileHeader.setLong(3, fileHeader.getLastModifiedSeconds());
            psAddFileHeader.setString(4, Status.WAITING_FOR_FILE.toString());
            psAddFileHeader.execute();
            log.debug("???????????????? ?????????????????? ?????????? (" + owner + ")" + fileHeader.getFilePath());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addFileSuccess(long id, String hash, long writeTime) {

        try {
            if (psAddFileSuccess == null)
                psAddFileSuccess = connection.prepareStatement(statementAddedFileSuccess);
//                                  ""UPDATE files set hash = ?, last_modified_sec = ?, status = ? where id = ?;"";

            psAddFileSuccess.setString(1, hash);
            psAddFileSuccess.setLong(2, writeTime);
            psAddFileSuccess.setString(3, Status.OK.toString());
            psAddFileSuccess.setLong(4, id);
            psAddFileSuccess.execute();
            log.debug("???????????????????? ?? ???? ?????????? ??????????????????. FileId = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<FileMessage> getDataBaseFileList(String owner) {
        ArrayList<FileMessage> fileHeadersList = new ArrayList<>();
        try {
            if (psGetUserFilesList == null)
                psGetUserFilesList = connection.prepareStatement(statementGetUserFilesList);
//                                 "select path, last_modified_sec, status from files where owner = ?;";
            psGetUserFilesList.setString(1, owner);
            ResultSet rs = psGetUserFilesList.executeQuery();

            while (rs.next()) {
                String path = rs.getString(1);
                Long lastModified = rs.getLong(2);
                boolean status = Status.valueOf(rs.getString(3)) == Status.DELETED ? true : false;
                //todo ???????? ????????????????????
               // fileHeadersList.add(new FileMessage(lastModified, path, true));
            }

            return fileHeadersList;


        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean checkFileInHeadersList(String owner, String path, String hash, Long lastModified, ArrayList<FileMessage> fileHeadersList) {

        Iterator<FileMessage> it = fileHeadersList.iterator();
        while (it.hasNext()) {
            FileMessage fileHeader = it.next();
            if (path.equals(fileHeader.getFilePath())) {             //???????? ?????????? ???????? ???????? ?? ???????????????????? ????????????
                //todo ???????????????? ????????, ??????????, ?????????????????????????????? ???????????? ?? ???? ?????? ???????????????? ?? ???????????? ???????????????? (???????????????? ????) ??????????????
                log.debug("???????? ?????? ???????? ?? ???? (" + owner + ")" + path);
                it.remove();
                return true;
            }
        }
        return false;

    }


//    public void addFileContent(String owner, String path, InputStream file) {
//        try {
//            if (psAddFileContent == null)
//                psAddFileContent = connection.prepareStatement(statementAddFileContent);
////                                  "UPDATE files set file = ? where owner = ? and path = ?";
//            psAddFileContent.setBlob(1, file);
//            psAddFileContent.setString(2, owner);
//            psAddFileContent.setString(3, path);
//
//            if (psAddFileContent.executeUpdate() == 1)
//                log.debug("?????????????????? ???????? ?????????? (" + owner + ")" + path);
//            else {
//                log.debug("???????????? ???????????????????? ???????? ?????????? (" + owner + ")" + path);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        if (checkFileWithHash(owner,path)){
//            //todo file status
//
//        } else {
//            //todo : ?????????????????? ?? ?????????????? ????????
//        }
//
//    }

    public static long getFileTime(String owner, String path) throws DBFileNotFoundException, MySQLConnectException {
        try {
            if (psGetFileTime == null)
                psGetFileTime = connection.prepareStatement(statementGetFileTime);
//                                  "SELECT time_modified_sec, status from files where owner = ? and path = ?;";

            psGetFileTime.setString(1, owner);
            psGetFileTime.setString(2, path);
            ResultSet rs = psGetFileTime.executeQuery();

            if (rs.next()) {
                if (Status.valueOf(rs.getString(2)) != Status.DELETED)
                    return rs.getLong(1);
                else
                    return -rs.getLong(1);
            } else {
                log.debug("???????? (" + owner + ")" + path + " ???????????????????? ?? ????");
                throw new DBFileNotFoundException("???????? (" + owner + ")" + path + " ???????????????????? ?? ????");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLConnectException("???????????? ???????????? ????????");
        }
    }

    public static long getFileID(String owner, String path) throws DBFileNotFoundException, MySQLConnectException {
        try {
            if (psGetFileID == null)
                psGetFileID = connection.prepareStatement(statementGetFileID);
//                                  "SELECT time_modified_sec, status from files where owner = ? and path = ?;";

            psGetFileID.setString(1, owner);
            psGetFileID.setString(2, path);
            ResultSet rs = psGetFileID.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                log.debug("???????? (" + owner + ")" + path + " ???????????????????? ?? ????");
                throw new DBFileNotFoundException("???????? (" + owner + ")" + path + " ???????????????????? ?? ????");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MySQLConnectException("???????????? ???????????? ????????");
        }
    }


    public static String getHash(String owner, String path) {
        log.debug("???????????????? ?????????? ???? ???????? (" + owner + ")" + path + " ...");
        try {
            if (psGetHash == null)
                psGetHash = connection.prepareStatement(statementGetHash);
//                                      "select hash from files where owner = ? and path = ?;";
            psGetHash.setString(1, owner);
            psGetHash.setString(2, path);

            ResultSet rs = psGetHash.executeQuery();

            if (rs.next()) {
                return rs.getString(1);

            } else {
                log.warn("???????? (" + owner + ")" + path + " ???? ???????????? ?? ????!");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}