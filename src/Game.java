import java.util.*;
import logic.GameLogic;
import logic.SolutionFinder;
import logic.MoveGenerator;
import logic.MoveEvaluator;
import model.*;
import ui.GameUI;
import ui.TableUI;
import utils.InputParser;

public class Game {
    private GameState gameState;
    private Scanner scanner;
    private GameUI ui;
    private TableUI tableUI;
    private GameLogic gameLogic;
    private SolutionFinder solutionFinder;
    private MoveGenerator moveGenerator;
    private MoveEvaluator moveEvaluator;

    public Game() {
        Player player = new Player();
        this.gameState = new GameState(player);
        this.scanner = new Scanner(System.in);
        this.ui = new GameUI();
        this.tableUI = new TableUI();
        this.gameLogic = new GameLogic();
        this.solutionFinder = new SolutionFinder();
        this.moveGenerator = new MoveGenerator(gameLogic);
        this.moveEvaluator = new MoveEvaluator();
    }

    public void start() {
        ui.displayWelcomeMessage();
        String input = scanner.nextLine();

        List<Tile> tiles = InputParser.parseTiles(input);
        for (Tile tile : tiles) {
            gameState.getPlayer().addTile(tile);
        }

        gameLoop();
    }

    private void gameLoop() {
        boolean gameRunning = true;

        while (gameRunning) {
            if (gameState.isFirstMove()) {
                handleFirstMove();
            } else {
                handleNextMove();
            }

            // After each move, ask if the player wants to continue
            ui.displayContinuePrompt();
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("n")) {
                gameRunning = false;
            } else {
                // Get new tile(s) if continuing
                ui.displayNewTilesPrompt();
                input = scanner.nextLine();
                List<Tile> newTiles = InputParser.parseTiles(input);
                for (Tile tile : newTiles) {
                    gameState.getPlayer().addTile(tile);
                }
            }
        }
    }

    private void handleFirstMove() {
        List<List<Group>> solutions = solutionFinder.findFirstMoveSolutions(gameState.getPlayer().getHand());
        ui.displaySolutions(solutions);

        if (!solutions.isEmpty()) {
            ui.displaySelectSolutionPrompt();
            int selection = Integer.parseInt(scanner.nextLine());

            if (selection > 0 && selection <= solutions.size()) {
                List<Group> selectedSolution = solutions.get(selection - 1);

                // Add selected groups to table
                for (Group group : selectedSolution) {
                    gameState.getTable().addGroup(group);

                    // Remove tiles from player's hand
                    for (Tile tile : group.getTiles()) {
                        gameState.getPlayer().removeTile(tile);
                    }
                }

                gameState.setFirstMoveDone();
            }
        }
    }

    private void handleNextMove() {
        // Display current state
        tableUI.displayTable(gameState.getTable());
        ui.displayHand(gameState.getPlayer().getHand());

        // Generate possible moves
        List<Move> possibleMoves = moveGenerator.generatePossibleMoves(gameState);

        // Evaluate and rank moves
        possibleMoves = moveEvaluator.rankMoves(possibleMoves, gameState);

        // Display top moves
        ui.displayPossibleMoves(possibleMoves);

        // Get player selection
        ui.displaySelectMovePrompt();
        int selection = Integer.parseInt(scanner.nextLine());

        if (selection > 0 && selection <= possibleMoves.size()) {
            Move selectedMove = possibleMoves.get(selection - 1);

            // Update table with new groups
            gameState.getTable().getGroups().clear();
            for (Group group : selectedMove.getResultingGroups()) {
                gameState.getTable().addGroup(group);
            }

            // Remove played tiles from hand
            for (Tile tile : selectedMove.getTilesPlayed()) {
                gameState.getPlayer().removeTile(tile);
            }
        }
    }
}