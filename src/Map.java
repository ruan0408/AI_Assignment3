import java.util.Arrays;

public class Map {

	private char[][] map;
	
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
				if(i == 2 && j == 2) setCharAt(r, c, ' ');
				else setCharAt(r-2+i, c-2+j, viewMap.getCharAt(i, j));
			}
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
