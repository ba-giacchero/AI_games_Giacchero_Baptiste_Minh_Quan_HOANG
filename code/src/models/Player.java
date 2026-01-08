package models;

public class Player {
    private final int index; // 0 = Player1 (odd holes), 1 = Player2 (even holes)
    private final String name;
    private int captured;

    public Player(int index, String name) {
        this.index = index;
        this.name = name;
        this.captured = 0;
    }

    public Player(Player clone){
        this.index = clone.index;
        this.name = clone.name;
        this.captured = clone.captured;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getCaptured() {
        return captured;
    }

    public void addCaptured(int seeds) {
        captured += seeds;
    }

    public boolean ownsHoleIndex(int holeIndex0Based) {
        int num = holeIndex0Based + 1; // 1..16
        if (index == 0) {
            return num % 2 == 1; // P1 owns odd holes
        } else {
            return num % 2 == 0; // P2 owns even holes
        }
    }

    public void addCapturedRed(int taken) {
        captured += taken;
    }

    public int getCapturedRed() {
        return captured;
    }
}

