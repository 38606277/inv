package root.form.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import root.configure.AppConstants;
import root.report.db.DbFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhouji
 *
 */
@RestController
@RequestMapping("/reportServer")
public class TaskTableControl {
	/**
	 * *取所有的报表定义，返回以下json格式
	 * @return
	 * {
	 *    id:order_herder,
	 *    name:order_name
	 * }
	 */
	@RequestMapping(value = "/TaskTable/saveFillTask", produces = "text/plain; charset=utf-8")
	public String saveFillTask(@RequestBody String dtInfo) throws Exception {
		//获取新增保存参数
		String retCode = "true";
		String retMsg = "保存填报任务成功";
		JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
		String fillTaskName = (String)jsonObject.get("fillTaskName");
		String fillTemplate = (String)jsonObject.get("fillTemplate");
		String fillTimeFor = (String)jsonObject.get("fillTimeFor");
		String fillTimeTo = (String)jsonObject.get("fillTimeTo");
		fillTimeFor = fillTimeFor + " 00:00:00";
		fillTimeTo = fillTimeTo + " 23:59:59";
		String isExcel = (String)jsonObject.get("isExcel");
		String isWeb = (String)jsonObject.get("isWeb");
		String isPhone = (String)jsonObject.get("isPhone");
		String createBy = (String)jsonObject.get("createBy");
		String priority = (String)jsonObject.get("priority");
		Map<Object, Object> taskColmn = new HashMap<>();
		taskColmn.put("taskName", fillTaskName);
		taskColmn.put("template", fillTemplate);
		taskColmn.put("beginDate", fillTimeFor);
		taskColmn.put("endDate", fillTimeTo);
		taskColmn.put("isExcel", isExcel);
		taskColmn.put("isWeb", isWeb);
		taskColmn.put("isPhone", isPhone);
		taskColmn.put("createBy", createBy);
		taskColmn.put("priority", priority);
//		List<Map> tasklist = DbFactory.Open().selectList("task.getTaskByTaskName",taskColmn);
//		if(tasklist.size()>0){
//			retCode = "false";
//			retMsg = "保存填报任务失败,任务名已存在";
//		}else{
			int num = DbFactory.Open(DbFactory.FORM).insert("task.addTaskTable",taskColmn);
			if(num!=1){
				retCode = "false";
				retMsg = "保存填报任务失败";
			}else{
				String taskid = taskColmn.get("task_id").toString();
				JSONArray jsonArray = (JSONArray) jsonObject.get("personnelAllotment");
				//JSONArray jsonArray  = JSON.parseArray(data);
				Map taskLines = new HashMap<>();
				int size = jsonArray.size();
				for(int index = 0; index < size; index++) {
					JSONObject json = (JSONObject) JSON.parseObject(jsonArray.get(index).toString());
					taskLines.put("taskId", taskid);
					taskLines.put("userId", json.get("userId"));
					taskLines.put("state", json.get("fillState"));
					if(DbFactory.Open(DbFactory.FORM).insert("task.addTaskLines",taskLines)!=1){
						retCode = "false";
						retMsg = "保存填报任务失败";
					}
				}  
			}
//		}
		if("true".equals(retCode)){
		    String serverPath = AppConstants.getFillTemplatePath();
		    String dir = fillTemplate.substring(0, fillTemplate.lastIndexOf("."));
            serverPath += "/"+dir+"/"+fillTaskName;
		    File dirFile = new File(serverPath);
            if(!dirFile.exists()){
                dirFile.mkdirs();
            }
		}
		Map result = new HashMap<>();
		result.put("retCode", retCode);
		result.put("retMsg", retMsg);
		return JSON.toJSONString(result);
	}
	
	@RequestMapping(value = "/TaskTable/getFillTaskByTaskId/{taskId}", produces = "text/plain; charset=utf-8")
	public String getFillTaskByTaskId(@PathVariable("taskId") String taskId) throws Exception {
		List<Map> taskList =DbFactory.Open(DbFactory.FORM).selectList("task.getTaskById",taskId);
		for (Map map : taskList) {
			List<Map> taskLineList  = DbFactory.Open(DbFactory.FORM).selectList("task.getTaskLineByTaskId",taskId);
			map.put("personnelAllotment", taskLineList);
		}
		return JSON.toJSONString(taskList);
	}
	
