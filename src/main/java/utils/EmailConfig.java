package utils;

public final class EmailConfig {

    public static final String SMTP_USERNAME = EnvConfig.get("SMTP_USERNAME", "");
    public static final String SMTP_PASSWORD = EnvConfig.get("SMTP_PASSWORD", "");
    public static final String SMTP_HOST = EnvConfig.get("SMTP_HOST", "smtp.gmail.com");
    public static final String SMTP_PORT = EnvConfig.get("SMTP_PORT", "587");

    private EmailConfig() {
    }
}
