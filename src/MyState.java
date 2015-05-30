import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MyState {

	private final int FRONT = 0;
	private final int LEFT = 1;
	private final int BACK = 2;
	private final int RIGHT = 3;

	private static WorldMap map;
	Boolean axe;
	Boolean boat;
	Boolean gold;
	Integer dynamites;
	Position position;
	Orientation orientation;

	private int fValue;
	private int gValue;
	private MyState father;
	private Map<Position, Character> changes;

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
		changes = new HashMap<Position, Character>();
	}

	public MyState(MyState prototype) {
		axe = prototype.axe();
		boat = prototype.boat();
		gold = prototype.gold();
		dynamites = prototype.dynamites();
		position = new Position(prototype.row(), prototype.column());
		orientation = prototype.orientation().next(0);
		changes = new HashMap<Position, Character>(prototype.changes);
		
	}
	
	public MyState(Boolean axe, Boolean boat, Boolean gold, Integer dyn, 
			Position pos, Orientation ori) {
		this.axe = axe;
		this.boat = boat;
		this.gold = gold;
		this.dynamites = dyn;
		position = pos;
		orientation = ori;
		changes = null;
	}

	public MyState copy() {
		MyState child = new MyState(this); 
		child.setFather(this);
		return child;
	}
	
	public static MyState treeState(Position pos) {
		MyState s = new MyState(true, null, null, null, pos, null);
		return s;
	}

	public static MyState borderState(Position pos) {
		MyState s = new MyState(null, null, null, null, pos, null);
		return s;
	}

	public static MyState goldState(Position gold) {
		return new MyState(null, null, true, null, gold, null);
	}

	public static MyState axeState(Position axe) {
		return new MyState(true, null, true, null, axe, null);
	}

	public static MyState finalState() {
		return new MyState(null, null, true, null, new Position(79, 79), null);
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
		if(dynamites != null) dynamites++;
	}
	public void useDynamite() {
		if(dynamites != null) dynamites--;
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

	public void setBoat(boolean b){boat = b;}
	public void setAxe(boolean b){axe = b;}
	public void setGold(){gold = true;}
	public void setFValue(int v){fValue = v;}
	public void setGValue(int g){gValue = g;}
	public int getFValue(){return fValue;}
	public int getGValue(){return gValue;}
	public void setFather(MyState father){this.father = father;}
	public MyState getFather(){return father;}
	public int distance(MyState b) {
		return Math.abs(row()-b.row())+Math.abs(column()-b.column());
	}

	public void print(String pad) {
		System.out.print(pad);
		position.print();
		System.out.println(pad+"FValue: "+fValue);
		System.out.println(pad+"GValue: "+gValue);
		System.out.println(pad+"Father: "+father);
		System.out.println(pad+"Axe: "+axe);
		System.out.println(pad+"Dynamites: "+dynamites); 
		System.out.println(pad+"Boat: "+boat);
		System.out.println(pad+"Gold: "+gold);
		System.out.println(pad+"Orientation: "+orientation);
		System.out.println();
	}

	public List<MyState> validChildrenStates(Set<MyState>visualized) {
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

	private MyState getState(int side, Set<MyState> visualized) {
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

	private MyState setUp(int r, int c, Set<MyState> visualized) {
		if(r < 0 || r >= map.rows() || c < 0 || c >= map.columns()) return null;
	
		MyState newState = copy();
		newState.setPosition(new Position(r, c));
	
		char tile = newState.getChar();
		newState.updateOrientation();
	
		switch(tile) {
		case ' ':
			if(boat()) newState.setCharAt(position, 'B');
			newState.setBoat(false);
			break;
		case '~':
			if(!boat()) newState = null;
			//else newState.setCharAt(position, '~');
			break;
		case 'T':
		case '*':
			if(boat()) newState.setCharAt(position, 'B');
			newState.setBoat(false);
			if(newState.canDestroyObstable(tile)) 
				newState.setChar(' ');
			else newState = null;
			break;
		case 'B':
			newState.setBoat(true);
			newState.setChar(' ');
			break;
		case 'd':
			if(boat()) newState.setCharAt(position, 'B');
			newState.setBoat(false);
			newState.addDynamite();
			newState.setChar(' ');
			break;
		case 'a':
			if(boat()) newState.setCharAt(position, 'B');
			newState.setBoat(false);
			newState.setAxe(true);
			newState.setChar(' ');
			break;
		case 'g':
			if(boat()) newState.setCharAt(position, 'B');
			newState.setBoat(false);
			newState.setGold();
			newState.setChar(' ');
			break;
		default://'?' and '.'
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
		case 'T': 	
			if(axe()) {return true;}
			if(dynamite()) {useDynamite();return true;}
			break;
		case '*':	
			if(dynamite()) {useDynamite(); return true;}
			break;
		default:	return false;
		}
		return false;
	}
	
	public char getChar() {return getCharAt(position);}
	
	public char getCharAt(Position p) {
		if(changes.containsKey(p)) 
			return changes.get(p);

		return map.getCharAt(p);
	}
	
	private void setCharAt(Position p, char c) {changes.put(p, c);}
	
	private void setChar(char c) {setCharAt(position, c);}
	
	private boolean equalT(Object o1, Object o2) {
		if(o1 == null || o2 == null) return true;
		return o1.equals(o2);
	}
	
	@Override
	public boolean equals(Object b) {
		MyState s = (MyState)b;

		if((equalT(axe, s.axe) && equalT(boat, s.boat) && equalT(gold, s.gold) &&
				equalT(dynamites, s.dynamites) && position.equals(s.position) && 
				equalT(orientation, s.orientation)/* && equalT(changes, s.changes)*/))
			return true;

		return false;
	}

	@Override
	public int hashCode() {
		return row()*column() + row()+column();
	}
}
