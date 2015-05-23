import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;


public class Map {

    private char[][] map;
    private List<Resource> dynamites = new LinkedList<Resource>();
    private Resource gold = null;
    private Resource axe = null;
    private Resource boat = null;
	
    public Map(char[][] map) {
	this.map = map;
    }
	
    public void rotate(Orientation ori) {
	char[][] newMap = new char[rows()][columns()];
	Position aux;
	for(int i = 0; i < rows(); i++) {
	    for(int j = 0; j < columns(); j++) {
		aux = transformPosition(i, j, ori);
		newMap[aux.getRow()][aux.getColumn()] = map[i][j];	
	    }
	}
	map = newMap;
    }
	
    public void update(char[][] view, Orientation ori, Position myPos) {
	Map viewMap = new Map(view);
	viewMap.rotate(ori);
	viewMap.print();
	int r = myPos.getRow();
	int c = myPos.getColumn();
	for(int i = 0; i < viewMap.rows() ; i++)
	    for(int j = 0; j < viewMap.columns(); j++) {
		if(i == 2 && j == 2) 
		    setCharAt(r, c, ' ');
		else{
		    char tile =  viewMap.getCharAt(i, j);
		    setCharAt(r-2+i, c-2+j, tile);
		    if(isResource(tile))
			addResource(tile, r-2+i, c-2+j);
		}
	    }
    }

    private boolean isResource(char c){
	if( c == 'd' || c == 'a' || c == 'g' || c == 'B')
	    return true;
	return false;
    }

    private void addResource(char tile, int r, int c){
	switch(tile){
	case 'd': dynamites.add(new Resource(r,c)); break;
	case 'a': axe = new Resource(r,c); break;
	case 'g': gold = new Resource(r,c); break;
	case 'B': boat = new Resource(r,c); break;
	default: break;
	}
    }
    
    public Position getGoldPosition(){
	if(gold == null)
	    return null;
	return gold.getPosition();
    }

    public Position getAxePostion(){
	if(axe == null)
	    return null;
	return axe.getPosition();
    }

    public Position getBoatPostion(){
	if(boat == null)
	    return null;
	return boat.getPosition();
    }

    public List<Position> getDynamitePostions(){
	List<Position> dynamitesPositions = new LinkedList<Position>();
	for(Resource d : dynamites)
	    dynamitesPositions.add(d.getPosition());
	return dynamitesPositions;
    }

    public void fill(char c) {
	for(char[] row : map) 
	    Arrays.fill(row, c);
    }
	
    public void print() {
	for(int i = 0; i < rows(); i++)
	    System.out.println(Arrays.toString(map[i]).replaceAll(", ", ""));
    }
	
    public int rows() {
	return map.length;
    }
	
    public int columns() {
	return map[0].length;
    }
	
    public void setCharAt(int row, int column, char c) {
	map[row][column] = c;
    }
	
    public char getCharAt(int row, int column) {
	return map[row][column];
    }
	
    private Position transformPosition(int i, int j, Orientation orientation) {
	int row, column;
	row = column = 0;
	switch(orientation) {
	case NORTH: row = i; 			column = j; 			break;
	case WEST: 	row = rows()-1-j;	column = i; 			break;
	case SOUTH:	row = rows()-1-i;	column = columns()-1-j;	break;
	case EAST:	row = j;			column = columns()-1-i;	break;
	}
	return new Position(row, column);
    }
	
    public char getFrontTile(Position p, Orientation orientation) {
	int r = p.getRow();
	int c = p.getColumn();
		
	switch(orientation) {
	case EAST:	c++; break;
	case NORTH: r--; break;
	case WEST: 	c--; break;
	case SOUTH: r++; break;
		
	}
	return map[r][c];
    }
	
    public void setFrontTile(Position p, Orientation orientation, char ch) {
	int r = p.getRow();
	int c = p.getColumn();
		
	switch(orientation) {
	case EAST:	c++; break;
	case NORTH: r--; break;
	case WEST: 	c--; break;
	case SOUTH: r++; break;
		
	}
	map[r][c] = ch;
    }
}
