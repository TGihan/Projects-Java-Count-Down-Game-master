/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.floggame.server;

import com.spacex.floggame.database.DBControl;
import com.spacex.floggame.model.FunctionElement;
import com.spacex.floggame.model.Game;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 *
 * @author Tharindu Gihan
 */
public class GameServer extends javax.swing.JFrame {

    ArrayList clientOutputStreams;
    ArrayList<String> onlineUsers;
    ArrayList<String> waitingUsers = new ArrayList<>();
    ArrayList<String> playingUsers;
    ArrayList<String> ToStartUsers = new ArrayList<>();
    Properties props = new Properties();
    InputStream inputStream;

    int serverPort;
    String serverIp;
    int groupCount = 2;

    public void accessToProperties() {

        try {

            inputStream = new FileInputStream("config.properties");
            props.load(inputStream);
            serverIp = props.getProperty("SERVER_IP");
            serverPort = Integer.parseInt(props.getProperty("SERVER_PORT"));

            ipaddress.setText(serverIp);
            portnum.setText(String.valueOf(serverPort));

        } catch (IOException e) {
            System.out.println("Can't load properties file");
        }
    }

    public class ClientHandler implements Runnable {

        BufferedReader reader;
        Socket sock;
        PrintWriter client;

        public ClientHandler(Socket clientSocket, PrintWriter user) {
            // new inputStreamReader and then add it to a BufferedReader
            client = user;
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            } // end try
            catch (Exception ex) {
                outputPane.append("Error beginning StreamReader. \n");
            } // end catch

        } // end ClientHandler()

