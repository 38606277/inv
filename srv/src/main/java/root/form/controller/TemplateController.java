package root.form.controller;


import java.io.*;
import java.net.URLDecoder;
import java.sql.*;
import java.util.*;
import java.util.Date;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.session.SqlSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import root.configure.AppConstants;
import root.form.constant.ColumnType;
import root.report.common.BaseControl;
import root.report.db.DbFactory;
import root.report.excel.XSSFExcelToHtml;
import root.report.excel.XSSFExcelToHtmlReact;
import root.report.excel.XSSFExcelToHtmlReactView;
import root.report.sys.SysContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/reportServer/dataCollect")
public class TemplateController extends BaseControl {

    /**
     * 添加模板
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/createTemplate", method = RequestMethod.POST)
    public String create(@RequestPart(value = "file", required = true) MultipartFile file) {
        return this.doExecuteWithROReturn(() -> {
            //保存文件
            if (file == null) throw new RuntimeException("请上传模板文件");
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            //判断是否有同名模板
            List list = session.selectList("dataCollect.getTemplateByName", file.getOriginalFilename());
            if(list != null && list.size()>0) throw new RuntimeException("该模板与服务器上其它模板同名！");
            String destPath = AppConstants.getTemplatePath() + "/" + SysContext.getRequestUser().getUserName();
            File destDir = new File(destPath);
            if (!destDir.exists()) destDir.mkdirs();
            File destFile = new File(destDir + "/" + file.getOriginalFilename());
            file.transferTo(destFile);
            //保存文件信息到数据据库
            Map<String, Object> map = new HashMap<>();
            map.put("template_name", file.getOriginalFilename());
            map.put("createby", SysContext.getRequestUser().getUserName());
            map.put("create_date", new Date());
            map.put("template_path", destFile.getAbsolutePath());
            session.insert("dataCollect.addTemplate", map);
            return "";
        });
    }

    /**
     * 下载模板
     *
     * @param response
     * @param req
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/downloadTemplate", produces = "text/plain;charset=UTF-8")
    public void downloadTemplate(HttpServletResponse response, HttpServletRequest req) throws IOException {
        //JSONObject json = (JSONObject) JSON.parse(pJson);
        String path = req.getParameter("filePath");
        String fileName = req.getParameter("fileName");
        if (StringUtil.isEmpty(fileName)) {
            fileName = path;
        }
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
     * 查询所有模板，附带模板关联的任务
     *
     * @return
     */
    @RequestMapping(value = "/listTemplate")
    public String listTemplate() {
        return this.doExecuteWithROReturn(() -> {
            String currentUser = SysContext.getRequestUser().getUserName();
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            List<Map<String, Object>> result = session.selectList("dataCollect.getTemplateList", currentUser);
            //循环模板列表，添加任务列表
            for (Map<String, Object> map : result) {
                Object table_id = map.get("table_id");
                List taskList = session.selectList("dataCollect.getTableTaskList", table_id);
                map.put("taskList", taskList);
            }
            return result;
        });
    }

    /**
     * 根据模板ID删除模板，步骤：
     * 先查询模板对应的表ID,再查询表ID对应的任务，
     * 如果没有任务，则删除表定义及字段信息，再删除模板
     *
     * @param templateId
     * @return
     */
    @RequestMapping(value = "/removeTemplate/{templateId}", produces = "text/plain; charset=utf-8")
    public String rmTemplate(@PathVariable("templateId") Integer templateId) {
        return this.doExecuteWithROReturn(() -> {
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            Map<String, Object> params = new HashMap<>();
            params.put("templateId", templateId);
            //查询模板关联表
            Map<String, Object> table = session.selectOne("dataCollect.getFrmTable", params);
            //查询表关联任务
            Object tableId = table.get("table_id");
            List taskList = session.selectList("dataCollect.getTableTaskList", tableId);
            if (!CollectionUtils.isEmpty(taskList)) throw new RuntimeException("该模板关联的有任务，不能删除");
            //删除自定义表
            session.delete("dataCollect.dropNewTable", table.get("table_name"));
            //删除关联表定义和字段定义
            session.delete("dataCollect.deleteFromFrmTableField", tableId);
            session.delete("dataCollect.deleteFromFrmTable", tableId);
            //删除模板
            session.delete("dataCollect.rmTemplate", templateId);
            return "";
        });
    }

    @RequestMapping(value = "/listAllColumnTypes", produces = "text/plain;charset=UTF-8")
    public String listAllColumnTypes() {
        return this.doExecuteWithROReturn(() -> {
            return ColumnType.listAllColumnTypes();
        });
    }

