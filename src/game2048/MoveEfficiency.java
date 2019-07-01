package game2048;

/**
 * Created by AtagaN on 04.01.2018.
 */
public class MoveEfficiency implements Comparable<MoveEfficiency> {
    private int numberOfEmptyTiles;
    private int score;
    private Move move;


    public MoveEfficiency(int numberOfEmptyTiles, int score, Move move) {
        this.numberOfEmptyTiles = numberOfEmptyTiles;
        this.move = move;
        this.score = score;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public int compareTo(MoveEfficiency o) {
        if (this.numberOfEmptyTiles > o.numberOfEmptyTiles) {
            return 1;
        }
        if (this.numberOfEmptyTiles < o.numberOfEmptyTiles) {
            return -1;
        }
        if (this.numberOfEmptyTiles == o.numberOfEmptyTiles) {
            if (this.score > o.score) {
                return 1;
            }
            if (this.score < o.score) {
                return -1;
            }
            if (this.score < o.score) {
                return 0;
            }
        }
        return 0;
    }
}
