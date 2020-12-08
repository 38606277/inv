package root.report.sys;

public class SysApiLog {
	private int log_id; 
	private String api_code;      //服务URL
	private String api_name;      //服务名称
	private String client;        //客户端IP
	private String begin_time; //调用开始时间
	private String end_time;   //调用结束时间
	private String in_param;      //传入参数
	private String out_param;     //返回结果
	
	public int getLog_id() {
		return log_id;
	}
	public void setLog_id(int log_id) {
		this.log_id = log_id;
	}
	public String getApi_code() {
		return api_code;
	}
	public void setApi_code(String api_code) {
		this.api_code = api_code;
	}
	public String getApi_name() {
		return api_name;
	}
	public void setApi_name(String api_name) {
		this.api_name = api_name;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getBegin_time() {
		return begin_time;
	}
	public void setBegin_time(String begin_time) {
		this.begin_time = begin_time;
	}
	public String getEnd_time() {
		return end_time;
	}
	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}
	public String getIn_param() {
		return in_param;
	}
	public void setIn_param(String in_param) {
		this.in_param = in_param;
	}
	public String getOut_param() {
		return out_param;
	}
	public void setOut_param(String out_param) {
		this.out_param = out_param;
	}
	
	@Override
	public String toString() {
		return "SysApiLog [log_id=" + log_id + ", api_code=" + api_code + ", api_name=" + api_name + ", client="
				+ client + ", begin_time=" + begin_time + ", end_time=" + end_time + ", in_param=" + in_param
				+ ", out_param=" + out_param + "]";
	}
	
}
