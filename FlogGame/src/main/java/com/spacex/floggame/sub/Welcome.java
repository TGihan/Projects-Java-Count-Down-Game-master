/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.floggame.sub;

import com.spacex.floggame.FlogElement;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;

public class Welcome extends JWindow {
  private int duration;
  public Welcome(int d) {
    duration = d;
  }

  // A simple little method to show a title screen in the center
  // of the screen for the amount of time given in the constructor
  public void showSplash() {
    JPanel content = (JPanel)getContentPane();
    content.setBackground(Color.white);

    // Set the window's bounds, centering the window
    int width = 440;
    int height =240;
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (screen.width-width)/2;
    int y = (screen.height-height)/2;
    setBounds(x,y,width,height);

    // Build the splash screen
    JLabel label = new JLabel(new ImageIcon(Welcome.class
                    .getClassLoader()
                    .getResource("images//loading.png")));
    
      //("Copyright 2014, Gihan Corporation GRC Area Human Power Scanning ", JLabel.CENTER);
    //copyrt.setFont(new Font("tahoma", Font.PLAIN, 12));
    content.add(label, BorderLayout.CENTER);    

    // Display it
    setVisible(true);

    // Wait a little while, maybe while loading resources
    try { Thread.sleep(duration); } catch (Exception e) {}

    setVisible(false);
  }

  public void showSplashAndExit() {
    showSplash();
      FlogElement nj=new FlogElement();
    nj.setVisible(true);
  }

  public static void main(String[] args) {
    // Throw a nice little title page up on the screen first
    Welcome splash = new Welcome(5000);
    // Normally, we'd call splash.showSplash() and get on with the program.
    // But, since this is only a test...
    splash.showSplashAndExit();
  }
}