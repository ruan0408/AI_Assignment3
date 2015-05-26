/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2012
 */

import java.io.*;
import java.net.*;

public class Agent {

	private boolean hasAxe;
	private boolean hasGold;
	private boolean isOnBoat;
	private int numberOfDynamites;

	private Position myPos;
	private Orientation ori;
	protected WorldMap map;
	private TourGuide guide;

	public Agent() {
		map = new WorldMap(new char[160][160]);
		map.fill('?');
		myPos = new Position(79, 79);
		ori = Orientation.SOUTH;
		guide = new TourGuide(this);
		hasAxe = hasGold = isOnBoat = false;
		numberOfDynamites = 0;
	}
	
	public Position getPosition(){return myPos;}
	public Orientation getOrientation(){return ori;}
	public void setHasGold(boolean b){hasGold = b;}
	public boolean hasGold(){return hasGold;}
	public boolean hasAxe(){return hasAxe;}
	public boolean hasDynamite(){return numberOfDynamites != 0;}
	public void addDynamite(){numberOfDynamites++;}
	public int numberDynamites(){return numberOfDynamites;}
	public void setAxe(){hasAxe = true;}
	public boolean isOnBoat(){return isOnBoat;}
	public void setOnBoat(Boolean b){isOnBoat = b;}

	public char get_action( char view[][] ) {
		char action;
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		map.update(view, ori, myPos);
		map.print();
		action = guide.next();

		try {
			updateState(action);
			Thread.sleep(3);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return action;
	}

	private void updateState(char action) throws Exception {
		switch(action) {
		case 'L':
			ori = ori.next(1); break;
		case 'R':
			ori = ori.next(3); break;
		case 'F':
			if(!allowedToMove()) return;
			switch(ori) {
			case EAST: 	myPos.incrementColumn(1); 	break;
			case NORTH: myPos.incrementRow(-1); 	break;
			case WEST: 	myPos.incrementColumn(-1); 	break;
			case SOUTH: myPos.incrementRow(1); 		break;
			}
		case 'C':
			if(map.getFrontTile(myPos, ori) == 'T') 
				map.setFrontTile(myPos, ori, ' ');
			break;
		case 'B':
			if(map.getFrontTile(myPos, ori) == '*') 
				map.setFrontTile(myPos, ori, ' ');
			break;
		}
	}

	private boolean allowedToMove() {
		if(map.getFrontTile(myPos, ori) == '*' || 
				(map.getFrontTile(myPos, ori) == 'T'))
			return false;
		return true;
	}
	
	public char getFrontTile() {
		return map.getFrontTile(myPos, ori);
	}

//	private char getMe() {
//		switch(ori) {
//		case NORTH: return '^';
//		case WEST: 	return '<';
//		case SOUTH: return 'v';
//		case EAST:	return '>';
//		default: 	return 'e';
//		}
//	}

	void print_view( char view[][] )
	{
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

	public static void main( String[] args )
	{
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
				agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION
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
