package root.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import root.configure.AppConstants;
import root.form.constant.TaskStateType;
import root.report.common.BaseControl;
import root.report.db.DbFactory;
import root.report.sys.SysContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
@RestController
@RequestMapping("/reportServer/formReport")
public class FormReportControl extends BaseControl {

	/**
	 * 上传报表
	 * @return
	 */
	@RequestMapping(value = "/uploadReport")
	public String uploadReport(@RequestPart(value="file", required=true) MultipartFile  file) {
		return this.doExecuteWithROReturn(()->{
			//保存文件
			if(file == null) throw new RuntimeException("请上传模板文件");
			String destPath = AppConstants.getTemplatePath()+"/"+SysContext.getRequestUser().getUserName();
			File destDir = new File(destPath);
			if(!destDir.exists()) destDir.mkdirs();
			File destFile = new File(destDir+"/"+file.getOriginalFilename());
			file.transferTo(destFile);
			//保存文件信息到数据据库
			Map<String, String> map = new HashMap<>();
			map.put("reportName", file.getOriginalFilename());
			map.put("reportState", "0");
			map.put("createBy", SysContext.getRequestUser().getUserName());
			map.put("filePath", destFile.getAbsolutePath());
			DbFactory.Open(DbFactory.FORM).insert("frmReport.insert", map);
			return "";
		});
	}
	
	/**
	 * 根据登陆人获取报表
	 * @return
	 */
	@RequestMapping(value = "/listReport", produces = "text/plain;charset=UTF-8")
	public String listReport() {
		return this.doExecuteWithROReturn(()->{
			String currentUser =  SysContext.getRequestUser().getUserName();
			List<?> result =  DbFactory.Open(DbFactory.FORM).selectList("frmReport.listReportByCreater", currentUser);
			return result;
		});
	}
	
