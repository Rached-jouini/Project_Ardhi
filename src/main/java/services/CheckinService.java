package services;

import java.sql.*;
import java.util.concurrent.TimeUnit;

public class CheckinService {

    // Scheduler STATIQUE : survit au garbage collector, partagé par toute l'appli
    private static final java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newScheduledThreadPool(4);

    private final EmailService emailService = new EmailService();
    private final CertificateService certificateService = new CertificateService();

    public void validatePresence(int inscriptionId) {
        try {
            // 1. Marquer comme présent dans les deux tables
            Connection conn = utils.MyDataBase.getInstance().getConnection();

            String query = "UPDATE inscription SET statut_participation = 'Présent' WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, inscriptionId);
            ps.executeUpdate();

            // Sync vers participantsevenement
            String getEmail = "SELECT email FROM inscription WHERE id = ?";
            PreparedStatement psEmail = conn.prepareStatement(getEmail);
            psEmail.setInt(1, inscriptionId);
            ResultSet rsEmail = psEmail.executeQuery();
            if (rsEmail.next()) {
                String email = rsEmail.getString("email");
                String updateParticipant = "UPDATE participantsevenement SET statut = 'Présent' WHERE email = ?";
                PreparedStatement psP = conn.prepareStatement(updateParticipant);
                psP.setString(1, email);
                psP.executeUpdate();
            }

            System.out.println("[CHECKIN] Inscription #" + inscriptionId + " → PRESENTE en BDD.");

            // 2. Récupérer les infos du participant et de l'événement
            String getInfo = "SELECT i.nom, i.email, e.nom as event_nom FROM inscription i " +
                             "JOIN evenement e ON i.id_evenement = e.id WHERE i.id = ?";
            ps = conn.prepareStatement(getInfo);
            ps.setInt(1, inscriptionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                final String userName  = rs.getString("nom");
                final String userEmail = rs.getString("email");
                final String eventName = rs.getString("event_nom");

                // 3. Programmer l'envoi du certificat dans 5 minutes
                System.out.println("[CHECKIN] Certificat programmé dans 5 minutes pour " + userName + " (" + userEmail + ")");

                scheduler.schedule(() -> {
                    try {
                        System.out.println("[CHECKIN] ⏱ Génération du certificat pour " + userName);
                        String pdfPath = certificateService.generateCertificate(userName, eventName);

                        String subject = "🏅 Votre Certificat de Présence - " + eventName;
                        String body =
                            "<html><body style='font-family:\"Segoe UI\",sans-serif;background:#f4f7f6;padding:20px;color:#333'>" +
                            "<div style='max-width:600px;margin:auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1)'>" +

                            // Header
                            "<div style='background:linear-gradient(135deg,#1B4332,#2D6A4F);padding:40px 20px;text-align:center;color:white'>" +
                            "<h1 style='margin:0;font-size:28px'>🌿 Ardhi Platform</h1>" +
                            "<p style='margin-top:8px;opacity:0.9;font-size:15px'>Certificat Officiel de Présence</p>" +
                            "</div>" +

                            // Body
                            "<div style='padding:40px 30px;line-height:1.7'>" +
                            "<h2 style='color:#1B4332'>Félicitations, " + userName + " !</h2>" +
                            "<p style='font-size:16px;margin:15px 0'>Nous confirmons votre présence à l'événement :</p>" +
                            "<div style='background:#f0fff4;padding:15px;border-left:4px solid #2D6A4F;margin:20px 0;font-weight:bold;color:#1B4332;font-size:18px'>" +
                            "📅 " + eventName + "</div>" +
                            "<p>Votre présence a été <strong>validée et enregistrée</strong> sur la Plateforme Ardhi. " +
                            "Le certificat PDF joint à ce mail atteste officiellement de votre participation.</p>" +
                            "<div style='text-align:center;margin:30px 0'>" +
                            "<span style='background:#1B4332;color:white;padding:12px 28px;border-radius:50px;font-weight:bold;font-size:15px'>✅ Présence Confirmée</span>" +
                            "</div></div>" +

                            // Footer
                            "<div style='background:#f8f9fa;padding:20px;text-align:center;border-top:1px solid #eee;font-size:12px;color:#888'>" +
                            "<strong>Ardhi - Plateforme de Gestion Agricole</strong><br/>" +
                            "Innover pour l'agriculture de demain 🌱" +
                            "</div></div></body></html>";

                        emailService.sendEmailWithAttachment(userEmail, subject, body, pdfPath);
                        new java.io.File(pdfPath).delete();
                        System.out.println("[CHECKIN] ✅ Certificat envoyé à " + userEmail);

                    } catch (Exception ex) {
                        System.err.println("[CHECKIN] ❌ Erreur certificat : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }, 5, TimeUnit.MINUTES); // ← 5 minutes comme demandé
            }

        } catch (SQLException e) {
            System.err.println("[CHECKIN] Erreur BDD : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
