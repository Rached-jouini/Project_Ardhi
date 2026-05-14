package services;

import utils.EmailConfig;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public boolean isConfigured() {
        return !EmailConfig.SMTP_USERNAME.isBlank() && !EmailConfig.SMTP_PASSWORD.isBlank();
    }

    public boolean sendVerificationCode(String recipientEmail, String code, String subject, String heading) {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        if (!isConfigured()) {
            throw new IllegalStateException("SMTP is not configured. Add SMTP_USERNAME and SMTP_PASSWORD to .env.");
        }

        Properties prop = new Properties();
        prop.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        prop.put("mail.smtp.port", EmailConfig.SMTP_PORT);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        prop.put("mail.smtp.ssl.trust", EmailConfig.SMTP_HOST);

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.SMTP_USERNAME, EmailConfig.SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.SMTP_USERNAME));
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
}
