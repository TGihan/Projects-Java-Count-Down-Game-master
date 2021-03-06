/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.floggame.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author Tharindu Gihan
 */
public class DBControl {

    static Properties props = new Properties();
    static InputStream inputStream;
    
    static int dbport;
    static String dbname;
    static String dbhostname;
    static String username;
    static String password;

    //NOTE: Get database config details from properties file
    public static void accessToProperties() {

        try {

            inputStream = new FileInputStream("config.properties");
            props.load(inputStream);
            dbhostname = props.getProperty("DB_HOST_NAME");
            dbport = Integer.parseInt(props.getProperty("DB_PORT"));
            System.out.println("gihan port is " + dbport);
            dbname = props.getProperty("DB_NAME");
            username = props.getProperty("DB_USER_NAME");
            password = props.getProperty("DB_PASSWORD");

        } catch (IOException e) {
            System.out.println("Can't load properties file");
        }
    }

    public static Connection getDBConnection() {

        Connection dbConnection = null;
        accessToProperties();
        try {

            Class.forName("com.mysql.jdbc.Driver");

        } catch (ClassNotFoundException e) {

            System.out.println(e.getMessage());

        }

        try {

            System.out.println(dbhostname + " " + dbport + " " + dbname + " " + username + " " + password);

//            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/flogmaster", "root", "");
            dbConnection = DriverManager.getConnection("jdbc:mysql://" + dbhostname + ":" + dbport + "/" + dbname, username, password);
//             dbConnection = DriverManager.getConnection("jdbc:mysql://adminBcRJQkH:yznUug9lVMZS@127.5.227.130:3306/");
            return dbConnection;

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        }

        return dbConnection;

    }