	@RequestMapping(value = "/downloadReport", produces = "text/plain;charset=UTF-8")
	public ResponseEntity<byte[]> downloadReport(HttpServletResponse response,HttpServletRequest req) throws IOException{
		//JSONObject json = (JSONObject) JSON.parse(pJson);
		String path = req.getParameter("filePath");
		String fileName = req.getParameter("fileName");
		if(StringUtils.isEmpty(fileName)){
			fileName = path;
		}
		File file = new File(path);
		HttpHeaders headers = new HttpHeaders();
		fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
				headers, HttpStatus.CREATED);
	}
	
	/**
	 * 根据reportId删除报表
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/rmReport", produces = "text/plain;charset=UTF-8")
	public String rmReport(@RequestBody String pJson) {
		return this.doExecuteWithROReturn(()->{
			JSONObject json = (JSONObject) JSON.parse(pJson);
			DbFactory.Open(DbFactory.FORM).delete("frmReport.rmReport", json.get("reportId"));
			return "";
		});
	}
	
	/**
	 * 发布任务
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/assignTask", produces = "text/plain;charset=UTF-8")
	public String assignTask(@RequestBody String pJson) {
		return this.doExecuteWithROReturn(()->{
			JSONObject json = (JSONObject) JSON.parse(pJson);
			final int reportId = json.getIntValue("reportId");
			final String userName = SysContext.getRequestUser().getUserName();
			final Date createDate = new Date();
			JSONArray array = json.getJSONArray("data");
			SqlSession session = DbFactory.Open(false,DbFactory.FORM);
			//TODO删除任务之前需要先判断当前任务的状态，如果用户已经填报过数据，那么是否需要提示一下
			//先删掉该报表之前关联的任务-用于更新操作
			//删除任务主表信息frm_report_task
			session.delete("rmReportTask", reportId);
			//删除任务子表信息frm_report_task_cell
			session.delete("rmReportTaskCell", reportId);
			
			Map<String, Object> taskMap = new HashMap<>();
			taskMap.put("reportId", reportId);
			taskMap.put("createBy", userName);
			taskMap.put("createDate", createDate);
			//填报任务状态:0已经创建1已经下发2已经接收3已经提交
			taskMap.put("state", TaskStateType.ASSIGNED);
			array.forEach((c)->{
				JSONObject child = (JSONObject)c;
				String assignee = child.getString("userId");
				taskMap.put("userId", assignee);
				//保存主表信息frm_report_task
				session.insert("frmReport.insertReoportTask", taskMap);
				JSONArray cells = child.getJSONArray("data");
				cells.forEach(d->{
					//保存frm_report_task_cell
					JSONObject cell = (JSONObject)d;
					cell.put("reportId", reportId);
					cell.put("userId", assignee);
					session.insert("frmReport.insertReoportTaskCell", cell);
				});
			});
			DbFactory.commit(DbFactory.FORM);
			return "";
		});
	}
	
	/**
	 * 获取发布的任务,同时也可以查看到数据</br>
	 * 当前人如果是模板制作人，可以看到报表上的所有数据，如果是报表填报人，只能看到他自己填报的数据
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/viewTask", produces = "text/plain;charset=UTF-8")
	public String viewTask(@RequestBody String pJson) {
		return this.doExecuteWithROReturn(()->{
			JSONObject json = (JSONObject) JSON.parse(pJson);
			int reportId = json.getIntValue("reportId");
			SqlSession session = DbFactory.Open(DbFactory.FORM);
			//根据reportId获取frm_report_task
			Map<String,Object> taskParam = new HashMap<>();
			taskParam.put("reportId", reportId);
			
			Map<String, Object> report = DbFactory.Open(DbFactory.FORM).selectOne("frmReport.listReportByReportId", reportId);
			String createBy = (String)report.get("create_by");
			String userId = SysContext.getRequestUser().getUserName();
			if(!userId.equalsIgnoreCase(createBy)){
				//如果当前人只是任务处理人，那么他只能看到自己填报的数据
				taskParam.put("userId", userId);
			}
			
			List<Map<String, Object>> taskList = session.selectList("frmReport.listReportTask", taskParam);
			//获取frm_report_task_cell
			List<Map<String, Object>> taskListCell = session.selectList("frmReport.listReportTaskCell", taskParam);
			//循环task及taskcell，把taskcell放入对应的task
			taskList.forEach(task->{
				List<Map<String, Object>> toAddCellList = new ArrayList<>();
				task.put("taskCells", toAddCellList);
				String uid = task.get("user_id").toString();
				taskListCell.forEach(cell->{
					if(cell.get("user_id").toString().equalsIgnoreCase(uid)){
						toAddCellList.add(cell);
					}
				});
			});
			return taskList;
		});
	}
	
	
	/**
	 * 我的任务-待办0下发1已办3
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/myTask", produces = "text/plain;charset=UTF-8")
	public String myTask(@RequestBody String pJson) {
		return this.doExecuteWithROReturn(()->{
			JSONObject json = (JSONObject) JSON.parse(pJson);
			String userId = SysContext.getRequestUser().getUserName();
			String status = json.getString("status");
			SqlSession session = DbFactory.Open(DbFactory.FORM);
			Map<String,Object> taskParam = new HashMap<>();
			taskParam.put("userId", userId);
			taskParam.put("status", status);
			List<?> list = session.selectList("frmReport.listMyTask", taskParam);
			return list;
		});
	}
	
	/**
	 * 数据填报
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value = "/updateReportData", produces = "text/plain;charset=UTF-8")
	public String updateReportData(@RequestBody JSONObject pJson) {
		return this.doExecuteWithROReturn(()->{
			SqlSession session = DbFactory.Open(DbFactory.FORM);
			JSONArray array = pJson.getJSONArray("task_cells");
			array.forEach(json->{
				session.update("frmReport.updateReportTaskCell", json);
			});
			//更新填报任务状态
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("user_id", pJson.getString("user_id"));
			param.put("report_id", pJson.getString("report_id"));
			param.put("state", TaskStateType.HAS_FINISHED);
			session.update("frmReport.updateReportTask", param);
			return "";
		});
	}
	
}
