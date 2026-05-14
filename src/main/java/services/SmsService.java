package services;

import javafx.scene.control.Alert;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SmsService {
    
    // CLÉS INFOBIP RÉELLES
    private static final String API_KEY = "2f91a9567b287cd5d497fc3b8198f9ff-5bd94734-c8e6-41b1-87a4-7b0bb84c7316";
    private static final String BASE_URL = "x19x6q.api.infobip.com";

    public static void sendSms(String toNumber, String message) {
        // Nettoyer le numéro (enlever le + pour Infobip si besoin)
        final String cleanNumber = toNumber.replace("+", "").replace(" ", "");
        
        System.out.println("Tentative d'envoi via INFOBIP vers " + cleanNumber + "...");

        new Thread(() -> {
            try {
                URL url = new URL("https://" + BASE_URL + "/sms/2/text/advanced");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "App " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Construction du JSON pour Infobip
                String jsonBody = "{"
                    + "\"messages\": ["
                    + "  {"
                    + "    \"destinations\": [{\"to\":\"" + cleanNumber + "\"}],"
                    + "    \"from\": \"Ardhi\","
                    + "    \"text\": \"" + message + "\""
                    + "  }"
                    + "]"
                    + "}";

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 201) {
                    System.out.println("✅ SMS ENVOYÉ VIA INFOBIP !");
                } else {
                    System.out.println("❌ Erreur Infobip : " + responseCode);
                    Scanner s = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                    System.out.println("Détail : " + (s.hasNext() ? s.next() : ""));
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur critique : " + e.getMessage());
            }
        }).start();

        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Système SMS (Infobip)");
            alert.setHeaderText("Notification");
            alert.setContentText("Tentative d'envoi du message vers " + toNumber);
            alert.show();
        });
    }
}
