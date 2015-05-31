/*
  By:

  Gervasio Protasio dos Santos Neto - 5050769
  Ruan De Menezes Costa - 5050761
*/


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class TourGuide {

    private Agent agent;
    private List<Character> path;
    private Random random;
    private List<Position> visitedTiles;
    private LinkedList<Position> frontier;

    public TourGuide(Agent agent) {
	this.agent = agent;
	path = new ArrayList<Character>();
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
	
    private char explore(){ 
	/*Implements the fllod fil algorithm: It looks to the 4 adjacent positions and
	  if they are not on the stack or have not been visited before, and will
	  uncover previously unknow tiles, then the position is put in the stack.

	  It is possible that it is impossible to reach the position, or that when the
	  position is finally picked as a destination, going there is no longer worth
	  the trouble. If that is the case, we try to pick a new position. However, if
	  the position might still be useful to visit in the future, it will be added to
	  the end of the stack.

	  If there were too many rejected positions (over 550), we assume that the whole
	  map has already been explored (so it's not really worth oging anywhere) or the
	  available positions are not good picks. So instead of trying to continue
	  selecting positions from the stack, we choose a random movement. We also pick
	  a random movement if the stack becomes empty and for any reason, the gold
	  hasnt been accquired.*/

	WorldMap map = agent.map;
	MyState agentState = agent.getState();
	Position current = agent.getPosition();

	if(!visitedTiles.contains(current))
	    visitedTiles.add(current);

	for(int i = 0; i < 4; i++){
	    Position b = map.getFrontPosition(current,OriFromInt(i));
	    if(!visitedTiles.contains(b) && isWorthExplorin(b) && !frontier.contains(b)){
		frontier.push(b);
	    }
	}

	Position p = frontier.poll();
	int numIt = 0;
	try{
	    while( !canGetTo(p) || !isWorthExplorin(p) || visitedTiles.contains(p) ){
		if(isWorthExplorin(p) && !isSurroundedByRocks(p) || !visitedTiles.contains(p))
		    frontier.add(p);
		p = frontier.pop();
		numIt++;
		if(numIt > 550)
		    return exploreRandom();
	    }
	} catch(Exception e){
	    return exploreRandom();
	}

	MyState newState = MyState.borderState(p);
	path = findActions(agentState, newState, false, true);
	return setPathAndAct(path);

    }

    
    private char exploreRandom() {
	/*Picks a radom movement. If that movement would kill the agent, another one
	  is selected until a safe move is found.*/
	String a = "RLF";
	char action = 0;
	do {
	    action = a.charAt(random.nextInt(a.length()));
	} while(isAgentGoingToDie(action));

	return action;
    }

    
    private static Orientation OriFromInt(int i){
	/*FInds the orientation associated witha given integer ( 0<= i <= 4)*/
	return	Orientation.values()[i];
    }

    
    private char setPathAndAct(List<Character> actions) {
	/*Sets the path variable. This means there is a course of action to be executed.
	  If the path was empty, the explore function would be called to pick a new one.*/
	path = actions;
		
	return path.remove(0);
    }

    private boolean willUncoverUnknow(Position p){ 
	/*If the agent was standin ing Position p, it would be able to see things in a 5x5
	window centered around itself. If seeing anything in this window would make
	previously unknown tiles know, this means this Position will unconver something.

	This method iterate over this 5x5 window searching for tiles that will become
	known if the agent goes there.  It returns false if it does not find any.*/

	int r = p.getRow();
	int c = p.getColumn();
	for(int i = r - 2; r <= r + 2; i++)
	    for(int j = c - 2; j <= c + 2; j++)
		if(agent.map.isUnknown(i,j))
		    return true;
	return false;
    }

    private boolean isSurroundedByRocks(Position p){
	/*Returns true if all positions adjacent to P are rocks. False otherwise*/

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
	/*Returns true if the tile is Position p is blank, or is a tree and the agent has
	 an axe or is water and the agent can get to a boat.

	If the place is outside the map, surround by rocks or if to get to it the agent
	needs resources it does not have, it returns false.*/
	
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
	/*Returns true if going to p will make previously unkwon tiles known and the tile
	 is not ouside the map.*/
	WorldMap map = agent.map;
	int i = p.getRow();
	int j = p.getColumn();
	char tile = map.getCharAt(i,j);
	return ((tile != '.') && willUncoverUnknow(p));
    }

    private boolean isAgentGoingToDie(char action) {
	/*Returs true if the movement is legal and wont trigger a Game Over.*/
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
	return pathToActions(path);
    }
	
    private List<Character> pathToActions(List<MyState> states) {
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
