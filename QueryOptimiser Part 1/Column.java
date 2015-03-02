/**
 * CS 267 - Project - Implements Column of a Table.
 */
public class Column {
	private int colId;
	private String colName;
	private ColType colType;
	private int colLength;
	private boolean colNullable;
	private int colCard;
	private String hiKey;
	private String loKey;
	
	public enum ColType {
		CHAR, INT;
	}
	
	public Column(int id, String name, ColType type, int length, boolean nullable) {
		colId = id;
		colName = name;
		colType = type;
		colLength = length;
		colNullable = nullable;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public int getColId() {
		return colId;
	}

	public void setColId(int colId) {
		this.colId = colId;
	}

	public ColType getColType() {
		return colType;
	}

	public void setColType(ColType colType) {
		this.colType = colType;
	}
	
	public int getColLength() {
		return colLength;
	}
	
	public void setColLength(int colLength) {
		this.colLength = colLength;
	}

	public boolean isColNullable() {
		return colNullable;
	}

	public void setColNullable(boolean colNullable) {
		this.colNullable = colNullable;
	}

	public int getColCard() {
		return colCard;
	}

	public void setColCard(int colCard) {
		this.colCard = colCard;
	}

	public String getHiKey() {
		return hiKey;
	}

	public void setHiKey(String hiKey) {
		this.hiKey = hiKey;
	}

	public String getLoKey() {
		return loKey;
	}

	public void setLoKey(String loKey) {
		this.loKey = loKey;
	}
}
