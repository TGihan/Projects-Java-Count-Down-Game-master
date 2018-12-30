/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spacex.floggame.sub;

import com.spacex.floggame.FlogElement;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class GetSupport extends FlogElement{
    
    //NOTE: send support request email to flog support team
    public GetSupport(String question,String receivedEmail){
    
        final String username = "floggamegihan@gmail.com";
        final String password = "flog1992";

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("floggamegihan@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("gihangreen@gmail.com"));
            message.setSubject("Flog Game Suppport Requested From " +receivedEmail);
            message.setText(question);

            Transport.send(message);

            JOptionPane.showConfirmDialog(null, "Your Question successfully sent to Flog Game support team \n"+
                            "Our team member will be contact you soon.", "", JOptionPane.DEFAULT_OPTION);

        } catch (MessagingException e) {
            
            JOptionPane.showConfirmDialog(null, "Please check your internet connection", "", JOptionPane.DEFAULT_OPTION);

        }
    
    }

}
