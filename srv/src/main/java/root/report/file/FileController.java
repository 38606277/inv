package root.report.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import root.configure.AppConstants;
import root.report.file.Excel2MysqlForTax.AssetTaxUploadParam;
import root.report.util.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/file")
public class FileController {
	public static final String setupPath = "/file/";// "/WEB-INF/classes/report/";
	public static final String filePath = "/WEB-INF/classes/iReport/file/";//上传文件目录

	// exe文件下载
	@RequestMapping(value="/downloadSetup",produces = "text/plain;charset=UTF-8")
	public ResponseEntity<byte[]> downloadSetup(HttpServletRequest request,
			HttpServletResponse response) {
		String filePath = request.getSession().getServletContext()
				.getRealPath(this.setupPath);
		String filename = "F:/ewqe.exe";// "deploy.exe";//"Firefox_49.0.2.6136_setup.exe";
//		filename = filePath + filename;
		File file = new File(filename);
		try {
			// 获得请求文件名
			// String filename = request.getParameter("filename");
			System.out.println(filename);
			OutputStream out = null;

			response.setContentType("application/octet-stream; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ file.getName());
			out = response.getOutputStream();
			out.write(FileUtils.readFileToByteArray(file));
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// try {
		// HttpHeaders headers = new HttpHeaders();
		// fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");//
		// 为了解决中文名称乱码问题
		// headers.setContentDispositionFormData("attachment", fileName);
		// headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		// return new ResponseEntity<byte[]>(
		// FileUtils.readFileToByteArray(file), headers,
		// HttpStatus.CREATED);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return null;
	}

	/**
	 * userCode 用户ID
	 * fileDir 文件目录 eg:web or report or template
	 * oldName 文件名称 or 文件原名称
	 * newName 修改后文件名称
	 * @param request
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value="/addDir",produces = "text/plain;charset=UTF-8")
	public String addDir(HttpServletRequest request, @RequestBody String pJson) {
		try {
			pJson = new String(pJson.getBytes("iso-8859-1"), "UTF-8");
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);

			String userCode = jsonObject.getString("userCode");
			String fileDir = jsonObject.getString("fileDir");
			String oldName = jsonObject.getString("oldName");

			String filePath = request.getSession().getServletContext()
					.getRealPath(this.filePath);
			filePath = filePath + fileDir + File.separator + userCode
					+ File.separator + oldName;

			File file = new File(filePath);
			if (file.exists()) { // 判断文件是否存在
				// 文件夹名重复，请重新命名
			} else {
				file.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * userCode 用户ID
	 * fileDir 文件目录 eg:web or report or template
	 * oldName 文件名称 or 文件原名称
	 * newName 修改后文件名称
	 * @param request
	 * @param pJson
	 * @return
	 */
	@RequestMapping(value="/dleDir",produces = "text/plain;charset=UTF-8")
	public String delDir(HttpServletRequest request, @RequestBody String pJson) {
		try {
			pJson = new String(pJson.getBytes("iso-8859-1"), "UTF-8");
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);

			String userCode = jsonObject.getString("userCode");
			String fileDir = jsonObject.getString("fileDir");
			String oldName = jsonObject.getString("oldName");

			String filePath = request.getSession().getServletContext()
					.getRealPath(this.filePath);
			filePath = filePath + fileDir + File.separator + userCode
					+ File.separator + oldName;

			File file = new File(filePath);
			if (file.exists()) { // 判断文件是否存在
				// 文件夹名重复，请重新命名
				FileUtil.deleteDir(file);
			} else {
				// 文件夹名不存在
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	// 文件改名及文件夹改名
	/**
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/fileRename",produces = "text/plain;charset=UTF-8")
	public String fileRename(HttpServletRequest request,
			@RequestBody String pJson) {
		// JSONArray jsonArray = (JSONArray) JSON.parse(pJson);
		// JSONObject jsonObject = jsonArray.getJSONObject(0);
		try {
			pJson = new String(pJson.getBytes("iso-8859-1"), "UTF-8");
			JSONObject jsonObject = (JSONObject) JSON.parse(pJson);

			String userCode = jsonObject.getString("userCode");
			String fileDir = jsonObject.getString("fileDir");
			String oldName = jsonObject.getString("oldName");
			String newName = jsonObject.getString("newName");
			String filePath = request.getSession().getServletContext()
					.getRealPath(this.filePath);
			filePath = filePath + fileDir + File.separator + userCode
					+ File.separator + oldName;
			String newFile = request.getSession().getServletContext()
					.getRealPath(this.filePath)
					+ fileDir
					+ File.separator
					+ userCode
					+ File.separator
					+ newName;
			// try {
			// filePath = new String(filePath.getBytes("UTF-8"), "iso-8859-1");
			// newFile = new String(newFile.getBytes("UTF-8"), "iso-8859-1");
			// } catch (UnsupportedEncodingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			File file = new File(filePath);
			if (file.exists()) { // 判断文件是否存在
				if (file.isFile()) { // 判断是否是文件


					File newfile = new File(newFile);
					if (newfile.exists()) {
						// 文件名重复，请重新命名
					} else {
						boolean isOk = file.renameTo(newfile);
					}
					// 是删除的意思;
				} else if (file.isDirectory()) { // 否则如果它是一个目录

					File newfile = new File(newFile);
					if (newfile.exists()) {
						// 文件名重复，请重新命名
					} else {
						boolean isOk = file.renameTo(newfile);
					}

				}

			} else {
				System.out.println(" 文件不存在！" + '\n');
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@RequestMapping(value="/upload",produces = "text/plain;charset=UTF-8")
	public String addUser(@RequestParam("file") CommonsMultipartFile[] files,
			HttpServletRequest request) {

		for (int i = 0; i < files.length; i++) {
			System.out.println("fileName---------->"
					+ files[i].getOriginalFilename());

			if (!files[i].isEmpty()) {
				int pre = (int) System.currentTimeMillis();
				try {
					// 拿到输出流，同时重命名上传的文件
					FileOutputStream os = new FileOutputStream("H:/"
							+ new Date().getTime()
							+ files[i].getOriginalFilename());
					// 拿到上传文件的输入流
					FileInputStream in = (FileInputStream) files[i]
							.getInputStream();

					// 以写字节的方式写文件
					int b = 0;
					while ((b = in.read()) != -1) {
						os.write(b);
					}
					os.flush();
					os.close();
					in.close();
					int finaltime = (int) System.currentTimeMillis();
					System.out.println(finaltime - pre);

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("上传出错");
				}
			}
		}
		return "/success";
	}

	@RequestMapping(value="/upload2",produces = "text/plain;charset=UTF-8")
	public String upload2(HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException,
			IOException {
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
						String path = "/Users/wangjian/Documents/iReport/file/"
								+ fileName;
						File localFile = new File(path);
						file.transferTo(localFile);
					}
				}
				// 记录上传该文件后的时间
				int finaltime = (int) System.currentTimeMillis();
				System.out.println(finaltime - pre);
			}

		}
		return "success";
	}

	@RequestMapping(value="/toUpload",produces = "text/plain;charset=UTF-8")
	public String toUpload() {

		return "/upload";
	}

	@RequestMapping(value="/download",produces = "text/plain;charset=UTF-8")
	public ResponseEntity<byte[]> download() throws IOException {
		String path =AppConstants.getClientInstallFile()+ "/iBas2Setup.msi";
		File file = new File(path);
		HttpHeaders headers = new HttpHeaders();
		String fileName = new String("你好.ppt".getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
				headers, HttpStatus.CREATED);
	}

	// 得到目录树
	@CrossOrigin
	@RequestMapping(value="/GetDirectory",produces = "text/plain;charset=UTF-8")
	public String GetDirectory(String dirPath) throws IOException {
		// readfile("/Users/wangjian/Documents/test");
		File file = new File("/Users/wangjian/Documents/iReport/file/wangjian");
		rootNode = new JSONArray();

		showAllFiles(file, rootNode);

		return rootNode.toJSONString();
	}

	private JSONArray rootNode;
	private JSONObject currNode;

	public void showAllFiles(File dir, JSONArray aNode) {
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

	// 得到目录的XML文件
	@RequestMapping(value = "/GetDirectoryXML", produces = "text/plain;charset=UTF-8")
	public @ResponseBody String GetDirectoryXML(String dirPath) {

		File file = new File("/Users/wangjian/Documents/iReport/web/help");
		Document document = DocumentHelper.createDocument();
		Element rootNode = document.addElement("xml");
		GetFilesXML(file, rootNode);
		// document.

		return document.asXML();
	}

	//
	public void GetFilesXML(File dir, Element aNode) {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (!(fs[i].isHidden())) {
				Element iNode = aNode.addElement(fs[i].getName());

				if (fs[i].isDirectory()) {

					GetFilesXML(fs[i], iNode);
				}
			}

		}

	}

	private Map dirMap = new HashMap<String, Object>();

	// private Json JsonObject=Json.createObjectBuilder();

	//@Test
	public String GetFile(String filepath) throws FileNotFoundException,
			IOException {
		try {

			File file = new File(filepath);
			if (!file.isDirectory()) {

				dirMap.put(file.getName(), "0");

			} else if (file.isDirectory()) {

				Map aMap = new HashMap<String, Object>();
				dirMap.put(file.getName(), aMap);

				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					if (!readfile.isDirectory()) {

						aMap.put(file.getName(), "0");

					} else if (readfile.isDirectory()) {
						GetFile(filepath + "\\" + filelist[i]);
					}
				}

			}

		} catch (FileNotFoundException e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
		return dirMap.toString();
	}

	public boolean readfile(String filepath) throws FileNotFoundException,
			IOException {
		try {

			File file = new File(filepath);
			if (!file.isDirectory()) {
				// System.out.println("文件");
				// System.out.println("path=" + file.getPath());
				System.out.println("absolutepath=" + file.getAbsolutePath());
				// System.out.println("name=" + file.getName());

			} else if (file.isDirectory()) {
				System.out.println("文件夹");
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filepath + "\\" + filelist[i]);
					if (!readfile.isDirectory()) {
						// System.out.println("path=" + readfile.getPath());
						System.out.println("absolutepath="
								+ readfile.getAbsolutePath());
						// System.out.println("name=" + readfile.getName());

					} else if (readfile.isDirectory()) {
						readfile(filepath + "\\" + filelist[i]);
					}
				}

			}

		} catch (FileNotFoundException e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
		return true;
	}

	/**
	 * request需要传递 userCode：用户名 返回值filePath：文件路径树json格式
	 */

	@RequestMapping(value = "/GetExcelDirectory/{userCode}", produces = "text/plain; charset=utf-8")
	public @ResponseBody String GetExcelDirectory(
			@PathVariable("userCode") String userCode) {

		String serverPath = AppConstants.getReportPath();

		File file = new File(serverPath + "/" + userCode);
		JSONArray rootNode = new JSONArray();

		showAllFiles(file, rootNode);

		return rootNode.toJSONString();
	}

	/**
	 * request需要传递 userCode：用户名 filePath：文件路径
	 */

	@RequestMapping(value="/uploadExcel",produces = "text/plain;charset=UTF-8")
	public @ResponseBody String uploadExcel(HttpServletRequest request)
			throws IllegalStateException, IOException {

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
						String userCode = multiRequest.getParameter("userCode");
						String filePath = multiRequest.getParameter("filePath");
						String ServerPath = AppConstants.getReportPath();

						String path = ServerPath + "/" + userCode + "/"
								+ filePath;
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
		return "success";
	}
	@RequestMapping(value="/uploadExcelFile/{excelType}/{cities}/{credentials}",method={RequestMethod.POST, RequestMethod.OPTIONS},  produces = {"text/plain", "application/*"})
    public @ResponseBody String uploadExcelFile(HttpServletRequest request,@PathVariable("excelType") String excelType,@PathVariable("cities") String cities,@PathVariable("credentials") String credentials)
            throws Exception {
	    Excel2Mysql excel2Mysql = new Excel2Mysql();
	    JSONObject json = (JSONObject)JSONObject.parse(credentials);
        String userCode = (String)json.get("UserCode");
	    String path = "";
	    Map<String,String> map = new HashMap<String,String>();
        // 创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        try{
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
                            String ServerPath = AppConstants.getExcelFilePath();

                            path = ServerPath + "/"+fileName;
                            // 保存文件
                            File localFile = new File(path);
                            FileUtils.copyInputStreamToFile(file.getInputStream(), localFile);
                            excel2Mysql.loadAndSave(path, excelType,cities,userCode);
                        }
                    }
                }
            }
            map.put("state", "success");
        }catch (Exception e) {
            e.printStackTrace();
            map.put("state", "failture");
            map.put("msg", "Excel 导入数据库失败:"+e.getMessage());
        }finally{
        	File localFile = new File(path);
        	if(localFile!=null && localFile.exists()) localFile.delete();
        }
        return JSON.toJSONString(map);
    }
	@RequestMapping(value="/uploadFile/{excelType}/{cities}/{asset}/{period}/{credentials}",method={RequestMethod.POST, RequestMethod.OPTIONS},  produces = {"text/plain", "application/*"})
    public @ResponseBody String uploadFile(HttpServletRequest request,@PathVariable("excelType") String excelType,@PathVariable("cities") String cities,@PathVariable("asset") String asset,@PathVariable("period") String period,@PathVariable("credentials") String credentials)
            throws Exception {
	    Excel2MysqlForTax excel2MysqlForTax = new Excel2MysqlForTax();
	    JSONObject json = (JSONObject)JSONObject.parse(credentials);
	    String userCode = (String)json.get("UserCode");
        String path = "";
        File localFile = null;
        Map<String,String> map = new HashMap<String,String>();
        // 创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        try{
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
                            String ServerPath = AppConstants.getExcelFilePath();

                            path = ServerPath + "/"+fileName;
                         // 保存文件
                            localFile = new File(path);
                            FileUtils.copyInputStreamToFile(file.getInputStream(), localFile);
                            AssetTaxUploadParam param = new AssetTaxUploadParam(excelType, cities, userCode, period, asset);
                            excel2MysqlForTax.loadAndSave(path, param);
                        }
                    }
                }
            }
            map.put("state", "success");
        }catch (Exception e) {
            e.printStackTrace();
            map.put("state", "failture");
            map.put("msg", "Excel 导入数据库失败:"+e.getMessage());
        }finally{
            localFile = new File(path);
            if(localFile!=null && localFile.exists()) localFile.delete();
        }
        return JSON.toJSONString(map);
    }

	// @RequestMapping("/downloadExcel/{userCode}/{filePath}")

	// public ResponseEntity<byte[]> downloadExcel(@PathVariable("userCode")
	// String userCode,
	// @PathVariable("filePath") String filePath) throws IOException {
	//
	@RequestMapping(value="/downloadExcel",produces = "text/plain;charset=UTF-8")
	public ResponseEntity<byte[]> downloadExcel(HttpServletRequest req)
			throws IOException {

		String ServerPath = AppConstants.getStaticReportPath();

		String path = ServerPath + "/" + req.getParameter("userCode") + "/"
				+ req.getParameter("filePath");

		File file = new File(path);
		HttpHeaders headers = new HttpHeaders();
		String fileName = new String(path.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
				headers, HttpStatus.CREATED);
	}

	/**
	 * 稿源周报excel表格下载
	 * @return
	 */

	@RequestMapping(value = "/downExcelInstall", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String downExcelInstall(HttpServletResponse response) throws UnsupportedEncodingException {


//		String path =AppConstants.getClientInstallFile()+ "/iBas2Setup.msi";
//		File file = new File(path);
//		HttpHeaders headers = new HttpHeaders();
//		String fileName = new String("你好.ppt".getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
//		headers.setContentDispositionFormData("attachment", fileName);
//		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
//				headers, HttpStatus.CREATED);

		String filename= AppConstants.getClientInstallFile()+ "/iBas2Setup.msi";

		// 如果文件名不为空，则进行下载
		if (filename != null) {
			File file = new File(filename);
			// 如果文件存在，则进行下载
			if (file.exists()) {
				// 配置文件下载
				response.setHeader("content-type", "application/octet-stream");
				response.setContentType("application/octet-stream");
				response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
				// 下载文件能正常显示中文
				response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("iBas2Setup.msi", "UTF-8"));
				// 实现文件下载
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
					System.out.println("Download  successfully!");
					return "successfully";

				} catch (Exception e) {
					System.out.println("Download  failed!");
					return "failed";

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
		}
		return "";

	}


	@RequestMapping(value="/mkDir/{userCode}/{filePath}",produces = "text/plain;charset=UTF-8")
	public ResponseEntity<byte[]> mkDir(
			@PathVariable("userCode") String userCode,
			@PathVariable("filePath") String filePath) throws IOException {
		String path = userCode + filePath;
		File file = new File(path);
		file.mkdir();
		HttpHeaders headers = new HttpHeaders();
		String fileName = new String(path.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
				headers, HttpStatus.CREATED);
	}

}
