public class Board {
    private static final int NUM_HOLES = 16;
    private final Hole[] holes;

    public Board() {
        holes = new Hole[NUM_HOLES];
        for (int i = 0; i < NUM_HOLES; i++) {
            holes[i] = new Hole(2, 2, 2);
        }
    }

    public Board(Board clone) {
        int n = clone.getNumHoles();
        this.holes = new Hole[n];
        for (int i = 0; i < n; i++) {
            this.holes[i] = new Hole(clone.getHole(i));
        }
    }

    public int getNumHoles() {
        return NUM_HOLES;
    }

    public Hole getHole(int index) {
        return holes[index];
    }

    public int totalSeeds() {
        int sum = 0;
        for (Hole h : holes) {
            sum += h.total();
        }
        return sum;
    }

    public int totalSeedsOwnedBy(Player player) {
        int sum = 0;
        for (int i = 0; i < NUM_HOLES; i++) {
            if (player.ownsHoleIndex(i)) {
                sum += holes[i].total();
            }
        }
        return sum;
    }

    public int collectAllSeeds() {
        int sum = totalSeeds();
        for (Hole h : holes) {
            h.clear();
        }
        return sum;
    }
}
