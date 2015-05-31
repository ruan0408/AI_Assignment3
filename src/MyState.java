/*
By:

Gervasio Protasio dos Santos Neto - 5050769
Ruan De Menezes Costa - 5050761
*/


import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class MyState {

	private final int FRONT = 0;
	private final int LEFT = 1;
	private final int BACK = 2;
	private final int RIGHT = 3;

	private static WorldMap map; // current map
	Boolean axe;
	Boolean boat;
	Boolean gold;
	Integer dynamites;
	Position position;
	Orientation orientation;

	private int fValue; 	// f() from the A*
	private int gValue; 	// g() from the A*
	private MyState father;	// state that generated me
	private Deque<Entry<Position, Character>> changes; // changes that have been made to the map during the A*

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
		changes = new ArrayDeque<Entry<Position, Character>>();
	}

	/*
	 * Creates a new MyState given a prototype state
	 */
	public MyState(MyState prototype) {
		axe = prototype.axe();
		boat = prototype.boat();
		gold = prototype.gold();
		dynamites = prototype.dynamites();
		position = new Position(prototype.row(), prototype.column());
		orientation = prototype.orientation().next(0);
		changes = new ArrayDeque<Entry<Position, Character>>(prototype.changes);
	}
	
	public MyState(Boolean axe, Boolean boat, Boolean gold, Integer dyn, 
			Position pos, Orientation ori) {
		this.axe = axe;
		this.boat = boat;
		this.gold = gold;
		this.dynamites = dyn;
		position = pos;
		orientation = ori;
	}

	
	//Returns a copy of this state 
	public MyState copy() {
		MyState child = new MyState(this); 
		child.setFather(this);
		return child;
	}
    //Returns a state that represents a border in the position pos
	public static MyState borderState(Position pos) {
		MyState s = new MyState(null, null, null, null, pos, null);
		return s;
	}
    //Returns a state that represents having the gold in position gold
	public static MyState goldState(Position gold) {
		return new MyState(null, null, true, null, gold, null);
	}
    //Returns a state that represents having an axe at the position axe
	public static MyState axeState(Position axe) {
		return new MyState(true, null, true, null, axe, null);
	}
	//Returns a state the represents the final position of the game, that is, having the gold in the initial position
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
	//Returns a list of valid neighbors states
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
	//Returns a new state for the given side if this new state is valid. Otherwise returns null
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
	// Creates a new state with positioned at r, c if it's valid
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
			else newState.setCharAt(position, '~');
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

	//Update my orientation. The reference is my father (state that generated me)
	private void updateOrientation() {
		Orientation aux = null;
		for(int i = 0 ; i < 4; i++) {
			aux = father.orientation().next(i);
			if(map.getFrontPosition(father.getPosition(), aux).equals(getPosition()))
				break;
		}
		orientation = aux;
	}
	// Returns true if this is state can destroy an obstable and updates the state.
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
	//Returns the char at position p. First searches on my own list of changes. If it hasn't been
	//changed, looks for it in the map
	public char getCharAt(Position p) {
		for(Entry<Position, Character> e : changes)
			if(e.getKey().equals(p))
				return e.getValue().charValue();
		
		return map.getCharAt(p);
	}
	
	public char getChar() {return getCharAt(position);}
	
	//Register a change on a position.
	private void setCharAt(Position p, char c) {
		changes.add(new AbstractMap.SimpleEntry<Position, Character>(p, c));
	}
	
	private void setChar(char c) {setCharAt(position, c);}
	
	//Implements equality with "don't cares". Don't cares are represented as null.
	private boolean equalT(Object o1, Object o2) {
		if(o1 == null || o2 == null) return true;
		return o1.equals(o2);
	}
	
	@Override
	public boolean equals(Object b) {
		MyState s = (MyState)b;

		if((equalT(axe, s.axe) && equalT(boat, s.boat) && equalT(gold, s.gold) &&
				equalT(dynamites, s.dynamites) && position.equals(s.position) && 
				equalT(orientation, s.orientation)))
			return true;

		return false;
	}

	@Override
	public int hashCode() {
		return row()*column() + row()+column();
	}
}
