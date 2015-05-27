import java.io.IOException;
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

public class TourGuide {

	private Agent agent;
	private List<Character> path;
	private Set<Position> border;
	private Random random;
	private final String bla = "RLF";

	public TourGuide(Agent agent) {
		this.agent = agent;
		path = new ArrayList<Character>();
		border = new HashSet<Position>();
		random = new Random();
	}

	public char next() {
//		char ch = 0;
//		System.out.print("Enter Action(s): ");
//
//	      try {
//	         while ( ch != -1 ) {
//	            // read character from keyboard
//	            ch  = (char) System.in.read();
//
//	            switch( ch ) { // if character is a valid action, return it
//	            case 'F': case 'L': case 'R': case 'C': case 'B':
//	            case 'f': case 'l': case 'r': case 'c': case 'b':
//	               return((char) ch );
//	            }
//	         }
//	      }
//	      catch (IOException e) {
//	         System.out.println ("IO error:" + e );
//	      }
//	      return 0;
		WorldMap map = agent.map;
		Position gold = map.getGoldPosition();
		Position axe = map.getAxePostion();
		List<Position> dynamites = map.getDynamitePostions();
		//Position agentPosition = agent.getPosition();
		
		System.out.println("Do I think I have the gold?: "+agent.hasGold());
		System.out.println("Do I think I'm in a boat?: "+agent.onBoat());
		System.out.println("Do I think I have dynamite?: "+agent.numberDynamites());
		System.out.println("My orientation: "+agent.getOrientation());
		
		
//		char a;
//		do {
//			a = bla.charAt(random.nextInt(3));
//		} while(isAgentGoingToDie(a));
//		return a;
		if(!path.isEmpty()) return path.remove(0);
		return explore();
//		if(agent.hasGold()) {
//			return setPathAndAct(getActionsToPosition(agent.getInitialPosition()));
//		}
//		if(gold == null) {
//			if(!agent.hasAxe() && axe != null) 
//				return setPathAndAct(getActionsToPosition(axe));
//			if(agent.numberDynamites() < 3 && dynamites.size() != 0)
//				return setPathAndAct(getActionsToPosition(dynamites.get(0)));
//			return explore();
//		}
//		else {
//			System.out.println("aeuhuaehuahea");
//			return setPathAndAct(getActionsToPosition(gold));
//			//System.exit(-1);
//		}
	}
	
	private char explore() {
		Position tree = agent.map.randomTree();
		if(agent.hasAxe() && tree != null)
			return setPathAndAct(getActionsToPosition(tree));
		
		border.addAll(findUnexploredBorder());
		Iterator<Position> it = border.iterator();
		int i = random.nextInt(border.size());
		for(int j = 0; j < i; j++) it.next();
		Position p = it.next();
		System.out.println("Going to "+p.toString());
		return setPathAndAct(getActionsToPosition(p));
	}
	
	private char setPathAndAct(List<Character> actions) {
		path = actions;
		System.out.println(Arrays.toString(actions.toArray(new Character[actions.size()])));
		return path.remove(0);
	}
	
	private List<Character> getActionsToPosition(Position end) {
		System.out.println("My position: "+agent.getPosition().toString());
		System.out.println("End tile: "+ agent.map.getCharAt(end));
		System.out.println("Going to: "+ end.toString());
		List<Position> l = findPath(agent.getPosition(), end);
		for(Position p : l) System.out.println("\t\t "+ p.toString());
		return pathToActions(findPath(agent.getPosition(), end));
	}
		
	private List<Position> findPath(Position start, Position end) {
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
		AgentState s = new AgentState(agent.hasAxe(), agent.onBoat(), agent.hasGold(), agent.numberDynamites());
		states.put(start, s);
		
		while(!f.isEmpty() && !(current = getBestPosition(f)).equals(end)) {
			//current.print();
			for(Position candPos : getValidNeighbours(current, explored, states)) {
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
		case '.':
		case '?': return null;
		case ' ':
			newState.boat = false;
			break;
		case '~': if(!currentState.boat) return null; break;
		case 'T':
			newState.boat = false;
			if(currentState.axe) return newState;
			else if(currentState.dynamites > 0) {newState.dynamites--;}
			else newState = null;
			break;
		case '*':
			newState.boat = false;
			if(currentState.dynamites > 0) {newState.dynamites--;}
			else newState = null;
			break;
		case 'B':
			newState.boat = true; 
			break;
		case 'd': 
			newState.boat = false;
			newState.dynamites++;
			break;
		case 'a':
			newState.boat = false;
			newState.axe = true;
			break;
		case 'g':
			newState.boat = false;
			newState.gold = true;
			break;
		default:
			newState = null;
		}
		return newState;
	}

	private List<Position> findUnexploredBorder() {
		WorldMap map = agent.map;
		List<Position> list = new ArrayList<Position>();
		for(int i = 0; i < agent.map.rows(); i++)
			for(int j = 0 ; j < agent.map.columns(); j++)
				try {
					if(!map.isUnknown(i, j) && ((map.isUnknown(i-1, j) || map.isUnknown(i+1, j) ||
							map.isUnknown(i, j-1) || map.isUnknown(i, j+1))))
						list.add(new Position(i, j));
				} catch(Exception e) {continue;}

		return list;
	}

	private List<Character> pathToActions(List<Position> list) {
		List<Character> l = new ArrayList<Character>();
		String actions = "C";//useless action for avoiding exceptions
		Position current = agent.getPosition();
		Orientation currentOri = agent.getOrientation();
		System.out.println(agent.getOrientation());
		boolean axe = agent.hasAxe();
		boolean boat = agent.onBoat();
		int dynamites = agent.numberDynamites();
		char tile;
		for(Position pos : list) {
			tile = agent.map.getCharAt(pos);
			
			if(tile == 'T' && axe) {
				actions += "C";
				boat = false;
			}
			else if(tile == 'T' && dynamites != 0) {
				actions += "B";
				dynamites--;
				boat = false;
			}
			if(tile == '*') {
				actions += "B";
				dynamites--;
				boat = false;
			}
			if(tile == '~' && !boat) System.exit(-1);
			
			if(tile == 'd') dynamites++;
			if(tile == 'a') axe = true;
			if(tile == 'B') boat = true;
			
			if(getSidePosition(current, Orientation.NORTH, currentOri).equals(pos)) 
				actions += "F";
			if(getSidePosition(current, Orientation.SOUTH, currentOri).equals(pos)) { 
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
		System.out.println(agent.getOrientation());
		System.out.println(currentOri);
		//System.exit(-1);
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
		if(action == 'F' && (frontTile == 'T' || frontTile == '*' || frontTile == '.' || (!agent.onBoat() && frontTile == '~')))
			return true;
		return false;
	}
}
