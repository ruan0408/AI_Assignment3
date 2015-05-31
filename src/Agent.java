/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2012
 */

/*
By:

Gervasio Protasio dos Santos Neto - 5050769
Ruan De Menezes Costa - 5050761
*/

/*HOW IT WORKS:

The main flow of the program is: If the agent is currently on it's way to
somewhere, it executes the next action.  If not, and the agent has the gold,
then go to the initial state.  If it doesn't but there is a path to the gold, it
goes to get it. If the agent doesn't have an axe, but it can reach one (without
using dynamites), it also goes get it.  If none of that happens, keep exploring.

The exploring is done using the flood fill algorithm (for each adjacent
position, if it hasn't been explored and doing so will uncover something new,
put it in a stack. Then get the top of the stack).

The program works based on tates (MyState class). At each step, the agent has a
state. A state is made of a position on the map, number of dynamites, current
orientation, and two booleans to know if the agent is on a boat and if it has an
axe.  Two states are equal if all of this information is the same or if some of
them are the same and the other are "don't cares" (which is represented by a
null).

To see if it can reach another state, the A* algorithm is used it expands the
state tree with the valid states of it's 4 neighbors on the map. We can see if a
state is valid by looking at the current state.  States that have already been
visualized are kept on a HashSet and will not be generated again. The A* returns
a list of positions to the goal.  There is another method that turns this list
of positions into a list of actions (list of chars).
*/

import java.io.*;
import java.net.*;

public class Agent {

	private boolean hasAxe;
	private boolean hasGold;
	private boolean onBoat;
	private int numberOfDynamites;
	private boolean hasBeenOnBoat;
	private Position initialPosition;
	private Position myPos;
	private Orientation ori;
	protected WorldMap map;
	private TourGuide guide;

	public Agent() {
		map = new WorldMap(new char[160][160]);
		map.fill('?');
		myPos = new Position(79, 79);
		initialPosition = new Position(79, 79);
		ori = Orientation.SOUTH;
		guide = new TourGuide(this);
		hasAxe = hasGold = onBoat = hasBeenOnBoat =  false;
		numberOfDynamites = 0;
	}

	public Position getPosition(){return new Position(myPos.getRow(), myPos.getColumn());}
	public Position getInitialPosition(){return initialPosition;}
	public Orientation getOrientation(){return ori;}
	public void setGold(){hasGold = true;}
	public boolean hasGold(){return hasGold;}
	public void setAxe(){hasAxe = true;}
	public boolean hasAxe(){return hasAxe;}
	public void addDynamite(){numberOfDynamites++;}
	public void useDynamite(){numberOfDynamites = numberOfDynamites > 0 ? --numberOfDynamites : 0;}
	public boolean hasDynamite(){return numberOfDynamites != 0;}
	public int numberDynamites(){return numberOfDynamites;}
	public boolean onBoat(){return onBoat;}
	public void setOnBoat(Boolean b){
		if(b) hasBeenOnBoat = true;
		onBoat = b;
	}

	public boolean hasBeenOnBoat(){return hasBeenOnBoat;}

	//Returns the current state of the agent.
	public MyState getState() {
		return new MyState(hasAxe, onBoat, hasGold, numberOfDynamites, myPos, ori, map);
	}

	public char get_action( char view[][] ) {
		char action;
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		map.update(view, ori, getPosition());
	 
		action = guide.next();
		if(!updateState(action)) return 'C';
		return action;
	}
	//Updates the current state given the action chosen by the TourGuide.
	private boolean updateState(char action) {
		char frontTile = map.getFrontTile(myPos, ori);
		switch(action) {
		case 'C':
			break;
		case 'B':
			useDynamite();
			break;
		case 'L':
			ori = ori.next(1);
			break;
		case 'R':
			ori = ori.next(3); 
			break;
		case 'F':
			if(!allowedToMove()) return false;
			switch(frontTile) {
			case 'T': 
				setOnBoat(false);
				if(!hasAxe()) useDynamite();
				goForward();
				break;
			case '*': 
				setOnBoat(false);
				useDynamite();
				goForward();
				break;
			case 'B': 
				setOnBoat(true);
				goForward();
				break;
			case '~':
				goForward();
				break;
			case 'a': 
				setOnBoat(false);
				setAxe();
				goForward();
				break;
			case 'd': 
				setOnBoat(false);
				addDynamite();
				goForward();
				break;
			case 'g': 
				setOnBoat(false);
				setGold();
				goForward();
				break;
			case ' ': 
				setOnBoat(false);
				goForward();
				break;
			default:
			break;
			}
			break;
		default: 
		}
		return true;
	}
	//Updates the position of the agent
	private void goForward() {
		try {
			switch(ori) {
			case EAST: 	myPos.incrementColumn(1); 	break;
			case NORTH: myPos.incrementRow(-1); 	break;
			case WEST: 	myPos.incrementColumn(-1); 	break;
			case SOUTH: myPos.incrementRow(1); 		break;
			}
		} catch(Exception e) {
		    return ;
		}
	}
	//Returns false if the tour guide made a mistake (shouldn't happen).
	private boolean allowedToMove() {
		char frontTile = map.getFrontTile(myPos, ori);
		if(frontTile == ' ' || frontTile == 'd' || frontTile == 'a' || 
				frontTile == 'B' || frontTile == 'g' || (frontTile == '~' && onBoat()) ||
				(frontTile == 'T' && (hasAxe() || hasDynamite())) ||
				(frontTile == '*' && hasDynamite())) {
			return true;
		}
		return false;
	}
	//Gets the tile in front of the agent
	public char getFrontTile() {
		return map.getFrontTile(myPos, ori);
	}

	void print_view( char view[][] ) {
		int i,j;

		System.out.println("\n+-----+");
		for( i=0; i < 5; i++ ) {
			System.out.print("|");
			for( j=0; j < 5; j++ ) {
				if(( i == 2 )&&( j == 2 )) {
					System.out.print('^');
				}
				else {
					System.out.print( view[i][j] );
				}
			}
			System.out.println("|");
		}
		System.out.println("+-----+");
	}

	public static void main( String[] args ) {
		InputStream in  = null;
		OutputStream out= null;
		Socket socket   = null;
		Agent  agent    = new Agent();
		char   view[][] = new char[5][5];
		char   action   = 'F';
		int port;
		int ch;
		int i,j;

		if( args.length < 2 ) {
			System.out.println("Usage: java Agent -p <port>\n");
			System.exit(-1);
		}

		port = Integer.parseInt( args[1] );

		try { // open socket to Game Engine
			socket = new Socket( "localhost", port );
			in  = socket.getInputStream();
			out = socket.getOutputStream();
		}
		catch( IOException e ) {
			System.out.println("Could not bind to port: "+port);
			System.exit(-1);
		}

		try { // scan 5-by-5 wintow around current location
			while( true ) {
				for( i=0; i < 5; i++ ) {
					for( j=0; j < 5; j++ ) {
						if( !(( i == 2 )&&( j == 2 ))) {
							ch = in.read();
							if( ch == -1 ) {
								System.exit(-1);
							}
							view[i][j] = (char) ch;
						}
					}
				}
				action = agent.get_action( view );
				out.write( action );
			}
		}
		catch( IOException e ) {
			System.out.println("Lost connection to port: "+ port );
			System.exit(-1);
		}
		finally {
			try {
				socket.close();
			}
			catch( IOException e ) {}
		}
	}
}