    /**
     * 根据前台传递的表及字段信息建表
     *
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/createTable", produces = "text/plain;charset=UTF-8")
    public String createTable(@RequestBody String pJson) {
        JSONObject json = (JSONObject) JSON.parse(pJson);
        String tableName = json.getString("table_name");
        JSONArray columns = json.getJSONArray("columns");
        try {
            SqlSession session = DbFactory.Open(false, DbFactory.FORM);
            //查找表名是否已经存在
            Map<String, Object> params = new HashMap<>();
            params.put("tableName", tableName);
            List<Map<String, Object>> tableList = session.selectList("dataCollect.getFrmTable", params);
            if(tableList != null && tableList.size()>0) throw new RuntimeException("该表已经存在，请更换表名！");
            Date now = new Date();
            //frm_table添加数据
            String currentUser = SysContext.getRequestUser().getUserName();
            json.put("create_by", currentUser);
            json.put("create_date", now);
            session.insert("dataCollect.addTableRecord", json);
            //frm_table_filed添加数据
            Integer tableId = json.getInteger("_id");
            columns.forEach(j -> ((JSONObject) j).put("table_id", tableId));
            session.insert("dataCollect.addTableFields", columns);
            //创建表
            this.addCommonColumns(columns);
            StringBuffer sb = new StringBuffer();
            sb.append("_id int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, ");
            //拼接建表sql
            columns.forEach(col -> {
                JSONObject jsonCol = (JSONObject) col;
                sb.append(jsonCol.getString("field_name") + " " + ColumnType.getDbType(jsonCol.getString("data_type")));
                String length = jsonCol.getString("data_length");
                if (null != length && !"".equals(length)) sb.append("(" + length + ")");
                sb.append(",");
            });
            String createSql = "create table " + tableName + "(" + sb.deleteCharAt(sb.length() - 1) + ")";
            session.update("dataCollect.createNewTable", createSql);
            session.commit();
            return this.SuccessMsg("操作成功", null);
        } catch (Exception e) {
            DbFactory.rollback(DbFactory.FORM);
            return this.ExceptionMsg(e.getMessage());
        }
    }

    //添加建表共有字段
    private void addCommonColumns(JSONArray columns) {
        JSONObject taskCol = new JSONObject();
        taskCol.put("field_name", "task_id");
        taskCol.put("data_type", ColumnType.NUMBER);
        JSONObject userCol = new JSONObject();
        userCol.put("field_name", "create_by");
        userCol.put("data_type", ColumnType.STRING);
        userCol.put("data_length", "128");
        JSONObject createTimeCol = new JSONObject();
        createTimeCol.put("field_name", "create_time");
        createTimeCol.put("data_type", ColumnType.DATE);
        columns.add(taskCol);
        columns.add(userCol);
        columns.add(createTimeCol);
    }

    /**
     * 查询当前用户创建的表及字段描述
     *
     * @return
     */
    @RequestMapping(value = "/listTable", produces = "text/plain;charset=UTF-8")
    public String listTable(@RequestBody String pJson) {
        JSONObject json = (JSONObject) JSON.parse(pJson);
        return this.doExecuteWithROReturn(() -> {
            String currentUser = SysContext.getRequestUser().getUserName();
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            //查询用户创建的表
            Map<String, Object> params = new HashMap<>();
            params.put("createBy", currentUser);
            if (json != null) params.put("tableId", json.get("tableId"));
            List<Map<String, Object>> tableList = session.selectList("dataCollect.getFrmTable", params);
            //根据表id查询字段及描述
            tableList.forEach(t -> {
                Object tid = t.get("table_id");
                List<Map<String, Object>> fieldList = session.selectList("dataCollect.getTableFieldDesc", tid);
                t.put("fieldList", fieldList);
            });
            return tableList;
        });
    }

    /**
     * 保存或发布任务
     * 保存任务：taskState:0
     * 发布任务：taskState：1
     *
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/saveTask", produces = "text/plain;charset=UTF-8")
    public String saveTask(@RequestBody String pJson) {
        JSONObject json = (JSONObject) JSON.parse(pJson);
        try {
            SqlSession session = DbFactory.Open(false, DbFactory.FORM);
            Integer taskId = json.getInteger("task_id");
            if (taskId != null) {
                List<Map> list = session.selectList("dataCollect.getTaskInfo", taskId);
                Map<String, Object> map = list.get(0);
                //任务如果已经发布，则不允许修改
                if ("1".equals(map.get("task_state"))) throw new RuntimeException("任务已经发布，不能修改");
                json.put("create_date", map.get("create_date"));
                json.put("task_id", map.get("task_id"));
                deleteTaskCascade(session, taskId);
            }
            taskId = saveTaskCascade(json, session);
            session.commit();
            return this.SuccessMsg("操作成功", taskId);
        } catch (Exception e) {
            DbFactory.rollback(DbFactory.FORM);
            return this.ExceptionMsg(e.getMessage());
        }
    }

    /**
     * 删除任务及任务用户信息
     *
     * @param session
     * @param taskId
     */
    private void deleteTaskCascade(SqlSession session, Integer taskId) {
        //删除任务用户信息
        session.delete("dataCollect.deleteTaskUserInfo", taskId);
        //删除任务信息
        session.delete("dataCollect.deleteTaskInfo", taskId);
    }

