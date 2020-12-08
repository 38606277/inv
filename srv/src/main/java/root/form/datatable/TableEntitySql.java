package root.form.datatable;

import java.util.List;

public class TableEntitySql {
	private String database;
	private String tableName;
	private List<ColumnEntitySql> Cols;
	private String keyName;
	
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<ColumnEntitySql> getCols() {
		return Cols;
	}
	public void setCols(List<ColumnEntitySql> cols) {
		Cols = cols;
	}
	public String getKeyName() {
		return keyName;
	}
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
}