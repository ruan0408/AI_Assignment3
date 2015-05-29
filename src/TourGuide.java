import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

public class TourGuide {

	private final int FRONT = 0;
	private final int RIGHT = 1;
	private final int BACK = 2;
	private final int LEFT = 3;
	private Agent agent;
	private List<Character> path;
	private Set<Position> border;
	private Random random;
	private List<Position> visitedTiles;
	private LinkedList<Position> frontier;
	//private MyState finalState;

	public TourGuide(Agent agent) {
		this.agent = agent;
		path = new ArrayList<Character>();
		border = new HashSet<Position>();
		random = new Random();
		frontier = new LinkedList<Position>();
		visitedTiles = new LinkedList<Position>();
	}

	public char next() {
		WorldMap map = agent.map;
		Position gold = map.getGoldPosition();
		Position axe = map.getAxePostion();
		MyState agentState = agent.getState();
		MyState finalState = MyState.finalState();
		MyState goldState = MyState.goldState(gold);
		MyState axeState = MyState.axeState(axe);
		
		agentState.print("");
		System.out.println("Visualized gold: "+gold);
		System.out.println("Seen dynamites: "+map.getDynamitePostions().size());
	
		if(!path.isEmpty()) return path.remove(0);
		if(agentState.gold()) {
			System.out.println("ON MY WAY TO THE VICTORY===================================================================!!!");
			return setPathAndAct(findActions(agentState, finalState, true, false));
		}
		if(gold != null && (path = findActions(agentState, goldState, true, false)) != null) {
			System.out.println("ON MY WAY TO THE GOLDDDDDDDD===================================================================!!!");
			return path.remove(0);
		}
		if(!agentState.axe() && axe != null && (path = findActions(agentState, axeState, false, false)) != null) {
			System.out.println("ON MY WAY TO THE AXEEEEEE===================================================================!!!");
			return path.remove(0);
		}
		else return explore2();
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

	private char explore2() {
		MyState agentState = agent.getState();
		//Position tree = MyState.map.randomTree();
		MyState s;

		//		if(tree != null) {
		//			System.out.println("LETS CUT SOME TREES BITCHES");
		//			s = MyState.treeState(tree);
		//			path = findActions(agentState, s);
		//			if(path != null) return setPathAndAct(path);
		//		}

		System.out.println("CHOOSING BORDER");
		border.clear();
		border.addAll(findUnexploredBorder());
		List<Position> list = new ArrayList<Position>(border);
		do {
			System.out.println("\tCHOOSING BORDER LOOP");
			int i = random.nextInt(list.size());
			s = MyState.borderState(list.get(i));
			path = findActions(agentState, s, false, true);
		} while(path == null || path.isEmpty());

		return setPathAndAct(path);
	}

	private char explore3(){
		MyState agentState = agent.getState();
		MyState goalState = new MyState(null, null, true, null, null, null);
		Position gold = agentState.map.getGoldPosition();
		List<Character> roadToVictory = null;

		// try{
		//    roadToVictory = findActions(agentState, goalState);
		// }catch(Exception e){
		//     roadToVictory = null;
		// }

		if(gold != null && roadToVictory != null) //knows where gold is and can reach it
			return setPathAndAct(roadToVictory);

		Position tree = agentState.map.randomTree();
		MyState s;

		if(tree != null && agent.hasAxe()) {
			System.out.println("LETS CUT SOME TREES BITCHES");
			s = MyState.treeState(tree);
			path = findActions(agentState, s, false, true);
			if(path != null) return setPathAndAct(path);
		}

		List<Position> unexplored = findUnexploredBorder();

		Iterator it = unexplored.iterator();
		while(it.hasNext()){
			Position b = (Position) it.next();
			if(!visitedTiles.contains(b)){
				visitedTiles.add(b);
				frontier.push(b);
			}
		}

		Position p = findNextBorder(agent.getPosition());

		// do{
		//     p = frontier.pop();
		// }while(!isWorthExplorin(p));

		System.out.println("Going to " + p.toString());
		return setPathAndAct(getActionsToPosition(p));
	}

	private boolean isBorderTile(char c){
		return ((c == 'T' && !agent.hasAxe()) || c == '*'); 
	}

	/*private List<Character> exploringPath;
    private void explore3(List<Character> actions){
	Position currentPosition = agent.getPosition();
	if(visitedTiles.contains(currentPosition))
	    return ;

	 visitedTiles.add(currentPosition);
	 Position neighborPosition[] = new Position[4];
	 neighborPosition[0] = agent.getState().map.getFrontPosition(currentPosition,NORTH);
	 neighborPosition[1] = agent.getState().map.getFrontPosition(currentPosition,SOUTH);
	 neighborPosition[2] = agent.getState().map.getFrontPosition(currentPosition,EAST);
	 neighborPosition[3] = agent.getState().map.getFrontPosition(currentPosition,WEST);



	 for(int i = 0; i < 4; i++){
	     char targetTile = agent.getState().map.getTileByPosition(neighborPosition[i]);
	     if(!visitedTiles.contains(neighborTiles[i]) && !isBorderTile(targetTile){
		 goToPosition(neighborTiles[i]);
		 explore3();
		 goToPosition(currentPosition);
	     }
	 }

	 }*/

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
		while(!current.equals(start)) {
			current = parent.get(current);
			path.add(current);
		}
		Collections.reverse(path);
		path.remove(0);
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

	private Position findNextBorder(Position p){
		int min = Integer.MIN_VALUE;
		Position target = null;
		for(Position b : frontier){
			int distance = p.distance(b);
			if(isWorthExplorin(b) && distance > min){
				target = b;
				min = distance;
			}
		}
		frontier.remove(target);
		return target;
	}

	private List<Position> findUnexploredBorder() {
		WorldMap map = agent.map;
		int r = agent.getPosition().getRow();
		int c = agent.getPosition().getColumn();
		List<Position> list = new ArrayList<Position>();
		for(int i = 0; i /*r + 4*/< agent.map.rows(); i++)
			for(int j = 0 ; j /*<= c + 4*/<agent.map.columns(); j++)
				try {
					if(!map.isUnknown(i, j) && (map.isUnknown(i-1, j) || map.isUnknown(i+1, j) ||
							map.isUnknown(i, j-1) || map.isUnknown(i, j+1)))
						list.add(new Position(i, j));
				} catch(Exception e) {continue;}

		return list;
	}

	private boolean isWorthExplorin(Position p){
		WorldMap map = agent.map;
		int i = p.getRow();
		int j = p.getColumn();
		char tile = map.getCharAt(i,j);
		return (tile != '.') && (!isBorderTile(tile)) &&
				(map.isUnknown(i-1, j) || map.isUnknown(i+1, j) || map.isUnknown(i, j-1) || map.isUnknown(i, j+1));
	}

	private List<Character> pathToActions(List<Position> list) {
		List<Character> l = new ArrayList<Character>();
		String actions = "";//useless action for avoiding exceptions
		Position current = agent.getPosition();
		Orientation currentOri = agent.getOrientation();
		System.out.println(agent.getOrientation());
		boolean axe = agent.hasAxe();
		int dynamites = agent.numberDynamites();
		char tile;
		for(Position pos : list) {
			tile = agent.map.getCharAt(pos);

			if(getSidePosition(current, BACK, currentOri).equals(pos)) { 
				actions += "RR";
				currentOri = currentOri.next(2);
			}
			else if(getSidePosition(current, RIGHT, currentOri).equals(pos)) { 
				actions += "R";
				currentOri = currentOri.next(3);
			}
			else if(getSidePosition(current, LEFT, currentOri).equals(pos)) {
				actions += "L";
				currentOri = currentOri.next(1);
			}

			if(tile == 'T' && axe) actions += "C";

			else if(tile == 'T' && dynamites != 0) {
				actions += "B";
				dynamites--;
			}
			if(tile == '*') {
				actions += "B";
				dynamites--;
			}
			actions += "F";

			if(tile == 'd') dynamites++;
			if(tile == 'a') axe = true;

			current = pos;
		}
		System.out.println(agent.getOrientation());
		System.out.println(currentOri);

		for(char c : actions.toCharArray()) l.add(c);
		return l;
	}

	private Position getSidePosition(Position pos, int side, Orientation currentOrientation) {
		Orientation newOrientation = null;
		switch(side) {
		case FRONT:	newOrientation = currentOrientation.next(0);	break; 
		case LEFT:	newOrientation = currentOrientation.next(1);	break; 
		case RIGHT:	newOrientation = currentOrientation.next(3);	break; 
		case BACK:	newOrientation = currentOrientation.next(2);	break; 
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
	
	
	
	/*
	 * Gets as close as possible to the end without using dynamites
	 */
	private List<Character> findActionsToExplore(MyState start, MyState end) {
		PriorityQueue<MyState> queue = new PriorityQueue<MyState>(10, new StateComparator());
		List<MyState> visualized = new ArrayList<MyState>();
		List<MyState> path = new ArrayList<MyState>();
		queue.add(agent.getState());
		visualized.add(agent.getState());
		MyState father = null;

		while(!queue.isEmpty() && !(father = queue.remove()).equals(end)) {
			for(MyState child : father.validChildrenStates(visualized)) {
				if(child.dynamites() < father.dynamites()) {
					visualized.add(child);
					continue;
				}
				child.setGValue(father.getGValue()+1);
				child.setFValue(child.getGValue()+child.distance(end));
				visualized.add(child);
				queue.add(child);
			}
		}

		while(father != null) {
			path.add(father);
			father = father.getFather();
		} 
		Collections.reverse(path);
		List<Character> l = pathToActions2(path);
		l.add(0, 'C');
		return l;
	}
	
	
	
	
	
	
	
	
	/*
	 * Finds a path between start and end. If dyn is true, it's allowed to use dynamites.
	 * If close is true, the path will led as close as possible to end.
	 */
	private List<Character> findActions(MyState start, MyState end, boolean dyn, boolean close) {
		PriorityQueue<MyState> queue = new PriorityQueue<MyState>(10, new StateComparator());
		List<MyState> visualized = new ArrayList<MyState>();
		List<MyState> path = new ArrayList<MyState>();
		queue.add(agent.getState());
		visualized.add(agent.getState());
		MyState father = null;

		while(!queue.isEmpty() && !(father = queue.remove()).equals(end)) {
			//System.out.println("===========FIND ACTIONS MAIN LOOP=================");
			//father.print("");
			for(MyState child : father.validChildrenStates(visualized)) {
				//System.out.println("\t\t===========FIND ACTIONS INTERNAL LOOP=================");
				if(!dyn && father.dynamites() > child.dynamites()) {
					visualized.add(child);
					continue;
				}
				child.setGValue(father.getGValue()+1);
				child.setFValue(child.getGValue()+child.distance(end));
				visualized.add(child);
				queue.add(child);
				//child.print("\t\t");
			}
		}
		if(!close && !father.equals(end)) return null;

		while(father != null) {
			path.add(father);
			father = father.getFather();
		} 

		Collections.reverse(path);
		return pathToActions2(path);
	}
	private List<Character> pathToActions2(List<MyState> states) {
		List<Character> l = new ArrayList<Character>();
		String actions = "";
		char oldTile, newTile;
		
		if(states.size() <= 1) {l.add('C');return l;}
		MyState previous = states.remove(0);
		//System.out.println("===========PATH TO ACTIONS LOOP=================");
		for(MyState s : states) {
			oldTile = previous.map.getCharAt(s.getPosition());
			newTile = s.map.getCharAt(s.getPosition());

			actions += Orientation.difference(previous.orientation(), s.orientation());
			if(oldTile == 'T' && newTile == ' ') actions += "C";
			else if(oldTile == '*' && newTile == ' ') actions += "B";

			if(!s.getPosition().equals(previous.getPosition())) actions += "F";
			previous = s;
		}

		for(char c : actions.toCharArray()) l.add(c);
		return l;
	}
}
