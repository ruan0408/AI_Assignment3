import java.util.ArrayList;
import java.util.List;

public class MyState {

	private final int FRONT = 0;
	private final int LEFT = 1;
	private final int BACK = 2;
	private final int RIGHT = 3;
//	private final static Boolean nullBool = null;
//	private final static Integer nullInt = -1;
//	private final static Position nullPos = new Position(-100, -100);
//	private final static Orientation nullOri = Orientation.NULL;
	
	static WorldMap map;
	
	Boolean axe;
	Boolean boat;
	Boolean gold;
	Integer dynamites;
	Position position;
	Orientation orientation;
	private Boolean cut;
	private Boolean blasted;
	
	private int fValue;
	private int gValue;
	private MyState father;
	
	
	public MyState(boolean axe, boolean boat, boolean gold, int dynammites, 
					Position pos, Orientation ori, WorldMap map) {
		this.axe = axe;
		this.boat = boat;
		this.gold = gold;
		this.dynamites = dynammites;
		position = pos;
		orientation = ori;
		gValue = fValue = 0;
		MyState.map = map;
		father = null;
		cut = false; 
		blasted = false;
	}
	
	public MyState(MyState prototype) {
		axe = prototype.axe();
		boat = prototype.boat();
		gold = prototype.gold();
		dynamites = prototype.dynamites();
		position = prototype.getPosition();
		orientation = prototype.orientation();
		cut = prototype.cut;
		blasted = prototype.blasted;
	}
	
	public MyState(Boolean axe, Boolean boat, Boolean gold, Integer dyn, 
			Position pos, Orientation ori, Boolean blasted, Boolean cut) {
		this.axe = axe;
		this.boat = boat;
		this.gold = gold;
		this.dynamites = dyn;
		position = pos;
		orientation = ori;
	}
	
	public MyState copy() {
		MyState child = new MyState(this); 
		child.setFather(this);
		child.blasted = false;
		child.cut = false;
		return child;
	}
	public int row(){return position.getRow();}
	public int column(){return position.getColumn();}
	public Position getPosition() {return position;}
	public void setPosition(Position p){position = p;}
	public Orientation orientation(){return orientation;};
	public int dynamites(){
		if(dynamites != null) return dynamites;
		return 0;
	}
	public boolean dynamite(){
		if(dynamites != null) return dynamites > 0;
		return false;
	}
	public void addDynamite() {
		if(dynamite()) dynamites++;
	}
	public void useDynamite() {
		--dynamites;
	}
	public boolean axe() {
		if(axe != null) return axe;
		return false;
	}
	public boolean gold() {
		if(gold != null) return gold;
		return false;
	}
	public boolean boat() {
		if(boat != null) return boat;
		return false;
	}
	public boolean cut() {
		if(cut != null) return cut;
		return false;
	}
	public boolean blasted() {
		if(blasted != null) return blasted;
		return false;
	}
	public void setBoat(boolean b){boat = b;}
	public void setAxe(boolean b){axe = b;}
	public void setGold(){gold = true;}
	public void setFValue(int v){fValue = v;}
	public void setGValue(int g){gValue = g;}
	public int getFValue(){return fValue;}
	public int getGValue(){return gValue;}
	public void setFather(MyState father){this.father = father;}
	public MyState getFather(){return father;}
	public void setCut(boolean b){cut = b;}
	public void setBlasted(boolean b){blasted = b;}
	public int distance(MyState b) {
		return Math.abs(row()-b.row())+Math.abs(column()-b.column());
	}
	
	public void print(String pad) {
		System.out.print(pad);
		position.print();
		System.out.println(pad+"FValue: "+fValue);
		System.out.println(pad+"GValue: "+gValue);
		if(father != null) System.out.println(pad+"Father: "+true);
		else System.out.println(pad+"Father: "+false);
		System.out.println(pad+"Axe: "+axe);
		if(dynamite())	System.out.println(pad+"Dynamites: "+dynamites); 
		System.out.println(pad+"Boat: "+boat);
		System.out.println(pad+"Gold: "+gold);
		if(orientation != null)System.out.println(pad+"Orientation: "+orientation);
		System.out.println(pad+"Cut: "+cut);
		System.out.println(pad+"Blasted: "+blasted);
		System.out.println();
	}
	
