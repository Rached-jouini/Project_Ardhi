package test;

import controllers.Home;

/**
 * Classe principale fusionnée.
 * Elle lance l'application via le contrôleur Home (Authentification).
 */
public class Main {
    public static void main(String[] args) {
        // Lancement de l'interface graphique (Auth -> Dashboard)
        Home.main(args);
    }
}