    /**
     * 保存任务及任务用户
     *
     * @param json
     * @param session
     */
    private Integer saveTaskCascade(JSONObject json, SqlSession session) {
        //添加任务
        Date now = new Date();
        String currentUser = SysContext.getRequestUser().getUserName();
        json.put("create_by", currentUser);
        json.putIfAbsent("create_date", now);
        //如果是发布操作，则填充发布时间
        if ("1".equals(json.get("task_state"))) json.put("deploy_date", now);
        session.insert("dataCollect.insertTaskInfo", json);
        //添加任务用户中间表
        Integer taskId = json.getInteger("task_id");
        JSONArray taskUsers = json.getJSONArray("task_user");
        taskUsers.forEach(u -> {
            JSONObject j = (JSONObject) u;
            j.put("task_id", taskId);
            j.put("create_date", now);

        });
        session.insert("dataCollect.inserTaskUserInfo", taskUsers);
        return taskId;
    }

    /**
     * 删除任务及任务用户信息
     *
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/deleteTask/{task_id}", produces = "text/plain;charset=UTF-8")
    public String deleteTask(@PathVariable("task_id") Integer taskId) {
        try {
            //如果任务已经发布，则不允许删除
            SqlSession session = DbFactory.Open(false, DbFactory.FORM);
            Map<String, Object> taskInfo = session.selectOne("dataCollect.getTaskInfo", taskId);
            if (taskInfo != null) {
                Object taskState = taskInfo.get("task_state");
                if ("0".equals(taskState)) {
                    deleteTaskCascade(session, taskId);
                } else {
                    throw new RuntimeException("该任务已经发布，不能删除");
                }
            }
            session.commit();
            return this.SuccessMsg("操作成功", null);
        } catch (RuntimeException e) {
            DbFactory.rollback(DbFactory.FORM);
            return this.ExceptionMsg(e.getMessage());
        }
    }

    /**
     * 查询任务及任务用户信息
     *
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/taskAndUsers/{task_id}", produces = "text/plain;charset=UTF-8")
    public String taskAndUsers(@PathVariable("task_id") Integer taskId) {
        return this.doExecuteWithROReturn(() -> {
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            Map taskMap = session.selectOne("dataCollect.getTaskInfo", taskId);
            List<Map> taskUserList = session.selectList("dataCollect.getTaskUserInfo", taskId);
            taskMap.put("taskUserList", taskUserList);
            return taskMap;
        });
    }

    /**
     * 根据任务ID和当前用户查询他的填报数据
     *
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/reportData", produces = "text/plain;charset=UTF-8")
    public String getReportData(@RequestBody String pJson) {
        JSONObject json = (JSONObject) JSON.parse(pJson);
        String tableName = json.getString("tableName");
        Integer taskId = json.getInteger("taskId");
        return this.doExecuteWithROReturn(() -> {
            if (root.report.util.StringUtil.isBlank(tableName)) throw new RuntimeException("tableName参数不能为空!");
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            String currentUser = SysContext.getRequestUser().getUserName();
            Map<String, Object> params = new HashMap<>();
            params.put("tableName", tableName);
            params.put("taskId", taskId);
            //判断当前用户是否任务的创建人
            if (taskId != null) {
                Map<String, Object> taskInfo = session.selectOne("dataCollect.getTaskInfo", taskId);
                String createBy = (String) taskInfo.get("create_by");
                if (!createBy.equals(currentUser)) {
                    params.put("createBy", currentUser);
                }
            }
            return session.selectList("dataCollect.reportData", params);
        });
    }

    /**
     * pJson格式
     * {
     * tableName:"",
     * taskId:"",
     * data:[{col1:'',col2:'',col3:''}]
     * }
     *
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/saveData", produces = "text/plain;charset=UTF-8")
    public String saveData(@RequestBody String pJson) throws SQLException {
        JSONObject json = (JSONObject) JSON.parse(pJson);
        //添加任务ID，创建人，创建时间
        String currentUser = SysContext.getRequestUser().getUserName();
        addCommonColumnsToReportData(json, currentUser);
        JSONArray list = json.getJSONArray("data");
        if (CollectionUtils.isEmpty(list)) return "";
        JSONObject row = list.getJSONObject(0);

        List<String> columnNames = new ArrayList<>();
        for (String key : row.keySet()) {
            columnNames.add(key);
        }
        Connection conn = null;
        PreparedStatement psD = null;
        PreparedStatement psI = null;
        try {
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            conn = session.getConnection();
            conn.setAutoCommit(false);
            String tableName = json.getString("tableName");
            //删除当前用户的任务填报数据(如果有)
            String delSql = "delete from " + tableName + " where create_by=? and task_id = ?";
            psD = conn.prepareStatement(delSql);
            psD.setString(1, currentUser);
            psD.setInt(2, json.getInteger("taskId"));
            psD.execute();

            //保存数据
            StringBuffer inserSqlBuffer = new StringBuffer("insert into " + tableName);
            inserSqlBuffer.append("(");
            columnNames.forEach(cn -> inserSqlBuffer.append(cn + ","));
            inserSqlBuffer.deleteCharAt(inserSqlBuffer.length() - 1).append(")");
            inserSqlBuffer.append(" values (");
            columnNames.forEach(k -> inserSqlBuffer.append("?,"));
            inserSqlBuffer.deleteCharAt(inserSqlBuffer.length() - 1).append(")");
            psI = conn.prepareStatement(inserSqlBuffer.toString());
            for (Object r : list) {
                JSONObject j = (JSONObject) r;
                for (int i = 1; i < columnNames.size() + 1; i++) {
                    psI.setObject(i, j.get(columnNames.get(i - 1)));
                }
                psI.addBatch();
            }
            psI.executeBatch();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            conn.rollback();
            return ExceptionMsg(e.getMessage());

        } finally {
            if (psD != null) psD.close();
            if (psI != null) psI.close();
        }

        return SuccessMsg("操作成功", "");
    }

    /*
    添加任务id，创建人，创建时间到数据表
     */
    private void addCommonColumnsToReportData(JSONObject json, String currentUser) {
        JSONArray array = json.getJSONArray("data");
        if (array == null) return;
        Date now = new Date();
        array.forEach(j -> {
            JSONObject js = (JSONObject) j;
            js.putIfAbsent("task_id", json.getInteger("taskId"));
            js.putIfAbsent("create_by", currentUser);
            js.putIfAbsent("create_time", now);
        });
    }

