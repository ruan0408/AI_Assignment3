import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class Map {

	private char[][] map;
	private List<Resource> dynamites = new LinkedList<Resource>();
	private Resource gold = null;
	private Resource axe = null;
	private List<Resource> boats = new LinkedList<Resource>();

	public Map(char[][] map) {
		this.map = map;
	}

	public int rows() {return map.length;}
	public int columns() {return map[0].length;}
	public void setCharAt(int row, int column, char c) {map[row][column] = c;}
	public char getCharAt(int row, int column) {return map[row][column];}

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

	public List<Position> findPath(Position a, Position b) {
		java.util.Map<Position, Integer> list = new HashMap<Position, Integer>();
		java.util.Map<Position, Integer> explored = new HashMap<Position, Integer>();
		java.util.Map<Position, Position> parent  = new HashMap<Position, Position>(); //child father
		List<Position> path = new ArrayList<Position>();
		Position current;
		list.put(a, 0);
		explored.put(a, 1);
		while(!(current = getBestPosition(list)).equals(b) && !list.isEmpty()) {
			for(Position pos : getValidNeighbours(current)) {
				if(explored.get(pos) == null) {
					explored.put(pos, 1);
					list.put(pos, list.get(current) + pos.distance(b));
					parent.put(pos, current);
				}
			}
			list.remove(current);
		}

		current = b;
		path.add(current);
		do {
			current = parent.get(current);
			path.add(current);
		} while(!current.equals(a));
		Collections.reverse(path);
		return path;
	}

	// If necessary, can be improved using a priority queue
	private Position getBestPosition(java.util.Map<Position, Integer> list) {
		Set<Entry<Position, Integer>> entries = list.entrySet();
		int min = Integer.MAX_VALUE;
		Position best = null;
		for(Entry<Position, Integer> entry : entries)
			if(entry.getValue() < min) { 
				min = entry.getValue();
				best = entry.getKey();
			}
		return best;
	}

	private List<Position> getValidNeighbours(Position pos) {
		List<Position> list = new ArrayList<Position>();
		int r = pos.getRow(); int c = pos.getColumn();
		if(isValid(r-1, c)) list.add(new Position(r-1, c));
		if(isValid(r, c+1)) list.add(new Position(r, c+1));
		if(isValid(r+1, c)) list.add(new Position(r+1, c));
		if(isValid(r, c-1)) list.add(new Position(r, c-1));
		return list;
	}

	private boolean isValid(int r, int c) {
		if(r >= 0 && r < rows() && c >= 0 && c < columns() && 
				getCharAt(r, c) != '.' && getCharAt(r, c) != '*' && getCharAt(r, c) != 'T')
			return true;
		return false;
	}

	private boolean isResource(char c)	{
		if( c == 'd' || c == 'a' || c == 'g' || c == 'B')
			return true;
		return false;
	}

	private void addResource(char tile, int r, int c) {
		switch(tile) {
		case 'd': dynamites.add(new Resource(r,c)); break;
		case 'a': axe = new Resource(r,c); break;
		case 'g': gold = new Resource(r,c); break;
		case 'B': boats.add(new Resource(r,c)); break;
		default: break;
		}
	}

	public void removeGold(){
		gold = null;
	}

	public void removeDynamite(Position pos){
		for(Resource d: dynamites)
			if (d.getPosition().equals(pos))
				dynamites.remove(d);
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

	public List<Position> getBoatPostion(){
		List<Position> boatPositions = new LinkedList<Position>();
		for(Resource b : boats)
			boatPositions.add(b.getPosition());
		return boatPositions;
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
}
