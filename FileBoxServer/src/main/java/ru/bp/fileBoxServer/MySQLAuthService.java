package ru.bp.fileBoxServer;

import lombok.extern.log4j.Log4j2;
import ru.bp.fileBoxServer.error.AuthConnectException;
import ru.bp.fileBoxServer.error.WrongCredentialsException;


import java.sql.*;

@Log4j2
public class MySQLAuthService {
    private static Connection connection;
    private static Statement statement;

    private static PreparedStatement psAddUser;
    private static PreparedStatement psGetByLogin;
    private static PreparedStatement psGetByNick;
    private static PreparedStatement psUpdateNick;
    private static PreparedStatement psUpdatePassword;

    private static final String statementAddUser = "insert into users (login, password, nick) values (?, ?, ?);";
    private static final String statementGetByNick = "SELECT login, password, nick from users where nick = (?);";
    private static final String statementGetByLogin = "SELECT login, password, nick from users where login = (?);";
    private static final String statementUpdateNick = "UPDATE users SET nick = (?) WHERE nick = (?)";
    private static final String statementUpdatePassword = "UPDATE users SET password = (?) WHERE login = (?)";
    private static final String CREATE_REQUEST = "create table users (id integer primary key autoincrement, login text UNIQUE NOT NULL, password text, nick text UNIQUE NOT NULL);";


    public void start() throws AuthConnectException {
        try {
            connect();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthConnectException("Ошибка запуска сервиса авторизации: " + e.getMessage());
        }
        try {
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        disconnect();
    }

    public String authorizeUserByLoginAndPassword(String login, String password) {
        try {
            psGetByLogin.setString(1, login);
            ResultSet result = psGetByLogin.executeQuery();
            if (result.next()) {
                if (result.getString("password").equals(password))
                    return result.getString("nick");
                else
                    throw new WrongCredentialsException("Wrong password");
            } else {
                throw new WrongCredentialsException("User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean changeNick(String oldNick, String newNick) {
        try {
            if (isNickBusy(newNick)) {
                throw new WrongCredentialsException("Ник занят");
            } else {
                if (psUpdateNick == null)
                    psUpdateNick = connection.prepareStatement(statementUpdateNick);
                psUpdateNick.setString(1, newNick);
                psUpdateNick.setString(2, oldNick);
                psUpdateNick.execute();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String createNewUser(String login, String password, String nick) {
        try {
            if (psAddUser == null)
                psAddUser = connection.prepareStatement(statementAddUser);

            if (isNickBusy(nick)) throw new WrongCredentialsException("Ник занят");
            if (isLoginBusy(login)) throw new WrongCredentialsException("Логин уже существует");

            psAddUser.setString(1, login);
            psAddUser.setString(2, password);
            psAddUser.setString(3, nick);
            psAddUser.execute();
            return nick;
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return null;
    }

    public void deleteUser(String login, String pass) {
    }

    public boolean changePassword(String login, String oldPass, String newPass) {
        try {
            psGetByLogin.setString(1, login);
            ResultSet result = psGetByLogin.executeQuery();
            if (result.next()) {
                if (result.getString("password").equals(oldPass)) {
                    if (psUpdatePassword == null)
                        psUpdatePassword = connection.prepareStatement(statementUpdatePassword);
                    psUpdatePassword.setString(2, login);
                    psUpdatePassword.setString(1, newPass);
                    psUpdatePassword.execute();
                    return true;
                } else
                    throw new WrongCredentialsException("Wrong old password");
            } else {
                throw new WrongCredentialsException("User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void resetPassword(String login, String newPass, String secret) {
    }

    private void connect() throws SQLException {
        log.info("Сервис аторизации подключается к БД.. ");
        connection = DriverManager.getConnection(PropertyReader.getInstance().getDbConnectionName());
        statement = connection.createStatement();
        log.info("Сервис аторизации подключён к БД (" + PropertyReader.getInstance().getDbConnectionName() + ")");
    }

    private void createTable() throws SQLException {
        log.trace("Проверка наличия (создание) базы пользователей");
        statement.execute(CREATE_REQUEST);
        psGetByLogin = connection.prepareStatement(statementGetByLogin);
        {                                   //создание тестовых пользователей
            try {
                for (int i = 1; i < 6; i++) {
                    createNewUser("log" + i, "pass", "nick" + i);
                }
            } catch (WrongCredentialsException ignored) {

            }
        }
    }

    private void disconnect() {
        log.info("Отключение сервиса авторизации от БД");
        try {
            if (statement != null) statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psAddUser != null) psAddUser.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psGetByLogin != null) psGetByLogin.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psGetByNick != null) psGetByNick.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psUpdatePassword != null) psUpdatePassword.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psUpdateNick != null) psUpdateNick.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info("Сервис авторизации отключен от БД");
    }


    private boolean isNickBusy(String nick) throws SQLException {
        if (psGetByNick == null)
            psGetByNick = connection.prepareStatement(statementGetByNick);
        psGetByNick.setString(1, nick);
        return psGetByNick.executeQuery().next();
    }

    private boolean isLoginBusy(String login) throws SQLException {
        psGetByLogin.setString(1, login);
        return psGetByLogin.executeQuery().next();
    }
}
