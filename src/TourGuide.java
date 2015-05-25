import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.event.ListSelectionEvent;

public class TourGuide {

	private Agent agent;
	private List<Character> path;
	private Set<Position> border;

	private final String actions = "FLR";
	private Iterator<Integer> ints;
	private Random random;
	private int count = 0;

	public TourGuide(Agent agent) {
		this.agent = agent;
		path = new ArrayList<Character>();
		border = new HashSet<Position>();
		random = new Random();
		//ints = r.ints(0, actions.length()).iterator();
	}

	public char next() {
		char action;
		Position gold = agent.map.getGoldPosition();
		Position agentPosition = agent.getPosition();
		WorldMap map = agent.map;

		do {
			if(!path.isEmpty()) { 
				action = path.remove(0);
			} else if(map.hasBoat() && !agent.isOnBoat()) { 
				path = pathToActions(findPath(agentPosition, map.closestBoat()));
				for(char a : path)System.out.print(a);
				System.out.println("\n");
				//agent.setOnBoat(true);
				action = path.remove(0);
			}/*else if(gold != null && !gold.equals(agentPosition)) {
				path = pathToActions(map.findPath(agentPosition, gold));
				for(Character a : path) System.out.println(a);
				action = path.remove(0);
			} else if(gold != null && gold.equals(agentPosition)){
				agent.setHasGold(true);
				map.removeGold();
				path = pathToActions(map.findPath(agentPosition, new Position(79,79)));
				action = path.remove(0);
			}*/ else {
				border.addAll(findUnexploredBorder());
				Iterator<Position> it = border.iterator();
				int i = random.nextInt(border.size());
				for(int j = 0; j < i; j++) it.next();
				path = pathToActions(findPath(agentPosition, it.next()));
				action = path.remove(0);
				//action = actions.charAt(ints.next());				
			}
		} while(isAgentGoingToDie(action));

		return action;
	}

//	public List<Position> findPath(Position a, Position b) {
//		Map<Position, Integer> list = new HashMap<Position, Integer>();
//		Map<Position, Integer> explored = new HashMap<Position, Integer>();
//		Map<Position, Position> parent  = new HashMap<Position, Position>(); //child father
//		List<Position> path = new ArrayList<Position>();
//		Position current = null;
//		list.put(a, 0);
//		explored.put(a, 1);
//		parent.put(a, a);
//		while(!list.isEmpty() && !(current = getBestPosition(list)).equals(b)) {
//			for(Position pos : getValidNeighbours(current)) {
//				if(explored.get(pos) == null) {
//					explored.put(pos, 1);
//					list.put(pos, list.get(current) + pos.distance(b));
//					parent.put(pos, current);
//				}
//			}
//			list.remove(current);
//		}
//		for(Position p : parent.keySet()) {
//			p.print();
//			System.out.print("\t\t");
//			parent.get(p).print();
//		}
//
//		//current = b;
//		path.add(current);
//		do {
//			current = parent.get(current);
//			path.add(current);
//		} while(!current.equals(a));
//		Collections.reverse(path);
//		return path;
//	}
	
	public List<Position> findPath(Position start, Position end) {
		Map<Position, Integer> list = new HashMap<Position, Integer>();
		Map<Position, Integer> explored = new HashMap<Position, Integer>();
		Map<Position, Position> parent  = new HashMap<Position, Position>(); //child father
		List<Position> path = new ArrayList<Position>();
		Position current, reached = null;
		list.put(start, 0);
		explored.put(start, 1);
		parent.put(start, start);
		int nDyn = agent.numberDynamites();
		boolean axe = agent.hasAxe();
		boolean boat = agent.isOnBoat();
		System.out.println(agent.isOnBoat());
		
		current = reached = findPathR(start, end, nDyn, axe, boat, list, explored, parent);
		
		path.add(reached);
		do {
			current = parent.get(current);
			path.add(current);
		} while(!current.equals(reached));
		Collections.reverse(path); System.out.println(agent.isOnBoat());
		//System.exit(-1);
		return path;
	}
	
	public Position findPathR(Position mid, Position end, Integer nDyn, Boolean axe, Boolean boat, Map<Position, Integer> list, 
						  Map<Position, Integer> explored, Map<Position, Position> parent) {
		
		if(!list.isEmpty() && !mid.equals(end)) {
			for(Position pos : getValidNeighbours(mid, nDyn, axe, boat)) {
				if(explored.get(pos) == null) {System.out.println(++count);
					explored.put(pos, 1);
					list.put(pos, list.get(mid) + pos.distance(end));
					parent.put(pos, mid);
				}
			}
			list.remove(mid);
			Position current = getBestPosition(list);
			findPathR(current, end, nDyn, axe, boat, list, explored, parent);
		}
		return mid;
	}

