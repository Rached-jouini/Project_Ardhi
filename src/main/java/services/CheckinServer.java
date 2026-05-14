package services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class CheckinServer {

    private static HttpServer server;
    private static String localIp = "localhost";
    private static final int PORT = 8080;

    public static void start() {
        try {
            // Trouver la vraie IP Wi-Fi (ignorer VMware, VirtualBox, loopback)
            localIp = getRealWifiIp();
            System.out.println("[SERVER] IP détectée : " + localIp);

            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/checkin", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String id = "?";
                String nom = "Participant";

                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=");
                        if (kv.length == 2) {
                            if (kv[0].equals("id")) id = kv[1];
                            if (kv[0].equals("nom")) nom = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        }
                    }
                }

                String html = buildPage(id, nom);
                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            });

            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
            server.start();
            System.out.println("[SERVER] Serveur démarré → http://" + localIp + ":" + PORT + "/checkin");

        } catch (Exception e) {
            System.err.println("[SERVER] Erreur démarrage serveur : " + e.getMessage());
        }
    }

    public static String getQrUrl(int inscriptionId, String nom) {
        try {
            return "http://" + localIp + ":" + PORT + "/checkin?id=" + inscriptionId
                    + "&nom=" + URLEncoder.encode(nom, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "http://" + localIp + ":" + PORT + "/checkin?id=" + inscriptionId;
        }
    }

    public static void stop() {
        if (server != null) server.stop(0);
    }

    private static String buildPage(String id, String nom) {
        return "<!DOCTYPE html>" +
            "<html lang='fr'>" +
            "<head>" +
            "  <meta charset='UTF-8'/>" +
            "  <meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
            "  <title>Ardhi - Validation</title>" +
            "  <style>" +
            "    * { margin:0; padding:0; box-sizing:border-box; }" +
            "    body { font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #1B4332 0%, #2D6A4F 50%, #40916C 100%); min-height:100vh; display:flex; align-items:center; justify-content:center; }" +
            "    .card { background:white; border-radius:24px; padding:40px 30px; max-width:380px; width:90%; text-align:center; box-shadow: 0 20px 60px rgba(0,0,0,0.3); animation: fadeIn 0.6s ease; }" +
            "    @keyframes fadeIn { from { opacity:0; transform:translateY(30px); } to { opacity:1; transform:translateY(0); } }" +
            "    .check-circle { width:90px; height:90px; background: linear-gradient(135deg, #1B4332, #40916C); border-radius:50%; display:flex; align-items:center; justify-content:center; margin: 0 auto 25px; box-shadow: 0 8px 25px rgba(27,67,50,0.4); }" +
            "    .check-icon { font-size:45px; }" +
            "    .logo { color:#1B4332; font-size:13px; font-weight:700; letter-spacing:3px; text-transform:uppercase; margin-bottom:5px; }" +
            "    h1 { color:#1B4332; font-size:26px; font-weight:800; margin-bottom:8px; }" +
            "    .subtitle { color:#6B7280; font-size:15px; margin-bottom:28px; }" +
            "    .info-box { background:#F0FDF4; border:2px solid #BBF7D0; border-radius:14px; padding:18px; margin:20px 0; }" +
            "    .info-row { display:flex; justify-content:space-between; align-items:center; padding:6px 0; }" +
            "    .info-label { color:#6B7280; font-size:13px; }" +
            "    .info-value { color:#1B4332; font-weight:700; font-size:14px; }" +
            "    .badge { background: linear-gradient(135deg, #1B4332, #40916C); color:white; padding:10px 24px; border-radius:50px; font-weight:700; font-size:14px; display:inline-block; margin-top:10px; letter-spacing:1px; }" +
            "    .footer { margin-top:25px; color:#9CA3AF; font-size:12px; }" +
            "    .divider { border:none; border-top:1px solid #E5E7EB; margin:15px 0; }" +
            "  </style>" +
            "</head>" +
            "<body>" +
            "  <div class='card'>" +
            "    <p class='logo'>🌿 Ardhi Platform</p>" +
            "    <div class='check-circle'><span class='check-icon'>✓</span></div>" +
            "    <h1>Présence Validée !</h1>" +
            "    <p class='subtitle'>Bienvenue, votre pass a été scanné avec succès.</p>" +
            "    <div class='info-box'>" +
            "      <div class='info-row'><span class='info-label'>Participant</span><span class='info-value'>" + nom + "</span></div>" +
            "      <hr class='divider'/>" +
            "      <div class='info-row'><span class='info-label'>Référence Pass</span><span class='info-value'>#" + id + "</span></div>" +
            "      <hr class='divider'/>" +
            "      <div class='info-row'><span class='info-label'>Statut</span><span class='info-value'>✅ Présent</span></div>" +
            "    </div>" +
            "    <div class='badge'>✓ Accès Autorisé</div>" +
            "    <p class='footer'>Plateforme de Gestion Agricole Ardhi<br/>Accueil de l'événement</p>" +
            "  </div>" +
            "</body></html>";
    }

    /**
     * Trouve la vraie IP Wi-Fi en ignorant les adaptateurs virtuels (VMware, VirtualBox, loopback)
     */
    private static String getRealWifiIp() {
        try {
            java.util.Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Ignorer les interfaces désactivées, loopback, et virtuelles
                if (!iface.isUp() || iface.isLoopback()) continue;

                String name = iface.getName().toLowerCase();
                String displayName = iface.getDisplayName().toLowerCase();

                // Ignorer VMware, VirtualBox, Docker, Hyper-V
                if (displayName.contains("vmware") || displayName.contains("virtual")
                        || displayName.contains("vbox") || displayName.contains("docker")
                        || displayName.contains("hyper") || displayName.contains("bluetooth")
                        || name.contains("vm") || name.contains("veth")) continue;

                java.util.Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Prendre uniquement les IPv4 privées (192.168.x.x ou 10.x.x.x)
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            System.out.println("[SERVER] Interface Wi-Fi trouvée : " + iface.getDisplayName() + " → " + ip);
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SERVER] Erreur détection IP : " + e.getMessage());
        }
        return "localhost";
    }
}
