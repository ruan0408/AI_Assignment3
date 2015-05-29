
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
	
	public static String difference(Orientation o1, Orientation o2) {
		if(o1.next(1).equals(o2)) return "L";
		if(o1.next(2).equals(o2)) return "LL";
		if(o1.next(3).equals(o2)) return "R";
		return "";
	}
}
