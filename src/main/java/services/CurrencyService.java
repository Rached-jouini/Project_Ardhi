package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class CurrencyService {

    // API Gratuite pour les taux de change
    private static final String API_URL = "https://open.er-api.com/v6/latest/TND";

    public static double convertTNDtoEUR(double amount) {
        try {
            double rate = getRate("EUR");
            return amount * rate;
        } catch (Exception e) {
            return amount / 3.4; // Valeur par defaut si l'API echoue
        }
    }

    public static double convertTNDtoUSD(double amount) {
        try {
            double rate = getRate("USD");
            return amount * rate;
        } catch (Exception e) {
            return amount / 3.1; // Valeur par defaut si l'API echoue
        }
    }

    private static double getRate(String targetCurrency) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        JSONObject json = new JSONObject(result.toString());
        return json.getJSONObject("rates").getDouble(targetCurrency);
    }
}