	@RequestMapping(value = "/TaskTable/editFillTask", produces = "text/plain; charset=utf-8")
	public @ResponseBody String editFillTask(@RequestBody String dtInfo) throws Exception {
		//获取新增保存参数
		String retCode = "true";
		String retMsg = "保存填报任务成功";
		JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
		String fillTaskId = (String)jsonObject.get("fillTaskId");
		String fillTaskName = (String)jsonObject.get("fillTaskName");
		String fillTemplate = (String)jsonObject.get("fillTemplate");
		String fillTimeFor = (String)jsonObject.get("fillTimeFor");
		String fillTimeTo = (String)jsonObject.get("fillTimeTo");
		fillTimeFor = fillTimeFor + " 00:00:00";
		fillTimeTo = fillTimeTo + " 23:59:59";
		String isExcel = (String)jsonObject.get("isExcel");
		String isWeb = (String)jsonObject.get("isWeb");
		String isPhone = (String)jsonObject.get("isPhone");
		String createBy = (String)jsonObject.get("createBy");
		String priority = (String)jsonObject.get("priority");
		Map<Object, Object> taskColmn = new HashMap<>();
		taskColmn.put("taskId", fillTaskId);
		taskColmn.put("taskName", fillTaskName);
		taskColmn.put("template", fillTemplate);
		taskColmn.put("beginDate", fillTimeFor);
		taskColmn.put("endDate", fillTimeTo);
		taskColmn.put("isExcel", isExcel);
		taskColmn.put("isWeb", isWeb);
		taskColmn.put("isPhone", isPhone);
		taskColmn.put("createBy", createBy);
		taskColmn.put("priority", priority);
		int num = DbFactory.Open(DbFactory.FORM).update("task.updateTask",taskColmn);
		if(num!=1){
			retCode = "false";
			retMsg = "保存填报任务失败";
		}else{
			JSONArray jsonArray = (JSONArray) jsonObject.get("personnelAllotment");
			Map taskLines = new HashMap<>();
			int size = jsonArray.size();
			for(int index = 0; index < size; index++) {
				JSONObject json = (JSONObject) JSON.parseObject(jsonArray.get(index).toString());
				taskLines.put("taskLineId", json.get("taskLineId"));
				taskLines.put("userId", json.get("userId"));
				taskLines.put("state", json.get("fillState"));
				if(DbFactory.Open(DbFactory.FORM).update("task.updateTaskLines",taskLines)!=1){
					retCode = "false";
					retMsg = "保存填报任务失败";
				}
			}  
		}
		Map result = new HashMap<>();
		result.put("retCode", retCode);
		result.put("retMsg", retMsg);
		return JSON.toJSONString(result);
	}
	
