// one origin square, one destination square
public class Move {
    private long origin; 
    private long destination;
    private PieceName pieceType;
    private PieceName capture;
    
    // for tracking castling 
    private long rookOriginCastling;
    private long rookDestinationCastling;
    private boolean revokedCastleRights; // did this move revoke the side's castling rights?
    private boolean firstKingsideRookMove; // was this the first time the kingside rook moved?
    private boolean firstQueensideRookMove;
    private boolean castle;

    private boolean enPassant;
    private long enPassantTarget = 0L; // the location of the en passant target when this move was made

    // pawn move that promotes to this piece
    private boolean promotion = false;
    private PieceName promotionPiece;

    // track what the move count towards a draw was when the move was made
    private int moveCountDraw; 

    Move(PieceName pieceType, long origin, long destination) {
        this.pieceType = pieceType; 
        this.origin = origin; 
        this.destination = destination;
    }

    Move(PieceName pieceType, long origin, long destination, PieceName promotion) {
        this.pieceType = pieceType; 
        this.origin = origin; 
        this.destination = destination;
        this.promotion = true;
        this.promotionPiece = promotion;
    }

    Move(PieceName pieceType, long origin, long destination, long rookOriginCastling, long rookDestinationCastling) {
        this.pieceType = pieceType; 
        this.origin = origin; 
        this.destination = destination;
        this.rookOriginCastling = rookOriginCastling;
        this.rookDestinationCastling = rookDestinationCastling;
        this.castle = true;
    }

    public long getOrigin() {
        return this.origin;
    }

    public long getDestination() {
        return this.destination;
    }

    public PieceName getCapture() {
        return this.capture;
    }

    public PieceName getPieceType() {
        return this.pieceType;
    }

    public long getRookOriginCastling() {
        return this.rookOriginCastling;
    }

    public long getRookDestinationCastling() {
        return this.rookDestinationCastling;
    }

    public boolean getRevokedCastleRights() {
        return this.revokedCastleRights;
    }

    public boolean getFirstKingsideRookMove() {
        return this.firstKingsideRookMove;
    }

    public boolean getFirstQueensideRookMove() {
        return this.firstQueensideRookMove;
    }

    public long getEnPassantTarget() {
        return this.enPassantTarget;
    }

    public boolean getEnPassant() {
        return this.enPassant;
    }

    public boolean checkCastle() {
        return this.castle;
    }

    public boolean getPromotion() {
        return this.promotion;
    }

    public PieceName getPromotionPiece() {
        return this.promotionPiece;
    }

    public int getMoveCountDraw() {
        return this.moveCountDraw;
    }

    public void setCapture(PieceName capture) {
         this.capture = capture; 
    }

    public void setRevokedCastleRights(boolean set) {
        this.revokedCastleRights = set;
    }

    public void setFirstKingsideRookMove(boolean set) {
        this.firstKingsideRookMove = set;
    }

    public void setFirstQueensideRookMove(boolean set) {
        this.firstQueensideRookMove = set;
    }

    public void setCastle(boolean set) {
        this.castle = set;
    }

    public void setEnPassantTarget(long target) {
        this.enPassantTarget = target;
    }

    public void setEnPassant(boolean set) {
        this.enPassant = set; 
    }

    public void setMoveCountDraw(int moveCountDraw) {
        this.moveCountDraw = moveCountDraw; 
    }
 }
