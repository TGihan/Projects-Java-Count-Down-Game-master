/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.floggame;

import com.spacex.floggame.database.DBControl;
import com.spacex.floggame.sub.EmailValidator;
import com.spacex.floggame.sub.GameSounds;
import com.spacex.floggame.sub.GetSupport;
import com.spacex.model.FunctionElement;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * n
 *
 * @author Tharindu Gihan
 */
public class FlogElement extends javax.swing.JFrame {

    private Point start_drag;
    private Point start_loc;
    Socket sock;
    BufferedReader reader;
    PrintWriter writer;
    InputStream inputStream = null;
    Timer timer;
    GameSounds music;
    File musicfile;
    Properties props = new Properties();

    ArrayList<String> userList = new ArrayList();
    ArrayList<Character> defaultLetters = new ArrayList();
    public List<Character> letterStack = new ArrayList<>();
    ArrayList<Integer> maxScores = new ArrayList();

    int Port;
    int gameid = 0;
    int stage = 0;
    int checkWait = 0;

    String playerID = null;
    String username;
    String serverIP;
    int groupCount = 2;
    int connectedCount=0;

    Boolean isConnected = false;

    public FlogElement() {
        setLayout(new GridBagLayout());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, 950, 600, 35, 35));

            }
        });

        initComponents();

        roundNo.setText(String.valueOf(1));
        mygif.setVisible(false);
        mygifsub.setVisible(false);
        usersList.setVisible(false);
        onlineLabel.setVisible(false);
        readyWaiting.setVisible(false);
        timerStatus.setVisible(false);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);

        userNameLabel.setText("");
        hidGameID.setVisible(false);

        settingSide.setVisible(false);
        winnerMark.setVisible(false);
        backslide.setVisible(false);

        jToggleButton1.setBackground(new Color(0, 204, 102));

        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel3);
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel6.removeAll();
        jPanel6.repaint();
        jPanel6.revalidate();

        jPanel6.add(jPanel7);
        jPanel6.repaint();
        jPanel6.revalidate();

        try {
            accessToProperties();
        } catch (Exception e) {
        }

    }

    //NOTE: Get properties file details
    public void accessToProperties() {

        try {
            inputStream = new FileInputStream("config.properties");
            props.load(inputStream);
            System.out.println("Game Server IP is := " + props.getProperty("SERVER_IP"));
            serverIP = props.getProperty("SERVER_IP");

            //serverIP="127.5.227.130"; openshit server
            Port = Integer.valueOf(props.getProperty("SERVER_PORT"));
            System.out.println("Game Server Listen Port is := " + props.getProperty("SERVER_PORT"));
            inputStream.close();

        } catch (IOException e) {
            System.out.println("Can't load properties file");
        }
    }

    //NOTE: Main message reader all imcoming server messages reading in this section
    public class IncomingReader implements Runnable {

        @Override
        public void run() {
            String[] data;
            String waiting = "Waiting", canstart = "CanStart", started = "Started", allready = "AllReady";
            String stream, done = "Done", connect = "Connect", disconnect = "Disconnect", chat = "Chat";
            String allfinish = "AllFinishRound";

            try {
                while ((stream = reader.readLine()) != null) {

                    //NOTE: All messages split by ':' mark and get splitted data 
                    data = stream.split(":");

                    if (data[2].equals(connect)) {
                        userAdd(data[0]);

                    } else if (data[2].equals(disconnect)) {

                        userRemove(data[0]);

                    } else if (data[2].equals(done)) {

                        userList.clear();

                    } else if (data[2].equals(waiting)) {
                        System.out.println("Waiting list " + data[0]);
                        String opponentlist = stringSplitter(data[0]);
                        String[] oppo = opponentlist.split(",");

                        writeUsers(oppo);
                        System.out.println("check wait " + checkWait + " " + gameid);

                        if (checkWait == 0) {
                            startGame.setVisible(true);
                            generateLetters.setVisible(true);
                            music.close();
                        } else {
                            startGame.setVisible(false);
                        }
                        //NOTE: Get player start game signal    
                    } else if (data[2].equals(started)) {

                        mygif.setVisible(false);
                        mygifsub.setVisible(false);
                        stage = 2;
                        System.out.println("music " + music);

                        if (gameid == 0) {

                            gameid = Integer.valueOf(data[0]);
                            System.out.println("My name " + usernameField.getText());

                            System.out.println("Game id " + gameid);

                            System.out.println("Default letters " + data[3]);

                            String nlist = stringSplitter(data[3]);

                            jPanel1.removeAll();
                            jPanel1.repaint();
                            jPanel1.revalidate();

                            jPanel1.add(jPanel2);
                            jPanel1.repaint();
                            jPanel1.revalidate();
                            settingSide.setVisible(true);

                            showDefLetters(nlist);

                            try {
                                DBControl dbc = new DBControl();
                                dbc.addUser(gameid, username);
                            } catch (Exception e) {
                                JOptionPane.showConfirmDialog(null, " Hi " + username + "\n Database connection failed. \n Please configure config.properties file correctly", "", JOptionPane.DEFAULT_OPTION);
                                System.exit(1);
                            }

                            hidGameID.setText(String.valueOf(gameid));
                        }

                        //NOTE: Read all finish message to start timer for all players   
                    } else if (data[2].equals(allready) && (data[0].equals(hidGameID.getText()))) {

                        music.close();
                        File file = new File("classes//sounds//clock.mp3");
                        music = new GameSounds(file.getAbsolutePath());
                        music.play();

                        timerStatus.setVisible(true);
                        jPanel6.removeAll();
                        jPanel6.repaint();
                        jPanel6.revalidate();

                        jPanel6.add(jPanel10);
                        jPanel6.repaint();
                        jPanel6.revalidate();
                        stage = 2;

                        finishRound.setEnabled(true);
                        checkWord.setEnabled(true);
                        timerRemain();
                        timerRemain.setText("Type your longest word here");
                        timer = new Timer();
                        timer.schedule(new FlogElement.UpdateUITask(), 0, 1000);

                    } else if (data[2].equals(allfinish) && (data[0].equals(hidGameID.getText()))) {

                        nextDefaultLetters.setVisible(false);
                        nextRound.setVisible(false);
                        stage = 3;
                        roundStatus.setText("After Round " + data[3] + " Score");

                        if ("5".equals(data[3])) {
                            roundStatus.setText("Final Score");

                        }

//                        if (data[5].equals(username)) {
                        System.out.println("it for me");

                        DBControl dBControl = new DBControl();
                        try {

                            HashMap hm;
                            hm = dBControl.showScore(Integer.valueOf(hidGameID.getText()));

                            System.out.println("users count" + hm.size());
                            connectedCount=hm.size();
                            for (int i = 0; i < hm.size(); i++) {
                                music.close();
                            }

                            writeScore(hm);

                            jPanel1.removeAll();
                            jPanel1.repaint();
                            jPanel1.revalidate();

                            jPanel1.add(jPanel11);
                            jPanel1.repaint();
                            jPanel1.revalidate();

                        } catch (NumberFormatException | SQLException e) {
                            e.getMessage();
                        }

                        nextRound.setText(data[3]);
                        nextDefaultLetters.setText(data[4]);
//                        }
                    }

                }
            } catch (IOException | NumberFormatException | SQLException ex) {
            }
        }
    }

    //NOTE: Reset GUI for next round
    public void resetBoxes() {

        box3.setText("");
        box4.setText("");
        box5.setText("");
        box6.setText("");
        box7.setText("");
        box8.setText("");
        box9.setText("");
        box10.setText("");
        box11.setText("");
        box12.setText("");

        replaceText.setText("");
    }

    public void ListenThread() {
        Thread IncomingReader = new Thread(new IncomingReader());
        IncomingReader.start();
    }

    public void userAdd(String data) {
        userList.add(data);

    }

    public void userRemove(String data) {
        JOptionPane.showConfirmDialog(null, data + " has disconnected.", "", JOptionPane.DEFAULT_OPTION);

    }

    public void writeScore(HashMap hm) throws SQLException {

        // Display elements
        playerNames.setText("");
        scores.setText("");
        DBControl dbc = new DBControl();
        int maxValue = (int) Collections.max(hm.values());

        System.out.println("maxxx " + maxValue);

        int round = Integer.parseInt(roundNo.getText());
        int score = dbc.getScore(gameid, username);
        int pastScore = dbc.getPastScore(gameid, username);
        int newScore = 0;
        System.out.println("my score " + score);
//        if (maxValue != score) {
//            FunctionElement functionElement = new FunctionElement();
//            newScore = functionElement.punishing(maxValue, score, username, gameid);
//        } else {
//            newScore = score;
//        }

        dbc.setScore(gameid, username, round, score + pastScore, 0);
        dbc.updateStatus("INIT", gameid, round + 1);

        String winnerName;
        hm = dbc.showScore(Integer.valueOf(hidGameID.getText()));
        Set set = hm.entrySet();
        int maxaValue = (int) Collections.max(hm.values());
        // Get an iterator
        Iterator i = set.iterator();

        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            if (me.getValue().toString().equals(String.valueOf(maxaValue))) {
                winnerName = me.getKey().toString();
                System.out.println("winnerName " + winnerName);

                if (winnerName.equals(username) && "5".equals(roundNo.getText())) {
                    winnerMark.setVisible(true);
                }
            }

            if (playerNames.getText().isEmpty()) {
                playerNames.setText(playerNames.getText() + me.getKey());
                scores.setText(scores.getText() + me.getValue());
            } else {

                playerNames.setText("<html>" + playerNames.getText() + "<br><br>" + me.getKey());
                scores.setText("<html>" + scores.getText() + "<br><br>" + me.getValue());
            }

        }
    }

    public void writeUsers(String[] oppoents) {

        if (gameid == 0) {
            usersList.setText("");
            for (int i = 0; i < oppoents.length; i++) {
                usersList.setText("<html>" + usersList.getText() + "<br>" + oppoents[i]);
            }
        }

    }

    //NOTE: Send disconnect signal to server
    public void sendDisconnect() {

        String bye = (username + ": :Disconnect");
        try {
            writer.println(bye); // Sends server the disconnect signal.
            writer.flush(); // flushes the buffer
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(null, "Could not send Disconnect message.", "", JOptionPane.DEFAULT_OPTION);
        }

    }

    public void Disconnect() {

        try {
            JOptionPane.showConfirmDialog(null, "Disconnected.", "", JOptionPane.DEFAULT_OPTION);
            sock.close();
        } catch (HeadlessException | IOException ex) {
            JOptionPane.showConfirmDialog(null, "Failed to disconnect.", "", JOptionPane.DEFAULT_OPTION);
        }
        isConnected = false;
        usernameField.setEditable(true);

    }

    //NOTE: Show default letters in first two boxes
    public void showDefLetters(String defLett) throws SQLException {
        List<String> gameDefLeters = new ArrayList<>();
        String[] arr = defLett.split(",");
        for (int i = 0; i < arr.length; i++) {
            gameDefLeters.add(arr[i]);
        }
        System.out.println("leter stacks " + letterStack);
        letterStack.add(gameDefLeters.get(0).charAt(0));
        letterStack.add(gameDefLeters.get(1).charAt(0));
        System.out.println("leter stacks1 " + letterStack);
        box1.setText(gameDefLeters.get(0));
        box2.setText(gameDefLeters.get(1));

    }

    public String stringSplitter(String arr) {
        int length = arr.length();
        String list = arr.substring(1, length - 1).replaceAll("\\s+", "");
        return list;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        box2 = new javax.swing.JLabel();
        box1 = new javax.swing.JLabel();
        box4 = new javax.swing.JLabel();
        box5 = new javax.swing.JLabel();
        box7 = new javax.swing.JLabel();
        box3 = new javax.swing.JLabel();
        box6 = new javax.swing.JLabel();
        box9 = new javax.swing.JLabel();
        box8 = new javax.swing.JLabel();
        box10 = new javax.swing.JLabel();
        box11 = new javax.swing.JLabel();
        box12 = new javax.swing.JLabel();
        roundLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        comboVowel = new javax.swing.JComboBox();
        comboConstant = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        replaceLetter = new javax.swing.JButton();
        replaceText = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        generateLetters = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        readyWaiting = new javax.swing.JLabel();
        imready = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        longWord = new javax.swing.JTextField();
        checkWord = new javax.swing.JButton();
        timerRemain = new javax.swing.JLabel();
        finishRound = new javax.swing.JButton();
        roundNo = new javax.swing.JLabel();
        hidGameID = new javax.swing.JLabel();
        timerStatus = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        roundStatus = new javax.swing.JLabel();
        moveNext = new javax.swing.JButton();
        nextRound = new javax.swing.JLabel();
        nextDefaultLetters = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        playerNames = new javax.swing.JLabel();
        scores = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        startGame = new javax.swing.JButton();
        setting = new javax.swing.JButton();
        howtoplay = new javax.swing.JButton();
        support = new javax.swing.JButton();
        mygif = new javax.swing.JLabel();
        mygifsub = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel12 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        settingSide = new javax.swing.JButton();
        onlineLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        usersList = new javax.swing.JLabel();
        backslide = new javax.swing.JLabel();
        winnerMark = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        gameID = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(800, 550));
        jPanel4.setLayout(null);

        jLabel4.setFont(new java.awt.Font("Impact", 1, 36)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("SpaceX ");
        jPanel4.add(jLabel4);
        jLabel4.setBounds(20, 0, 170, 60);

        jLabel20.setBackground(new java.awt.Color(54, 54, 54));
        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mini.png"))); // NOI18N
        jLabel20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel20.setPreferredSize(new java.awt.Dimension(8, 18));
        jLabel20.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jLabel20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel20MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel20MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel20MouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel20MousePressed(evt);
            }
        });
        jPanel4.add(jLabel20);
        jLabel20.setBounds(780, 0, 80, 60);

        jLabel21.setBackground(new java.awt.Color(54, 54, 54));
        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        jLabel21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel21MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel21MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel21MouseExited(evt);
            }
        });
        jPanel4.add(jLabel21);
        jLabel21.setBounds(860, 0, 80, 60);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.CardLayout());

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));
        jPanel2.setOpaque(false);

        box2.setBackground(new java.awt.Color(0, 51, 255));
        box2.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box2.setForeground(new java.awt.Color(255, 255, 255));
        box2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box2.setOpaque(true);

        box1.setBackground(new java.awt.Color(0, 51, 255));
        box1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box1.setForeground(new java.awt.Color(255, 255, 255));
        box1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box1.setOpaque(true);

        box4.setBackground(new java.awt.Color(0, 153, 204));
        box4.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box4.setForeground(new java.awt.Color(255, 255, 255));
        box4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box4.setOpaque(true);

        box5.setBackground(new java.awt.Color(0, 153, 204));
        box5.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box5.setForeground(new java.awt.Color(255, 255, 255));
        box5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box5.setOpaque(true);

        box7.setBackground(new java.awt.Color(0, 153, 204));
        box7.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box7.setForeground(new java.awt.Color(255, 255, 255));
        box7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box7.setOpaque(true);

        box3.setBackground(new java.awt.Color(0, 153, 204));
        box3.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box3.setForeground(new java.awt.Color(255, 255, 255));
        box3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box3.setOpaque(true);

        box6.setBackground(new java.awt.Color(0, 153, 204));
        box6.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box6.setForeground(new java.awt.Color(255, 255, 255));
        box6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box6.setOpaque(true);

        box9.setBackground(new java.awt.Color(0, 153, 204));
        box9.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box9.setForeground(new java.awt.Color(255, 255, 255));
        box9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box9.setOpaque(true);

        box8.setBackground(new java.awt.Color(0, 153, 204));
        box8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box8.setForeground(new java.awt.Color(255, 255, 255));
        box8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box8.setOpaque(true);

        box10.setBackground(new java.awt.Color(0, 153, 204));
        box10.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box10.setForeground(new java.awt.Color(255, 255, 255));
        box10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box10.setOpaque(true);

        box11.setBackground(new java.awt.Color(0, 153, 204));
        box11.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box11.setForeground(new java.awt.Color(255, 255, 255));
        box11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box11.setOpaque(true);

        box12.setBackground(new java.awt.Color(0, 153, 204));
        box12.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        box12.setForeground(new java.awt.Color(255, 255, 255));
        box12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        box12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        box12.setOpaque(true);

        roundLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 18)); // NOI18N
        roundLabel.setForeground(new java.awt.Color(255, 255, 255));
        roundLabel.setText("Round :");

        jLabel2.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Vowels");

        jLabel3.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Constants");

        comboVowel.setBackground(new java.awt.Color(0, 153, 204));
        comboVowel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));
        comboVowel.setBorder(null);

        comboConstant.setBackground(new java.awt.Color(0, 153, 204));
        comboConstant.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "7", "8", "9" }));
        comboConstant.setBorder(null);

        jPanel6.setBackground(new java.awt.Color(51, 51, 51));
        jPanel6.setOpaque(false);
        jPanel6.setLayout(new java.awt.CardLayout());

        jPanel8.setBackground(new java.awt.Color(51, 51, 51));
        jPanel8.setOpaque(false);

        replaceLetter.setBackground(new java.awt.Color(255, 51, 51));
        replaceLetter.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        replaceLetter.setForeground(new java.awt.Color(255, 255, 255));
        replaceLetter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/replace.png"))); // NOI18N
        replaceLetter.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, new java.awt.Color(255, 51, 51), java.awt.Color.white, null));
        replaceLetter.setBorderPainted(false);
        replaceLetter.setContentAreaFilled(false);
        replaceLetter.setFocusPainted(false);
        replaceLetter.setFocusable(false);
        replaceLetter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceLetterActionPerformed(evt);
            }
        });

        replaceText.setBackground(new java.awt.Color(204, 204, 204));
        replaceText.setColumns(1);
        replaceText.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        replaceText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        replaceText.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 0, 153), 6));
        replaceText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceTextActionPerformed(evt);
            }
        });
        replaceText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                replaceTextKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                replaceTextKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(replaceLetter, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(replaceText, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 599, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(replaceText, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceLetter, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel8, "card9");

        jPanel7.setBackground(new java.awt.Color(51, 51, 51));
        jPanel7.setOpaque(false);

        generateLetters.setBackground(new java.awt.Color(0, 204, 153));
        generateLetters.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        generateLetters.setForeground(new java.awt.Color(255, 255, 255));
        generateLetters.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fillletters.png"))); // NOI18N
        generateLetters.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 204, 51), new java.awt.Color(0, 204, 153), new java.awt.Color(0, 204, 102), new java.awt.Color(0, 204, 153)));
        generateLetters.setBorderPainted(false);
        generateLetters.setContentAreaFilled(false);
        generateLetters.setFocusPainted(false);
        generateLetters.setFocusable(false);
        generateLetters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateLettersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generateLetters, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(692, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generateLetters)
                .addContainerGap(107, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel7, "card8");

        jPanel9.setBackground(new java.awt.Color(51, 51, 51));
        jPanel9.setOpaque(false);

        readyWaiting.setBackground(new java.awt.Color(0, 0, 0));
        readyWaiting.setFont(new java.awt.Font("Impact", 0, 24)); // NOI18N
        readyWaiting.setForeground(new java.awt.Color(255, 255, 255));
        readyWaiting.setText("                         Waiting your opponents ready");
        readyWaiting.setToolTipText("");

        imready.setBackground(new java.awt.Color(255, 204, 0));
        imready.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        imready.setForeground(new java.awt.Color(255, 255, 255));
        imready.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ready.png"))); // NOI18N
        imready.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, new java.awt.Color(255, 51, 51), java.awt.Color.white, null));
        imready.setBorderPainted(false);
        imready.setContentAreaFilled(false);
        imready.setFocusPainted(false);
        imready.setFocusable(false);
        imready.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imreadyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imready, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(readyWaiting)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(imready)
                    .addComponent(readyWaiting))
                .addContainerGap(106, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel9, "card8");

        jPanel10.setBackground(new java.awt.Color(51, 51, 51));
        jPanel10.setOpaque(false);

        longWord.setBackground(new java.awt.Color(204, 204, 204));
        longWord.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        longWord.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        longWord.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 204), 3));
        longWord.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                longWordCaretUpdate(evt);
            }
        });
        longWord.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                longWordInputMethodTextChanged(evt);
            }
        });
        longWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longWordActionPerformed(evt);
            }
        });
        longWord.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                longWordKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                longWordKeyTyped(evt);
            }
        });

        checkWord.setBackground(new java.awt.Color(0, 153, 204));
        checkWord.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        checkWord.setForeground(new java.awt.Color(255, 255, 255));
        checkWord.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/checkword.png"))); // NOI18N
        checkWord.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, new java.awt.Color(255, 51, 51), java.awt.Color.white, null));
        checkWord.setBorderPainted(false);
        checkWord.setContentAreaFilled(false);
        checkWord.setFocusPainted(false);
        checkWord.setFocusable(false);
        checkWord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkWordActionPerformed(evt);
            }
        });

        timerRemain.setFont(new java.awt.Font("Impact", 0, 24)); // NOI18N
        timerRemain.setText("Time Over");

        finishRound.setBackground(new java.awt.Color(0, 204, 153));
        finishRound.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/finish.png"))); // NOI18N
        finishRound.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(0, 204, 153), null, new java.awt.Color(0, 204, 153), java.awt.Color.white));
        finishRound.setBorderPainted(false);
        finishRound.setContentAreaFilled(false);
        finishRound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishRoundActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timerRemain)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(longWord, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(finishRound))
                        .addGap(18, 18, 18)
                        .addComponent(checkWord, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(409, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(timerRemain)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(checkWord)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(longWord, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(finishRound, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel6.add(jPanel10, "card8");

        roundNo.setFont(new java.awt.Font("Comic Sans MS", 1, 18)); // NOI18N
        roundNo.setForeground(new java.awt.Color(255, 255, 255));
        roundNo.setText("1");

        hidGameID.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        hidGameID.setForeground(new java.awt.Color(0, 153, 255));

        timerStatus.setFont(new java.awt.Font("Segoe UI", 1, 60)); // NOI18N
        timerStatus.setForeground(new java.awt.Color(255, 255, 255));
        timerStatus.setText("00:00");

        jLabel17.setFont(new java.awt.Font("Impact", 1, 36)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("I Need");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(box1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(box2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(75, 75, 75)
                                                .addComponent(box5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(box6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(comboVowel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel3)
                                                .addGap(18, 18, 18)
                                                .addComponent(comboConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(113, 113, 113))))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                                .addGap(136, 136, 136)
                                                .addComponent(box3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(box4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(box7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(box8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(box9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(box10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(18, 18, 18)
                                        .addComponent(box11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(box12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(95, 95, 95)))
                                .addComponent(timerStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(roundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(roundNo, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(hidGameID, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(206, 206, 206)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(hidGameID, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(roundLabel)
                            .addComponent(roundNo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(comboVowel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)
                                    .addComponent(comboConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(box6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(box10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(box7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(timerStatus))
                .addGap(70, 70, 70)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        jPanel1.add(jPanel2, "card2");

        jPanel11.setBackground(new java.awt.Color(51, 51, 51));
        jPanel11.setOpaque(false);

        roundStatus.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        roundStatus.setForeground(new java.awt.Color(255, 255, 255));
        roundStatus.setText("jLabel6");

        moveNext.setBackground(new java.awt.Color(255, 204, 0));
        moveNext.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        moveNext.setForeground(new java.awt.Color(255, 255, 255));
        moveNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/nextround.png"))); // NOI18N
        moveNext.setBorder(null);
        moveNext.setBorderPainted(false);
        moveNext.setContentAreaFilled(false);
        moveNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveNextActionPerformed(evt);
            }
        });

        nextRound.setText("jLabel6");

        nextDefaultLetters.setText("jLabel6");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Player Name");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Score");

        playerNames.setFont(new java.awt.Font("Impact", 0, 24)); // NOI18N
        playerNames.setForeground(new java.awt.Color(255, 255, 255));
        playerNames.setText("jLabel19");
        playerNames.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        scores.setFont(new java.awt.Font("Impact", 0, 24)); // NOI18N
        scores.setForeground(new java.awt.Color(255, 255, 255));
        scores.setText("jLabel22");
        scores.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addComponent(nextDefaultLetters)
                .addGap(18, 18, 18)
                .addComponent(nextRound)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(moveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(playerNames, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(roundStatus))
                        .addGap(230, 230, 230)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scores, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 143, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(roundStatus)
                .addGap(39, 39, 39)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(playerNames, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scores, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(moveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(nextRound)
                        .addComponent(nextDefaultLetters)))
                .addGap(52, 52, 52))
        );

        jPanel1.add(jPanel11, "card9");

        jPanel3.setBackground(new java.awt.Color(51, 51, 51));
        jPanel3.setOpaque(false);

        jLabel1.setFont(new java.awt.Font("Comic Sans MS", 1, 36)); // NOI18N
        jLabel1.setText("Enter a nick name");

        usernameField.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        usernameField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        usernameField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255), 4));
        usernameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameFieldActionPerformed(evt);
            }
        });
        usernameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                usernameFieldKeyPressed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Calibri", 0, 36)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(0, 153, 204));
        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/countdown.png"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(251, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(85, 85, 85))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(139, 139, 139))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(72, Short.MAX_VALUE)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(117, 117, 117))
        );

        jPanel1.add(jPanel3, "card3");

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));
        jPanel5.setOpaque(false);

        startGame.setBackground(new java.awt.Color(51, 51, 51));
        startGame.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        startGame.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play-now.png"))); // NOI18N
        startGame.setBorder(null);
        startGame.setBorderPainted(false);
        startGame.setContentAreaFilled(false);
        startGame.setFocusPainted(false);
        startGame.setFocusable(false);
        startGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startGameActionPerformed(evt);
            }
        });

        setting.setBackground(new java.awt.Color(255, 0, 102));
        setting.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        setting.setForeground(new java.awt.Color(255, 255, 255));
        setting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings.png"))); // NOI18N
        setting.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(255, 255, 255), new java.awt.Color(255, 0, 102), java.awt.Color.white, new java.awt.Color(255, 0, 102)));
        setting.setBorderPainted(false);
        setting.setContentAreaFilled(false);
        setting.setFocusPainted(false);
        setting.setFocusable(false);
        setting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingActionPerformed(evt);
            }
        });

        howtoplay.setBackground(new java.awt.Color(255, 204, 0));
        howtoplay.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        howtoplay.setForeground(new java.awt.Color(255, 255, 255));
        howtoplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/howtoplay.png"))); // NOI18N
        howtoplay.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(255, 255, 255), new java.awt.Color(255, 0, 102), java.awt.Color.white, new java.awt.Color(255, 0, 102)));
        howtoplay.setBorderPainted(false);
        howtoplay.setContentAreaFilled(false);
        howtoplay.setFocusPainted(false);
        howtoplay.setFocusable(false);
        howtoplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                howtoplayActionPerformed(evt);
            }
        });

        support.setBackground(new java.awt.Color(0, 153, 255));
        support.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        support.setForeground(new java.awt.Color(255, 255, 255));
        support.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/support.png"))); // NOI18N
        support.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(255, 255, 255), new java.awt.Color(255, 0, 102), java.awt.Color.white, new java.awt.Color(255, 0, 102)));
        support.setBorderPainted(false);
        support.setContentAreaFilled(false);
        support.setFocusPainted(false);
        support.setFocusable(false);
        support.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supportActionPerformed(evt);
            }
        });

        mygif.setFont(new java.awt.Font("Comic Sans MS", 1, 48)); // NOI18N
        mygif.setForeground(new java.awt.Color(255, 255, 255));

        mygifsub.setFont(new java.awt.Font("Comic Sans MS", 1, 48)); // NOI18N
        mygifsub.setForeground(new java.awt.Color(255, 255, 255));
        mygifsub.setText("Waiting for opponent...");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(74, 74, 74)
                .addComponent(support, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(howtoplay, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(setting, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(mygifsub)
                        .addGap(73, 73, 73))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(startGame, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(197, 197, 197))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(mygif, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(279, 279, 279))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addComponent(startGame, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mygifsub)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mygif, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(howtoplay, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(support, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(setting, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(57, 57, 57))
        );

        jPanel1.add(jPanel5, "card3");

        jPanel13.setBackground(new java.awt.Color(51, 51, 51));
        jPanel13.setOpaque(false);

        jLabel12.setFont(new java.awt.Font("Comic Sans MS", 1, 36)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("How to play this game");

        jLabel13.setFont(new java.awt.Font("Comic Sans MS", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("<html> <center> You should select required vowels and constant count first <br> After generate letters you have one chance to replace a letter<br> After all team players ready game will start<br> You should enter longest english word from above generated letters<br>  <h3 color=\"#ffffff\">Score and bonus is caculated as below:</h3>  if your word contain all the letters or word length greater than 4 you will add bonus<br> if your word is invalid your score will be zero<br> </center> </htmll>");

        jButton3.setBackground(new java.awt.Color(51, 51, 51));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.png"))); // NOI18N
        jButton3.setContentAreaFilled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(191, 191, 191)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap(124, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addGap(247, 247, 247))))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel12)
                .addGap(53, 53, 53)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(62, 62, 62))
        );

        jPanel1.add(jPanel13, "card3");

        jPanel14.setBackground(new java.awt.Color(51, 51, 51));
        jPanel14.setOpaque(false);

        jLabel14.setFont(new java.awt.Font("Comic Sans MS", 1, 36)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Settings");

        jButton4.setBackground(new java.awt.Color(51, 51, 51));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.png"))); // NOI18N
        jButton4.setContentAreaFilled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Comic Sans MS", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Players:");

        jLabel16.setFont(new java.awt.Font("Comic Sans MS", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Music : ");

        jToggleButton1.setSelected(true);
        jToggleButton1.setText("On");
        jToggleButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jToggleButton1StateChanged(evt);
            }
        });
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Two", "Three", "Four" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jComboBox1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBox1PropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(288, 288, 288)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel14Layout.createSequentialGroup()
                            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel15)
                                .addComponent(jLabel16))
                            .addGap(51, 51, 51)
                            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel14Layout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jButton4))))
                .addContainerGap(268, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jLabel14)
                .addGap(104, 104, 104)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(27, 27, 27)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 114, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(66, 66, 66))
        );

        jPanel1.add(jPanel14, "card3");

        jPanel12.setBackground(new java.awt.Color(51, 51, 51));
        jPanel12.setOpaque(false);

        jLabel9.setFont(new java.awt.Font("Comic Sans MS", 1, 36)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Flog Support Team");

        jButton1.setBackground(new java.awt.Color(51, 51, 51));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.png"))); // NOI18N
        jButton1.setContentAreaFilled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(51, 51, 51));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/submit.png"))); // NOI18N
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel15.setOpaque(false);

        jLabel10.setFont(new java.awt.Font("Comic Sans MS", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Please enter your email address");

        jTextField1.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextField1.setAlignmentX(1.5F);
        jTextField1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255), 3));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Comic Sans MS", 1, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Give your questions or feedback");

        jScrollPane2.setBorder(null);
        jScrollPane2.setOpaque(false);

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Comic Sans MS", 1, 12)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255), 3));
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1))
                .addContainerGap(398, Short.MAX_VALUE))
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 589, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(203, Short.MAX_VALUE)))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(34, 34, 34)
                .addComponent(jLabel10)
                .addGap(28, 28, 28)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(64, Short.MAX_VALUE))
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addGap(43, 43, 43)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(42, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel9)
                        .addGap(43, 43, 43)))
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel12, "card3");

        jPanel4.add(jPanel1);
        jPanel1.setBounds(0, 50, 760, 540);

        settingSide.setBackground(new java.awt.Color(0, 153, 204));
        settingSide.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        settingSide.setForeground(new java.awt.Color(255, 255, 255));
        settingSide.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settingssmall.png"))); // NOI18N
        settingSide.setBorder(null);
        settingSide.setBorderPainted(false);
        settingSide.setContentAreaFilled(false);
        settingSide.setFocusPainted(false);
        settingSide.setFocusable(false);
        settingSide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingSideActionPerformed(evt);
            }
        });
        jPanel4.add(settingSide);
        settingSide.setBounds(780, 130, 160, 50);

        onlineLabel.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        onlineLabel.setForeground(new java.awt.Color(255, 255, 255));
        onlineLabel.setText("~~Joined Players ~~");
        jPanel4.add(onlineLabel);
        onlineLabel.setBounds(790, 240, 150, 21);

        userNameLabel.setFont(new java.awt.Font("Impact", 0, 36)); // NOI18N
        userNameLabel.setForeground(new java.awt.Color(255, 255, 255));
        userNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        userNameLabel.setText("User Name");
        jPanel4.add(userNameLabel);
        userNameLabel.setBounds(170, 10, 250, 40);

        usersList.setFont(new java.awt.Font("Impact", 0, 18)); // NOI18N
        usersList.setForeground(new java.awt.Color(255, 255, 255));
        usersList.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        usersList.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel4.add(usersList);
        usersList.setBounds(800, 270, 140, 320);

        backslide.setBackground(new java.awt.Color(51, 51, 51));
        backslide.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        backslide.setForeground(new java.awt.Color(255, 255, 255));
        jPanel4.add(backslide);
        backslide.setBounds(760, 80, 190, 520);

        winnerMark.setFont(new java.awt.Font("Impact", 0, 24)); // NOI18N
        winnerMark.setForeground(new java.awt.Color(255, 255, 51));
        winnerMark.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/youwin.png"))); // NOI18N
        jPanel4.add(winnerMark);
        winnerMark.setBounds(450, 12, 290, 40);

        jLabel5.setBackground(new java.awt.Color(51, 51, 51));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mainsback.png"))); // NOI18N
        jLabel5.setMaximumSize(new java.awt.Dimension(800, 550));
        jLabel5.setMinimumSize(new java.awt.Dimension(800, 550));
        jLabel5.setOpaque(true);
        jLabel5.setPreferredSize(new java.awt.Dimension(1280, 700));
        jLabel5.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jLabel5MouseDragged(evt);
            }
        });
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel5MouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel5MousePressed(evt);
            }
        });
        jPanel4.add(jLabel5);
        jLabel5.setBounds(0, 0, 950, 600);

        gameID.setText("jLabel2");
        jPanel4.add(gameID);
        gameID.setBounds(40, 50, 34, 14);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 950, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel20MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MouseClicked
        this.setState(FlogElement.ICONIFIED);
    }//GEN-LAST:event_jLabel20MouseClicked

    private void jLabel20MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MouseEntered
        jLabel20.setBackground(Color.BLUE);
    }//GEN-LAST:event_jLabel20MouseEntered

    private void jLabel20MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MouseExited
        jLabel20.setBackground(new java.awt.Color(54, 54, 54));
    }//GEN-LAST:event_jLabel20MouseExited

    private void jLabel20MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel20MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel20MousePressed

    private void jLabel21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MouseClicked

        System.exit(0);
    }//GEN-LAST:event_jLabel21MouseClicked

    private void jLabel21MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MouseEntered
        jLabel21.setBackground(Color.red);
    }//GEN-LAST:event_jLabel21MouseEntered

    private void jLabel21MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel21MouseExited
        jLabel21.setBackground(new java.awt.Color(54, 54, 54));
    }//GEN-LAST:event_jLabel21MouseExited

    private void startGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startGameActionPerformed

        checkWait = 1;
        try {

            musicfile = new File("classes//sounds//america.mp3");
            music = new GameSounds(musicfile.getAbsolutePath());
            music.play();

            System.out.println("file path " + musicfile.getAbsolutePath());

            //Icon icon1 = new ImageIcon("src//main//resources//gihan/images//magnify.fig");
            Icon icon = new ImageIcon(getClass().getResource("/images/magnify.gif"));
            mygif.setVisible(true);
            mygifsub.setVisible(true);
            mygif.setIcon(icon);
            writer.println(username + ":" + "is" + ":" + "Waiting");
            writer.flush();

            startGame.setVisible(false);
            startGame.setEnabled(false);

            int randomplayers = 2 + (int) (Math.random() * 4);
            String players = String.valueOf(randomplayers);
            try {
                writer.println(username + ":" + "want" + ":" + "ToStart" + ":" + "for " + ":" + players);
                writer.flush();
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(null, "To start message was not sent.", "", JOptionPane.DEFAULT_OPTION);
            }
        } catch (HeadlessException e) {
            e.getMessage();
        }
    }//GEN-LAST:event_startGameActionPerformed

    //NOTE: Timer stop when it reach 30 seconds
    public class UpdateUITask extends TimerTask {

        int nSeconds = 0;

        @Override
        public void run() {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    String rep = String.valueOf(nSeconds);
                    if (rep.length() <= 1) {
                        timerStatus.setText("00:0" + String.valueOf(nSeconds++));
                    } else {
                        timerStatus.setText("00:" + String.valueOf(nSeconds++));
                    }
                    if (nSeconds > 30) {
                        longWord.setEditable(false);
                        longWord.setEnabled(false);

                        timerRemain.setText("Time Over");
                        sendFinishRound();
                        timerRemain.setIcon(null);
                        timer.cancel();

                    }
                }
            });
        }
    }

    //NOTE: Replace a letter from letter stack
    public List<Character> replaceLetter(char letter) {

        List<Character> getLetterStack = letterStack;
        char defaultOne = letterStack.get(0);
        char defaultTwo = letterStack.get(1);

        getLetterStack.remove(0);
        getLetterStack.remove(0);

        for (int i = 0; i < 1; i++) {
            int rand = (int) (Math.random() * getLetterStack.size());
            getLetterStack.remove(rand);
            getLetterStack.add(rand, letter);
        }
        getLetterStack.add(0, defaultOne);
        getLetterStack.add(1, defaultTwo);

        System.out.println(getLetterStack);
        return getLetterStack;
    }

    //NOTE: Get requsted constants count by using function element class methods
    public void getConstants(int constantCount) {
        if (letterStack.size() > 1) {
            if (letterStack.size() > 12) {
                letterStack.clear();
            }
            List<String> lists = new ArrayList<>();
            String alphabet = "BCDFGHJKLMNPQRSTVXZWY";
            List<String> values = new ArrayList<>(Arrays.asList(alphabet.split("")));
            for (int row = 0; row < 1; row++) {
                Collections.shuffle(values);
                for (int col = 0; col < constantCount; col++) {

                    lists.add(values.get(col));
                    System.out.println("requested contant count " + constantCount);
                    System.out.println("letter stack " + letterStack);
                    System.out.println("values " + values);

                    if (letterStack.contains(values.get(col).charAt(0))) {
                        System.out.println("giho lists " + lists);
                        System.out.println("giho col " + col);
                        if (col == 0) {
                            lists.remove(col);
                            System.out.println("removed 1 " + values.get(col));
                            constantCount++;
                        } else {
                            if (col == lists.size()) {
                                lists.remove(col - 1);
                                System.out.println("removed 3" + values.get(col));
                                constantCount++;
                            } else {
                                lists.remove(col);
                                System.out.println("removed 4" + values.get(col));
                                constantCount++;
                            }

                        }
                    }
                }
                for (int i = 0; i < lists.size(); i++) {
                    letterStack.add(lists.get(i).charAt(0));
                }
            }

        }

    }

    //NOTE: Get requsted vowels count by using function element class methods
    public void getVowels(int vowelCount) {

        if (letterStack.size() > 1) {

            if (letterStack.size() > 12) {
                letterStack.clear();
            }

            List<String> lists = new ArrayList<>();
            String alphabet = "AEIOU";
            List<String> values = new ArrayList<>(Arrays.asList(alphabet.split("")));
            for (int row = 0; row < 1; row++) {
                Collections.shuffle(values);
                for (int col = 0; col < vowelCount; col++) {
                    lists.add(values.get(col));
                }
                for (int i = 0; i < lists.size(); i++) {
                    letterStack.add(lists.get(i).charAt(0));
                }
            }
        }
    }

    //NOTE: Show all letters after all letters changed
    public void showLetters(List letterlist) {

        System.out.println("Final letter list " + letterlist);
        for (int i = 0; i < letterStack.size(); i++) {
            if (i == 0) {
                box1.setText(letterStack.get(i).toString());
            }
            if (i == 1) {
                box2.setText(letterStack.get(i).toString());
            }
            if (i == 2) {
                box3.setText(letterStack.get(i).toString());
            }
            if (i == 3) {
                box4.setText(letterStack.get(i).toString());
            }
            if (i == 4) {
                box5.setText(letterStack.get(i).toString());
            }
            if (i == 5) {
                box6.setText(letterStack.get(i).toString());
            }
            if (i == 6) {
                box7.setText(letterStack.get(i).toString());
            }
            if (i == 7) {
                box8.setText(letterStack.get(i).toString());
            }
            if (i == 8) {
                box9.setText(letterStack.get(i).toString());
            }
            if (i == 9) {
                box10.setText(letterStack.get(i).toString());
            }
            if (i == 10) {
                box11.setText(letterStack.get(i).toString());
            }
            if (i == 11) {
                box12.setText(letterStack.get(i).toString());
            }

        }

    }

    //NOTE: Generate letters with function element class methods
    private void generateLettersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateLettersActionPerformed
        int valueVowel = Integer.valueOf(comboVowel.getSelectedItem().toString());
        int valueCons = Integer.valueOf(comboConstant.getSelectedItem().toString());
        System.out.println("leter stacks2 " + letterStack);
        int total = valueVowel + valueCons;

        if (total == 10) {
            System.out.println("after default letter stack " + letterStack.size());

            getVowels(valueVowel);
            getConstants(valueCons);
            showLetters(letterStack);
            jPanel6.removeAll();
            jPanel6.repaint();
            jPanel6.revalidate();

            jPanel6.add(jPanel8);
            jPanel6.repaint();
            jPanel6.revalidate();

        } else {
            JOptionPane.showConfirmDialog(null, "Total letter count must be 10",
                    "", JOptionPane.DEFAULT_OPTION);
        }

    }//GEN-LAST:event_generateLettersActionPerformed

    //NOTE: Calling Replace letter method
    private void replaceLetterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceLetterActionPerformed
        generateLetters.setVisible(false);
        boolean match = replaceText.getText().matches("[a-zA-Z]+");
        if (replaceText.getText().isEmpty()) {

            JOptionPane.showConfirmDialog(null, "Please enter a replace letter", "", JOptionPane.DEFAULT_OPTION);
        } else if (!match) {
            replaceText.setText("");
            JOptionPane.showConfirmDialog(null, "Please enter a English letter", "", JOptionPane.DEFAULT_OPTION);
        } else {
            String repLetter = replaceText.getText().toUpperCase();
            char letter = repLetter.charAt(0);
            List newLetterStack = replaceLetter(letter);
            showLetters(newLetterStack);
            jPanel6.removeAll();
            jPanel6.repaint();
            jPanel6.revalidate();

            jPanel6.add(jPanel9);
            jPanel6.repaint();
            jPanel6.revalidate();

            imready.setVisible(true);
            readyWaiting.setVisible(false);
//            } else {
//                JOptionPane.showConfirmDialog(null, "Replace letter should not same to current letters.", "", JOptionPane.DEFAULT_OPTION);
//            }
        }


    }//GEN-LAST:event_replaceLetterActionPerformed

    public void waitingOpponentReady() {
        imready.setVisible(false);
        Icon icon = new ImageIcon(getClass().getResource("/images/ellipsis.gif"));
        readyWaiting.setVisible(true);
        readyWaiting.setIcon(icon);
    }

    public void timerRemain() {
        Icon icon = new ImageIcon(getClass().getResource("/images/hourglass.gif"));

        timerRemain.setIcon(icon);
    }

    private void imreadyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imreadyActionPerformed
        try {
            writer.println(username + ":" + "want" + ":" + "Ready" + ":" + "Game id is " + ":" + hidGameID.getText() + ":" + roundNo.getText());
            writer.flush(); // flushes the buffer
            waitingOpponentReady();
            longWord.setText("");
            longWord.setEnabled(true);
            longWord.setEditable(true);

            music.close();

        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(null, "Ready message was not sent.", "", JOptionPane.DEFAULT_OPTION);
        }
    }//GEN-LAST:event_imreadyActionPerformed

    private void longWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longWordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_longWordActionPerformed

    private void longWordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_longWordKeyPressed
        String longword = longWord.getText();

        StringBuilder sb = new StringBuilder();
        for (Character character : letterStack) {
            sb.append(character);
        }
        System.out.println(sb.toString());
        String outer = sb.toString();

        String word = longword.toUpperCase();
        String list = outer;

        String[] items = word.split("");
        int count = 0;

        for (int i = 0; i < items.length; i++) {
            if (list.contains(items[i])) {
                count = count + 1;
            }
        }

        int length = word.length();
        System.out.println("string length " + length);
        System.out.println("count is " + count);

        if (count < length) {
            System.out.println("count is " + count);
            JOptionPane.showConfirmDialog(null, "You can't use out side letters", "", JOptionPane.DEFAULT_OPTION);
            longWord.setText("");
        }

        FunctionElement functionElement = new FunctionElement();

        boolean repeats = functionElement.Findrepeter(longWord.getText());

        if (repeats) {
            JOptionPane.showConfirmDialog(null, "A letter should be use at most once", "", JOptionPane.DEFAULT_OPTION);
            longWord.setText("");
        }
    }//GEN-LAST:event_longWordKeyPressed

    //NOTE: Feature check word validation before submit server
    private void checkWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkWordActionPerformed

        try {
            FunctionElement functionElement = new FunctionElement();
            boolean check = functionElement.checkWord(longWord.getText());

            if (check) {
                JOptionPane.showConfirmDialog(null, "Valid word",
                        "", JOptionPane.DEFAULT_OPTION);
            } else {
                JOptionPane.showConfirmDialog(null, "Inalid word",
                        "", JOptionPane.DEFAULT_OPTION);
            }

        } catch (URISyntaxException ex) {
            Logger.getLogger(FlogElement.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_checkWordActionPerformed

    private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameFieldActionPerformed
        // TODO add your handling code here:y
    }//GEN-LAST:event_usernameFieldActionPerformed

    private void usernameFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameFieldKeyPressed
        //insert to database
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

            boolean match = usernameField.getText().matches("[a-zA-Z]+");
            int length = usernameField.getText().length();

            if (match && length >= 4) {

                if (isConnected == false) {
                    username = usernameField.getText();
                    usernameField.setEditable(false);
                    userNameLabel.setText(usernameField.getText());

                    try {

                        sock = new Socket(serverIP, Port);
                        InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
                        reader = new BufferedReader(streamreader);
                        writer = new PrintWriter(sock.getOutputStream());

                        writer.println(username + ":has connected.:Connect"); // Displays to everyone that user connected.
                        writer.flush(); // flushes the buffer
                        isConnected = true; // Used to see if the client is connected.

                        jPanel1.removeAll();
                        jPanel1.repaint();
                        jPanel1.revalidate();

                        jPanel1.add(jPanel5);
                        jPanel1.repaint();
                        jPanel1.revalidate();

                        backslide.setVisible(true);
                        usersList.setVisible(true);
                        onlineLabel.setVisible(true);

                    } catch (Exception ex) {
                        JOptionPane.showConfirmDialog(null, " Cannot connect to server ! \n Please configure config.properties file Server IP and Port correclty. \n Try Again.", "", JOptionPane.DEFAULT_OPTION);
                        usernameField.setEditable(true);
                    }
                    ListenThread();
                } else if (isConnected == true) {
                    JOptionPane.showConfirmDialog(null, "You are already connected.", "", JOptionPane.DEFAULT_OPTION);
                }

            }

            if (!match) {
                JOptionPane.showConfirmDialog(null, "You can't enter special letters or numbers",
                        "", JOptionPane.DEFAULT_OPTION);

            } else if (length < 4) {
                JOptionPane.showConfirmDialog(null, "Username must have at least 4 character",
                        "", JOptionPane.DEFAULT_OPTION);

            }

        }
    }//GEN-LAST:event_usernameFieldKeyPressed
    //NOTE: Handle underated GUI points for mouse moving
    Point getScreenLocation(MouseEvent e) {
        Point cursor = e.getPoint();
        Point target_location = this.getLocationOnScreen();
        return new Point((int) (target_location.getX() + cursor.getX()),
                (int) (target_location.getY() + cursor.getY()));
    }
    private void jLabel5MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseDragged
        Point current = this.getScreenLocation(evt);
        Point offset = new Point((int) current.getX() - (int) start_drag.getX(),
                (int) current.getY() - (int) start_drag.getY());

        Point new_location = new Point(
                (int) (this.start_loc.getX() + offset.getX()), (int) (this.start_loc
                .getY() + offset.getY()));
        this.setLocation(new_location);
    }//GEN-LAST:event_jLabel5MouseDragged

    private void jLabel5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseEntered

    }//GEN-LAST:event_jLabel5MouseEntered

    private void jLabel5MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MousePressed
        this.start_drag = this.getScreenLocation(evt);
        this.start_loc = this.getLocation();
    }//GEN-LAST:event_jLabel5MousePressed

    private void replaceTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_replaceTextActionPerformed

    private void replaceTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_replaceTextKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_replaceTextKeyPressed

    private void replaceTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_replaceTextKeyTyped
        if (replaceText.getText().length() >= 1) {
            getToolkit().beep();
            evt.consume();
        }

    }//GEN-LAST:event_replaceTextKeyTyped

    //NOTE: Send finish round 
    public void sendFinishRound() {

        String longword = longWord.getText();

        if (longWord.getText() == null) {
            longWord.setText("");
        }

        String round = roundNo.getText();
        String tim = timerStatus.getText().substring(3, 5);

        String allLetters = getLetterStack(letterStack);

        System.out.println("to bab " + letterStack);
        letterStack.clear();
        timerStatus.setVisible(false);
        longWord.setText("Your word submitted");
        longWord.setEnabled(false);
        timer.cancel();

        if ("5".equals(round)) {
            Icon icon = new ImageIcon(getClass().getResource("/images/home.png"));
            moveNext.setIcon(icon);
            System.out.println("Game Finish");
        }

        try {
            writer.println(username + ":" + "Finished Round" + ":" + "FinishRound" + ":" + longword + ":" + "round is" + ":" + round + ":" + "Game id" + ":" + hidGameID.getText() + ":" + "Timer" + ":" + tim + ":" + "allLetters" + ":" + allLetters);
            writer.flush(); // flushes the buffer
        } catch (Exception ex) {
        }
//        }
    }

    private void finishRoundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishRoundActionPerformed
        sendFinishRound();
        finishRound.setEnabled(false);
        checkWord.setEnabled(false);
        music.close();
        music.close();
    }//GEN-LAST:event_finishRoundActionPerformed

    public String getLetterStack(List<Character> letters) {
        String allLetters = null;

        StringBuilder result = new StringBuilder(letters.size());
        for (Character c : letters) {
            result.append("," + c);
        }
        allLetters = result.toString().substring(1);

        return allLetters;

    }

    //NOTE: Move to next round
    private void moveNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveNextActionPerformed

        winnerMark.setVisible(false);

        musicfile = new File("classes//sounds//america.mp3");
        music = new GameSounds(musicfile.getAbsolutePath());
        music.play();

        if ("5".equals(roundNo.getText())) {
            jPanel1.removeAll();
            jPanel1.repaint();
            jPanel1.revalidate();

            jPanel1.add(jPanel5);
            jPanel1.repaint();
            jPanel1.revalidate();

            startGame.setVisible(true);
            startGame.setEnabled(true);
            settingSide.setVisible(false);

            for (int i = 0; i < connectedCount; i++) {
                music.close();
            }
            
            gameid = 0;
            stage = 0;
            checkWait = 0;
            roundNo.setText("1");
            resetBoxes();
            System.out.println("Back to home");
            jPanel6.removeAll();
            jPanel6.repaint();
            jPanel6.revalidate();

            jPanel6.add(jPanel7);
            jPanel6.repaint();
            jPanel6.revalidate();

        } else {
            try {
                int replaceRound = Integer.valueOf(nextRound.getText());
                int rep = replaceRound + 1;

                String nlist = stringSplitter(nextDefaultLetters.getText());
                showDefLetters(nlist);

                String newRound = String.valueOf(rep);

                timerStatus.setVisible(false);
                roundNo.setText(newRound);
                generateLetters.setVisible(true);

                resetBoxes();

                jPanel1.removeAll();
                jPanel1.repaint();
                jPanel1.revalidate();

                jPanel1.add(jPanel2);
                jPanel1.repaint();
                jPanel1.revalidate();

                jPanel6.removeAll();
                jPanel6.repaint();
                jPanel6.revalidate();

                jPanel6.add(jPanel7);
                jPanel6.repaint();
                jPanel6.revalidate();

            } catch (Exception e) {
            }
        }


    }//GEN-LAST:event_moveNextActionPerformed

    private void settingSideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingSideActionPerformed
        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel14);
        jPanel1.repaint();
        jPanel1.revalidate();


    }//GEN-LAST:event_settingSideActionPerformed

    private void supportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supportActionPerformed
        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel12);
        jPanel1.repaint();
        jPanel1.revalidate();

        Icon icon = new ImageIcon(getClass().getResource("/images/comment.gif"));
        jLabel8.setVisible(true);
        jLabel8.setIcon(icon);

        jTextArea1.setVisible(false);
        jScrollPane2.setVisible(false);
        jLabel11.setVisible(false);
        jLabel10.setVisible(true);
        jTextField1.setVisible(true);

        jButton2.setVisible(false);

    }//GEN-LAST:event_supportActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel5);
        jPanel1.repaint();
        jPanel1.revalidate();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String question = jTextArea1.getText();
        String playerEmail = jTextField1.getText();

        if (!"".equals(question)) {

            GetSupport getSupport = new GetSupport(question, playerEmail);

            jPanel1.removeAll();
            jPanel1.repaint();
            jPanel1.revalidate();

            jPanel1.add(jPanel5);
            jPanel1.repaint();
            jPanel1.revalidate();
        } else {
            JOptionPane.showConfirmDialog(null, "No question mentioned", "", JOptionPane.DEFAULT_OPTION);
        }


    }//GEN-LAST:event_jButton2ActionPerformed

    private void howtoplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_howtoplayActionPerformed
        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel13);
        jPanel1.repaint();
        jPanel1.revalidate();
    }//GEN-LAST:event_howtoplayActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel5);
        jPanel1.repaint();
        jPanel1.revalidate();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        if (stage == 2) {
            jPanel1.removeAll();
            jPanel1.repaint();
            jPanel1.revalidate();

            jPanel1.add(jPanel2);
            jPanel1.repaint();
            jPanel1.revalidate();

        } else if (stage == 3) {
            jPanel1.removeAll();
            jPanel1.repaint();
            jPanel1.revalidate();

            jPanel1.add(jPanel11);
            jPanel1.repaint();
            jPanel1.revalidate();
        } else {
            jPanel1.removeAll();
            jPanel1.repaint();
            jPanel1.revalidate();

            jPanel1.add(jPanel5);
            jPanel1.repaint();
            jPanel1.revalidate();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void settingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingActionPerformed
        jPanel1.removeAll();
        jPanel1.repaint();
        jPanel1.revalidate();

        jPanel1.add(jPanel14);
        jPanel1.repaint();
        jPanel1.revalidate();
    }//GEN-LAST:event_settingActionPerformed

    private void jToggleButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton1StateChanged

    }//GEN-LAST:event_jToggleButton1StateChanged

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if ("On".equals(jToggleButton1.getText())) {
            jToggleButton1.setText("Off");
            jToggleButton1.setBackground(Color.WHITE);
            music.close();
        } else {
            jToggleButton1.setText("On");
            music.play();
            jToggleButton1.setBackground(new Color(0, 204, 102));
        }

    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1KeyTyped

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

            EmailValidator emailValidator = new EmailValidator();

            boolean emailvalidation = emailValidator.validate(jTextField1.getText());

            if (emailvalidation) {

                jTextArea1.setVisible(true);
                jScrollPane2.setVisible(true);
                jLabel11.setVisible(true);

                jLabel10.setVisible(false);
                jTextField1.setVisible(false);
                jButton2.setVisible(true);
            } else {
                JOptionPane.showConfirmDialog(null, "Invalid emial", "", JOptionPane.DEFAULT_OPTION);
            }

        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBox1PropertyChange


    }//GEN-LAST:event_jComboBox1PropertyChange

    private void longWordKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_longWordKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_longWordKeyTyped

    private void longWordInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_longWordInputMethodTextChanged

    }//GEN-LAST:event_longWordInputMethodTextChanged

    private void longWordCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_longWordCaretUpdate


    }//GEN-LAST:event_longWordCaretUpdate

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FlogElement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FlogElement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FlogElement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FlogElement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FlogElement().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backslide;
    private javax.swing.JLabel box1;
    private javax.swing.JLabel box10;
    private javax.swing.JLabel box11;
    private javax.swing.JLabel box12;
    private javax.swing.JLabel box2;
    private javax.swing.JLabel box3;
    private javax.swing.JLabel box4;
    private javax.swing.JLabel box5;
    private javax.swing.JLabel box6;
    private javax.swing.JLabel box7;
    private javax.swing.JLabel box8;
    private javax.swing.JLabel box9;
    private javax.swing.JButton checkWord;
    private javax.swing.JComboBox comboConstant;
    private javax.swing.JComboBox comboVowel;
    private javax.swing.JButton finishRound;
    private javax.swing.JLabel gameID;
    private javax.swing.JButton generateLetters;
    private javax.swing.JLabel hidGameID;
    private javax.swing.JButton howtoplay;
    private javax.swing.JButton imready;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField longWord;
    private javax.swing.JButton moveNext;
    private javax.swing.JLabel mygif;
    private javax.swing.JLabel mygifsub;
    private javax.swing.JLabel nextDefaultLetters;
    private javax.swing.JLabel nextRound;
    private javax.swing.JLabel onlineLabel;
    private javax.swing.JLabel playerNames;
    private javax.swing.JLabel readyWaiting;
    private javax.swing.JButton replaceLetter;
    private javax.swing.JTextField replaceText;
    private javax.swing.JLabel roundLabel;
    private javax.swing.JLabel roundNo;
    private javax.swing.JLabel roundStatus;
    private javax.swing.JLabel scores;
    private javax.swing.JButton setting;
    private javax.swing.JButton settingSide;
    private javax.swing.JButton startGame;
    private javax.swing.JButton support;
    private javax.swing.JLabel timerRemain;
    private javax.swing.JLabel timerStatus;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usersList;
    private javax.swing.JLabel winnerMark;
    // End of variables declaration//GEN-END:variables
}
