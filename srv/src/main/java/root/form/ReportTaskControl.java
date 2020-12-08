package root.form;

import root.report.common.RO;

public class ReportTaskControl extends RO {
	
	//新建报表任务
	//json为任务列表，包括用户名称，
	public String CreateReportTask(String Json) {
		//将user列表insert进frm_report_task中
		return SuccessMsg("创建成功","");
	}
	public String GetReportTask(String Json) {
		//将user列表insert进frm_report_task中
		return SuccessMsg("创建成功","");
	}
	//为报表任务分配单元格
	public String DistributeTaskCell(String Json) {
		return SuccessMsg("创建成功","");
	}
	//
	public String GetTaskCell(String Json) {
		return SuccessMsg("创建成功","");
	}
	//修改reportTask
	public String UpdateReportTask(String Json) {
		return SuccessMsg("创建成功","");
	}
	//
	
	
	//提交reportTask,将frm_report_task状态置成1
	public String DistributeTask(String Json)
	{
		return SuccessMsg("创建成功","");
	}
	//提交reportTask，将frm_report_task状态置成2
	public String CommitReportTask(String Json)
	{
		return SuccessMsg("创建成功","");
	}
	
	
	
	
	

}
