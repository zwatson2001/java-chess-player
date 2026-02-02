import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

public class Board {
    private final EnumMap<PieceName, Long> pieceMap = new EnumMap<>(PieceName.class);

    private long[] fileMasks = {
        0x0101010101010101L, // File A
        0x0202020202020202L, // File B
        0x0404040404040404L, // File C
        0x0808080808080808L, // File D
        0x1010101010101010L, // File E
        0x2020202020202020L, // File F
        0x4040404040404040L, // File G
        0x8080808080808080L  // File H
    };
    // bottom to top
    private long[] rankMasks = {
        0xFF00000000000000L,
        0x00FF000000000000L,
        0x0000FF0000000000L,
        0x000000FF00000000L,
        0x00000000FF000000L,
        0x0000000000FF0000L,
        0x000000000000FF00L,
        0x00000000000000FFL
    };
    private long[] diagonalMasks = {
        0x0000000000000001L, // a1
        0x0000000000000102L, // a1-b2
        0x0000000000010204L, // a1-c3
        0x0000000001020408L, // a1-d4
        0x0000000102040810L, // a1-e5
        0x0000010204081020L, // a1-f6
        0x0001020408102040L, // a1-g7
        0x0102040810204080L, // a1-h8 (main diagonal)
        0x0204081020408000L, // b2-h8
        0x0408102040800000L, // c3-h8
        0x0810204080000000L, // d4-h8
        0x1020408000000000L, // e5-h8
        0x2040800000000000L, // f6-h8
        0x4080000000000000L, // g7-h8
        0x8000000000000000L  // h8
    };
    private long[] antiDiagonalMasks = { 
        0x0100000000000000L,
        0x0201000000000000L,
        0x0402010000000000L,
        0x0804020100000000L,
        0x1008040201000000L,
        0x2010080402010000L,
        0x4020100804020100L,
        0x8040201008040201L,
        0x0080402010080402L,
        0x0000804020100804L,
        0x0000008040201008L,
        0x0000000080402010L,
        0x0000000000804020L,
        0x0000000000008040L,
        0x0000000000000080L
    };

    public long lastMoveOrigin = 0L; // for viewing convenience

    private boolean canWhiteCastle = true;
    private boolean canBlackCastle = true;
    public long[] startingRookLocations = {
        1L << 63, // white kingside 
        1L << 56, // white queenside
        1L << 7, // black kingside
        1L // black queenside
    };
    private boolean[] rooksMoved = {false, false, false, false};

    private long enPassantTarget = 0L;

    private int moveCountDraw = 0; 

    // piece-square tables
    private final Map<PieceName, int[]> pieceSquareTableMap = Map.ofEntries(
        entry(
            PieceName.whiteKing,
            new int[] {
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                50, 50, 0, 0, 0, 0, 50, 50
            }
        ),
        entry(
            PieceName.blackKing,
            new int[] {
                50, 50, 0, 0, 0, 0, 50, 50,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0
            }
        )
    );

    Board(String position) {
        for (PieceName pieceType : PieceName.values()) {
            pieceMap.put(pieceType, 0L);
        }

        setBoard(position);
    }

    public long[] getRankMasks() {
        return this.rankMasks;
    }
    public EnumMap<PieceName, Long> getPieceMap() {
        return pieceMap;
    }

    public int getMoveCountDraw() {
        return this.moveCountDraw; 
    }

    // fen string to board converter
    public void setBoard(String position) {
        int rank = 7;
        int file = 0;

        for (char c : position.toCharArray()) {
            if (Character.isDigit(c)) {
                file += Character.getNumericValue(c);
            } else if (c == '/') {
                rank--;
                file = 0;
            } else {
                long square = 1L << (rank * 8 + (7 - file));
            
                switch (c) {
                    case 'P' -> pieceMap.put(PieceName.whitePawns, pieceMap.get(PieceName.whitePawns) | square);
                    case 'N' -> pieceMap.put(PieceName.whiteKnights, pieceMap.get(PieceName.whiteKnights) | square);
                    case 'B' -> pieceMap.put(PieceName.whiteBishops, pieceMap.get(PieceName.whiteBishops) | square);
                    case 'R' -> pieceMap.put(PieceName.whiteRooks, pieceMap.get(PieceName.whiteRooks) | square);
                    case 'Q' -> pieceMap.put(PieceName.whiteQueens, pieceMap.get(PieceName.whiteQueens) | square);
                    case 'K' -> pieceMap.put(PieceName.whiteKing, pieceMap.get(PieceName.whiteKing) | square);
                    case 'p' -> pieceMap.put(PieceName.blackPawns, pieceMap.get(PieceName.blackPawns) | square);
                    case 'n' -> pieceMap.put(PieceName.blackKnights, pieceMap.get(PieceName.blackKnights) | square);
                    case 'b' -> pieceMap.put(PieceName.blackBishops, pieceMap.get(PieceName.blackBishops) | square);
                    case 'r' -> pieceMap.put(PieceName.blackRooks, pieceMap.get(PieceName.blackRooks) | square);
                    case 'q' -> pieceMap.put(PieceName.blackQueens, pieceMap.get(PieceName.blackQueens) | square);
                    case 'k' -> pieceMap.put(PieceName.blackKing, pieceMap.get(PieceName.blackKing) | square);
                }
                file++;
            }
        }
    }

