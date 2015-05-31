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

       
	if(!visitedTiles.contains(agent.getPosition()))
	    visitedTiles.add(agent.getPosition());

	if(path != null && !path.isEmpty()) return path.remove(0);
	if(agentState.gold()) {
	    return setPathAndAct(findActions(agentState, finalState, true, false));
	}
	if(gold != null && (path = findActions(agentState, goldState, true, false)) != null) {
	    return path.remove(0);
	}
	if(!agentState.axe() && axe != null && (path = findActions(agentState, axeState, false, false)) != null) {
	    return path.remove(0);
	}
	else return explore();
    }

    private char explore_random() {
	String a = "RLF";
	char action = 0;
	do {
	    action = a.charAt(random.nextInt(a.length()));
	} while(isAgentGoingToDie(action));

	return action;
    }

    private static Orientation OriFromInt(int i){
	return	Orientation.values()[i];
    }

    private char explore(){ //flood fill
	WorldMap map = agent.map;
	MyState agentState = agent.getState();
	Position current = agent.getPosition();

	if(!visitedTiles.contains(current))
	    visitedTiles.add(current);

	for(int i = 0; i < 4; i++){
	    Position b = map.getFrontPosition(current,OriFromInt(i));
	    char tile = map.getCharAt(b.getRow(), b.getColumn());
	    if(!visitedTiles.contains(b) && isWorthExplorin(b) && !frontier.contains(b)){
		frontier.push(b);
	    }
	}
	
	Position p = frontier.poll();
	int numIterations = 0;
	try{
	    while( !canGetTo(p) || !isWorthExplorin(p) || visitedTiles.contains(p) ){
		if(isWorthExplorin(p) && !isSurroundedByRocks(p) || !visitedTiles.contains(p))
		    frontier.add(p);
		p = frontier.pop();
		numIterations++;
		if(numIterations > 550)
		    return explore_random();
	    }
	} catch(Exception e){
	    return explore_random();
	}



	MyState newState = MyState.borderState(p);
	path = findActions(agentState, newState, false, true);
	return setPathAndAct(path);

    }

    private boolean isBorderTile(char c){
	return ((c == 'T' && !agent.hasAxe()) || c == '*' || c == '.'); 
    }

    private char setPathAndAct(List<Character> actions) {
	path = actions;
	return path.remove(0);
    }
    
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
  
    private Position findNextBorder(Position p){
	int min = Integer.MAX_VALUE;
	Position target = null;
	for(Position b : frontier){
	    int distance = p.distance(b);
	    if(isWorthExplorin(b) && distance < min){
		target = b;
		min = distance;
	    }
	}
	frontier.remove(target);
	return target;
    }

    private boolean willUncoverUnknow(Position p){
	int r = p.getRow();
	int c = p.getColumn();
	for(int i = r - 2; r <= r + 2; i++)
	    for(int j = c - 2; j <= c + 2; j++)
		if(agent.map.isUnknown(i,j))
		    return true;
	return false;
    }

    private boolean isSurroundedByRocks(Position p){
	int r = p.getRow();
	int c = p.getColumn();
	char tile = agent.map.getCharAt(r,c);
	if(tile == '*') return true;
	for(int i = 0; i < 4; i++){
	    tile = agent.map.getCharAt(p.getRow(), p.getColumn());
	    if(tile != '*' && tile != '?')
		return false;
	}
	return true;
    }

    private boolean canGetTo(Position p){
	int i = p.getRow();
	int j = p.getColumn();
	char tile = agent.map.getCharAt(i,j);
	if((tile == 'T' && !agent.hasAxe()) || (tile == '~' && !agent.hasBeenOnBoat()))
	    return false;

	if(isSurroundedByRocks(p))
	    return false;

	return true;
    }

    private boolean isWorthExplorin(Position p){
	WorldMap map = agent.map;
	int i = p.getRow();
	int j = p.getColumn();
	char tile = map.getCharAt(i,j);
	return ((tile != '.')  && willUncoverUnknow(p));
    }

    private List<Character> pathToActions(List<Position> list) {
	List<Character> l = new ArrayList<Character>();
	String actions = "";//useless action for avoiding exceptions
	Position current = agent.getPosition();
	Orientation currentOri = agent.getOrientation();
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
     * Finds a path between start and end. If dyn is true, it's allowed to use dynamites.
     * If close is true, the path will led as close as possible to end.
     */
    private List<Character> findActions(MyState start, MyState end, boolean dyn, boolean close) {
	PriorityQueue<MyState> queue = new PriorityQueue<MyState>(10, new StateComparator());
	Set<MyState> visualized = new HashSet<MyState>();
	List<MyState> path = new ArrayList<MyState>();
	queue.add(agent.getState());
	visualized.add(agent.getState());
	MyState father = null;

	while(!queue.isEmpty() && !(father = queue.remove()).equals(end)) {
	    for(MyState child : father.validChildrenStates(visualized)) {
		if(!dyn && father.dynamites() > child.dynamites()) {
		    visualized.add(child);
		    continue;
		}
		child.setGValue(father.getGValue()+1);
		child.setFValue(child.getGValue()+child.distance(end));
		visualized.add(child);
		queue.add(child);
		
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
	for(MyState s : states) {
	    oldTile = previous.getCharAt(s.getPosition());
	    newTile = s.getChar();

	    actions += Orientation.difference(previous.orientation(), s.orientation());
	    if(previous.dynamites() > s.dynamites()) actions += "B";
	    else if(oldTile == 'T' && newTile == ' ') actions += "C";
	
	    if(!s.getPosition().equals(previous.getPosition())) actions += "F";
	    previous = s;
	}

	for(char c : actions.toCharArray()) l.add(c);
	return l;
    }
}
