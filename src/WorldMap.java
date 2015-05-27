import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class WorldMap {

	private char[][] map;
	private List<Resource> dynamites = new LinkedList<Resource>();
	private Resource gold = null;
	private Resource axe = null;
	//private List<Resource> boats = new ArrayList<Resource>();
	private List<Resource> trees = new ArrayList<Resource>();
	private Random random = new Random();

	public WorldMap(char[][] map) {
		this.map = map;
	}

	public int rows() {return map.length;}
	public int columns() {return map[0].length;}
	public void setCharAt(int row, int column, char c) {map[row][column] = c;}
	public char getCharAt(int row, int column) {return map[row][column];}
	public char getCharAt(Position p){return map[p.getRow()][p.getColumn()];}
//	public boolean hasBoat() {return !boats.isEmpty();}
//	public Position closestBoat(){
//		if(!boats.isEmpty()) return boats.get(0).getPosition();
//		return null;
//	}
	public Position randomTree() {
		if(trees.isEmpty()) return null;
		int i = random.nextInt(trees.size());
		return trees.get(i).getPosition();
	}
	
	public boolean isUnknown(int row, int column) throws ArrayIndexOutOfBoundsException{
		return getCharAt(row, column) == '?';
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
		WorldMap viewMap = new WorldMap(view);
		viewMap.rotate(ori);
		viewMap.print();
		int r = myPos.getRow();
		int c = myPos.getColumn();
		for(int i = 0; i < viewMap.rows() ; i++)
			for(int j = 0; j < viewMap.columns(); j++) {
				if(i == 2 && j == 2) 
					setCharAt(r, c, getMe(ori));
				else{
					char tile =  viewMap.getCharAt(i, j);
					setCharAt(r-2+i, c-2+j, tile);
					if(isResource(tile))
						addResource(tile, r-2+i, c-2+j);
				}
			}
		updateResources();
	}
	
	private void updateResources() {
		if(gold != null && getCharAt(getGoldPosition()) != 'g')
			removeGold();
		if(axe != null && getCharAt(getAxePostion()) != 'a')
			removeAxe();
		for(Position p : getDynamitePostions())
			if(getCharAt(p) != 'd') removeDynamite(p);
		for(Position p : getTreePostions())
			if(getCharAt(p) != 'T') removeTree(p);
			
	}

	private boolean isResource(char c)	{
		if( c == 'd' || c == 'a' || c == 'g' || c == 'B' || c == 'T')
			return true;
		return false;
	}

	private void addResource(char tile, int r, int c) {
		switch(tile) {
		case 'd': dynamites.add(new Resource(r,c)); break;
		case 'T': trees.add(new Resource(r,c)); break;
		case 'a': axe = new Resource(r,c); break;
		case 'g': gold = new Resource(r,c); break;
		default: break;
		}
	}

	public void removeGold(){gold = null;}
	public void removeAxe(){axe = null;}

	public void removeDynamite(Position pos){
		for(int i = 0; i < dynamites.size(); i++)
			if(dynamites.get(i).getPosition().equals(pos))
				dynamites.remove(i);
	}
	
	public void removeTree(Position pos){
		for(int i = 0; i < trees.size(); i++)
			if(trees.get(i).getPosition().equals(pos))
				trees.remove(i);
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
	
	public List<Position> getTreePostions(){
		List<Position> treePositions = new ArrayList<Position>();
		for(Resource d : trees)
			treePositions.add(d.getPosition());
		return treePositions;
	}

	public void fill(char c) {
		for(char[] row : map) 
			Arrays.fill(row, c);
	}

	public void print() {
		for(int i = 0; i < rows(); i++)
			System.out.println(Arrays.toString(map[i]).replaceAll(", ", ""));
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
	
	private char getMe(Orientation ori) {
		switch(ori) {
		case NORTH: return '^';
		case WEST: 	return '<';
		case SOUTH: return 'v';
		case EAST:	return '>';
		default: 	return 'e';
	}
}
}