        public void run() {
            String message, connect = "Connect", disconnect = "Disconnect", isset = "isset", chat = "Chat";
            String waiting = "Waiting", tostart = "ToStart", ready = "Ready", finishround = "FinishRound";
            String[] data;

            try {
                while ((message = reader.readLine()) != null) {

                    outputPane.append("Received: " + message + "\n");
                    data = message.split(":");
                    for (String token : data) {

                        outputPane.append(token + "\n");
                    }

                    if (data[2].equals(connect)) {

                        tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                        userAdd(data[0]);

                    } else if (data[2].equals(disconnect)) {

                        tellEveryone((data[0] + ":has disconnected." + ":" + chat));
                        userRemove(data[0]);

                    } else if (data[2].equals(chat)) {

                        tellEveryone(message);

                    } else if (data[2].equals(waiting)) {

                        sendWaitingUsers(data[0]);
                        if (waitingUsers.size() > 1) {
                            sendCanStart(data[0]);
                        }

                    } else if (data[2].equals(tostart)) {
                        ToStartUsers.add(data[0]);

                        createGame(data[0], data[4]);

                    } else if (data[2].equals(ready)) {

                        int rgameid = Integer.valueOf(data[4]);
                        int round = Integer.valueOf(data[5]);
                        addPlayerStatus(rgameid, data[0], round);
                        checkReadyCount(data[0], rgameid, round);

                    } else if (data[2].equals(finishround)) {

                        int rgameid = Integer.valueOf(data[7]);
                        int round = Integer.valueOf(data[5]);

                        int takeTime = Integer.valueOf(data[9]);
                        String allLetters = data[11];

                        updateRound(rgameid, data[0], round, data[3], "ROUNDFINISH", takeTime, allLetters);

                        checkFinishCount(data[0], rgameid, round, "READY");

                    } else if (data[2].equals(isset)) {

                    } else {
                        outputPane.append("No Conditions were met. \n");
                    }

                } // end while
            } // end try
            catch (Exception ex) {
                outputPane.append("Lost a connection. \n");
                ex.printStackTrace();
                clientOutputStreams.remove(client);
            } // end catch
        } // end run()
    } // end class ClientHandler

    public GameServer() {
        initComponents();
    }

    public class ServerStart implements Runnable {

        public void run() {
            clientOutputStreams = new ArrayList();
            onlineUsers = new ArrayList();

            try {
                ServerSocket serverSock = new ServerSocket(serverPort);

                while (true) {
                    // set up the server writer function and then begin at the same
                    // the listener using the Runnable and Thread
                    outputPane.append("Flog server started. \n");
                    Socket clientSock = serverSock.accept();

                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
                    clientOutputStreams.add(writer);

                    // use a Runnable to start a 'second main method that will run
                    // the listener
                    Thread listener = new Thread(new GameServer.ClientHandler(clientSock, writer));
                    listener.start();
                    outputPane.append("Got a connection. \n");
                } // end while
            } // end try
            catch (Exception ex) {
                outputPane.append("Error making a connection. \n");
            } // end catch

        } // end go()
    }

    public void checkFinishCount(String player, int gameid, int round, String status) throws SQLException {

        String allfinish = "AllFinishRound";
        DBControl dbc = new DBControl();

        int count = dbc.checkRoundFinishCount(player, gameid, round, status);

        if (count == 0) {

            int newRound = round + 1;
            ArrayList<Character> defaultLetters = new ArrayList();
            FunctionElement functionElement = new FunctionElement();
            defaultLetters = functionElement.getDefaultLetters();
            //gihoooo
            tellEveryone(gameid + ":" + "all players are" + ":" + allfinish + ":" + round + ":" + defaultLetters + ":" + player);
            System.out.println(gameid + " All are ready to next round");
            dbc.updateStatus("INIT", gameid, newRound);

        }
    }

    public int calculateScore(int gameid, String word, int time, String allLetters, String player) throws URISyntaxException, SQLException {
        int score = 0;

        //NOTE: Calculate Scrabble points for longest word by split letters
        FunctionElement functionElement = new FunctionElement();
        score = functionElement.calculateScrabblePoints(word);
        System.out.println(player + " word is " + word);
        System.out.println(player + " scrabble score is " + score);

        //NOTE: adding bonus to players accoding to word length and finish time
        if (time <= 15 && word.length() >= 5) {
            System.out.println("adding bonus to " + player);
            score = score + 10;
        } else {
            System.out.println("no bonus to " + player);
        }

        //NOTE: if all the letters are used score multiply by player count
        boolean checkAll = checkAllLetters(word, allLetters);
        if (checkAll) {
            System.out.println(player + " All letters are used");
            DBControl dBControl = new DBControl();
            int playerCount = dBControl.playerCount(gameid);
            score = score * playerCount;
        }

        return score;
    }

    public boolean checkAllLetters(String word, String list) {

        //gihan baba
        String[] items = word.split("");
        String[] split = list.split(",");
        int count = 0;

        for (int i = 0; i < items.length; i++) {
            if (list.contains(items[i])) {
                count = count + 1;
            }
        }

        return split.length == count;

    }

    public void updateRound(int gameid, String player, int round, String word, String status, int time, String allLetters) throws SQLException, URISyntaxException {

        DBControl dbc = new DBControl();

        //NOTE: Update round status, score and etc to database.
        dbc.updateRoundStatus(gameid, player, round, word, status);

        //NOTE: Calling and assign caculate functions
        int score = calculateScore(gameid, word, time, allLetters, player);

        //NOTE: Get past score for set final score
        int pastScore = dbc.getPastScore(gameid, player);

        //NOTE: Setting final score after all caculations
        dbc.setScore(gameid, player, round, score, pastScore);

    }

    //NOTE: Check all players ready state to start game
    public void checkReadyCount(String player, int gameid, int round) throws SQLException {

        String allready = "AllReady";
        DBControl dbc = new DBControl();
        int count = dbc.checkReadyCount(player, gameid, round);
        if (count == 0) {
            tellEveryone(gameid + ":" + "all players are" + ":" + allready);
            System.out.println(gameid + " All are ready to start round");
        }

    }

    //NOTE: Add player state ready and set past scores
    public void addPlayerStatus(int gameid, String player, int round) throws SQLException {

        DBControl dbc = new DBControl();
        dbc.updateStatus(gameid, player, round);
        int pastScore = dbc.getScore(gameid, player);
        dbc.setPastScore(gameid, player, pastScore);

    }

    public void createGame(String data, String players) {

        System.out.println("Now to start list " + ToStartUsers);

        String started = "Started";

        if (ToStartUsers.size() >= groupCount) {

            Game game = new Game();
            int randomNum = 5 + (int) (Math.random() * 6000);
            game.gameId = randomNum;

            //NOTE: Setting default letters for new game from functionelement class
            ArrayList<Character> defaultLetters = new ArrayList();
            FunctionElement functionElement = new FunctionElement();

            defaultLetters = functionElement.getDefaultLetters();

            for (String object : ToStartUsers) {
                game.gameUsers.add(object);
                tellEveryone(game.gameId + ":" + "game" + ":" + started + ":" + defaultLetters);
                waitingUsers.clear();
            }

            ToStartUsers.clear();

        }

    }

    public void sendCanStart(String data) {
        String canstart = "CanStart", name = data;
        String[] tempList = new String[(waitingUsers.size())];
        waitingUsers.toArray(tempList);

        for (String token : tempList) {
            tellEveryone(data + ":" + "you" + ":" + canstart);
        }
    }

    //NOTE: Send wating users list to player's sockets
    public void sendWaitingUsers(String data) {
        String message, add = ": :Connect", waiting = "Waiting", name = data;
        waitingUsers.add(data);
        String[] tempList = new String[(waitingUsers.size())];
        waitingUsers.toArray(tempList);

        for (String token : tempList) {
            tellEveryoneList(waitingUsers, ":" + "is" + ":" + waiting);
        }

    }

    //Note: Add players to server
    public void userAdd(String data) {
        String message, add = ": :Connect", done = "Server: :Done", name = data;

        onlineUsers.add(name);
        outputPane.append("After " + name + " added. \n");
        String[] tempList = new String[(onlineUsers.size())];
        onlineUsers.toArray(tempList);

        for (String token : tempList) {

            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }

    //NOTE: Remove disconnected users
    public void userRemove(String data) {
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        onlineUsers.remove(name);
        String[] tempList = new String[(onlineUsers.size())];
        onlineUsers.toArray(tempList);

        for (String token : tempList) {

            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }

    public void tellEveryoneList(List yourlist, String message) {
        // sends message to everyone connected to server
        Iterator it = clientOutputStreams.iterator();

        System.out.println("tell everyone list message " + yourlist + message);

        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(yourlist + message);
                outputPane.append("Sending: " + yourlist + message + "\n");
                writer.flush();
//                outputPane.setCaretPosition(outputPane.getDocument().getLength());

            } // end try
            catch (Exception ex) {
                outputPane.append("Error telling everyone. \n");
            } // end catch
        } // end while
    } // end tellEveryone()

    //NOTE: Server message send to all players
    public void tellEveryone(String message) {
        // sends message to everyone connected to server
        Iterator it = clientOutputStreams.iterator();

        System.out.println("tell everyone message " + message);

        while (it.hasNext()) {
            try {
                PrintWriter writer = (PrintWriter) it.next();
                writer.println(message);
                outputPane.append("Sending: " + message + "\n");
                writer.flush();
                //outputPane.setCaretPosition(outputPane.getDocument().getLength());

            } // end try
            catch (Exception ex) {
                outputPane.append("Error telling everyone. \n");
            } // end catch
        } // end while
    } // end tellEveryone()

    public int calculateScore(String word) {
        int score = 0;

        score = 5;

        return score;
    }

    //NOTE: Start server main thread
    public void starting() {
        accessToProperties();
        Thread starter = new Thread(new GameServer.ServerStart());
        starter.start();
    }

    public void stoping() {
        Thread stoping = new Thread(new GameServer.ServerStart());
        stoping.interrupt();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputPane = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        ipaddress = new javax.swing.JLabel();
        portnum = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(51, 51, 51));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));

        jButton1.setBackground(new java.awt.Color(0, 204, 153));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Start");
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 204, 153), 2));
        jButton1.setBorderPainted(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 102, 102));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Stop");
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 51, 51), 2));
        jButton2.setBorderPainted(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        outputPane.setEditable(false);
        outputPane.setBackground(new java.awt.Color(102, 102, 102));
        outputPane.setColumns(20);
        outputPane.setFont(new java.awt.Font("Consolas", 0, 14)); // NOI18N
        outputPane.setForeground(new java.awt.Color(255, 255, 255));
        outputPane.setRows(5);
        outputPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jScrollPane1.setViewportView(outputPane);

        jLabel1.setFont(new java.awt.Font("Impact", 0, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 153, 204));
        jLabel1.setText("FLOG SERVER LOG");

        jComboBox1.setBackground(new java.awt.Color(102, 102, 102));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "Random" }));
        jComboBox1.setBorder(null);
        jComboBox1.setOpaque(false);
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
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

        jLabel2.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 153, 255));
        jLabel2.setText("Players for a game");

        ipaddress.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        ipaddress.setForeground(new java.awt.Color(0, 153, 204));
        ipaddress.setText("Not Started");

        portnum.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        portnum.setForeground(new java.awt.Color(0, 153, 204));
        portnum.setText("Not Started");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ipaddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portnum))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1)
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ipaddress)
                        .addComponent(portnum))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 439, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(65, 65, 65)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        starting();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        stoping();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBox1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBox1PropertyChange
        
    }//GEN-LAST:event_jComboBox1PropertyChange

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        String selectedCount = (String) jComboBox1.getSelectedItem();
        JOptionPane.showConfirmDialog(null, "Server need restart to change effect", "", JOptionPane.DEFAULT_OPTION);
        String lower = selectedCount.toLowerCase();
        if ("two".equals(lower)) {
            groupCount = 2;

        } else if ("three".equals(lower)) {
            groupCount = 3;

        } else if ("four".equals(lower)) {
            groupCount = 4;

        } else if ("random".equals(lower)) {
            Random rand = new Random();
            int randomNum = rand.nextInt((4 - 2) + 1) + 2;
            System.out.println("ra "+randomNum);
            groupCount = randomNum;

        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GameServer gameServer = new GameServer();
                gameServer.setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ipaddress;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea outputPane;
    private javax.swing.JLabel portnum;
    // End of variables declaration//GEN-END:variables
}
