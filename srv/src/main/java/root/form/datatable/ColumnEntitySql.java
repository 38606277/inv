package root.form.datatable;

public class ColumnEntitySql {
	private String dbColName;
	private String operateIfNull;
	private String value;
	public String getDbColName() {
		return dbColName;
	}
	public void setDbColName(String dbColName) {
		this.dbColName = dbColName;
	}
	public String getInsertIfNull() {
		return operateIfNull;
	}
	public void setOperateIfNull(String operateIfNull) {
		this.operateIfNull = operateIfNull;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
