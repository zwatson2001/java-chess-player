// use this to store multiple moves for one piece
public class PieceMoves {
    private long origin, destinations; 

    PieceMoves(long origin, long destinations) {
        this.origin = origin;
        this.destinations = destinations;
    }

    public long getDestinations() {
        return destinations;
    }

    public long getOrigin() {
        return origin;
    }
}
