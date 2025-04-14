import java.util.*;
import logic.GameLogic;
import logic.SolutionFinder;
import logic.MoveGenerator;
import logic.MoveEvaluator;
import logic.GroupValidator;
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
    private int totalPlayers;
    private int humanPlayerIndex;

    public Game() {
        this.scanner = new Scanner(System.in);
        this.ui = new GameUI();
        this.tableUI = new TableUI();
        this.gameLogic = new GameLogic();
        this.solutionFinder = new SolutionFinder();
        this.moveEvaluator = new MoveEvaluator();
    }

    public void start() {
        ui.displayWelcomeMessage();

        // Ask for the number of players
        ui.displayMessage("Enter the number of players (2-4): ");
        totalPlayers = Integer.parseInt(scanner.nextLine());

        // Validate input
        if (totalPlayers < 2 || totalPlayers > 4) {
            ui.displayMessage("Invalid number of players. Setting to 2.");
            totalPlayers = 2;
        }

        // Ask for human player position
        ui.displayMessage("Enter your player position (1-" + totalPlayers + "): ");
        humanPlayerIndex = Integer.parseInt(scanner.nextLine()) - 1;

        // Validate input
        if (humanPlayerIndex < 0 || humanPlayerIndex >= totalPlayers) {
            ui.displayMessage("Invalid player position. Setting to Player 1.");
            humanPlayerIndex = 0;
        }

        // Initialize game state with multiple players
        this.gameState = new GameState(totalPlayers);
        this.gameState.setCurrentPlayerIndex(humanPlayerIndex);
        this.moveGenerator = new MoveGenerator(gameLogic);

        // Set up initial tiles for the human player
        ui.displayMessage("Enter your initial tiles (e.g., '1R 2B 3O 4G JR'): ");
        String input = scanner.nextLine();
        List<Tile> tiles = InputParser.parseTiles(input);
        for (Tile tile : tiles) {
            gameState.getCurrentPlayer().addTile(tile);
        }

        gameLoop();
    }

    private void gameLoop() {
        boolean gameRunning = true;

        while (gameRunning) {
            // Display current player info
            //ui.displayMessage("Current player: Player " + (gameState.getCurrentPlayerIndex() + 1));

            // Handle other players' moves before the current player's turn
            if (gameState.getCurrentPlayerIndex() == humanPlayerIndex) {
                handleOtherPlayersMoves();

                // Now handle human player's move
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
                        gameState.getCurrentPlayer().addTile(tile);
                    }

                    // Move to next player
                    gameState.nextPlayer();
                }
            } else {
                // Skip AI players for now (they're handled by handleOtherPlayersMoves)
                gameState.nextPlayer();
            }
        }
    }

    private void handleOtherPlayersMoves() {
        // Ask if any other players made moves
        ui.displayMessage("Did any other players make moves? (y/n): ");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("y")) {
            // Display current table state
            ui.displayMessage("\nCURRENT TABLE STATE:");
            tableUI.displayTable(gameState.getTable());

            boolean addingMoves = true;

            while (addingMoves) {
                ui.displayMessage("\nWhich player made a move? (1-" + totalPlayers + ", 0 to finish): ");
                int playerIndex = Integer.parseInt(scanner.nextLine()) - 1;

                if (playerIndex == -1) {
                    addingMoves = false;
                    continue;
                }

                if (playerIndex < 0 || playerIndex >= totalPlayers || playerIndex == humanPlayerIndex) {
                    ui.displayMessage("Invalid player index.");
                    continue;
                }

                // Handle adding groups to the table
                handleAddGroupsFromOtherPlayer();

                // Ask if there are more moves from other players
                ui.displayMessage("Any more moves from other players? (y/n): ");
                if (!scanner.nextLine().equalsIgnoreCase("y")) {
                    addingMoves = false;
                }
            }
        }
    }

    private void handleAddGroupsFromOtherPlayer() {
        boolean addingGroups = true;

        while (addingGroups) {
            ui.displayMessage("\nEnter tiles for a new group (e.g., '1R 2R 3R' or '7B 7R 7G'), or 'done' to finish: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("done")) {
                addingGroups = false;
                continue;
            }

            List<Tile> tiles = InputParser.parseTiles(input);

            if (tiles.isEmpty()) {
                ui.displayMessage("No valid tiles entered.");
                continue;
            }

            // Validate if this forms a valid group
            if (GroupValidator.isValidGroup(tiles)) {
                Group newGroup = new Group(tiles);
                gameState.getTable().addGroup(newGroup);
                ui.displayMessage("Group added successfully.");
            } else {
                ui.displayMessage("Invalid group! Groups must be either a run (same color, consecutive numbers) or a set (same number, different colors).");
            }

            // Display updated table
            ui.displayMessage("\nUPDATED TABLE STATE:");
            tableUI.displayTable(gameState.getTable());
        }
    }

    private void handleFirstMove() {
        List<List<Group>> solutions = solutionFinder.findFirstMoveSolutions(gameState.getCurrentPlayer().getHand());
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
                        gameState.getCurrentPlayer().removeTile(tile);
                    }
                }

                gameState.setFirstMoveDone();
            }
        }
    }

    private void handleNextMove() {
        // Display current state
        tableUI.displayTable(gameState.getTable());
        ui.displayHand(gameState.getCurrentPlayer().getHand());

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
                gameState.getCurrentPlayer().removeTile(tile);
            }
        }
    }
}