package game2048;

import java.util.*;

/**
 * Created by AtagaN on 29.12.2017.
 */
public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    protected int score = 0;
    protected int maxTile = 2;
    private Stack<Tile[][]> previousStates = new Stack<>();
    protected Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;


    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public void rollback() {
        if (!previousScores.empty() && !previousStates.empty()) {
            this.gameTiles = previousStates.pop();
            this.score = previousScores.pop();
        }
    }

    private void saveState(Tile[][] t) {
        this.isSaveNeeded = false;
        Tile[][] tiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tiles[i][j] = new Tile(t[i][j].value);
            }
        }
        this.previousStates.push(tiles);
        this.previousScores.push(score);
    }

    public synchronized boolean canMove() {
        synchronized (gameTiles) {
            if (!getEmptyTiles().isEmpty()) return true;

            boolean isChange = false;

            for (int i = 0; i < FIELD_WIDTH; i++) {
                for (int j = 1; j < FIELD_WIDTH; j++) {
                    if (gameTiles[i][j].value == gameTiles[i][j - 1].value) {
                        isChange = true;
                    }
                }
            }
            for (int i = 1; i < FIELD_WIDTH; i++) {
                for (int j = 0; j < FIELD_WIDTH; j++) {
                    if (gameTiles[i][j].value == gameTiles[i - 1][j].value) {
                        isChange = true;
                    }
                }
            }


            return isChange;
        }

    }

    private void addTile() {
        List<Tile> list = getEmptyTiles();
        if (list != null && list.size() != 0) {
            list.get((int) (list.size() * Math.random())).setValue(Math.random() < 0.9 ? 2 : 4);
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> result = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == 0) result.add(gameTiles[i][j]);
            }
        }
        return result;
    }

    protected void resetGameTiles() {
        this.gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean result = false;
        for (int i = 1; i < tiles.length; i++) {
            if (tiles[i].value != 0) {
                for (int j = 0; j < i; j++) {
                    if (tiles[j].value == 0) {
                        tiles[j].value = tiles[i].value;
                        tiles[i].value = 0;
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean result = false;
        for (int i = 0; i < (tiles.length - 1); i++) {
            if (tiles[i].value == tiles[i + 1].value && tiles[i].value != 0) {
                tiles[i].value = tiles[i].value * 2;
                tiles[i + 1].value = 0;
                result = true;
                score = score + tiles[i].value;
                if (tiles[i].value > maxTile) {
                    maxTile = tiles[i].value;
                }
            }
        }
        compressTiles(tiles);
        return result;
    }

    protected void left() {
        if (this.isSaveNeeded) {
            saveState(gameTiles);
            this.isSaveNeeded = true;
        }


        boolean isChanged = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                isChanged = true;
            }
        }
        if (isChanged) {
            if (getEmptyTiles().size() > 0) {
                addTile();
            }

        }
    }

    protected void right() {
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }

    protected void up() {
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();
    }

    protected void down() {
        saveState(gameTiles);
        rotate();
        left();
        rotate();
        rotate();
        rotate();

    }

    private synchronized void rotate() {
        Tile[][] ret = new Tile[FIELD_WIDTH][FIELD_WIDTH];

        for (int i = 0; i < FIELD_WIDTH; ++i) {
            for (int j = 0; j < FIELD_WIDTH; ++j) {
                ret[i][j] = gameTiles[FIELD_WIDTH - j - 1][i];
            }
        }

        for (int i = 0; i < FIELD_WIDTH; ++i) {
            for (int j = 0; j < FIELD_WIDTH; ++j) {
                gameTiles[i][j] = ret[i][j];
            }
        }
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;

        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                break;
        }
    }

    public boolean hasBoardChanged() {
        boolean result = false;
        int sum = 0;
        int sum2 = 0;
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                sum = sum + gameTiles[i][j].value;
            }
        }

        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                sum2 = sum2 + previousStates.peek()[i][j].value;
            }
        }
        if (sum != sum2) {
            result = true;
        }

        return result;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency;
        move.move();
        if (hasBoardChanged()) moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        else moveEfficiency = new MoveEfficiency(-1, 0, move);
        rollback();

        return moveEfficiency;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.add(getMoveEfficiency(this::right));
        queue.add(getMoveEfficiency(this::up));
        queue.add(getMoveEfficiency(this::down));
        Move move = queue.peek().getMove();
        move.move();
    }
}
