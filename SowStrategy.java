public interface SowStrategy {
    int sow(Board board,
            Player currentPlayer,
            int startIndex,
            int seeds,
            SeedColor color);
}
