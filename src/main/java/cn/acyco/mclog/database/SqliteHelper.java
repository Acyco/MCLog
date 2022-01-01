package cn.acyco.mclog.database;

import cn.acyco.mclog.MCLogCore;
import cn.acyco.mclog.config.Config;
import cn.acyco.mclog.config.ConfigData;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Acyco
 * @create 2022-01-01 21:18
 * @url https://acyco.cn
 */
public class SqliteHelper {
    //public static Connection connection = null;
    public static ConfigData configData = null;

    public static Connection getConnection() {
        configData = MCLogCore.config.getConfigData();
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            File datebaseFile = MCLogCore.getPathFile(configData.databaseFile);
             connection = DriverManager.getConnection("jdbc:sqlite:" +datebaseFile.getPath());
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void createTables() {
        MCLogCore.LOGGER.debug("create database!!!");

        try {
            Connection connection = SqliteHelper.getConnection();
            if (null == connection) {
                return;
            }
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + configData.prefix + "block (time INTEGER, user INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, type INTEGER, data INTEGER,action INTEGER);");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS "+ configData.prefix + "user (id INTEGER PRIMARY KEY ASC, time INTEGER, user TEXT, uuid TEXT);"); // user
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + configData.prefix + "world (id INTEGER, world TEXT);");
            //statement.executeUpdate("CREATE TABLE IF NOT EXISTS "+ configData.prefix + "user (id INTEGER PRIMARY KEY ASC, time INTEGER, user TEXT, uuid TEXT);"); // user
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
