package utils;

import models.Utilisateur;

public final class SessionManager {

    private static Utilisateur currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