    public int getScore(int gameid, String player) throws SQLException {

        int score = 0;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select * from playboard where gameid=? and player=?");
            preparedStatement.setInt(1, gameid);
            preparedStatement.setString(2, player);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {

                score = rs.getInt("score");

            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return score;

    }

    public int getPastScore(int gameid, String player) throws SQLException {

        int oldscore = 0;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select * from playboard where gameid=? and player=?");
            preparedStatement.setInt(1, gameid);
            preparedStatement.setString(2, player);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {

                oldscore = rs.getInt("oldscore");

            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return oldscore;

    }

    public HashMap showScore(int gameid) throws SQLException {

        String datas = null;
        //DefaultTableModel model = new DefaultTableModel(new String[]{"Player", "Score"}, 0);

        HashMap hm = new HashMap();

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select * from playboard where gameid=?");
            preparedStatement.setInt(1, gameid);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                String name = rs.getString("player");
                int score = rs.getInt("score");
                hm.put(name, score);

                //model.addRow(new Object[]{name, score});
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }
        return hm;

    }

    public boolean addReadyUser(int gameid, String name) throws SQLException {
        boolean st = false;
        System.out.println("gihan db");
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            String insertStmt = "insert into playboard(gameid,player,status) values (?,?,?)";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setInt(1, gameid);
            queryStmt.setString(2, name);
            queryStmt.setString(3, "READY");
            queryStmt.executeUpdate();
            System.out.println("gihan db2");
//            dbConnection.commit(); // make changes permanent

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return st;
    }

    public boolean addUser(int gameid, String name) throws SQLException {
        boolean st = false;

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            String insertStmt = "insert into playboard(gameid,player,status) values (?,?,?)";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setInt(1, gameid);
            queryStmt.setString(2, name);
            queryStmt.setString(3, "INIT");
            queryStmt.executeUpdate();
//            dbConnection.commit(); // make changes permanent

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return st;
    }

    public boolean updateStatus(String status, int gameid, int round) throws SQLException {
        boolean st = false;

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            String insertStmt = "UPDATE playboard set status =?,round=? where gameid=?";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setString(1, status);
            queryStmt.setInt(2, round);
            queryStmt.setInt(3, gameid);
            queryStmt.executeUpdate();

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return st;
    }

    public boolean setScore(int gameid, String player, int round, int score, int pastScore) throws SQLException {
        boolean st = false;

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            String insertStmt = "UPDATE playboard set status =?,score=?,oldscore=? where player =? and gameid=?";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setString(1, "ROUNDFINISH");
            queryStmt.setInt(2, score);
            queryStmt.setInt(3, pastScore);
            queryStmt.setString(4, player);
            queryStmt.setInt(5, gameid);
            queryStmt.executeUpdate();

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return st;
    }

    public int playerCount(int gameid) throws SQLException {
        boolean st = false;
        int numberOfRows = 0;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select count(*) from playboard where gameid=?");
            preparedStatement.setInt(1, gameid);

            ResultSet rs = preparedStatement.executeQuery();
            st = rs.next();
            if (!st) {
                System.out.println("not found other users");
                return 0;
            } else {
                numberOfRows = rs.getInt(1);
                return numberOfRows;
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return numberOfRows;

    }

    public int getMaxScore(int gameid) throws SQLException {
        boolean st = false;
        int maxScore = 0;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select max(score),player from playboard where gameid=?");
            preparedStatement.setInt(1, gameid);

            ResultSet rs = preparedStatement.executeQuery();
            st = rs.next();

//              while (rs.next()) {
//                maxScore = rs.getInt(1);   
//            }
            if (!st) {
                System.out.println("not found other users");
                return 0;
            } else {
                maxScore = rs.getInt(1);
                String player = rs.getString("player");

                return maxScore;
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return maxScore;

    }

    public int checkReadyCount(String player, int gameid, int round) throws SQLException {

        boolean st = false;
        int numberOfRows = 0;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select count(*) from playboard where gameid=? and status=? and round=?");
            preparedStatement.setInt(1, gameid);
            preparedStatement.setString(2, "INIT");
            preparedStatement.setInt(3, round);

            ResultSet rs = preparedStatement.executeQuery();
            st = rs.next();
            if (!st) {
                System.out.println("not found other users");
                return 0;
            } else {
                numberOfRows = rs.getInt(1);
                return numberOfRows;
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return numberOfRows;

    }

    public int checkRoundFinishCount(String player, int gameid, int round, String status) throws SQLException {

        boolean st = false;
        int numberOfRows = 0;
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            preparedStatement = dbConnection.prepareStatement("select count(*) from playboard where gameid=? and round=? and status=?");
            preparedStatement.setInt(1, gameid);
            preparedStatement.setInt(2, round);
            preparedStatement.setString(3, status);

            ResultSet rs = preparedStatement.executeQuery();
            st = rs.next();
            if (!st) {
                System.out.println("not found other users");
                return 0;
            } else {
                numberOfRows = rs.getInt(1);
                return numberOfRows;
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return numberOfRows;
    }

    public void updateRoundStatus(int gameid, String player, int round, String word, String status) throws SQLException {

        boolean st = false;

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            System.out.println("nimo " + status + round + word + player + gameid);
            String insertStmt = "UPDATE playboard set status =?,round=?,word=? where player =? and gameid=?";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setString(1, status);
            queryStmt.setInt(2, round);
            queryStmt.setString(3, word);
            queryStmt.setString(4, player);
            queryStmt.setInt(5, gameid);

            queryStmt.executeUpdate();

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

    }

    public void updateStatus(int gameid, String player, int round) throws SQLException {

        boolean st = false;

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            String insertStmt = "UPDATE playboard set status =?,round=? where player =? and gameid=?";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setString(1, "READY");
            queryStmt.setInt(2, round);
            queryStmt.setString(3, player);
            queryStmt.setInt(4, gameid);
            queryStmt.executeUpdate();

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

    }

    public boolean addWord(String name, String word) throws SQLException {
        boolean st = false;

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        try {

            dbConnection = getDBConnection();

            String insertStmt = "insert into playboard(name,word) values (?,?)";
            PreparedStatement queryStmt = dbConnection.prepareStatement(insertStmt);

            queryStmt.setString(1, name);
            queryStmt.setString(2, "WAITING");
            queryStmt.executeUpdate();
//            dbConnection.commit(); // make changes permanent

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return st;
    }
}