	// If necessary, can be improved using a priority queue
	private Position getBestPosition(Map<Position, Integer> list) {
		Set<Entry<Position, Integer>> entries = list.entrySet();
		int min = Integer.MAX_VALUE;
		Position best = null;
		for(Entry<Position, Integer> entry : entries)
			if(entry.getValue() < min) { 
				min = entry.getValue();
				best = entry.getKey();
			}
		return best;
	}

	private List<Position> getValidNeighbours(Position pos, Integer nDyn, Boolean axe, Boolean boat) {
		List<Position> list = new ArrayList<Position>();
		int r = pos.getRow(); int c = pos.getColumn();
		if(isValid(r-1, c, nDyn, axe, boat)) list.add(new Position(r-1, c));
		if(isValid(r, c+1, nDyn, axe, boat)) list.add(new Position(r, c+1));
		if(isValid(r+1, c, nDyn, axe, boat)) list.add(new Position(r+1, c));
		if(isValid(r, c-1, nDyn, axe, boat)) list.add(new Position(r, c-1));
		return list;
	}

	private boolean isValid(int r, int c, Integer nDyn, Boolean axe, Boolean boat) {
		WorldMap map = agent.map;
		if(r < 0 && r >= map.rows() && c < 0 && c >= map.columns()) return false;
		switch(map.getCharAt(r, c)) {
		case '~': return boat;
		case 'T':
			if(axe) return true;
			return (nDyn-- > 0);
		case '*': return nDyn-- > 0;
		case 'B': return (boat = true);
		case 'd': return (++nDyn > 0);
		case 'a': return (axe = true);
		case '.': return false;
		default: return true;
		}
	}
//		if(map.getCharAt(r, c) == '.' || map.getCharAt(r, c) == '?') 
//			return false;
//		else if(map.getCharAt(r, c) == '~' && boat) { 
//			return true;
//		}
//		if(map.getCharAt(r, c) == 'T' && (nDyn > 0 || axe)) 
//			return true;
//		if(map.getCharAt(r, c) == '*' && nDyn > 0)
//			return true;
//		return true;

	private List<Position> findUnexploredBorder() {
		WorldMap map = agent.map;
		List<Position> list = new ArrayList<Position>();
		for(int i = 0; i < agent.map.rows(); i++)
			for(int j = 0 ; j < agent.map.columns(); j++)
				try {
					if(!map.isUnknown(i, j) && (map.isUnknown(i-1, j) || map.isUnknown(i+1, j) ||
							map.isUnknown(i, j-1) || map.isUnknown(i, j+1)))
						list.add(new Position(i, j));
				} catch(Exception e) {continue;}

		return list;
	}

	private List<Character> pathToActions(List<Position> list) {
		List<Character> l = new ArrayList<Character>();
		String actions = "C";//useless action for avoiding exceptions
		Position current = agent.getPosition();
		Orientation currentOri = agent.getOrientation();
		for(Position pos : list) {
			if(getSidePosition(current, Orientation.NORTH, currentOri).equals(pos)) 
				actions += "F";
			else if(getSidePosition(current, Orientation.SOUTH, currentOri).equals(pos)) { 
				actions += "RRF";
				currentOri = currentOri.next(2);
			}
			else if(getSidePosition(current, Orientation.EAST, currentOri).equals(pos)) { 
				actions += "RF";
				currentOri = currentOri.next(3);
			}
			else if(getSidePosition(current, Orientation.WEST, currentOri).equals(pos)) {
				actions += "LF";
				currentOri = currentOri.next(1);
			}
			current = pos;
		}
		for(char c : actions.toCharArray()) l.add(c);
		return l;
	}

	private Position getSidePosition(Position pos, Orientation side, Orientation currentOrientation) {
		Orientation newOrientation = null;
		switch(side) {
		case NORTH:	newOrientation = currentOrientation.next(0);	break; //front
		case EAST:	newOrientation = currentOrientation.next(3);	break; //right
		case SOUTH:	newOrientation = currentOrientation.next(2);	break; //back
		case WEST:	newOrientation = currentOrientation.next(1);	break; //left
		default: break;
		}
		return agent.map.getFrontPosition(pos, newOrientation);
	}

	private boolean isAgentGoingToDie(char action) {
		char frontTile = agent.getFrontTile();
		if(action == 'F' && (frontTile == '.' || (!agent.isOnBoat() && frontTile == '~')))
			return true;
		return false;
	}
}
