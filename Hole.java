public class Hole {
    private int red;
    private int blue;
    private int transparent;

    public Hole(int red, int blue, int transparent) {
        this.red = red;
        this.blue = blue;
        this.transparent = transparent;
    }

    public Hole(Hole clone) {
        this.red = clone.red;
        this.blue = clone.blue;
        this.transparent = clone.transparent;
    }

    public int total() {
        return red + blue + transparent;
    }

    public void addSeed(SeedColor color) {
        switch (color) {
            case RED -> red++;
            case BLUE -> blue++;
            case TRANSPARENT -> transparent++;
        }
    }

    public int takeAllRed() {
        int s = red;
        red = 0;
        return s;
    }

    public int takeAllBlue() {
        int s = blue;
        blue = 0;
        return s;
    }

    public int takeAllTransparent() {
        int s = transparent;
        transparent = 0;
        return s;
    }

    public void clear() {
        red = blue = transparent = 0;
    }

    public int getRed() { return red; }
    public int getBlue() { return blue; }
    public int getTransparent() { return transparent; }
}
