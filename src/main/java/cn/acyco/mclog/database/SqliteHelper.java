package cn.acyco.mclog.database;

import cn.acyco.mclog.MCLogCore;
import cn.acyco.mclog.config.ConfigData;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Acyco
 * @create 2022-01-01 21:18
 * @url https://acyco.cn
 */
public class SqliteHelper {
    public static Connection connection = null;
    public static ConfigData configData = MCLogCore.config.getConfigData();
    ;
    public static String prefix = configData.prefix;
    public static String tableNameBlock = prefix + "block";
    public static String tableNameDeath = prefix + "death";
    public static String tableNameSesssion = prefix + "session";
    public static String tableNameUser = prefix + "user";
    public static String tableNameWorld = prefix + "world";

    public static Connection getConnection() {

        //Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            File datebaseFile = MCLogCore.getPathFile(configData.databaseFile);
            connection = DriverManager.getConnection("jdbc:sqlite:" + datebaseFile.getPath());
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void createTables() {
        MCLogCore.LOGGER.debug("create database!!!");

        Connection connection = SqliteHelper.getConnection();

        if (null == connection) {
            return;
        }
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameBlock + " (time INTEGER, user INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, type INTEGER, data INTEGER,action INTEGER);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameDeath + " (time INTEGER, uid INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, msg TEXT,attacker TEXT);");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameUser + " (id INTEGER PRIMARY KEY ASC, time INTEGER, user TEXT, uuid TEXT);"); // user
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameWorld + " (id INTEGER PRIMARY KEY , world TEXT);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameSesssion + " (time INTEGER,uid INTEGER,wid INTEGER,x INTEGER,y INTEGER,z INTEGER,action INTEGER);");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> getWorldMap() {
        String query = "SELECT id,world FROM " + tableNameWorld;
        HashMap<String, Integer> map = new HashMap<>();
        try (
                PreparedStatement preparedStatement = getConnection().prepareStatement(query);
                ResultSet rs = preparedStatement.executeQuery();
        ) {

            while (rs.next()) {
                String world = rs.getString("world");
                if (!map.containsKey(world)) {
                    map.put(world, rs.getInt("id"));
                }
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    public static HashMap<String, Integer> getUserMap() {
        String query = "SELECT id,user FROM " + tableNameUser;
        HashMap<String, Integer> map = new HashMap<>();
        try (
                PreparedStatement preparedStatement = getConnection().prepareStatement(query);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            while (rs.next()) {
                String world = rs.getString("user");
                if (!map.containsKey(world)) {
                    map.put(world, rs.getInt("id"));
                }
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }


    public static void insertWorld(String world) {
        MCLogCore.LOGGER.debug(" insert world");
        if (!MCLogCore.WORLDS.containsKey(world)) {
            String sql = "INSERT INTO " + tableNameWorld + " (world) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, world);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void insertUser(String user, String uuid) {
        MCLogCore.LOGGER.debug("insert user");
        if (!MCLogCore.USERS.containsKey(user)) {
            String sql = "INSERT INTO " + tableNameUser + " (time,user,uuid) VALUES (?,?,?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, (int) (new Date().getTime() / 1000));
                statement.setString(2, user);
                statement.setString(3, uuid);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void insertSession(int uid, int wid, int x, int y, int z, int action) {
        MCLogCore.LOGGER.debug("insert session");
        String sql = "INSERT INTO " + tableNameSesssion + " (time,uid,wid,x,y,z,action) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, (int) (new Date().getTime() / 1000));
            statement.setInt(2, uid);
            statement.setInt(3, wid);
            statement.setInt(4, x);
            statement.setInt(5, y);
            statement.setInt(6, z);
            statement.setInt(7, action); // action 1登录 0 退出
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertDeath(int uid, int wid, int x, int y, int z, String msg, String attacker) {
        String sql = "INSERT INTO " + tableNameDeath + " (time,uid,wid,x,y,z,msg,attacker) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, (int) (new Date().getTime() / 1000));
            statement.setInt(2, uid);
            statement.setInt(3, wid);
            statement.setInt(4, x);
            statement.setInt(5, y);
            statement.setInt(6, z);
            statement.setString(7, msg);
            statement.setString(8, attacker);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
