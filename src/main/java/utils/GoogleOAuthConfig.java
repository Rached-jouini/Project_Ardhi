package utils;

public final class GoogleOAuthConfig {
    public static final String CLIENT_ID = EnvConfig.get("GOOGLE_CLIENT_ID", "");
    public static final String CLIENT_SECRET = EnvConfig.get("GOOGLE_CLIENT_SECRET", "");
    public static final String REDIRECT_URI = EnvConfig.get("GOOGLE_REDIRECT_URI", "http://localhost:8888/callback");
    public static final int CALLBACK_PORT = Integer.parseInt(EnvConfig.get("GOOGLE_CALLBACK_PORT", "8888"));
    public static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    public static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    public static final String SCOPES = "openid email profile";

    private GoogleOAuthConfig() {
    }
}
