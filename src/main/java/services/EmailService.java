package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class EmailService {

    private final String username = "platformeardhi@gmail.com";
    private final String password = "knxb ugzy ehcw wxpk\n";

    public void sendEmailWithAttachment(String to, String subject, String htmlBody, String filePath) throws MessagingException, java.io.IOException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Multipart pour le corps + la pièce jointe
        Multipart multipart = new MimeMultipart();

        // Partie 1 : Le texte HTML
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(htmlBody, "text/html; charset=utf-8");
        multipart.addBodyPart(textPart);

        // Partie 2 : La pièce jointe (Le QR Code)
        if (filePath != null && new File(filePath).exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(filePath));
            attachmentPart.setFileName("Mon_Pass_Ardhi.png");
            multipart.addBodyPart(attachmentPart);
        }

        message.setContent(multipart);
        Transport.send(message);
        System.out.println("[EMAIL] Succès : E-mail avec pièce jointe envoyé.");
    }
}
