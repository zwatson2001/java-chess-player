import java.util.List;

public class Agent {
    Board board; 
    boolean isAgentWhite;
    int depth;

    Agent(Board board, boolean white, int depth) {
        this.isAgentWhite = white; 
        this.board = board;
        this.depth = depth;
    }

    // white is always positive
    private int max(int depth, int alpha, int beta) {
        if (board.inCheckmate(true)) {
            return Integer.MIN_VALUE;
        }
        if (board.inCheckmate(false)) {
            return Integer.MAX_VALUE;
        }
        if (depth == 0) {
            return board.evaluate();
        }

        boolean inCheck = this.board.inCheck(true);
        List<Move> allMoves;
        
        if (inCheck) {
            allMoves = board.getCheckBreakingMoves(true);
        } else {
            allMoves = this.board.getAllMoves(true);
        }

        int maxEval = Integer.MIN_VALUE; 
        for (Move move : allMoves) {
            this.board.makeMove(move);

            if (this.board.inCheck(true)) {
                this.board.unmakeMove(move);
                continue;
            }

            int eval = min(depth - 1, alpha, beta);
            maxEval = Math.max(eval, maxEval);
            this.board.unmakeMove(move);

            if (maxEval >= beta) {
                break;
            }
            alpha = Math.max(alpha, maxEval);
        }

        return maxEval; 
    }

    // black is always negative
    private int min(int depth, int alpha, int beta) {
         if (board.inCheckmate(false)) {
            return Integer.MAX_VALUE;
         }
         if (board.inCheckmate(true)) {
            return Integer.MIN_VALUE;
         }
         if (depth == 0) {
            return board.evaluate();
         }

        boolean inCheck = this.board.inCheck(false);
        List<Move> allMoves;

        if (inCheck) {
            allMoves = board.getCheckBreakingMoves(false);
        } else {
            allMoves = this.board.getAllMoves(false);
        }

        int minEval = Integer.MAX_VALUE; 
        for (Move move : allMoves) {
            this.board.makeMove(move);

            if (this.board.inCheck(false)) {
                this.board.unmakeMove(move);
                continue;
            }

            int eval = max(depth - 1, alpha, beta);
            minEval = Math.min(eval, minEval);
            this.board.unmakeMove(move);

            if (minEval <= alpha) {
                break;
            }
            beta = Math.min(beta, minEval);
        }

        return minEval;
    }

    private Move findBestMove() {
        Move bestMove = null;
        int bestEval = isAgentWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        boolean inCheck = this.board.inCheck(isAgentWhite);
        
        List<Move> allMoves;
        if (inCheck) {
            allMoves = board.getCheckBreakingMoves(this.isAgentWhite);
        } else {
            allMoves = this.board.getAllMoves(this.isAgentWhite);
        }
        
        for (Move move : allMoves) {
            this.board.makeMove(move);

            // don't consider moves that put agent in check
            if (this.board.inCheck(isAgentWhite)) {
              this.board.unmakeMove(move);
              continue;
            }

            int eval = isAgentWhite ? min(this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE) : max(this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (isAgentWhite && eval > bestEval) {
                bestEval = eval; 
                bestMove = move;
            }
            if (!isAgentWhite && eval < bestEval) {
                bestEval = eval; 
                bestMove = move;
            }
            
            this.board.unmakeMove(move);
        }

        return bestMove;
    }

    public boolean makeBestMove() {
        Move move = findBestMove();

        if (move == null) {
            return false;
        }

        this.board.makeMove(move);
        this.board.setLastMoveOrigin(move.getOrigin());

        return true;
    }
}
