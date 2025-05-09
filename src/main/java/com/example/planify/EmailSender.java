package com.example.planify;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.Session;

public class EmailSender {
    public void EmailSend(String toEmail, String nameOfAssignee, String messageText)
    {
        final String fromEmail = "planify.notify@gmail.com";
        final String appPassword = "lobw cgou cnqw invt";
        if (toEmail == null) {
            System.out.println("Email cannot be null");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Planify"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Hey " + nameOfAssignee + " !");
            message.setText(messageText);

            Transport.send(message);
            System.out.println("Message sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending message!");
        }
    }

    public static void main(String[] args) {

    }
}
