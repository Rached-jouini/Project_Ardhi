package services;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Equipement;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartManager {

    public static class CartEntry {
        private final Equipement equipement;
        private final IntegerProperty quantity;

        public CartEntry(Equipement e, int qty) {
            this.equipement = e;
            this.quantity = new SimpleIntegerProperty(qty);
        }

        public Equipement getEquipement() { return equipement; }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int qty) { quantity.set(qty); }
        public IntegerProperty quantityProperty() { return quantity; }
        public double getSubtotal() { return equipement.getPrix_location_jour() * quantity.get(); }
    }

    // Clé: id de l'équipement
    private static final ObservableList<CartEntry> cartEntries = FXCollections.observableArrayList();

    public static void addToCart(Equipement e, int qty) {
        // Chercher si cet équipement est déjà dans le panier
        for (CartEntry entry : cartEntries) {
            if (entry.getEquipement().getId() == e.getId()) {
                entry.setQuantity(entry.getQuantity() + qty);
                System.out.println("Quantité mise à jour. Total : " + entry.getQuantity());
                return;
            }
        }
        cartEntries.add(new CartEntry(e, qty));
        System.out.println("Article ajouté. Entrées panier : " + cartEntries.size());
    }

    public static ObservableList<CartEntry> getCartEntries() {
        return cartEntries;
    }

    public static int getCount() {
        return cartEntries.stream().mapToInt(CartEntry::getQuantity).sum();
    }

    public static void clearCart() {
        cartEntries.clear();
    }
}
