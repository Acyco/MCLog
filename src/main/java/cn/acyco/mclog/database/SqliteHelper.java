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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public static String tableNameBlockMap = prefix + "block_map";
    public static String tableNameBlockStateMap = prefix + "block_state_map";
    public static String tableNameContainer = prefix + "container";
    public static String tableNameDeath = prefix + "death";
    public static String tableNameItemMap = prefix + "item_map";
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

        Connection connection = SqliteHelper.getConnection();

        if (null == connection) {
            return;
        }
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameBlock + " (time INTEGER, uid INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, bid INTEGER, sid BLOB, action INTEGER,rolled_back INTEGER);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameBlockMap + " (id INTEGER PRIMARY KEY ASC,block TEXT);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameBlockStateMap + " (id INTEGER PRIMARY KEY ASC,state TEXT);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameContainer + " (time INTEGER, uid INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, msg TEXT,attacker TEXT);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameDeath + " (time INTEGER, uid INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, msg TEXT,attacker TEXT);");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameUser + " (id INTEGER PRIMARY KEY ASC, time INTEGER, user TEXT, uuid TEXT);"); // user
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameWorld + " (id INTEGER PRIMARY KEY , world TEXT);");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableNameSesssion + " (time INTEGER,uid INTEGER,wid INTEGER,x INTEGER,y INTEGER,z INTEGER,action INTEGER);");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static HashMap<String, Integer> getDataMap(String tableName, String key) {
        String query = "SELECT id," + key+ " FROM " + tableName;
        HashMap<String, Integer> map = new HashMap<>();
        try (
                PreparedStatement preparedStatement = getConnection().prepareStatement(query);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            while (rs.next()) {
                String keyStr = rs.getString(key);
                if (!map.containsKey(keyStr)) {
                    map.put(keyStr, rs.getInt("id"));
                }
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void insert(String tableName , LinkedHashMap<String,Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (");
        int size = map.keySet().size();
        int current = 0;
        StringBuilder  qm = new StringBuilder();
        for (String key : map.keySet()) {
            current++;
            sb.append(key);
            qm.append("?");
            if (size > current) {
                sb.append(",");
                qm.append(",");
            }
        }
        sb.append(") VALUES (");
        sb.append(qm);
        sb.append(")");
        try (PreparedStatement statement = connection.prepareStatement(sb.toString())) {
            int cu = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                cu++;
                statement.setObject(cu, entry.getValue());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
