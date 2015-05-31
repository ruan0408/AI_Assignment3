/*
By:

Gervasio Protasio dos Santos Neto - 5050769
Ruan De Menezes Costa - 5050761
*/

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

public class WorldMap {

	private char[][] map;

	private List<Resource> dynamites = new LinkedList<Resource>();
	private Resource gold = null;
	private Resource axe = null;

	public WorldMap(char[][] map) {
		this.map = map;
	}

	public int rows() {return map.length;}
	public int columns() {return map[0].length;}
	public void setCharAt(int row, int column, char c) {map[row][column] = c;}
	public char getCharAt(int row, int column) {return map[row][column];}
	public char getCharAt(Position p){return map[p.getRow()][p.getColumn()];}

	public boolean isUnknown(int row, int column) throws ArrayIndexOutOfBoundsException{
		return getCharAt(row, column) == '?';
	}
	
	public Position getFrontPosition(Position p, Orientation orientation) {
		int r = p.getRow();
		int c = p.getColumn();

		switch(orientation) {
		case EAST:	c++; break;
		case NORTH: r--; break;
		case WEST: 	c--; break;
		case SOUTH: r++; break;
		}
		return new Position(r, c);
	}

	public char getFrontTile(Position pos, Orientation orientation) {
		Position p = getFrontPosition(pos, orientation);
		int r = p.getRow();
		int c = p.getColumn();

		return map[r][c];
	}

	//Updates the map given the view of the agent, it's orientation and position
	public void update(char[][] view, Orientation ori, Position myPos) {
		WorldMap viewMap = new WorldMap(view);
		viewMap.rotate(ori);
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
		updateResources();
	}
	
	public void removeGold(){gold = null;}
	public void removeAxe(){axe = null;}

	public void removeDynamite(Position pos){
		for(int i = 0; i < dynamites.size(); i++)
			if(dynamites.get(i).getPosition().equals(pos))
				dynamites.remove(i);
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

	private void rotate(Orientation ori) {
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
	//Updates the resources (gold position, axe position, dynamites)
	private void updateResources() {
		Position aux = null;
		if(gold != null && getCharAt(getGoldPosition()) != 'g')
			removeGold();
		if(axe != null && getCharAt(getAxePostion()) != 'a')
			removeAxe();
		for(Position p : getDynamitePostions())
			if(getCharAt(p) != 'd') {
				aux = p;
				break;
			}
		if(aux != null) removeDynamite(aux);
	}

	private boolean isResource(char c)	{
		if( c == 'd' || c == 'a' || c == 'g')
			return true;
		return false;
	}

	private void addResource(char tile, int r, int c) {
		switch(tile) {
		case 'd': 
			if(!getDynamitePostions().contains(new Position(r, c))) 
				dynamites.add(new Resource(r,c)); break;
		case 'a': axe = new Resource(r,c); break;
		case 'g': gold = new Resource(r,c); break;
		default: break;
		}
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
}
