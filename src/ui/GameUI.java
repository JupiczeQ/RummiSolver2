package ui;

import java.util.*;
import model.Group;
import model.Move;
import model.Tile;
import utils.ScoreCalculator;

public class GameUI {
    private ScoreCalculator scoreCalculator;

    public GameUI() {
        this.scoreCalculator = new ScoreCalculator();
    }

    public void displayWelcomeMessage() {
        System.out.println("Witaj w pomocniku do gry Rummikub!");
        System.out.println("Wpisz karty w ręce (np. 5R, 10B, JR):");
    }

    public void displaySolutions(List<List<Group>> solutions) {
        if (!solutions.isEmpty()) {
            // Sortuj według największej liczby użytych kart, a następnie według wartości
            solutions.sort((s1, s2) -> {
                int used1 = scoreCalculator.countUniqueTiles(s1);
                int used2 = scoreCalculator.countUniqueTiles(s2);
                if (used1 != used2) {
                    return Integer.compare(used2, used1); // malejąco według liczby użytych kart
                }
                return Integer.compare(scoreCalculator.calculateTotalValue(s2),
                        scoreCalculator.calculateTotalValue(s1)); // malejąco według wartości
            });

            System.out.println("\n✅ Możesz się wyłożyć! Oto możliwe układy:");
            int count = 1;
            for (List<Group> solution : solutions) {
                int totalValue = scoreCalculator.calculateTotalValue(solution);
                int tilesUsed = scoreCalculator.countUniqueTiles(solution);

                System.out.println("Ruch #" + count++ + " (użyto " +
                        tilesUsed + " kart, wartość " + totalValue + "):");

                for (Group g : solution) {
                    System.out.println("  - " + g);
                }
                System.out.println();

                // Ogranicz wyświetlanie do 10 najlepszych rozwiązań
                if (count > 10) {
                    System.out.println("(pozostałe rozwiązania pominięto)");
                    break;
                }
            }
        } else {
            System.out.println("\n❌ Nie możesz się jeszcze wyłożyć (potrzebujesz układów o wartości co najmniej 30).");
        }
    }

