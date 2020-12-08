package root.report.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
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
import root.configure.WebApplicationContext;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.query.SelectControl;
import root.report.util.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/reportServer/template")
public class TemplateControl extends RO {
	public static final String setupPath = "/file/";// "/WEB-INF/classes/report/";
	public static final String filePath = "/WEB-INF/classes/iReport/file/template/";
	
	// 递归使用
	private void showAllFiles(File dir, JSONArray aNode) {
	    String name = dir.getName();
		File[] fs = dir.listFiles();
		if(null!=fs) {
			for (int i = 0; i < fs.length; i++) {

				if (fs[i].isHidden()) {
					continue;
				}
				JSONObject tNode = new JSONObject(true);
				tNode.put("name", fs[i].getName());
				if (!name.equals("template")) {
					tNode.put("value", name + "/" + fs[i].getName());
				} else {
					tNode.put("value", fs[i].getName());
				}
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

	/**
	 * request需要传递 userCode：用户名 返回值filePath：文件路径树json格式
	 */

	@RequestMapping(value = "/getDirectory", produces = "text/plain; charset=utf-8")
	public String getDirectory() {

		String serverPath = AppConstants.getTemplatePath();

		File file = new File(serverPath);
		JSONArray rootNode = new JSONArray();

		showAllFiles(file, rootNode);

		return rootNode.toJSONString();
	}
	// 递归使用
    private void showTemplateAuthList(File dir, JSONArray aNode,List<Map> templateAuthList) {
        String name = dir.getName();
        File[] fs = dir.listFiles();
        for (int i = 0; i < fs.length; i++) {
            for (Map template : templateAuthList) {
                if(template.get("name").equals(fs[i].getName())){
                    if (fs[i].isHidden()) {
                        continue;
                    }
                    JSONObject tNode = new JSONObject(true);
                    tNode.put("name", fs[i].getName());
                    if(!name.equals("template")){
                        tNode.put("value", name+"/"+fs[i].getName());
                    }else{
                        tNode.put("value", fs[i].getName());
                    }
                    tNode.put("path", fs[i].getPath());
                    aNode.add(tNode);

                    if (fs[i].isDirectory()) {

                        JSONArray nNode = new JSONArray();

                        tNode.put("children", nNode);
                        showTemplateAuthList(fs[i], nNode,templateAuthList);
                    }
                }
            }

        }

    }
	@RequestMapping(value = "/getTemplateAuthList/{userName}", produces = "text/plain; charset=utf-8")
    public String getTemplateAuthList(@PathVariable("userName") String userName) {

        try{
        	String serverPath = AppConstants.getTemplatePath();
            Map<String, String> map = new HashMap<String, String>();
            map.put("userName", userName);
            JSONArray rootNode = new JSONArray();
            File file = new File(serverPath);
            int isAdmin =  DbFactory.Open(DbFactory.FORM).selectOne("user.isAdmin",userName);
            if(isAdmin==1){
                showAllFiles(file, rootNode);
            }else{
                List<Map> templateAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getTemplateAuthList",map);
                for (Map template : templateAuthList) {
                    String name = (String) template.get("name");
                    if(name.lastIndexOf("/")>0){
                        name = name.substring(name.lastIndexOf("/")+1, name.length());
                        template.put("name", name);
                    }
                }
                showTemplateAuthList(file, rootNode,templateAuthList);
            }
            return SuccessMsg("查询成功", rootNode);
        }catch(Exception ex) {
        	ex.printStackTrace();
        	return ErrorMsg("3000", ex.getMessage());
        }
		

        
    }
	
	@RequestMapping(value = "/getTemplateByUserName/{userName}", produces = "text/plain; charset=utf-8")
    public String getDirectoryByUserName(@PathVariable("userName") String userName) {
	    Map<String,String> map = new HashMap<String,String>();
        map.put("userName",userName);
        List<Map> templateAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getTemplateAuthList",map);
        return JSON.toJSONString(templateAuthList);
    }
	@RequestMapping(value = "/getListByTemplateName/{userName}/{templateName}", produces = "text/plain;charset=UTF-8")
    public String getListByTemplateName(@PathVariable("userName") String userName,@PathVariable("templateName") String templateName) {
        String result = "";
        Map<String,String> cmap = new HashMap<String,String>();
        cmap.put("userName",userName);
        cmap.put("templateName",templateName);
        List<Map> selectAuthList = DbFactory.Open(DbFactory.FORM).selectList("rule.getSelectAuthListByClass",cmap);
        for (Map selectAuth : selectAuthList) {
            String[] str = selectAuth.get("name").toString().split("/");
            selectAuth.put("name", str[str.length-1]);
        }
        return JSON.toJSONString(selectAuthList);
	}
	
	/**
	 * @remark templateName 模板的全路径如供应商查询/查询供应商按
	 * @remark paramClass   参数类别 IN,OUT,ALL为全部
 	 * @return [{}]
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@RequestMapping(value = "/getTemplateParam", produces = "text/plain;charset=UTF-8")
    public String getTemplateParam(@RequestBody String pJson) throws IOException
	{
        // 模板文件
	    JSONObject pObj = JSONObject.parseObject(pJson);
        String templateFile = AppConstants.getTemplatePath() + File.separator + pObj.getString("templateName");
        XSSFWorkbook wk = new XSSFWorkbook(new FileInputStream(new File(templateFile)));
        XSSFSheet sheet = wk.getSheetAt(0);
        JSONArray inArr = new JSONArray();
        JSONArray outArr = new JSONArray();
        int lastrow = sheet.getLastRowNum();
        for (int i = 0; i <= lastrow; i++)
        {
            XSSFRow row = sheet.getRow(i);
            if(row!=null)
            {
                int lastCell = row.getLastCellNum();
                for(int j = 0; j <= lastCell; j++) 
                {
                    XSSFCell cell = row.getCell(j);
                    if(cell!=null)
                    {
                        XSSFComment hssfComment = cell.getCellComment();
                        JSONObject obj = new JSONObject();
                        if(hssfComment!=null)
                        {
                            String comment = hssfComment.getString().getString();
                            JSONObject commonObj = JSON.parseObject(comment);
                            String func = commonObj.getString("func");
                            String field = null;
                            Map<String, String> param = null;
                            if("in".equals(func))
                            {
                                field = commonObj.getString("field");
                                if(field==null)
                                {
                                    JSONArray arr = commonObj.getJSONArray("fields");
                                    field = arr.getString(0);
                                }
                                param = getFieldInfo(field, "in");
                                obj.put("id", param.get("id"));
                                obj.put("namespace", field.split("\\.")[0]);
                                obj.put("sqlid", field.split("\\.")[1]);
                                obj.put("name",field.split("\\.")[2]);
                                obj.put("type", "IN");
                                inArr.add(obj);
                            }
                            else
                            {
                                field = commonObj.getString("field");
                                param = getFieldInfo(field, "out");
                                obj.put("id", param.get("id"));
                                obj.put("namespace", field.split("\\.")[0]);
                                obj.put("sqlid", field.split("\\.")[1]);
                                obj.put("name",field.split("\\.")[2]);
                                obj.put("type","OUT");
                                outArr.add(obj);
                            }
                        }
                    }
                }
            }
        }
        if("IN".equals(pObj.getString("paramClass")))
        {
            return inArr.toJSONString();
        }
        else if("OUT".equals(pObj.getString("paramClass")))
        {
            return outArr.toJSONString();
        }
        return JSON.toJSONString(inArr.addAll(outArr));
	}
	
	//根据field字段获取字段名称,field字段组成:namespace.sqlid.columnname,type取值in/out
    private Map<String, String> getFieldInfo(String field, String type)
    {
        Map<String, String> map = new HashMap<String, String>();
        String[] arr = field.split("\\.");
        JSONObject obj = new JSONObject();
        obj.put("namespace", arr[0]);
        obj.put("sqlid", arr[1]);
		SelectControl selectControl = WebApplicationContext.getBean(SelectControl.class);
        String qrySelectSqlDetail = selectControl.qrySelectSqlDetail(obj.toJSONString());
        JSONObject commentObj = JSON.parseObject(qrySelectSqlDetail).getJSONObject("comment");
        JSONArray param = null;
        if("in".equals(type))
        {
            param = commentObj.getJSONArray("in");
            for (int i = 0; i < param.size(); i++)
            {
                JSONObject in = param.getJSONObject(i);
                if(arr[2].equals(in.getString("name")))
                {
                    map.put("id", in.getString("id"));
                    break;
                }
            }
        }
        else
        {
            param = commentObj.getJSONArray("out");
            for (int i = 0; i < param.size(); i++)
            {
                JSONObject in = param.getJSONObject(i);
                if(arr[2].equals(in.getString("name")))
                {
                    map.put("id", in.getString("id"));
                    break;
                }
            }
        }
        
        map.put("db", commentObj.getString("db"));
        return map;
    }
	/**
     * 合并单元格处理--加入list
     * 
     * @param sheet
     * @return
     */
     private List<CellRangeAddress> getCombineCell(XSSFSheet sheet) 
     {
         List<CellRangeAddress> list = new ArrayList<CellRangeAddress>();
         // 获得一个 sheet 中合并单元格的数量
         int sheetmergerCount = sheet.getNumMergedRegions();
         // 遍历合并单元格
         for (int i = 0; i < sheetmergerCount; i++) {
             // 获得合并单元格加入list中
             CellRangeAddress ca = sheet.getMergedRegion(i);
             list.add(ca);
         }
         return list;
     }
	/**
	 * request需要传递 userCode：用户名 filePath：文件路径
	 */

	@RequestMapping(value="/upload", produces = "text/plain; charset=utf-8")
	public String upload(HttpServletRequest request)
	{
		try
		{
			// 创建一个通用的多部分解析器
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
					request.getSession().getServletContext());
			// 判断 request 是否有文件上传,即多部分请求
			if (multipartResolver.isMultipart(request)) 
			{
				// 转换成多部分request
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
				// 取得request中的所有文件名
				Iterator<String> iter = multiRequest.getFileNames();
				while (iter.hasNext()) 
				{
					// 记录上传过程起始时的时间，用来计算上传时间
			        long t1 = System.nanoTime();
					// 取得上传文件
					MultipartFile file = multiRequest.getFile(iter.next());
					if (file != null)
					{
						// 取得当前上传文件的文件名称
						String myFileName = file.getOriginalFilename();
						// 如果名称不为“”,说明该文件存在，否则说明该文件不存在
						if (myFileName.trim() != "")
						{
							// 定义上传路径
							String filePath = multiRequest.getParameter("filePath");
							String ServerPath = AppConstants.getTemplatePath();
							String path = ServerPath + "/" + filePath;
							// 保存文件
							File localFile = new File(path);
							file.transferTo(localFile);
						}
					}
					// 记录上传该文件后的时间
			        long t2 = System.nanoTime();
			        System.out.println("time:" + String.format("%.4fs", (t2 - t1) * 1e-9));
				}
			}
			return "success";
		}
		catch(Exception e)
		{
			System.out.println("上传失败!");
			return  e.getMessage();
		}
	}

	// @RequestMapping("/downloadExcel/{userCode}/{filePath}")

	// public ResponseEntity<byte[]> downloadExcel(@PathVariable("userCode")
	// String userCode,
	// @PathVariable("filePath") String filePath) throws IOException {
	//
//	@RequestMapping(value="/download", produces = "text/plain; charset=utf-8")

//	public ResponseEntity<byte[]> download(HttpServletRequest req) throws IOException {
//
//		String ServerPath = AppConstants.getTemplatePath();
//
//		String path = ServerPath + "/" + req.getParameter("filePath");
//
//		File file = new File(path);
//		HttpHeaders headers = new HttpHeaders();
//		String fileName = new String(path.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
//		headers.setContentDispositionFormData("attachment", fileName);
//		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
//	}

	@RequestMapping(value = "/download", produces = "text/plain;charset=UTF-8")
	public void downloadTemplate(HttpServletResponse response, HttpServletRequest req) throws IOException {
		//JSONObject json = (JSONObject) JSON.parse(pJson);
		String path = req.getParameter("filePath");
		String fileName = req.getParameter("fileName");
		if (StringUtil.isEmpty(fileName)) {
			fileName = path;
		}
		String ServerPath = AppConstants.getTemplatePath();
//
		path = ServerPath + "/" + req.getParameter("filePath");
		File file = new File(path);
		fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		response.setContentType("application/force-download");// 设置强制下载不打开
		response.addHeader("Content-Disposition",
				"attachment;fileName=" + fileName);// 设置文件名
		response.setCharacterEncoding("UTF-8");

		byte[] buffer = new byte[1024];
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			OutputStream os = response.getOutputStream();
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer, 0, i);
				i = bis.read(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * fileDir 文件夹及其路径
	 * 
	 * filePath 文件默认路径 /WEB-INF/classes/iReport/file/template/
	 * 
	 * @param request
	 * @param pJson
	 * @return 0001:创建成功！ 0002:重名 0003:异常
	 * @throws IOException
	 */
	@RequestMapping(value="/mkDir", produces = "text/plain; charset=utf-8")
	public String mkDir(HttpServletRequest request, @RequestBody String pJson) throws IOException {
		String resultMsg = "0001";// 创建成功！
		try {
			// pJson = new String(pJson.getBytes("iso-8859-1"), "UTF-8");
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
			String fileDir = jsonObject.getString("fileDir");
			// String filePath = request.getSession().getServletContext()
			// .getRealPath(this.filePath);

			String ServerPath = AppConstants.getTemplatePath();

			String filePath = ServerPath + "/" + fileDir;
			// if(!StringUtils.isEmpty(fileDir)){
			// filePath = filePath + fileDir+File.separator;
			// }

			File file = new File(filePath);
			if (file.exists()) { // 判断文件是否存在
				// 文件夹名重复，请重新命名
				resultMsg = "0002";// 重名
			} else {
				file.mkdirs();

			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg = "0003";// 异常
		}
		return resultMsg;

	}

	/**
	 * oldName 原文件夹或者文件名称 几路径 wangjian or wangjian/test or wangjian/test.txt
	 * newName 新名称 filePath 文件默认路径 /WEB-INF/classes/iReport/file/template/
	 * 
	 * @return
	 */
	@RequestMapping(value="/reName", produces = "text/plain; charset=utf-8")
	public String reName(HttpServletRequest request, @RequestBody String pJson) throws IOException {
		String resultMsg = "0001";// 重命名成功！
		try {
			// pJson = new String(pJson.getBytes("iso-8859-1"), "UTF-8");
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
			String oldName = jsonObject.getString("oldName");
			String newName = jsonObject.getString("newName");
			// String filePath = request.getSession().getServletContext()
			// .getRealPath(this.filePath);
			// String newFile = filePath + newName;
			//
			String ServerPath = AppConstants.getTemplatePath();

			String newFile = ServerPath + "/" + newName;

			String oldFile = ServerPath + "/" + oldName;

			File file = new File(oldFile);
			if (file.exists()) { // 判断文件是否存在
				File newfile = new File(newFile);
				if (newfile.exists()) {
					// 文件名重复，请重新命名
					resultMsg = "0002";// 修改后名称已存在
				} else {
					boolean isOk = file.renameTo(newfile);
					if (!isOk) {
						resultMsg = "0005";// 修改失败！
					}
				}

			} else {
				resultMsg = "0003";// 原文件夹或者文件不存在
			}
		} catch (Exception e) {
			resultMsg = "0004";// 异常
			e.printStackTrace();
		}
		return resultMsg;
	}

	/**
	 * 
	 * filePath 文件默认路径 /WEB-INF/classes/iReport/file/template/ delName 文件夹或者文件名称
	 * 
	 * @return
	 */
	@RequestMapping(value="/deleteFile", produces = "text/plain; charset=utf-8")
	public String deleteFile(HttpServletRequest request, @RequestBody String pJson) throws IOException {
		String resultMsg = "0001";// 重命名成功！
		try {
			// pJson = new String(pJson.getBytes("iso-8859-1"), "UTF-8");
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
			String delName = jsonObject.getString("localPath");
			// String filePath = request.getSession().getServletContext()
			// .getRealPath(this.filePath);
			String ServerPath = AppConstants.getTemplatePath();

			String filePath = ServerPath + "/" + delName;

			File file = new File(filePath);
			if (file.exists()) { // 判断文件是否存在
				// 文件夹名重复，请重新命名
				FileUtil.deleteDir(file);
			} else {
				// 文件夹名不存在
				resultMsg = "0002";
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMsg = "0003";
		}
		return resultMsg;
	}

}
