package root.report.query;

public class FuncMetaData {
	private String id;
	private String[] paramVal;
	private String funcExpression;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String[] getParamVal() {
		return paramVal;
	}
	public void setParamVal(String[] paramVal) {
		this.paramVal = paramVal;
	}
	public String getFuncExpression() {
		return funcExpression;
	}
	public void setFuncExpression(String funcExpression) {
		this.funcExpression = funcExpression;
	}
	
}