    public void setLastMoveOrigin(long origin) {
        this.lastMoveOrigin = origin;
    }

    public String toString() { return displayBoard(false, true); }

    public String displayBoard(boolean showMoves, boolean showLastMoveOrigin) {
        long allWhiteMoves = 0L;
        long allBlackMoves = 0L;

        if (showMoves) {
            allWhiteMoves = 
                getAllMoves(true)
                .stream()
                .mapToLong(Move::getDestination)
                .reduce(0L, (x, y) -> x | y);

            allBlackMoves = 
                getAllMoves(false)
                .stream()
                .mapToLong(Move::getDestination)
                .reduce(0L, (x, y) -> x | y); 
        }

        String retVal = "";
        long[] pieces = new long[] {
            pieceMap.get(PieceName.whitePawns), 
            pieceMap.get(PieceName.whiteKnights),
            pieceMap.get(PieceName.whiteBishops), 
            pieceMap.get(PieceName.whiteRooks),
            pieceMap.get(PieceName.whiteQueens), 
            pieceMap.get(PieceName.whiteKing),
            pieceMap.get(PieceName.blackPawns), 
            pieceMap.get(PieceName.blackKnights), 
            pieceMap.get(PieceName.blackBishops), 
            pieceMap.get(PieceName.blackRooks),
            pieceMap.get(PieceName.blackQueens), 
            pieceMap.get(PieceName.blackKing),
            allWhiteMoves,
            allBlackMoves,
            this.lastMoveOrigin,
        };
        char[] symbols = new char[] {'♙', '♘', '♗', '♖', '♕', '♔', '♟', '♞', '♝', '♜', '♛', '♚', '□', '■', '*'};

        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                retVal += "\n" + (8 - i / 8) + " ";
            }

