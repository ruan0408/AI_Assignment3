/*
  By:

  Gervasio Protasio dos Santos Neto - 5050769
  Ruan De Menezes Costa - 5050761
*/

public enum Orientation {
    EAST, NORTH, WEST, SOUTH;
    
    /*
      The Orientaion "clock":
               N


          W          E

      
               S     

    */

    public Orientation next(int n) {
	/*
	  Returns Orientation obtatined by taking n counter-clockwise
	  steps
	 */
	switch((this.ordinal()+n)%4) {
	case 0: return EAST;
	case 1: return NORTH;
	case 2: return WEST;
	case 3: return SOUTH;
	default:return SOUTH;
	}
    }
	
    public static String difference(Orientation o1, Orientation o2) {
	/*
	  Returns the moves the need to be taken in order to go from
	  fancing Orientation o1 to facing Orientaion o2
	 */
	if(o1.next(1).equals(o2)) return "L";
	if(o1.next(2).equals(o2)) return "LL";
	if(o1.next(3).equals(o2)) return "R";
	return "";
    }
}
