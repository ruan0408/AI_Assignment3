
public class Position {

	private int row;
	private int column;
	
	public Position(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public void setRow(int r) {
		row = r;
	}
	
	public void setColumn(int c) {
		column = c;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public void incrementRow(int amount) throws Exception {
		row += amount;
		if(row < 0) throw new Exception();
	}
	
	public void incrementColumn(int amount) throws Exception {
		column += amount;
		if(column < 0) throw new Exception();
	}
}
