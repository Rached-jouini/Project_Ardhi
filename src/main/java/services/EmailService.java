package services;

import utils.EmailConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class EmailService {

    // Identifiants par défaut (Version Farah)
    private final String username = "platformeardhi@gmail.com";
    private final String password = "knxb ugzy ehcw wxpk";

    public boolean isConfigured() {
        return !EmailConfig.SMTP_USERNAME.isBlank() && !EmailConfig.SMTP_PASSWORD.isBlank();
    }

    /**
     * Envoi de code de vérification (Version Sabri)
     */
    public boolean sendVerificationCode(String recipientEmail, String code, String subject, String heading) {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        Properties prop = new Properties();
        prop.put("mail.smtp.host", EmailConfig.SMTP_HOST.isEmpty() ? "smtp.gmail.com" : EmailConfig.SMTP_HOST);
        prop.put("mail.smtp.port", EmailConfig.SMTP_PORT.isEmpty() ? "587" : EmailConfig.SMTP_PORT);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        prop.put("mail.smtp.ssl.trust", "*");

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                String u = EmailConfig.SMTP_USERNAME.isEmpty() ? username : EmailConfig.SMTP_USERNAME;
                String p = EmailConfig.SMTP_PASSWORD.isEmpty() ? password : EmailConfig.SMTP_PASSWORD;
                return new PasswordAuthentication(u, p);
            }
        });

        try {
            Message message = new MimeMessage(session);
            String from = EmailConfig.SMTP_USERNAME.isEmpty() ? username : EmailConfig.SMTP_USERNAME;
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h2>" + heading + "</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Voici votre code de verification :</p>"
                    + "<h1 style='color: #2d6a43; letter-spacing: 5px; padding: 10px; background: #f4f4f4; border-radius: 5px; width: fit-content;'>"
                    + code
                    + "</h1>"
                    + "<p>Ce code expirera bientot. Si vous n'avez pas fait cette demande, veuillez ignorer cet email.</p>"
                    + "<br><p>L'equipe Ardhi.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendSignupVerificationCode(String recipientEmail, String code) {
        return sendVerificationCode(recipientEmail, code, "Verification de votre compte Ardhi", "Verification d'inscription");
    }

    public boolean sendResetCode(String recipientEmail, String code) {
        return sendVerificationCode(recipientEmail, code, "Code de reinitialisation de votre mot de passe", "Reinitialisation de mot de passe");
    }

    /**
     * Envoi d'e-mail avec pièce jointe (Version Farah - pour QR Code)
     */
    public void sendEmailWithAttachment(String to, String subject, String htmlBody, String filePath) throws MessagingException, IOException {
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

        Multipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(htmlBody, "text/html; charset=utf-8");
        multipart.addBodyPart(textPart);

        if (filePath != null && new File(filePath).exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(filePath));
            attachmentPart.setFileName("Mon_Pass_Ardhi.png");
            multipart.addBodyPart(attachmentPart);
        }

        message.setContent(multipart);
        Transport.send(message);
        System.out.println("[EMAIL] Succes : E-mail avec piece jointe envoye.");
    }
}
