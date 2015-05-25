import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class TourGuide {

	private Agent agent;
	private List<Character> path;
	
	private final String actions = "FLR";
	private Iterator<Integer> ints;
	
	public TourGuide(Agent agent) {
		this.agent = agent;
		path = new ArrayList<Character>();
		Random r = new Random();
		ints = r.ints(0, actions.length()).iterator();
	}
	
	public char next() {
		char action;
		Position gold = agent.map.getGoldPosition();
		Position agentPosition = agent.getPosition();
		Map map = agent.map;
		
		if(!path.isEmpty()) {
			action = path.remove(0);
		} else if(gold != null && !gold.equals(agentPosition)) {
			path = pathToActions(map.findPath(agentPosition, gold));
			for(Character a : path) System.out.println(a);
			action = path.remove(0);
		} else if(gold != null && gold.equals(agentPosition)){
			agent.setHasGold(true);
			map.removeGold();
			path = pathToActions(map.findPath(agentPosition, new Position(79,79)));
			action = path.remove(0);
		}else {
			do {
				action = actions.charAt(ints.next());
			}
			while(isAgentGoingToDie(action));
		}
		return action;
	}
	
	private List<Character> pathToActions(List<Position> list) {
		List<Character> l = new ArrayList<Character>();
		String actions = "";
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
		if(action == 'F' && (frontTile == '.' || frontTile == '~'))
			return true;
		return false;
	}
}
