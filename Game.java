import java.util.Scanner;

public class Game {
    Board board;
    boolean playerWhite; 

    Game(Board board, boolean playerWhite) {
        this.board = board;
        this.playerWhite = playerWhite; 
    }

    public boolean makePlayerMove(String moveString, Scanner scanner) {
        String[] parts = moveString.split(" ");

        if (parts.length != 3) {
            return false;
        }

        PieceName pieceType;
        switch(parts[0].toLowerCase()) {
            case "p":
                pieceType = playerWhite ? PieceName.whitePawns : PieceName.blackPawns;
                break; 
            case "n":
                pieceType = playerWhite ? PieceName.whiteKnights : PieceName.blackKnights;
                break;
            case "b":
                pieceType = playerWhite ? PieceName.whiteBishops : PieceName.blackBishops;
                break;
            case "r":
                pieceType = playerWhite ? PieceName.whiteRooks : PieceName.blackRooks;
                break;
            case "q":
                pieceType = playerWhite ? PieceName.whiteQueens : PieceName.blackQueens;
                break;
            case "k":
                pieceType = playerWhite ? PieceName.whiteKing : PieceName.blackKing;
                break;
            default:
                return false;
        }

        String files = "abcdefgh";
        int originFile = files.indexOf(Character.toLowerCase(parts[1].charAt(0)));
        int originRank = 7 - Character.getNumericValue(parts[1].charAt(1) - 1);
        int destinationFile = files.indexOf(Character.toLowerCase(parts[2].charAt(0)));
        int destinationRank = 7 - Character.getNumericValue(parts[2].charAt(1) - 1);

        long originSquare = 1L;
        long destinationSquare = 1L;

        originSquare <<= ((originRank * 8) + originFile);
        destinationSquare <<= ((destinationRank * 8) + destinationFile);

        Move move = null;

        // check for castling
        boolean castle = false;
        long rookOrigin = 0L;
        long rookDestination = 0L;
        
        if (pieceType == PieceName.whiteKing || pieceType == PieceName.blackKing) {
            if (destinationSquare == originSquare << 2) { // kingside
                castle = true;
                rookOrigin = playerWhite ? this.board.startingRookLocations[0] : this.board.startingRookLocations[2];
                rookDestination = destinationSquare >>> 1;
            }
            if (destinationSquare == originSquare >>> 2) { // queenside
                castle = true;
                rookOrigin = playerWhite ? this.board.startingRookLocations[1] : this.board.startingRookLocations[3];
                rookDestination = destinationSquare << 1;
            }
        }

        // check for pawn promotion 
        if (
            (pieceType == PieceName.whitePawns) && ((this.board.getRankMasks()[7] & destinationSquare) != 0L) ||
            (pieceType == PieceName.blackPawns) && ((this.board.getRankMasks()[0] & destinationSquare) != 0L)
        ) {
            boolean invalidInput = true;

            System.out.println("\nWaddayawant?\n[n, r, b, q]");
            while(invalidInput) {
                String input = scanner.nextLine();

                switch(input) {
                    case "n":
                        invalidInput = false;
                        move = playerWhite ?
                            new Move(pieceType, originSquare, destinationSquare, PieceName.whiteKnights) :
                            new Move(pieceType, originSquare, destinationSquare, PieceName.blackKnights);
                        break;
                    case "b":
                        invalidInput = false;
                        move = playerWhite ?
                            new Move(pieceType, originSquare, destinationSquare, PieceName.whiteBishops) :
                            new Move(pieceType, originSquare, destinationSquare, PieceName.blackBishops);
                        break;
                    case "r":
                        invalidInput = false;
                        move = playerWhite ?
                            new Move(pieceType, originSquare, destinationSquare, PieceName.whiteRooks) :
                            new Move(pieceType, originSquare, destinationSquare, PieceName.blackRooks);
                        break;
                    case "q":
                        invalidInput = false;
                        move = playerWhite ?
                            new Move(pieceType, originSquare, destinationSquare, PieceName.whiteQueens) :
                            new Move(pieceType, originSquare, destinationSquare, PieceName.blackQueens);
                        break;
                    default:
                        System.out.println("Huh?");
                }
            }
        } else {
            move = castle ? 
                new Move(pieceType, originSquare, destinationSquare, rookOrigin, rookDestination) : 
                new Move(pieceType, originSquare, destinationSquare);
        }

         if (!(this.board.checkLegality(move))) {
            return false; 
        }

        this.board.makeMove(move);
        this.board.setLastMoveOrigin(move.getOrigin());

        return true;
    }
}