	@RequestMapping(value = "/TaskTable/getCurrentTask", produces = "text/plain; charset=utf-8")
	public String getCurrentTask() throws Exception {
	    JSONArray jsonArray = new JSONArray();
		List<Map> templateList =DbFactory.Open(DbFactory.FORM).selectList("task.getCurrentTemplateList");
		for (Map templateMap : templateList) {
		    String template = (String)templateMap.get("name");
		    String dir = template.substring(0, template.lastIndexOf("."));
		    List<Map> taskList =DbFactory.Open(DbFactory.FORM).selectList("task.getTaskListByTemplate",template);
		    Map result = new HashMap<>();
		    result.put("name", dir);
		    JSONArray aNode = new JSONArray();
		    for (int i = 0; i < taskList.size(); i++) {
		        String serverPath = AppConstants.getFillTemplatePath();
                JSONArray rootNode = new JSONArray();
                String taskName = taskList.get(i).get("task_name").toString();
                System.out.println(taskName+"    "+template);
                serverPath += "/"+dir+"/"+taskName;
                System.out.println(serverPath);
                File file = new File(serverPath);
                showAllFiles(file, rootNode);
                System.out.println(rootNode.toString());
                JSONObject tNode = new JSONObject(true);
                tNode.put("name", taskName);
                tNode.put("children", rootNode.toString());
                aNode.add(tNode);
            }
//	        for (Map taskMap : taskList) {
//	            String serverPath = SysContext.getFillTemplatePath();
//	            JSONArray rootNode = new JSONArray();
//	            String taskName = taskMap.get("task_name").toString();
//	            System.out.println(taskName+"    "+template);
//	            serverPath += "/"+dir+"/"+taskName;
//	            System.out.println(serverPath);
//	            File file = new File(serverPath);
//	            showAllFiles(file, rootNode);
//	            System.out.println(rootNode.toString());
//	            
//	            jsonArray.add(result.toString());
//	        }
	        result.put("children", aNode.toString());
	        jsonArray.add(result);
        }
		return JSON.toJSONString(jsonArray);
	}
	@RequestMapping(value = "/TaskTable/getCurrentTaskByTemplate", produces = "text/plain; charset=utf-8")
	public String getCurrentTaskByTemplate(@RequestBody String templateInfo) throws Exception {
	    JSONObject jsonObject = (JSONObject) JSON.parseObject(templateInfo);
        String template = (String)jsonObject.get("template");
	    String serverPath = AppConstants.getFillTemplatePath();
	    JSONArray rootNode = new JSONArray();
	    List<Map> taskList =DbFactory.Open(DbFactory.FORM).selectList("task.getTaskListByTemplate",template);
	    for (Map map : taskList) {
	        String taskName = map.get("task_name").toString();
            System.out.println(taskName+"    "+template);
            String dir = template.substring(0, template.lastIndexOf("."));
            serverPath += "/"+dir+"/"+taskName;
            System.out.println(serverPath);
            File file = new File(serverPath);
            showAllFiles(file, rootNode);
        }
	    return JSON.toJSONString(rootNode);
	}
	@RequestMapping(value = "/TaskTable/getCurrentTask/{userCode}", produces = "text/plain; charset=utf-8")
	public String getCurrentTask(@PathVariable("userCode") String userCode) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String now = df.format(new Date()).toString();// new Date()为获取当前系统时间
		Map condition = new HashMap<>();
		condition.put("now", now);
		condition.put("userCode", userCode);
		JSONArray msg = new JSONArray();
		List<Map> temNames =DbFactory.Open(DbFactory.FORM).selectList("task.getCurrentTaskTemplate",condition);
		for (Map map : temNames) {
			JSONObject result = new JSONObject(); 
			String temName = (String)map.get("template");
			result.put("name", temName);
			condition.put("template", temName);
			List<Map> taskNames  = DbFactory.Open(DbFactory.FORM).selectList("task.getTaskNameByCurrentTask",condition);
			result.put("children", taskNames);
			msg.add(result);
		}
		return JSON.toJSONString(msg);
	}
	@RequestMapping(value = "/TaskTable/getHistoricalTask/{userCode}", produces = "text/plain; charset=utf-8")
	public String getHistoricalTask(@PathVariable("userCode") String userCode) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String now = df.format(new Date()).toString();// new Date()为获取当前系统时间
		Map condition = new HashMap<>();
		condition.put("now", now);
		condition.put("userCode", userCode);
		JSONArray msg = new JSONArray();
		List<Map> temNames =DbFactory.Open(DbFactory.FORM).selectList("task.getHistoricalTaskTemplate",condition);
		for (Map map : temNames) {
			JSONObject result = new JSONObject(); 
			String temName = (String)map.get("template");
			result.put("name", temName);
			condition.put("template", temName);
			List<Map> taskNames  = DbFactory.Open(DbFactory.FORM).selectList("task.getTaskNameByHistoricalTask",condition);
			result.put("children", taskNames);
			msg.add(result);
		}
		return JSON.toJSONString(msg);
	}
	
	@RequestMapping(value = "/TaskTable/getTaskList/{userCode}/{state}", produces = "text/plain; charset=utf-8")
	public String getTaskList(@PathVariable("userCode") String userCode,@PathVariable("state") String state) throws Exception {
		String userId = DbFactory.Open(DbFactory.FORM).selectOne("user.getUserByCode",userCode);
		Map condition = new HashMap<>();
		condition.put("state", state);
		condition.put("userId", userId);
		List<Map> taskList =DbFactory.Open(DbFactory.FORM).selectList("task.getTaskList",condition);
		return JSON.toJSONString(taskList);
	}
	
	@RequestMapping(value = "/TaskTable/getFillTaskByTempTask", produces = "text/plain; charset=utf-8")
	public String getFillTaskByTempTask(@RequestBody String dtInfo) throws Exception {
	    JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
	    String tempName = (String)jsonObject.get("TempName");
	    String taskName = (String)jsonObject.get("TaskName");
	    Map condition = new HashMap<>();
	    condition.put("tempName", tempName+".xlsx");
	    condition.put("taskName", taskName);
	    condition.put("template", tempName+".xls");
	    List<Map> taskList = DbFactory.Open(DbFactory.FORM).selectList("task.getTaskLineByTask",condition);
	    Map task = taskList.get(0);
	    String taskId = task.get("task_id").toString();
	    List<Map> taskLineList = DbFactory.Open(DbFactory.FORM).selectList("task.getFillTaskLineByTaskId",taskId);
	    task.put("children", taskLineList.toString());
	    return JSON.toJSONString(task);
	}
	
	@RequestMapping(value = "/TaskTable/receiveReceipt/{taskLinesId}", produces = "text/plain; charset=utf-8")
	public String receiveReceipt(@PathVariable("taskLinesId") String taskLinesId,HttpServletRequest req) throws Exception {
		Map condition = new HashMap<>();
		condition.put("id", taskLinesId);
		String receiveDate = DbFactory.Open(DbFactory.FORM).selectOne("task.getReceiveDateByTaskLinesId",taskLinesId);
		if(receiveDate==null){
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			condition.put("receiveDate", df.format(new Date()));
			int num = DbFactory.Open(DbFactory.FORM).update("task.updateTaskLinesByCondition",condition);
		}
//		String template = DbFactory.Open().selectOne("task.getTemolateByTaskLinesId",taskLinesId);
//		System.out.println("template:"+template);
//		String ServerPath = SysContext.getFillTemplatePath();
//		String path = ServerPath + "/" + template;
		
//		String path =req.getParameter("filePath");
//		System.out.println("path:"+path);
//		File file = new File(path);
//		HttpHeaders headers = new HttpHeaders();
//		String fileName = new String(path.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
//		headers.setContentDispositionFormData("attachment", fileName);
//		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
		DbFactory.close(DbFactory.FORM);
		return null;
	}
	
	@RequestMapping("/TaskTable/download")
	public ResponseEntity<byte[]> download(HttpServletRequest req) throws IOException {
		String path =req.getParameter("filePath");
		System.out.println("path:"+path);
		File file = new File(path);
		HttpHeaders headers = new HttpHeaders();
		String fileName = new String(path.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/TaskTable/submitTask", produces = "text/plain; charset=utf-8")
	public String submitTask(@RequestBody String dtInfo,HttpServletRequest request) throws Exception {
		String retCode = "true";
		String retMsg = "提交任务成功";
		JSONObject jsonObject = (JSONObject) JSON.parseObject(dtInfo);
		String taskLineId = (String) jsonObject.get("taskLineId");
		String fillState = (String) jsonObject.get("fillState");
		String path = (String) jsonObject.get("path");
		
		Map condition = new HashMap<>();
		condition.put("id", taskLineId);
		condition.put("state", fillState);
		condition.put("formPath", path);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		condition.put("commitDate", df.format(new Date()));
		DbFactory.Open(DbFactory.FORM).update("task.updateTaskLinesByCondition",condition);
		List<Map> states = DbFactory.Open(DbFactory.FORM).selectList("task.getStateByTaskLinesId",taskLineId);
		Boolean falg = true;
		for (Map map : states) {
			String state = (String)map.get("state");
			if(state.equals("待办")){
				falg = false;
				break;
			}
		}
		if(falg){
			condition.put("state", "关闭");
			DbFactory.Open(DbFactory.FORM).update("task.updateTaskByCondition",condition);
		}
		Map result = new HashMap<>();
		result.put("retCode", retCode);
		result.put("retMsg", retMsg);
		return JSON.toJSONString(result);
	}
	@RequestMapping("/TaskTable/upload")
	public String upload(HttpServletRequest request) {
		Map result = new HashMap<>();
		try {
			String path = null;
			// 创建一个通用的多部分解析器
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
					request.getSession().getServletContext());
			// 判断 request 是否有文件上传,即多部分请求
			if (multipartResolver.isMultipart(request)) {
				// 转换成多部分request
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				// 取得request中的所有文件名
				Iterator<String> iter = multiRequest.getFileNames();

				while (iter.hasNext()) {
					// 记录上传过程起始时的时间，用来计算上传时间
					int pre = (int) System.currentTimeMillis();
					// 取得上传文件
					MultipartFile file = multiRequest.getFile(iter.next());
					if (file != null) {
						// 取得当前上传文件的文件名称
						String myFileName = file.getOriginalFilename();
						// 如果名称不为“”,说明该文件存在，否则说明该文件不存在
						if (myFileName.trim() != "") {
							System.out.println(myFileName);
							// 重命名上传后的文件名配置路径+用户+用户路径+文件名
							String fileName = file.getOriginalFilename();
							// 定义上传路径
							// String userCode =
							// multiRequest.getParameter("userCode");
							//文件名
							String filePath = multiRequest.getParameter("filePath");
							System.out.println("filePath: "+filePath);
							String ServerPath = AppConstants.getFillTemplatePath();
							//文件地址
							path = ServerPath + filePath;
							System.out.println("path: "+path);
							String dir = path.substring(0, path.lastIndexOf("/"));
							File dirFile = new File(dir);
							if(!dirFile.exists()){
								dirFile.mkdirs();
							}
							// 保存文件
							File localFile = new File(path);
							file.transferTo(localFile);
						}
					}

					// 记录上传该文件后的时间
					int finaltime = (int) System.currentTimeMillis();
					System.out.println(finaltime - pre);
				}

			}
			result.put("retCode", "success");
			result.put("retMsg", path);
			return JSON.toJSONString(result);
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			result.put("retCode", "fail");
			result.put("retMsg", e.getMessage());
			return JSON.toJSONString(result);
		}
		
	}
	private void showAllFiles(File dir, JSONArray aNode) {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {

			if (fs[i].isHidden()) {
				continue;
			}
			JSONObject tNode = new JSONObject(true);
			tNode.put("name", fs[i].getName());
			tNode.put("path", fs[i].getPath());
			aNode.add(tNode);

			if (fs[i].isDirectory()) {

				JSONArray nNode = new JSONArray();

				tNode.put("children", nNode);
				showAllFiles(fs[i], nNode);
			}

		}

	}
}
