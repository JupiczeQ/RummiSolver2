package ui;

import java.util.*;
import model.Group;
import model.Table;
import model.Tile;

public class TableUI {

    /**
     * Wyświetla aktualny stan stołu
     */
    public void displayTable(Table table) {
        System.out.println("\n===== STÓŁ =====");

        if (table.isEmpty()) {
            System.out.println("Stół jest pusty.");
            return;
        }

        List<Group> groups = table.getGroups();
        for (int i = 0; i < groups.size(); i++) {
            System.out.println("Grupa " + (i+1) + ": " + groups.get(i));
        }

        System.out.println("=================\n");
    }

    /**
     * Wyświetla stół przed i po proponowanym ruchu
     */
    public void displayTableBeforeAfter(Table before, Table after) {
        System.out.println("\n===== STÓŁ PRZED =====");
        if (before.isEmpty()) {
            System.out.println("Stół jest pusty.");
        } else {
            List<Group> beforeGroups = before.getGroups();
            for (int i = 0; i < beforeGroups.size(); i++) {
                System.out.println("Grupa " + (i+1) + ": " + beforeGroups.get(i));
            }
        }

        System.out.println("\n===== STÓŁ PO =====");
        if (after.isEmpty()) {
            System.out.println("Stół będzie pusty.");
        } else {
            List<Group> afterGroups = after.getGroups();
            for (int i = 0; i < afterGroups.size(); i++) {
                System.out.println("Grupa " + (i+1) + ": " + afterGroups.get(i));
            }
        }
        System.out.println("====================\n");
    }

    /**
     * Wyświetla różnicę między dwoma stanami stołu
     */
    public void displayTableDifference(Table before, Table after) {
        // Znajdź dodane i usunięte grupy
        List<Group> beforeGroups = before.getGroups();
        List<Group> afterGroups = after.getGroups();

        System.out.println("\n===== ZMIANY NA STOLE =====");

        List<Tile> beforeTiles = before.getAllTiles();
        List<Tile> afterTiles = after.getAllTiles();

        // Znajdź usunięte kafelki
        List<Tile> removedTiles = new ArrayList<>(beforeTiles);
        removedTiles.removeAll(afterTiles);

        // Znajdź dodane kafelki
        List<Tile> addedTiles = new ArrayList<>(afterTiles);
        addedTiles.removeAll(beforeTiles);

        if (!removedTiles.isEmpty()) {
            System.out.println("Usunięte kafelki: " + formatTileList(removedTiles));
        }

        if (!addedTiles.isEmpty()) {
            System.out.println("Dodane kafelki: " + formatTileList(addedTiles));
        }

        if (removedTiles.isEmpty() && addedTiles.isEmpty()) {
            System.out.println("Brak zmian w kafelkach (tylko przegrupowanie)");
        }

        System.out.println("==========================\n");
    }

    private String formatTileList(List<Tile> tiles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiles.size(); i++) {
            sb.append(tiles.get(i));
            if (i < tiles.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}