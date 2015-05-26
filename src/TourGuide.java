import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class TourGuide {

	private Agent agent;
	private List<Character> path;
	private Set<Position> border;

	//private final String actions = "FLR";
	//private Iterator<Integer> ints;
	//private int count = 0;
	private Random random;

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
	
//	public List<Position> findPath(Position start, Position end) {
//		Map<Position, Integer> f = new HashMap<Position, Integer>();
//		Map<Position, Integer> g = new HashMap<Position, Integer>();
//		Map<Position, Integer> explored = new HashMap<Position, Integer>();
//		Map<Position, Position> parent  = new HashMap<Position, Position>(); //child father
//		List<Position> path = new ArrayList<Position>();
//		Position current, reached = null;
//		g.put(start, 0);
//		f.put(start, start.distance(end));
//		explored.put(start, 1);
//		parent.put(start, start);
//		int nDyn = agent.numberDynamites();
//		boolean axe = agent.hasAxe();
//		boolean boat = agent.isOnBoat();
//		System.out.println(agent.isOnBoat());
//		System.out.println(agent.map.getCharAt(end.getRow(), end.getColumn()));
//		current = reached = findPathR(start, end, nDyn, axe, boat, f, g, explored, parent);
//		
//		path.add(reached);
//		do {
//			current = parent.get(current);
//			path.add(current);
//		} while(!current.equals(start));
//		Collections.reverse(path);
//		return path;
//	}
	
//	public Position findPathR(Position mid, Position end, Integer nDyn, Boolean axe, Boolean boat, Map<Position, Integer> f, 
//							  Map<Position, Integer> g, Map<Position, Integer> explored, Map<Position, Position> parent) {
//		
//		if(!f.isEmpty() && !mid.equals(end)) {
//			for(Position pos : getValidNeighbours(mid, explored, nDyn, axe, boat)) {
//				explored.put(pos, 1);
//				g.put(pos, g.get(mid)+1);
//				f.put(pos, g.get(pos) + pos.distance(end));
//				parent.put(pos, mid);
//			}
//			f.remove(mid);
//			g.remove(mid);
//			if(!f.isEmpty()){
//				System.out.println("lallaal");
//				System.out.println(getValidNeighbours(mid, explored, nDyn, axe, boat).size());
//				Position current = getBestPosition(f);
//				return findPathR(current, end, nDyn, axe, boat, f, g, explored, parent);
//			}
//		}
//		return mid;
//	}
//	
	public List<Position> findPath(Position start, Position end) {
		Map<Position, Integer> f = new HashMap<Position, Integer>();
		Map<Position, Integer> g = new HashMap<Position, Integer>();
		Map<Position, Integer> explored = new HashMap<Position, Integer>();
		Map<Position, Position> parent  = new HashMap<Position, Position>(); //child father
		Map<Position, AgentState> states  = new HashMap<Position, AgentState>();
		List<Position> path = new ArrayList<Position>();
		Position current = null;
		g.put(start, 0);
		f.put(start, start.distance(end));
		explored.put(start, 1);
		parent.put(start, start);
		AgentState s = new AgentState(agent.hasAxe(), agent.isOnBoat(), agent.numberDynamites());
		states.put(start, s);
		
		while(!f.isEmpty() && !(current = getBestPosition(f)).equals(end)) {
			current.print();
			for(Position candPos : getValidNeighbours(current, explored, states)) {
				//explored.put(candPos, 1);
				g.put(candPos, g.get(current)+1);
				f.put(candPos, g.get(candPos) + candPos.distance(end));
				parent.put(candPos, current);
			}
			f.remove(current);
			g.remove(current);
		}
		
		path.add(current);
		do {
			current = parent.get(current);
			path.add(current);
		} while(!current.equals(start));
		Collections.reverse(path);
		return path;
	}

	// If necessary, can be improved using a priority queue
	private Position getBestPosition(Map<Position, Integer> f) {
		Set<Entry<Position, Integer>> entries = f.entrySet();
		int min = Integer.MAX_VALUE;
		Position best = null;
		for(Entry<Position, Integer> entry : entries)
			if(entry.getValue() < min) { 
				min = entry.getValue();
				best = entry.getKey();
			}
		return best;
	}

	private List<Position> getValidNeighbours(Position pos, Map<Position, Integer>explored, 
											  Map<Position, AgentState> states) {
		List<Position> list = new ArrayList<Position>();
		int r = pos.getRow(); int c = pos.getColumn();
		AgentState newState;
		AgentState currentState = states.get(pos);
		Position newPos;
		
		System.out.println(currentState);
		if((newState = isValid(r-1, c, explored, currentState)) != null){
			newPos = new Position(r-1, c);
			list.add(newPos);
			explored.put(newPos, 1);
			states.put(newPos, newState);
		}
		if((newState = isValid(r, c+1, explored, currentState)) != null) {
			newPos = new Position(r, c+1);
			list.add(newPos);
			explored.put(newPos, 1);
			states.put(newPos, newState);
		}
		if((newState = isValid(r+1, c, explored, currentState)) != null) {
			newPos = new Position(r+1, c);
			list.add(newPos);
			explored.put(newPos, 1);
			states.put(newPos, newState);
		}
		if((newState = isValid(r, c-1, explored, currentState)) != null) {
			newPos = new Position(r, c-1);
			list.add(newPos);
			explored.put(newPos, 1);
			states.put(newPos, newState);
		}
		return list;
	}

	//returns the new state if this position is valid and null otherwise
	private AgentState isValid(int r, int c, Map<Position, Integer> explored, AgentState currentState) {
		WorldMap map = agent.map;
		if(r < 0 || r >= map.rows() || c < 0 || c >= map.columns()) return null;
		if(explored.containsKey(new Position(r, c))) return null;
	
		AgentState newState = new AgentState(currentState);
		switch(map.getCharAt(r, c)) {
		case '.': return null;
		case ' ': 
			if(currentState.boat) {newState.boat = false;}
			break;
		case '~': 
			if(!currentState.boat) newState = null;
			break;
		case 'T':
			if(currentState.boat) newState.boat = false;
			if(currentState.axe) return newState;
			else if(currentState.dynamites > 0) {newState.dynamites--;}
			break;
		case '*':
			if(currentState.boat) newState.boat = false;
			if(currentState.dynamites > 0) {newState.dynamites--;}
			break;
		case 'B':	newState.boat = true; break;
		case 'd': 
			if(currentState.boat) newState.boat = false;
			if(currentState.dynamites > 0) {newState.dynamites++;}
			break;
		case 'a':
			if(currentState.boat) newState.boat = false;
			newState.axe = true;
			break;
		default: newState = null;
		}
		return newState;
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
}