    /**
     * 生成绑定vue指令及相关代码的html文件
     *
     * @param tableId
     * @return
     */
    @RequestMapping(value = "/createHtml/{tableId}", produces = "text/plain;charset=UTF-8")
    public String generateHtml(@PathVariable("tableId") Integer tableId) {
        SqlSession session = DbFactory.Open(DbFactory.FORM);
        Map<String, Object> params = new HashMap<>();
        params.put("tableId", tableId);
        //查询模板关联表
        Map<String, Object> table = session.selectOne("dataCollect.getFrmTable", params);
        //查询字段描述
        Object tid = table.get("table_id");
        List<Map<String, Object>> fieldList = session.selectList("dataCollect.getTableFieldDesc", tid);
        //查询模板文件
        Map<String, Object> templateInfo = session.selectOne("dataCollect.getTemplate", table.get("template_id"));
        String templatePath = (String) templateInfo.get("template_path");
        XSSFExcelToHtml t = new XSSFExcelToHtml();
        File templateFile = new File(templatePath);
        //html文件创建位置
        File htmlFile = new File(AppConstants.getStaticReportPath() + File.separator + tid + ".html");
        boolean isCreate = t.convertToStaticHtml(templateFile, htmlFile);
        if(!isCreate) throw new RuntimeException("生成html文件失败");
        parseHtml(htmlFile);
        return null;
    }