    /**
     * Wyświetla karty w ręce gracza
     */
    public void displayHand(List<Tile> hand) {
        System.out.println("\n===== TWOJA RĘKA =====");
        if (hand.isEmpty()) {
            System.out.println("Nie masz kart w ręce!");
            return;
        }

        // Sortuj karty według koloru i wartości
        List<Tile> sortedHand = new ArrayList<>(hand);
        sortedHand.sort((t1, t2) -> {
            if (t1.isJoker() && !t2.isJoker()) return -1;
            if (!t1.isJoker() && t2.isJoker()) return 1;
            if (t1.isJoker() && t2.isJoker()) return 0;

            int colorComp = t1.getColor().compareTo(t2.getColor());
            if (colorComp != 0) return colorComp;

            return Integer.compare(t1.getVal(), t2.getVal());
        });

        Map<String, List<Tile>> tilesByColor = new HashMap<>();
        List<Tile> jokers = new ArrayList<>();

        for (Tile tile : sortedHand) {
            if (tile.isJoker()) {
                jokers.add(tile);
            } else {
                tilesByColor.computeIfAbsent(tile.getColor(), k -> new ArrayList<>()).add(tile);
            }
        }

        // Wyświetl jokery
        if (!jokers.isEmpty()) {
            System.out.print("Jokery: ");
            for (int i = 0; i < jokers.size(); i++) {
                System.out.print(jokers.get(i));
                if (i < jokers.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }

        // Wyświetl karty według kolorów
        for (Map.Entry<String, List<Tile>> entry : tilesByColor.entrySet()) {
            String color = entry.getKey();
            List<Tile> tiles = entry.getValue();

            // Sort by value
            tiles.sort(Comparator.comparingInt(Tile::getVal));

            System.out.print(formatColor(color) + ": ");
            for (int i = 0; i < tiles.size(); i++) {
                System.out.print(tiles.get(i));
                if (i < tiles.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }

        System.out.println("======================\n");
    }

    private String formatColor(String color) {
        // Format color for display
        switch (color.toLowerCase()) {
            case "red": return "Czerwone";
            case "blue": return "Niebieskie";
            case "black": return "Czarne";
            case "orange": return "Pomarańczowe";
            default: return color;
        }
    }

    /**
     * Wyświetla możliwe ruchy dla kolejnych tur
     */
    public void displayPossibleMoves(List<Move> moves) {
        if (moves.isEmpty()) {
            System.out.println("\n❌ Brak możliwych ruchów. Musisz dobrać kartę.");
            return;
        }

        System.out.println("\n===== MOŻLIWE RUCHY =====");
        int moveNumber = 1;

        // Ogranicz liczbę wyświetlanych ruchów
        int maxMovesToShow = Math.min(moves.size(), 10);

        for (int i = 0; i < maxMovesToShow; i++) {
            Move move = moves.get(i);
            System.out.println("Ruch #" + moveNumber++ + " (ocena: " + move.getScore() +
                    ", zagrano kart: " + move.tilesPlayedCount() + "):");

            // Wyświetl zagrane karty
            if (!move.getTilesPlayed().isEmpty()) {
                System.out.print("  Zagrano: ");
                List<Tile> tilesPlayed = move.getTilesPlayed();
                for (int j = 0; j < tilesPlayed.size(); j++) {
                    System.out.print(tilesPlayed.get(j));
                    if (j < tilesPlayed.size() - 1) System.out.print(", ");
                }
                System.out.println();
            }

            // Wyświetl pierwsze 3 grupy wynikowe (jeśli jest ich więcej)
            List<Group> resultingGroups = move.getResultingGroups();
            int groupsToShow = Math.min(resultingGroups.size(), 3);

            for (int j = 0; j < groupsToShow; j++) {
                System.out.println("  - " + resultingGroups.get(j));
            }

            if (resultingGroups.size() > 3) {
                System.out.println("  - ... (i " + (resultingGroups.size() - 3) + " więcej grup)");
            }

            System.out.println();
        }

        if (moves.size() > maxMovesToShow) {
            System.out.println("(oraz " + (moves.size() - maxMovesToShow) + " więcej ruchów)");
        }

        System.out.println("=========================\n");
    }

    /**
     * Wyświetla prompt do wyboru rozwiązania
     */
    public void displaySelectSolutionPrompt() {
        System.out.println("Wybierz numer rozwiązania, które chcesz zagrać (0 aby pominąć):");
    }

    /**
     * Wyświetla prompt do wyboru ruchu
     */
    public void displaySelectMovePrompt() {
        System.out.println("Wybierz numer ruchu, który chcesz wykonać (0 aby dobrać kartę):");
    }

    /**
     * Wyświetla prompt do kontynuacji gry
     */
    public void displayContinuePrompt() {
        System.out.println("\nCzy chcesz kontynuować grę? (t/n):");
    }

    /**
     * Wyświetla prompt do wprowadzenia nowych kart
     */
    public void displayNewTilesPrompt() {
        System.out.println("\nWprowadź nowe karty (np. 5R, 10B, JR):");
    }

    /**
     * Wyświetla komunikat o braku możliwych ruchów
     */
    public void displayNoMovesMessage() {
        System.out.println("\n❌ Nie ma możliwych ruchów. Musisz dobrać kartę.");
    }

    /**
     * Wyświetla statystyki gry
     */
    public void displayGameStats(int movesPlayed, int tilesInHand, int tilesOnTable) {
        System.out.println("\n===== STATYSTYKI GRY =====");
        System.out.println("Wykonane ruchy: " + movesPlayed);
        System.out.println("Karty w ręce: " + tilesInHand);
        System.out.println("Karty na stole: " + tilesOnTable);
        System.out.println("=========================\n");
    }
}