	public List<MyState> validChildrenStates(List<MyState>visualized) {
		List<MyState> list = new ArrayList<MyState>();
		MyState child;

		child = getState(FRONT, visualized);
		if(child != null) list.add(child);
		
		child = getState(LEFT, visualized);
		if(child != null) list.add(child);
		
		child = getState(BACK, visualized);
		if(child != null) list.add(child);
		
		child = getState(RIGHT, visualized);
		if(child != null) list.add(child);
		
		return list;
	}
	
	private MyState getState(int side, List<MyState> visualized) {
		int r = row();
		int c = column();
		MyState neighbor = null;
		switch(side) {
		case FRONT: neighbor = setUp(r+1, c, visualized); break; 
		case LEFT:	neighbor = setUp(r, c-1, visualized); break;
		case BACK: 	neighbor = setUp(r-1, c, visualized); break;
		case RIGHT:	neighbor = setUp(r, c+1, visualized); break;
		}
		return neighbor;
	}
	
	private MyState setUp(int r, int c, List<MyState> visualized) {
		if(r < 0 || r >= map.rows() || c < 0 || c >= map.columns()) return null;
		
		MyState newState = copy();
		newState.setPosition(new Position(r, c));
		
		newState.updateOrientation();
		
		char tile = map.getCharAt(r, c);
		switch(tile) {
//		case '.':
//		case '?': return null;
		case ' ':
			newState.setBoat(false);
			break;
		case '~': 
			if(!newState.boat()) newState = null;
			break;
		case 'T':
		case '*':
			newState.setBoat(false);
			if(!newState.canDestroyObstable(tile)) newState = null;
			break;
		case 'B':
			newState.setBoat(true); 
			break;
		case 'd': 
			newState.setBoat(false);
			newState.addDynamite();
			break;
		case 'a':
			newState.setBoat(false);
			newState.setAxe(true);
			break;
		case 'g':
			newState.setBoat(false);
			newState.setGold();
			break;
		default:
			newState = null;
		}
		
		if(newState == null || visualized.contains(newState)) 
			return null;

		return newState;
	}
	
	//the reference is my father
	private void updateOrientation() {
		Orientation aux = null;
		for(int i = 0 ; i < 4; i++) {
			aux = father.orientation().next(i);
			if(map.getFrontPosition(father.getPosition(), aux).equals(getPosition()))
				break;
		}
		orientation = aux;
	}
	
	private boolean canDestroyObstable(char obst) {
		switch(obst) {
		case 'T': 	if(axe()) {setCut(true);return true;}
					if(dynamite()) {setBlasted(true); useDynamite(); return true;}
					break;
		case '*':	if(dynamite()) {setBlasted(true); useDynamite(); return true;}
					break;
		default:	return false;
		}
		return false;
	}
	
	public static MyState treeState(Position pos) {
		MyState s = new MyState(true, null, null, null, pos, null, null, true);
		return s;
	}
	
	public static MyState borderState(Position pos) {
		MyState s = new MyState(null, null, null, null, pos, null, null, null);
		//MyState s = new MyState(null, null, null, null, pos, null, null, null);
		return s;
	}
	 private boolean equalT(Object o1, Object o2) {
		 if(o1 == null || o2 == null) return true;
		 return o1.equals(o2);
	 }
	@Override
	public boolean equals(Object b) {
		MyState s = (MyState)b;
		
		if((equalT(axe, s.axe) && equalT(boat, s.boat) && equalT(gold, s.gold) &&
			equalT(dynamites, s.dynamites) && position.equals(s.position) && 
			equalT(orientation, s.orientation) && equalT(cut, s.cut) && equalT(blasted, s.blasted)))
			return true;
			
		//if(axe.equals(s.axe) && boat.equals(s.boat) && gold.equals(s.gold) &&
//			dynamites.equals(s.dynamites) && position.equals(s.position) &&
//			orientation.equals(s.orientation) && cut.equals(s.cut) && blasted.equals(s.blasted))
//			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return position.getRow()*position.getColumn();
	}
}