    /**
     * 生成绑定vue指令及相关代码的html文件
     *
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/createHtmlForReact/{taskId}", produces = "text/plain;charset=UTF-8")
    public String createHtmlForReact(@PathVariable("taskId") Integer taskId,HttpServletRequest request) {
        String userInfo = request.getHeader("credentials");
        JSONObject obj2 = (JSONObject) JSON.parse(userInfo);
        SqlSession session = DbFactory.Open(DbFactory.FORM);

        Map<String,Object> map =new HashMap<String,Object>();
        map.put("userId",obj2.getString("userId"));
        map.put("taskId",taskId);

        Map  taskInfo =session.selectOne("dataCollect.getTaskUserInfoByUserId",map);
        if(null!=taskInfo && (null==taskInfo.get("receive_date") || "".equals(taskInfo.get("receive_date")))){
            session.update("dataCollect.updateReportTaskUserBytaskIdUserId", map);
        }

        Map<String, Object> paramsone = new HashMap<>();
        paramsone.put("taskId", taskId);
        //查询模板关联表
        Map<String, Object> table_id = session.selectOne("dataCollect.getFrmTableByTaskId", paramsone);
        Map<String, Object> params = new HashMap<>();
        params.put("tableId", table_id.get("table_id"));
        //查询模板关联表
        Map<String, Object> tables = session.selectOne("dataCollect.getFrmTable", params);
        //查询字段描述
        Object tid = tables.get("table_id");
        Object tName = tables.get("table_name");
        //获取已经填报的数据
        String selectTab=" select * from `"+tName +"` where create_by='"+obj2.get("userId").toString()+"'";
        List<Map<String, Object>> dataList = session.selectList("dataCollect.getTableData", selectTab);

        List<Map<String, Object>> fieldList = session.selectList("dataCollect.getTableFieldDesc", tid);
        //查询模板文件
        Map<String, Object> templateInfo = session.selectOne("dataCollect.getTemplate", tables.get("template_id"));
        //关闭连接
       // session.close();
        String templatePath = (String) templateInfo.get("template_path");
        XSSFExcelToHtmlReact t = new XSSFExcelToHtmlReact();
        File templateFile = new File(templatePath);
        //html文件创建位置
        File htmlFile = new File(AppConstants.getStaticReportPath() + File.separator + tid + ".html");

        boolean isCreate = t.convertToDynamicHtml(templateFile, htmlFile,AppConstants.getStaticReportPath());
        if(!isCreate) throw new RuntimeException("生成html文件失败");

        Map<String,Object> map2 =new HashMap<String,Object>();
        Map<String,Object> map3 =new HashMap<String,Object>();
        try {
            Document dom = Jsoup.parse(htmlFile, "UTF-8");
            //去掉无用标签字符
            dom.select("h2").remove();
            dom.select("body>table>thead").remove();
            dom.select("tbody>tr>th").remove();
            Elements els = dom.select("table>tbody>tr");
            dom.select("table>tbody").first().remove();
            Element table = dom.select("table").first();
            table.attr("id","tabid");
            els.forEach(e -> table.appendChild(e));
            //在head标签中追加script标签
            Element header = dom.select("head").get(0);

           // addScript(scripts, header);
            //在table外面增加div
            Attributes attrs = new Attributes();
            attrs.put("id", "app");
            Element div = new Element(Tag.valueOf("div"), "", attrs);
            div.appendChild(dom.select("body>table").first());
            dom.select("body>table").remove();
            dom.select("body").first().appendChild(div);
            //在tr上绑定v-for指令,查找到一个空行，然后把后面的tr都删除掉
            Elements trs = dom.select("table>tr");
            Elements table3 = dom.select("table");
            for (int i = 0; i < trs.size(); i++) {
                Element tr = trs.get(i);
                Elements tds = tr.children();
                boolean isBlank = Boolean.TRUE;
                for (int j = 0; j < tds.size(); j++) {
                    isBlank = isBlank && isBlank(tds.get(j).text());
                }
                if (isBlank && i < trs.size() - 1) tr.remove();
            }
            int oldLen=trs.size() - 1;
            int length=trs.size();
            Element oldTr = trs.get(oldLen);
            List<Element> trList = new ArrayList<>();
            if(dataList.size()>0){
                for(int d=0;d<dataList.size();d++) {
                    int newLen=length+d;
                    trs.add(newLen,oldTr.clone());
                    Element newTr =trs.get(trs.size()-1);
                    Elements newTd = newTr.children();
                    newTr.attr("id","del"+(newLen+1));
                    for (int jj = 0; jj < newTd.size(); jj++) {
                        for(int k=0;k<fieldList.size();k++) {
                            if (!newTd.get(jj).getElementsByClass(fieldList.get(k).get("field_name").toString().trim()).isEmpty()) {
                                newTd.get(jj).children().attr("value", dataList.get(d).get(fieldList.get(k).get("field_name").toString().trim())==null?"":dataList.get(d).get(fieldList.get(k).get("field_name").toString().trim()).toString());
                                break;
                            }
                        }
                    }
                    newTd.get(newTd.size()-1).children().get(0).attr("value",dataList.get(d).get("_id").toString());
                    newTd.get(newTd.size()-1).children().get(1).attr("onclick","deletetr(this,'"+dataList.get(d).get("_id").toString()+"')");
                    trList.add(newTr);
                }
//                List<Element> trListnew =  dom.select("div>table>tr");
//                trListnew.addAll(trList);
                els.addAll(oldLen,trList);
                els.forEach(e -> table.appendChild(e));
            }
           int leng=fieldList.size();
           /*  String strs="<tr><td></td>";
            for(int i=0;i<leng;i++){
                strs=strs+"<td><input type='text' name='"+fieldList.get(i).get("field_name")+"'style='width: 100px' value='' /></td>";
            }*/
            String btn="<div style='padding-top:10px;'><input type=\"button\" name=\"insert\"  value=\"增加一行\" style=\"width:80px\" " +
                    " onclick=\"addTableTr()\" />&nbsp&nbsp\n" +
                                              " <input type=\"button\"  value=\" 保    存 \"  style=\"width:80px\"  onclick=\"GetValue(false)\" />&nbsp;&nbsp;" +
                    "<input type=\"button\"  value=\" 提    交 \"  style=\"width:80px\"  onclick=\"GetValue(true)\" />" +
                    "<input type='hidden' id='fieldLength' value='"+leng+"'></div>";
            table3.after(btn);
            Object obj=dom.html();
            map3.put("taskInfo",obj);
        } catch (IOException e){
            e.printStackTrace();
        }


        map2.put("msg","查询成功");
        map2.put("data",map3);
        map2.put("status",0);
        return  SuccessMsg("操作成功", map3);
       // return JSON.toJSONString(map2);
    }

    String scripts[] = {"https://cdn.bootcss.com/vue/2.2.2/vue.min.js","https://cdn.bootcss.com/vue-resource/1.5.0/vue-resource.js"};

    private void parseHtml(File htmlFile) {
        try {
            Document dom = Jsoup.parse(htmlFile, "UTF-8");
            //去掉无用标签字符
            dom.select("h2").remove();
            dom.select("body>table>thead").remove();
            dom.select("tbody>tr>th").remove();
            Elements els = dom.select("table>tbody>tr");
            dom.select("table>tbody").first().remove();
            Element table = dom.select("table").first();
            els.forEach(e -> table.appendChild(e));
            //在head标签中追加script标签
            Element header = dom.select("head").get(0);
            addScripts(scripts, header);
            //在table外面增加div
            Attributes attrs = new Attributes();
            attrs.put("id", "app");
            Element div = new Element(Tag.valueOf("div"), "", attrs);
            div.appendChild(dom.select("body>table").first());
            dom.select("body>table").remove();
            dom.select("body").first().appendChild(div);
            //在tr上绑定v-for指令,查找到一个空行，然后把后面的tr都删除掉
            Elements trs = dom.select("table>tr");
            for (int i = 0; i < trs.size(); i++) {
                Element tr = trs.get(i);
                Elements tds = tr.children();
                boolean isBlank = Boolean.TRUE;
                for (int j = 0; j < tds.size(); j++) {
                    isBlank = isBlank && isBlank(tds.get(j).text());
                }
                if (isBlank && i < trs.size() - 1) tr.remove();
            }
            System.out.println(dom.html());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean isBlank(String str) {
        if (root.report.util.StringUtil.isBlank(str) || str.equals("&nbsp;")) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void addScripts(String[] scripts, Element header) {
        for (int i = 0; i < scripts.length; i++) {
            Attributes attrs = new Attributes();
            attrs.put("type=\"text/javascript\">", scripts[i]);
            Element s = new Element(Tag.valueOf("script"), "", attrs);
            header.appendChild(s);
        }
    }
    private void addScript(String scripts, Element header) {
        Attributes attrs = new Attributes();
        attrs.put("src", scripts);
            Element s = new Element(Tag.valueOf("script"),"", attrs);
            header.appendChild(s);

    }
    /**
     * 根据任务ID和当前用户查询他的填报数据
     * 代办任务
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getMyTaskByUserId", produces = "text/plain;charset=UTF-8")
    public String getMyTaskByUserId(@RequestBody String pJson,HttpServletRequest request) {
        JSONObject obj = (JSONObject) JSON.parse(pJson);
        //获取当前登录人所有信息
        String  userInfo=request.getHeader("credentials");
        JSONObject obj2 = (JSONObject) JSON.parse(userInfo);

        SqlSession session = DbFactory.Open(DbFactory.FORM);
        //判断当前用户是否任务的创建人
        Map<String,Object> map2 =new HashMap<String,Object>();
        Map<String,Object> map3 =new HashMap<String,Object>();
        try{
            Map<String,Object> map = new HashMap<String,Object>();
            int currentPage=Integer.valueOf(obj.getString("currentPage"));
            int perPage=Integer.valueOf(obj.getString("perPage"));
            if(1==currentPage|| 0==currentPage){
                currentPage=0;
            }else{
                currentPage=(currentPage-1)*perPage;
            }
            map.put("userId", obj2.getString("userId"));
            map.put("startIndex", currentPage);
            map.put("perPage",perPage);
            map.put("keyword",  obj.get("keyword")==null?"":obj.getString("keyword"));

            List<Map>  tasklist= session.selectList("dataCollect.getMytaskByUserId", map);
            String total=session.selectOne("dataCollect.countMytaskByUserId", map);
            map3.put("list",tasklist);
            map3.put("total",Integer.valueOf(total));
            map2.put("msg","查询成功");
            map2.put("data",map3);
            map2.put("status",0);
        }catch (Exception e){
            map3.put("list",null);
            map2.put("msg","查询失败");
            map2.put("data",map3);
            map2.put("status",10);
        }
        return JSON.toJSONString(map2);
    }
    /**
     * 根据任务ID和当前用户查询他的填报数据
     * 已办任务
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getMyTaskListByUserId", produces = "text/plain;charset=UTF-8")
    public String getMyTaskListByUserId(@RequestBody String pJson,HttpServletRequest request) {
        JSONObject obj = (JSONObject) JSON.parse(pJson);
        //获取当前登录人所有信息
        String  userInfo=request.getHeader("credentials");
        JSONObject obj2 = (JSONObject) JSON.parse(userInfo);

        SqlSession session = DbFactory.Open(DbFactory.FORM);
        //判断当前用户是否任务的创建人
        Map<String,Object> map2 =new HashMap<String,Object>();
        Map<String,Object> map3 =new HashMap<String,Object>();
        try{
            Map<String,Object> map = new HashMap<String,Object>();
            int currentPage=Integer.valueOf(obj.getString("currentPage"));
            int perPage=Integer.valueOf(obj.getString("perPage"));
            if(1==currentPage|| 0==currentPage){
                currentPage=0;
            }else{
                currentPage=(currentPage-1)*perPage;
            }
            map.put("userId", obj2.getString("userId"));
            map.put("startIndex", currentPage);
            map.put("perPage",perPage);
            map.put("keyword",  obj.get("keyword")==null?"":obj.getString("keyword"));
            List<Map>  tasklist= session.selectList("dataCollect.getMytaskListByUserId", map);
            String total=session.selectOne("dataCollect.countMytaskListByUserId", map);
            map3.put("list",tasklist);
            map3.put("total",Integer.valueOf(total));
            map2.put("msg","查询成功");
            map2.put("data",map3);
            map2.put("status",0);
        }catch (Exception e){
            map3.put("list",null);
            map2.put("msg","查询失败");
            map2.put("data",map3);
            map2.put("status",10);
        }
        return JSON.toJSONString(map2);
    }
    /**
     * 查询任务及任务用户信息
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/getTaskAndUsersByid", produces = "text/plain;charset=UTF-8")
    public String getTaskAndUsersByid(@RequestBody String taskId,HttpServletRequest request) {
            //获取当前登录人所有信息
            String  userInfo=request.getHeader("credentials");
            JSONObject obj2 = (JSONObject) JSON.parse(userInfo);

            JSONObject obj3 = (JSONObject) JSON.parse(taskId);
            SqlSession session = DbFactory.Open(DbFactory.FORM);
            Map<String,Object> map =new HashMap<String,Object>();
            map.put("userId",obj2.getString("userId"));
            map.put("taskId",Integer.valueOf(obj3.getString("taskId")));

            Map  taskInfo =session.selectOne("dataCollect.getTaskUserInfoByUserId",map);
            if(null!=taskInfo && (null==taskInfo.get("receive_date") || "".equals(taskInfo.get("receive_date")))){
             session.update("dataCollect.updateReportTaskUserBytaskIdUserId", map);
             }
            Map<String,Object> map2 =new HashMap<String,Object>();
            Map<String,Object> map3 =new HashMap<String,Object>();
            map3.put("taskInfo",taskInfo);
            map2.put("msg","查询成功");
            map2.put("data",map3);
            map2.put("status",0);
       return JSON.toJSONString(map2);

    }

    @RequestMapping(value = "/saveTaskInfo", produces = "text/plain;charset=UTF-8")
    public String saveTaskInfo(@RequestBody String taskjson,HttpServletRequest request) {
        //获取当前登录人所有信息
        String userInfo = request.getHeader("credentials");
        JSONObject obj2 = (JSONObject) JSON.parse(userInfo);
        JSONObject obj3 = (JSONObject) JSON.parse(taskjson);

        SqlSession session = DbFactory.Open(DbFactory.FORM);
        String userId=obj3.get("userId").toString();
        String taskId = obj3.get("taskId").toString();
        String delId= obj3.getString("delId");
        String isSubmit = obj3.getString("isSubmit");
        List<Object> jsonArray= (List<Object>) obj3.get("dataList");
        //insert into +表名（表中的字段，，）values（字段所对应的记录，，)(字段所对应的记录);
        String tableName="";//表名
        String valus=""; //执行值
        String fields=""; //列名
        boolean flag=true;
        for (int i=0;i<jsonArray.size();i++){
            List<String> l= (List<String>) jsonArray.get(i);
            String values="";
            boolean isUpdate=false;
            String forupdate="";
            String forupdatewhere="";
            for(int j=l.size()-1;j>=0;j--){
               String lastObject=l.get(j);
                try {
                    lastObject=URLDecoder.decode(lastObject, "UTF-8");//解码
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //进行值拆解 格式为 [表名.列名:值]
                if(null!=lastObject && !"".equals(lastObject)){
                    //分解字符串
                    String arr[]=lastObject.split("\\:");
                    //获取表名
                    String tableColumns[]=arr[0].split("\\.");
                    if(null==tableName || "".equals(tableName)){
                        tableName =tableColumns[0];
                    }
                    //获取值
                    String val="";
                    if(arr.length>1) {
                        val = arr[1];
                    }
                    if(tableColumns[1].equals("_id") && !"".equals(val)) {
                        isUpdate = true;
                        forupdatewhere=" where "+tableColumns[1]+"='"+val+"'";
                    }
                    if(isUpdate){
                        if(!tableColumns[1].equals("_id")) {
                            forupdate = forupdate + "," + tableColumns[1] + "='"+val+"'";
                        }
                    }else{
                        //获取列名，只有第一个循环进行拼接
                        if (flag && !tableColumns[1].equals("_id")) {
                            fields = fields + "," + tableColumns[1];
                        }
                        if(!tableColumns[1].equals("_id")) {
                            values = values + ",'" + val + "'";
                        }
                    }
                }
            }

            if(isUpdate){
                forupdate=forupdate.substring(1,forupdate.length());
                String up="update "+tableName+" set "+forupdate+forupdatewhere;
                session.selectOne("dataCollect.insetTaskInfo", up);
            }else {
                flag=false;
                //将值进行组装  values(1),(2);
                values = values.substring(1, values.length());
                valus = valus + ",(" + values + ",'" + taskId + "','" + userId + "',sysdate())";
            }
        }
        //生成sql语句，可以一次性执行多条数据保存
        if(!"".equals(fields)) {
            fields = fields.substring(1, fields.length());
            valus = valus.substring(1, valus.length());
            String insetsql = "insert into " + tableName + " (" + fields + ",task_id,create_by,create_time) values " + valus;
           // System.out.println(insetsql);
            //进行插入数据
            session.selectOne("dataCollect.insetTaskInfo", insetsql);
        }
        if(delId.trim().length()>0){
            delId=delId.trim();
            String delarr[]=delId.split(",");
            String delsql="delete from "+  tableName + " where _id in (";
            String delid="";
            for(int h=0;h<delarr.length;h++){
                delid=delid+",'"+delarr[h]+"'";
            }
            delid = delid.substring(1, delid.length());
            delsql=delsql+delid+")";
            session.selectOne("dataCollect.insetTaskInfo", delsql);
        }
        if("true".equals(isSubmit)){
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("userId",userId);
            map.put("taskId",taskId);
            session.update("dataCollect.commitReportTaskUserBytaskIdUserId", map);
        }
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        Map<String, Object> map3 = new HashMap<String, Object>();
        map2.put("msg", "查询成功");
        map2.put("data", obj3);
        map2.put("status", 0);
        return JSON.toJSONString(map2);
    }

    /**
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/viewHtmlForReact/{taskId}", produces = "text/plain;charset=UTF-8")
    public String viewHtmlForReact(@PathVariable("taskId") Integer taskId,HttpServletRequest request) {
        String userInfo = request.getHeader("credentials");
        JSONObject obj2 = (JSONObject) JSON.parse(userInfo);
        SqlSession session = DbFactory.Open(DbFactory.FORM);
        Map<String, Object> paramsone = new HashMap<>();
        paramsone.put("taskId", taskId);
        //查询模板关联表
        Map<String, Object> table_id = session.selectOne("dataCollect.getFrmTableByTaskId", paramsone);
        Map<String, Object> params = new HashMap<>();
        params.put("tableId", table_id.get("table_id"));
        //查询模板关联表
        Map<String, Object> tables = session.selectOne("dataCollect.getFrmTable", params);
        //查询字段描述
        Object tid = tables.get("table_id");
        Object tName = tables.get("table_name");
        //获取已经填报的数据
        String selectTab=" select * from `"+tName +"` where create_by='"+obj2.get("userId").toString()+"'";
        List<Map<String, Object>> dataList = session.selectList("dataCollect.getTableData", selectTab);

        List<Map<String, Object>> fieldList = session.selectList("dataCollect.getTableFieldDesc", tid);
        //查询模板文件
        Map<String, Object> templateInfo = session.selectOne("dataCollect.getTemplate", tables.get("template_id"));
        //关闭连接
        //session.close();
        String templatePath = (String) templateInfo.get("template_path");
        XSSFExcelToHtmlReactView t = new XSSFExcelToHtmlReactView();
        File templateFile = new File(templatePath);
        //html文件创建位置
        File htmlFile = new File(AppConstants.getStaticReportPath() + File.separator + tid + ".html");

        boolean isCreate = t.convertToDynamicHtml(templateFile, htmlFile,AppConstants.getStaticReportPath());
        if(!isCreate) throw new RuntimeException("生成html文件失败");

        Map<String,Object> map2 =new HashMap<String,Object>();
        Map<String,Object> map3 =new HashMap<String,Object>();
        try {
            Document dom = Jsoup.parse(htmlFile, "UTF-8");
            //去掉无用标签字符
            dom.select("h2").remove();
            dom.select("body>table>thead").remove();
            dom.select("tbody>tr>th").remove();
            Elements els = dom.select("table>tbody>tr");
            dom.select("table>tbody").first().remove();
            Element table = dom.select("table").first();
            table.attr("id","tabid");
            els.forEach(e -> table.appendChild(e));
            //在head标签中追加script标签
            Element header = dom.select("head").get(0);

            //在table外面增加div
            Attributes attrs = new Attributes();
            attrs.put("id", "app");
            Element div = new Element(Tag.valueOf("div"), "", attrs);
            div.appendChild(dom.select("body>table").first());
            dom.select("body>table").remove();
            dom.select("body").first().appendChild(div);
            //在tr上绑定v-for指令,查找到一个空行，然后把后面的tr都删除掉
            Elements trs = dom.select("table>tr");
            for (int i = 0; i < trs.size(); i++) {
                Element tr = trs.get(i);
                Elements tds = tr.children();
                boolean isBlank = Boolean.TRUE;
                for (int j = 0; j < tds.size(); j++) {
                    isBlank = isBlank && isBlank(tds.get(j).text());
                }
                if (isBlank && i < trs.size() - 1) tr.remove();
            }
            int oldLen=trs.size() - 1;
            int length=trs.size();
            Element oldTr = trs.get(oldLen);
            List<Element> trList = new ArrayList<>();
            if(dataList.size()>0){
                for(int d=0;d<dataList.size();d++) {
                    int newLen=length+d;
                    trs.add(newLen,oldTr.clone());
                    Element newTr =trs.get(trs.size()-1);
                    Elements newTd = newTr.children();

                    for (int jj = 0; jj < newTd.size(); jj++) {
                        for(int k=0;k<fieldList.size();k++) {
                            if (!newTd.get(jj).getElementsByClass(fieldList.get(k).get("field_name").toString().trim()).isEmpty()) {
                                newTd.get(jj).children().attr("value", dataList.get(d).get(fieldList.get(k).get("field_name").toString().trim())==null?"":dataList.get(d).get(fieldList.get(k).get("field_name").toString().trim()).toString());
                                break;
                            }
                        }
                    }
                    trList.add(newTr);
                }
                els.addAll(oldLen,trList);
                els.forEach(e -> table.appendChild(e));
                table.select("tr").last().remove();
            }
            Object obj=dom.html();
            map3.put("taskInfo",obj);
        } catch (IOException e){
            e.printStackTrace();
        }
        map2.put("msg","查询成功");
        map2.put("data",map3);
        map2.put("status",0);
        return SuccessMsg("查看",map3);//JSON.toJSONString(map2);
    }
}