            long mask = 1L << i;
            char symbol = ' ';
            for (int j = 0; j < pieces.length; j++) {
                if ((pieces[j] & mask) != 0) {
                    if (symbol == '□' && j == 13) {
                        symbol = '◪';
                    } else {
                        symbol = symbols[j];
                    }
                }
            }
            retVal += "[" + symbol + "]";
        }
        retVal += "\n   ";

        char[] fileNames = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'}; 
        for (char letter : fileNames) {
            retVal += letter + "  ";
        }

        return retVal;
    }

    // for debugging
    public void printOccupancy(long occ) {
        String retVal = "";
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                retVal += "\n";
            }

            long mask = 1L << i;
            if ((occ & mask) != 0) {
                retVal += "[X]";
            } else {
                retVal += "[ ]";
            }
        }

        System.out.println(retVal);
    }

    private long getWhitePieces() {
        return 
            pieceMap.get(PieceName.whitePawns) | 
            pieceMap.get(PieceName.whiteKnights) | 
            pieceMap.get(PieceName.whiteBishops) | 
            pieceMap.get(PieceName.whiteRooks) | 
            pieceMap.get(PieceName.whiteQueens) | 
            pieceMap.get(PieceName.whiteKing);
    }

    private long getBlackPieces() {
        return 
            pieceMap.get(PieceName.blackPawns) | 
            pieceMap.get(PieceName.blackKnights) | 
            pieceMap.get(PieceName.blackBishops) | 
            pieceMap.get(PieceName.blackRooks) | 
            pieceMap.get(PieceName.blackQueens) | 
            pieceMap.get(PieceName.blackKing);
    }

    private boolean isWhite(PieceName pieceName) {
        return pieceName.toString().substring(0,5).equals("white");
    }

    // formula for calculating sliding piece moves
    private long hyperbolaQuintessence(long mask, long pieceLocation, long occupied) {
        return (
            ((occupied - (pieceLocation << 1)) ^ Long.reverse(Long.reverse(occupied) - (Long.reverse(pieceLocation) << 1))) & mask
        );
    }

    private long getPawnMoves(long startingLocation, long ownPieces, long opponentPieces, boolean white) {
        long allPieces = opponentPieces | ownPieces;
        boolean canDoublePush = white ?
            (startingLocation & rankMasks[1]) != 0L :
            (startingLocation & rankMasks[6]) != 0L;

        long singlePush = white ?
            startingLocation >>> 8 & ~allPieces :
            startingLocation << 8 & ~allPieces;

        long doublePush;
        if (canDoublePush) {
            doublePush = white ?
                singlePush >>> 8 & ~allPieces :
                singlePush << 8 & ~allPieces;
        } else {
            doublePush = 0L;
        }
        
        long capture = white ?
            ((startingLocation >>> 7 & ~fileMasks[0]) | (startingLocation >>> 9 & ~fileMasks[7])) & (opponentPieces | this.enPassantTarget) & ~ownPieces :
            ((startingLocation << 7 & ~fileMasks[7]) | (startingLocation << 9 & ~fileMasks[0])) & (opponentPieces | this.enPassantTarget) & ~ownPieces ;

        return singlePush | doublePush | capture;

    }

    private long getKnightMoves(long startingLocation, long ownPieces) {
        long destinations =
            ((startingLocation << 17) & ~fileMasks[0]) |
            ((startingLocation << 15) & ~fileMasks[7]) |
            ((startingLocation << 10) & ~(fileMasks[0] | fileMasks[1])) |
            ((startingLocation << 6)  & ~(fileMasks[6] | fileMasks[7])) |
            ((startingLocation >>> 17) & ~fileMasks[7]) |
            ((startingLocation >>> 15) & ~fileMasks[0]) |
            ((startingLocation >>> 10) & ~(fileMasks[6] | fileMasks[7])) |
            ((startingLocation >>> 6)  & ~(fileMasks[0] | fileMasks[1]));

        return destinations & ~ownPieces;
    }

    private long getKingMoves(long startingLocation, long ownPieces) {
        long destinations = 
            ((startingLocation >>> 1) & ~fileMasks[7]) | 
            ((startingLocation << 1) & ~fileMasks[0]) | 
            ((startingLocation >>> 8) & ~rankMasks[0]) | 
            ((startingLocation << 8) & ~rankMasks[7]) | 
            ((startingLocation >>> 7) & ~(rankMasks[7] | fileMasks[0])) | 
            ((startingLocation << 7) & ~(rankMasks[0] | fileMasks[7]) | 
            ((startingLocation >>> 9) & ~(rankMasks[7] | fileMasks[7])) | 
            ((startingLocation << 9) & ~(rankMasks[0] | fileMasks[0])));

        return destinations & ~ownPieces;
    }

    private long getRookMoves(long startingLocation, long ownPieces) {
        long occupied = getWhitePieces() | getBlackPieces();

        long rankMask = rankMasks[Long.numberOfLeadingZeros(startingLocation) / 8];
        long fileMask = fileMasks[(Long.numberOfTrailingZeros(startingLocation) % 8)];

        long diagonal1 = hyperbolaQuintessence(rankMask, startingLocation, occupied & rankMask);
        long diagonal2 = hyperbolaQuintessence(fileMask, startingLocation, occupied & fileMask);
        long destinations = (diagonal1 | diagonal2) & ~ownPieces;

        return destinations; 
    }

    private long getBishopMoves(long startingLocation, long ownPieces) {
        long occupied = getWhitePieces() | getBlackPieces();
        
        // diagonal is row plus column, anti diagaonal is (7 - row) plus column
        long diagonalMask = diagonalMasks[(Long.numberOfTrailingZeros(startingLocation) / 8) + (Long.numberOfTrailingZeros(startingLocation) % 8)];
        long antiDiagonalMask = antiDiagonalMasks[(7 - (Long.numberOfTrailingZeros(startingLocation) / 8)) + (Long.numberOfTrailingZeros(startingLocation) % 8)];

        long diagonal1 = hyperbolaQuintessence(diagonalMask, startingLocation, occupied & diagonalMask);
        long diagonal2 = hyperbolaQuintessence(antiDiagonalMask, startingLocation, occupied & antiDiagonalMask);
        long destinations = (diagonal1 | diagonal2) & ~ownPieces;

        return destinations; 
    }

    private long getQueenMoves(long startingLocation, long ownPieces) {
        return getRookMoves(startingLocation, ownPieces) | getBishopMoves(startingLocation, ownPieces);
    }

    // don't run this if the side can't castle
    private List<Move> getCastles(boolean white) {
        if (white && !canWhiteCastle) {
            throw new AssertionError("White doesn't have castling rights");
        } else if (!white && !canBlackCastle) {
            throw new AssertionError("Black doesn't have castling rights");
        }

        long king = white ? pieceMap.get(PieceName.whiteKing) : pieceMap.get(PieceName.blackKing);
        long rooks = white ? pieceMap.get(PieceName.whiteRooks) : pieceMap.get(PieceName.blackRooks);
        boolean kingsideRights = white ? !this.rooksMoved[0] : !this.rooksMoved[2]; 
        boolean queensideRights = white ? !this.rooksMoved[1] : !this.rooksMoved[3]; 

        List<Move> castles = new ArrayList<Move>();

        long kingsideRook = white ? startingRookLocations[0] : startingRookLocations[2];
        long queensideRook = white ? startingRookLocations[1] : startingRookLocations[3];
        PieceName pieceName = white ? PieceName.whiteKing : PieceName.blackKing;

        if (((getRookMoves(kingsideRook, 0L) & king)) != 0L && kingsideRights && (kingsideRook & rooks) != 0L) {
            long kingDestination = king << 2; 

            Move castleKingside = new Move(
                pieceName,
                king, 
                kingDestination,
                kingsideRook,
                kingDestination >> 1
            );

            castles.add(castleKingside);
        }
        if (((getRookMoves(queensideRook, 0L) & king)) != 0L && queensideRights && (kingsideRook & rooks) != 0L) {
            long kingDestination = king >>> 2; 

            Move castleQueenside = new Move(
                pieceName,
                king, 
                kingDestination,
                queensideRook,
                kingDestination << 1
            );

            castles.add(castleQueenside);
        }

        return castles;
    }

    public void updateCastlingRights(Move move) {
        PieceName pieceName = move.getPieceType();
        long origin = move.getOrigin();

        if (pieceName == PieceName.whiteKing && canWhiteCastle) {
            this.canWhiteCastle = false;
            move.setRevokedCastleRights(true);
        }
        if (pieceName == PieceName.blackKing && canBlackCastle) {
            this.canBlackCastle = false;
            move.setRevokedCastleRights(true);
        }

        if (pieceName == PieceName.whiteRooks) {
            if (origin == startingRookLocations[0] && !rooksMoved[0]) {
                this.rooksMoved[0] = true;
                move.setFirstKingsideRookMove(true);
            } else if (origin == startingRookLocations[1] && !rooksMoved[1]) {
                this.rooksMoved[1] = true;
                move.setFirstQueensideRookMove(true);
            }
        } else if (pieceName == PieceName.blackRooks  && !rooksMoved[2]) {
            if (origin == startingRookLocations[2]) {
                this.rooksMoved[2] = true;
                move.setFirstKingsideRookMove(true);
            } else if (origin == startingRookLocations[3]  && !rooksMoved[3]) {
                this.rooksMoved[3] = true;
                move.setFirstQueensideRookMove(true);
            }
        }

        if (rooksMoved[0] && rooksMoved[1] && canWhiteCastle) {
            canWhiteCastle = false;
            move.setRevokedCastleRights(true);
        } else if (rooksMoved[2] && rooksMoved[3]  && canBlackCastle) {
            canBlackCastle = false;
            move.setRevokedCastleRights(true);
        }
    }

    private List<PieceMoves> getPieceMoves(PieceName pieceType) {
        List<PieceMoves> allPieceMoves = new ArrayList<>();
        
        String pieceString = pieceType.toString(); 
        boolean white = isWhite(pieceType);
        String pieceName = pieceType.toString().substring(5, pieceString.length());

        long ownPieces = white ? getWhitePieces() : getBlackPieces();
        long opponentPieces = white ? getBlackPieces() : getWhitePieces();

        long piecesOfThisType = pieceMap.get(pieceType);

        while (piecesOfThisType != 0L) {
            long piece = Long.highestOneBit(piecesOfThisType);

            long destinations; 
            switch(pieceName) {
            case "Pawns":
                destinations = getPawnMoves(piece, ownPieces, opponentPieces, white); 
                break;
            case "Knights":
                destinations = getKnightMoves(piece, ownPieces);
                break;
            case "Bishops":
                destinations = getBishopMoves(piece, ownPieces);
                break;
            case "Rooks":
                destinations = getRookMoves(piece, ownPieces);
                break;
            case "Queens":
                destinations = getQueenMoves(piece, ownPieces);
                break;
            case "King":
                destinations = getKingMoves(piece, ownPieces);
                break;
            default:
                throw new AssertionError("Incorrectly specified piece type: " + pieceName);
            }

            PieceMoves thisPieceMoves = new PieceMoves(piece, destinations);
            allPieceMoves.add(thisPieceMoves);
            piecesOfThisType &= ~piece; 
        }

        return allPieceMoves;
    }

    public List<Move> getAllMoves(boolean white) {
       List<Move> allMoves = new ArrayList<>();

       if ((white && canWhiteCastle) || (!white && canBlackCastle)) {
            List<Move> castles = getCastles(white);
            allMoves.addAll(castles);
        }
        
        for (PieceName pieceType : PieceName.values()) {
            String pieceString = pieceType.toString(); 
            List<PieceMoves> thisPieceTypeMoves = new ArrayList<PieceMoves>();
 
            if ((pieceString.substring(0,5).equals("white")) == white) {
                thisPieceTypeMoves = getPieceMoves(pieceType);
            }

            for (PieceMoves pieceMoves : thisPieceTypeMoves) {
                long origin = pieceMoves.getOrigin();
                long destinations = pieceMoves.getDestinations();

                while (destinations != 0L) {
                    long destination = Long.highestOneBit(destinations);
                    
                    if (
                        (white && destination == pieceMap.get(PieceName.blackKing)) || 
                        (!white && destination == pieceMap.get(PieceName.whiteKing))
                    ) {
                        destinations &= ~ destination;
                        continue;
                    }

                    // check for pawn promotion - no reason to consider bishops or rooks
                    if ((pieceType == PieceName.whitePawns) && ((destination & rankMasks[7]) != 0L)) {
                        Move promoteToKnight = new Move(pieceType, origin, destination, PieceName.whiteKnights);
                        Move promoteToQueen = new Move(pieceType, origin, destination, PieceName.whiteQueens);

                        allMoves.add(promoteToKnight); 
                        allMoves.add(promoteToQueen);
                    } else if (((pieceType == PieceName.blackPawns) && ((destination & rankMasks[0]) != 0L))) {
                        Move promoteToKnight = new Move(pieceType, origin, destination, PieceName.blackKnights);
                        Move promoteToQueen = new Move(pieceType, origin, destination, PieceName.blackQueens);

                        allMoves.add(promoteToKnight); 
                        allMoves.add(promoteToQueen);
                    } else {
                        Move move = new Move(pieceType, origin, destination);

                        allMoves.add(move);
                    }

                    

                    destinations &= ~ destination;
                }
            }
        }

        return allMoves;
    }

    // positive values are good for white
    public int evaluate() {
        int eval = 0;

        eval += 
            (Long.bitCount(pieceMap.get(PieceName.whitePawns)) + 
            (Long.bitCount(pieceMap.get(PieceName.whiteKnights)) * 300) + 
            (Long.bitCount(pieceMap.get(PieceName.whiteBishops)) * 300) + 
            (Long.bitCount(pieceMap.get(PieceName.whiteRooks)) * 500) +
            (Long.bitCount(pieceMap.get(PieceName.whiteQueens)) * 900)) +
            (pieceSquareTableMap.get(PieceName.whiteKing))[Long.numberOfTrailingZeros(pieceMap.get(PieceName.whiteKing))];

        eval -= 
            (Long.bitCount(pieceMap.get(PieceName.blackPawns)) +
            (Long.bitCount(pieceMap.get(PieceName.blackKnights)) * 300) +
            (Long.bitCount(pieceMap.get(PieceName.blackBishops)) * 300) +
            (Long.bitCount(pieceMap.get(PieceName.blackRooks)) * 500) + 
            (Long.bitCount(pieceMap.get(PieceName.blackQueens)) * 900)) +
            (pieceSquareTableMap.get(PieceName.blackKing))[Long.numberOfTrailingZeros(pieceMap.get(PieceName.blackKing))];
            
        return eval;
    }

    public void makeMove(Move move) {
        PieceName pieceType = move.getPieceType(); 
        long pieces = pieceMap.get(pieceType);
        long destination = move.getDestination();
        long origin = move.getOrigin();
        boolean white = isWhite(pieceType);

        List<PieceName> opponentPieceTypes = white
            ? Arrays.asList(
                PieceName.blackPawns,
                PieceName.blackBishops,
                PieceName.blackKnights,
                PieceName.blackRooks,
                PieceName.blackQueens,
                PieceName.blackKing
            )
            : Arrays.asList(
                PieceName.whitePawns,
                PieceName.whiteBishops,
                PieceName.whiteKnights,
                PieceName.whiteRooks,
                PieceName.whiteQueens,
                PieceName.whiteKing
              );

        move.setMoveCountDraw(moveCountDraw);
        moveCountDraw++;

        if (pieceType.toString().contains("Pawns")) {
            moveCountDraw = 0;
        }
              
        // move piece
        if (!move.getPromotion()) {
            pieces &= ~origin;
            pieces |= destination;
    
            pieceMap.put(pieceType, pieces);
        } else {
            PieceName promotionPieceType = move.getPromotionPiece();
            long piecesOfPromotionType = pieceMap.get(promotionPieceType);
            PieceName pawnName = white ? PieceName.whitePawns : PieceName.blackPawns;
            long pawns = white ? pieceMap.get(pawnName) : pieceMap.get(pawnName);

            piecesOfPromotionType |= move.getDestination();
            pawns &= ~origin;

            pieceMap.put(promotionPieceType, piecesOfPromotionType);
            pieceMap.put(pawnName, pawns);
        }

        // check for capture and remove captured piece from board
        for (PieceName opponentPieceType : opponentPieceTypes) {
            long opponentPiecesOfThisType = pieceMap.get(opponentPieceType);

            if ((pieceMap.get(opponentPieceType) & move.getDestination()) != 0L) {
                if ((opponentPieceType == PieceName.blackKing) | (opponentPieceType == PieceName.whiteKing)) {
                    throw new AssertionError(pieceType + " tried to capture the king.");
                }

                move.setCapture(opponentPieceType);
                opponentPiecesOfThisType &= ~destination; 
                pieceMap.put(opponentPieceType, opponentPiecesOfThisType);

                moveCountDraw = 0;

                break;
            }
        }

        // check en passant capture
        if (move.getDestination() == this.enPassantTarget) {
            PieceName opponentPawnName = white ? PieceName.blackPawns : PieceName.whitePawns;
            long capturedPawn = white ? this.enPassantTarget << 8 : this.enPassantTarget >>> 8;
            long opponentPawns = this.pieceMap.get(opponentPawnName);

            move.setEnPassant(true);
            this.pieceMap.put(opponentPawnName, (opponentPawns & ~capturedPawn)); 
        }

        if (move.checkCastle()) {
            long rooks = white ? pieceMap.get(PieceName.whiteRooks) : pieceMap.get(PieceName.blackRooks);
            
            rooks &= ~move.getRookOriginCastling();
            rooks |= move.getRookDestinationCastling(); 

            if (white) {
                pieceMap.put(PieceName.whiteRooks, rooks);
            } else {
                pieceMap.put(PieceName.blackRooks, rooks);
            }
        }

        updateCastlingRights(move);
        move.setEnPassantTarget(this.enPassantTarget);

        boolean doublePawnPush = 
            (move.getPieceType() == PieceName.whitePawns) && ((move.getOrigin() >>> 16) == move.getDestination()) ||
            (move.getPieceType() == PieceName.blackPawns) && ((move.getOrigin() << 16) == move.getDestination());

        // check for en passant target creation
        if (doublePawnPush) {
            this.enPassantTarget = white ? move.getOrigin() >>> 8 : move.getOrigin() << 8;
        } else {
            this.enPassantTarget = 0L;
        }

        System.out.println(moveCountDraw);
    }

    public void unmakeMove(Move move) {
        PieceName pieceType = move.getPieceType(); 
        long pieces = pieceMap.get(pieceType);
        long destination = move.getDestination();
        long origin = move.getOrigin();
        PieceName capture = move.getCapture();
        boolean white = isWhite(pieceType);
        boolean enPassant = move.getEnPassant();
        
        // move piece from destination back to origin
        pieces &= ~destination;
        pieces |= origin;
        pieceMap.put(pieceType, pieces);

        moveCountDraw = move.getMoveCountDraw();

        // resurect captured piece
        if (capture != null) {
            long piecesOfCapturedType = pieceMap.get(capture);
            pieceMap.put(capture, (piecesOfCapturedType | destination));
        }
        if (enPassant) {
            PieceName opponentPawnName = white ? PieceName.blackPawns : PieceName.whitePawns;
            long opponentPawns = pieceMap.get(opponentPawnName);

            if (white) {
                opponentPawns |= (move.getDestination() << 8);
            } else {
                opponentPawns |= (move.getDestination() >>> 8);
            }

            pieceMap.put(opponentPawnName, opponentPawns);
        }

        move.setCapture(null);
        move.setEnPassant(false);

        if (move.checkCastle()) {
            long rooks = white ? pieceMap.get(PieceName.whiteRooks) : pieceMap.get(PieceName.blackRooks);
            
            rooks &= ~move.getRookDestinationCastling();
            rooks |= move.getRookOriginCastling(); 

            if (white) {
                pieceMap.put(PieceName.whiteRooks, rooks);
            } else {
                pieceMap.put(PieceName.blackRooks, rooks);
            }
        }

        // restore castling rights to their states before the move was made
        if (move.getRevokedCastleRights()) {
            if (white) {
                this.canWhiteCastle = true;
            } else {
                this.canBlackCastle = true;
            }
        }
        if (move.getFirstKingsideRookMove()) {
            if (white) {
                this.rooksMoved[0] = false;
            } else {
                this.rooksMoved[2] = false;
            }
        }
        if (move.getFirstQueensideRookMove()) {
            if (white) {
                this.rooksMoved[1] = false;
            } else {
                this.rooksMoved[3] = false;
            }
        }

        // undo any pawn promotions
        if (move.getPromotion()) {
            PieceName promotionPieceType = move.getPromotionPiece();
            long piecesOfPromotionType = pieceMap.get(promotionPieceType);

            piecesOfPromotionType &= ~move.getDestination();

            pieceMap.put(promotionPieceType, piecesOfPromotionType);
        }

        this.enPassantTarget = move.getEnPassantTarget();

        System.out.println(moveCountDraw);
    }

    public boolean checkLegality(Move move) {
        PieceName pieceType = move.getPieceType();
        long piece = move.getOrigin(); 
        boolean white = isWhite(pieceType);
        long ownPieces = white ? getWhitePieces() : getBlackPieces(); 
        long opponentPieces = white ? getBlackPieces() : getWhitePieces(); 
        long ownPiecesOfThisType = pieceMap.get(pieceType);

        // check that there is a piece at the start
        if ((piece & ownPiecesOfThisType) == 0L) {
            return false;
        }

        // check that it can go to the destination
        long allPieceMoves = switch(pieceType) {
            case whitePawns, blackPawns -> getPawnMoves(piece, ownPieces, opponentPieces, white); 
            case whiteKnights, blackKnights -> getKnightMoves(piece, ownPieces); 
            case whiteBishops, blackBishops -> getBishopMoves(piece, ownPieces); 
            case whiteRooks, blackRooks -> getRookMoves(piece, ownPieces);
            case whiteQueens, blackQueens -> getQueenMoves(piece, ownPieces); 
            case whiteKing, blackKing -> getKingMoves(piece, ownPieces);
        };

        if ((allPieceMoves & move.getDestination()) == 0L && !move.checkCastle()) {
            return false;
        }

        // check that move does not hang king
        makeMove(move);
        if (inCheck(white)) {
            unmakeMove(move);
            return false;
        }
        unmakeMove(move);

        if (move.checkCastle()) {
            // make sure the side can castle
            if (white && !canWhiteCastle) {
                return false;
            } else if (!white && !canBlackCastle) {
                return false;
            }
        }

        return true;
    }

    private List<AttackerLocation> attackedBy(long square, boolean white) {
        long ownPieces = white ? getWhitePieces() : getBlackPieces();
        long opponentPieces = white ? getBlackPieces() : getWhitePieces();

        long opponentRooks = white ? pieceMap.get(PieceName.blackRooks) : pieceMap.get(PieceName.whiteRooks);
        long opponentBishops = white ? pieceMap.get(PieceName.blackBishops) : pieceMap.get(PieceName.whiteBishops);
        long opponentKnights = white ? pieceMap.get(PieceName.blackKnights) : pieceMap.get(PieceName.whiteKnights);
        long opponentQueens = white ? pieceMap.get(PieceName.blackQueens) : pieceMap.get(PieceName.whiteQueens);
        long opponentPawns = white ? pieceMap.get(PieceName.blackPawns) : pieceMap.get(PieceName.whitePawns);

        long rookAttacks = getRookMoves(square, ownPieces) & (opponentRooks | opponentQueens);
        long bishopAttacks = getBishopMoves(square, ownPieces) & (opponentBishops | opponentQueens);
        long knightAttacks = getKnightMoves(square, ownPieces) & opponentKnights; 
        long pawnAttacks = getPawnMoves(square, ownPieces, opponentPieces, white) & opponentPawns;

        List<AttackerLocation> attackerLocations = new ArrayList<>();

        while (rookAttacks != 0L) {
            long attack = Long.highestOneBit(rookAttacks);
            attackerLocations.add(new AttackerLocation(PieceType.rook, attack));
            rookAttacks &= ~attack;
        }
        while (bishopAttacks != 0L) {
            long attack = Long.highestOneBit(bishopAttacks);
            attackerLocations.add(new AttackerLocation(PieceType.bishop, attack));
            bishopAttacks &= ~attack;
        }
        while (knightAttacks != 0L) {
            long attack = Long.highestOneBit(knightAttacks);
            attackerLocations.add(new AttackerLocation(PieceType.knight, attack));
            knightAttacks &= ~attack;
        }
        while (pawnAttacks != 0L) {
            long attack = Long.highestOneBit(pawnAttacks);
            attackerLocations.add(new AttackerLocation(PieceType.pawn, attack));
            pawnAttacks &= ~attack;
        }

        return attackerLocations;
    }

    private List<AttackerLocation> getAttacksOnKing(boolean white) {
        long kingLocation = white ?
            pieceMap.get(PieceName.whiteKing) :
            pieceMap.get(PieceName.blackKing); 

        if (kingLocation == 0L) {
            throw new AssertionError("The king is missing from the board.");
        }

        return attackedBy(kingLocation, white);
    }

    public boolean inCheck(boolean white) {
        return getAttacksOnKing(white).size() > 0;
    }

    public boolean inCheckmate(boolean white) {
        if (!inCheck(white)) {
            return false;
        }

        List<Move> moves = getCheckBreakingMoves(white);
        for (Move move : moves) {
            makeMove(move);

            if (!inCheck(white)) {
                unmakeMove(move);
                return false;
            }

            unmakeMove(move);
        }

        return true;
    }

    // returns all moves that might break check
    public List<Move> getCheckBreakingMoves(boolean white) {
        List<AttackerLocation> attackerLocations = getAttacksOnKing(white);
        
        long kingLocation = white ?
            pieceMap.get(PieceName.whiteKing) :
            pieceMap.get(PieceName.blackKing);

        List<Move> checkBreakingMoves = new ArrayList<>();
        List<Move> allMoves = getAllMoves(white);
        // filter for:
        for (Move move : allMoves) {
            // king moves
            if ((move.getPieceType() == PieceName.whiteKing) || (move.getPieceType() == PieceName.blackKing)) {
                checkBreakingMoves.add(move);
                continue;
            }

            // moves that capture the attacker
            if ((attackerLocations.size() == 1) && ((move.getDestination() & attackerLocations.get(0).getLocation()) != 0L)) {
                checkBreakingMoves.add(move);
                continue;
            }

            // moves that block sliding pieces
            if ((attackerLocations.size() == 1) && (attackerLocations.get(0).getAttackType() == PieceType.rook)) {
                long allPieces = getWhitePieces() | getBlackPieces();
                long attackRay = (getRookMoves(kingLocation, allPieces)) & getRookMoves(attackerLocations.get(0).getLocation(), allPieces);

                if ((move.getDestination() & attackRay) != 0L) {
                    checkBreakingMoves.add(move);
                }
            }

            if ((attackerLocations.size() == 1) && (attackerLocations.get(0).getAttackType() == PieceType.bishop)) {
                long allPieces = getWhitePieces() | getBlackPieces();
                long attackRay = (getBishopMoves(kingLocation, allPieces)) & getBishopMoves(attackerLocations.get(0).getLocation(), allPieces);

                if ((move.getDestination() & attackRay) != 0L) {
                    checkBreakingMoves.add(move);
                }
            }
        }

        return checkBreakingMoves;
    }
}
