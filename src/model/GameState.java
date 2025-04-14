package model;

public class GameState {
    private Player player;
    private Table table;
    private boolean isFirstMove;
    private int totalMoves;

    public GameState(Player player) {
        this.player = player;
        this.table = new Table();
        this.isFirstMove = true;
        this.totalMoves = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public Table getTable() {
        return table;
    }

    public boolean isFirstMove() {
        return isFirstMove;
    }

    public void setFirstMoveDone() {
        this.isFirstMove = false;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void incrementMoves() {
        this.totalMoves++;
    }

    /**
     * Tworzy kopię bieżącego stanu gry
     */
    public GameState clone() {
        GameState clonedState = new GameState(new Player());

        // Kopiuj karty gracza
        for (Tile tile : player.getHand()) {
            clonedState.getPlayer().addTile(new Tile(tile.getVal(), tile.getColor(), tile.isJoker()));
        }

        // Kopiuj stół
        clonedState.table = table.clone();
        clonedState.isFirstMove = this.isFirstMove;
        clonedState.totalMoves = this.totalMoves;

        return clonedState;
    }
}