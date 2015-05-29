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
	// map.print();
	// System.out.println("\nAUXMAP:\n");
	// map.printAux();
	action = guide.next();
	updateState(action);
	return action;
    }

    private void updateState(char action) {
	char frontTile = map.getFrontTile(myPos, ori);
	//char currentTile = map.getCharAt(myPos);
	switch(action) {
	case 'C':
	    //if(frontTile == 'T') 
	    //map.setFrontTile(myPos, ori, ' ');
	    break;
	case 'B':
	    //System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO DYNAMITEEEEEEEEEEEEEE");
	    useDynamite();
	    break;
	    //if(frontTile == 'T'|| frontTile == '*') 
	    //map.setFrontTile(myPos, ori, ' ');
	case 'L':
	    ori = ori.next(1);
	    break;
	case 'R':
	    ori = ori.next(3); 
	    break;
	case 'F':
	    if(!allowedToMove()) return;
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
	    default: System.out.println("DEFAULT TILE HAPPENED");
		break;
	    }
	    break;
	default: System.out.println("DEFAULT ACTION HAPPENED");
	}
    }

    private void goForward() {
	try {
	    switch(ori) {
	    case EAST: 	myPos.incrementColumn(1); 	break;
	    case NORTH: myPos.incrementRow(-1); 	break;
	    case WEST: 	myPos.incrementColumn(-1); 	break;
	    case SOUTH: myPos.incrementRow(1); 		break;
	    }
	} catch(Exception e) {
	    e.printStackTrace(System.out);
	}
    }

    private boolean allowedToMove() {
	char frontTile = map.getFrontTile(myPos, ori);
	//System.out.println(onBoat());
	if(frontTile == ' ' || frontTile == 'd' || frontTile == 'a' || 
	   frontTile == 'B' || frontTile == 'g' || (frontTile == '~' && onBoat()) ||
	   (frontTile == 'T' && (hasAxe() || hasDynamite())) ||
	   (frontTile == '*' && hasDynamite())) {
	    return true;
	}
	return false;
    }

    public char getFrontTile() {
	return map.getFrontTile(myPos, ori);
    }

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
