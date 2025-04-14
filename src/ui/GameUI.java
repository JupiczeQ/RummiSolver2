package ui;

import java.util.List;
import model.Group;
import model.Tile;
import model.Move;

public class GameUI {

    public void displayMessage(String message) {
        System.out.print(message);
    }

    public void displayWelcomeMessage() {
        System.out.println("Welcome to Rummikub!");
        System.out.println("=====================");
    }

    public void displayHand(List<Tile> hand) {
        System.out.println("\nYour hand:");
        if (hand.isEmpty()) {
            System.out.println("Empty! You won!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(hand.get(i)).append(" ");
        }
        System.out.println(sb.toString());
    }

    public void displaySolutions(List<List<Group>> solutions) {
        if (solutions.isEmpty()) {
            System.out.println("No valid initial moves found. You need at least 30 points.");
            return;
        }

        System.out.println("\nPossible first moves:");
        for (int i = 0; i < solutions.size(); i++) {
            System.out.println((i + 1) + ". " + solutionToString(solutions.get(i)));
        }
    }

    private String solutionToString(List<Group> solution) {
        StringBuilder sb = new StringBuilder();
        int totalValue = 0;

        for (Group group : solution) {
            sb.append("[");
            for (Tile tile : group.getTiles()) {
                sb.append(tile).append(" ");
                if (!tile.isJoker()) {
                    totalValue += tile.getVal();
                }
            }
            sb.append("] ");
        }

        sb.append("(Total: ").append(totalValue).append(" points)");
        return sb.toString();
    }

    public void displayPossibleMoves(List<Move> moves) {
        if (moves.isEmpty()) {
            System.out.println("No valid moves found.");
            return;
        }

        System.out.println("\nTop moves:");
        int showCount = Math.min(moves.size(), 5);
        for (int i = 0; i < showCount; i++) {
            System.out.println((i + 1) + ". " + moves.get(i));
        }
    }

    public void displaySelectSolutionPrompt() {
        System.out.print("Select solution (number) or 0 to skip: ");
    }

    public void displaySelectMovePrompt() {
        System.out.print("Select move (number) or 0 to skip: ");
    }

    public void displayContinuePrompt() {
        System.out.print("\nContinue playing? (y/n): ");
    }

    public void displayNewTilesPrompt() {
        System.out.print("Enter new tiles (e.g., '1R 2B'): ");
    }
}