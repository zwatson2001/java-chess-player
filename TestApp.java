import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestApp {
    @Test
    public void testInCheck() {
        // check rook attacks
        Board board1 = new Board("8/8/3r4/8/8/3K4/8/8"); 
        assertTrue(board1.inCheck(true));

        Board board2 = new Board("8/8/8/8/8/R2k4/8/8"); 
        assertTrue(board2.inCheck(false));

        // check bishop attacks
        Board board3 = new Board("8/8/6b1/8/8/3K4/8/8"); 
        assertTrue(board3.inCheck(true));

        Board board4 = new Board("8/8/8/1B6/8/3k4/8/8");
        assertTrue(board4.inCheck(false));

        // check queen attacks
        Board board5 = new Board("8/8/3q4/8/8/3K4/8/8"); 
        assertTrue(board5.inCheck(true));

        Board board6 = new Board("8/8/8/1Q6/8/3k4/8/8");
        assertTrue(board6.inCheck(false));

        // check knight attacks
        Board board7 = new Board("8/8/8/2n6/8/3K4/8/8");
        assertTrue(board7.inCheck(true));

        // check pawn attacks
        Board board8 = new Board("8/8/8/8/8/3K4/4p3/8");
        assertTrue(board8.inCheck(true));

        // check no attacks
        Board board9 = new Board("8/8/2r5/8/8/3K4/8/8"); 
        assertTrue(!board9.inCheck(true));

        Board board10 = new Board("5k2/8/2R5/8/8/8/8/8"); 
        assertTrue(!board10.inCheck(false));
    }

    @Test
    public void testInCheckmate() {
        // checkmates
        Board board1 = new Board("3K3r/r7/8/8/8/8/8/3k4");
        assertTrue(board1.inCheckmate(true));
        assertTrue(!board1.inCheckmate(false));

        Board board2 = new Board("kr6/ppN5/8/8/8/8/8/3K4");
        assertTrue(board2.inCheckmate(false));
        assertTrue(!board2.inCheckmate(true));

        // non-checkmates
        Board board3 = new Board("RNBQKBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbqkbnr");
        assertTrue(!board3.inCheckmate(true));
        assertTrue(!board3.inCheckmate(false));
    }
}
