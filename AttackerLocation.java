public class AttackerLocation {
    private PieceType attackType; 
    private long location;

    AttackerLocation(PieceType attackType, long location) {
        this.attackType = attackType;
        this.location = location;
    }

    public long getLocation() {
        return location;
    }

    public PieceType getAttackType() {
        return attackType;
    }
}
