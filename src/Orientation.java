
public enum Orientation {
	EAST, NORTH, WEST, SOUTH;
	
	public Orientation next(int n) {
		switch((this.ordinal()+n)%4) {
		case 0: return EAST;
		case 1: return NORTH;
		case 2: return WEST;
		case 3: return SOUTH;
		default:return SOUTH;
		}
	}
}
