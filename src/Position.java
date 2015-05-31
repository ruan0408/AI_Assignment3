/*
By:

Gervasio Protasio dos Santos Neto - 5050769
Ruan De Menezes Costa - 5050761
*/

public class Position {

	private int row;
	private int column;

	public Position(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public void setRow(int r) {row = r;}
	public void setColumn(int c) {column = c;}
	public int getRow() {return row;}
	public int getColumn() {return column;}
	public void incrementRow(int amount) throws Exception {
		row += amount;
		if(row < 0) throw new Exception();
	}

	public void incrementColumn(int amount) throws Exception {
		column += amount;
		if(column < 0) throw new Exception();
	}

	public int distance(Position b) {
		return Math.abs(getRow()-b.getRow()) + Math.abs(getColumn()-b.getColumn());
	}

	public void print() {
		System.out.println("Position: row: "+row+", column: "+column);
	}

	@Override
	public String toString() {
		return "x: "+row+", y: "+column;
	}

	@Override
	public boolean equals(Object b) {
		Position aux = (Position)b;
		if(row == aux.getRow() && column == aux.getColumn())
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return row*column + row + column;
	}
}
