package root.report.leeutils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.*;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Lee
 * @ClassName: DbManagerHutool
 * @Description: TODO
 * @date 2020-10-16
 */

public final class DbManagerHutool {
    public static Map<String, String> getCatalogAndSchema(final String  dbConnName) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Map<String, String> map = new TreeMap<String, String>();
        map.put("catalog", dsSource.getConnection().getCatalog());
        map.put("schema", dsSource.getConnection().getSchema());
        return map;
    }

    public static Entity getTableInfo(final String  dbConnName, final String tableName) throws SQLException {
        List<Entity> l = getSchema(dbConnName, tableName);
        final Entity sampleE = Entity.create(tableName);
        for (Entity entity : l) {
            String field = entity.getStr("Field");
            sampleE.addFieldNames(field);
        }
        return sampleE;
    }

    public static Entity getTableInfoVer2(final String  dbConnName, final String tableName) throws SQLException {
        List<Entity> l = getSchema(dbConnName, tableName);
        final Entity sampleE = Entity.create(tableName);
        for (Entity entity : l) {
            String field = entity.getStr("Field");
            sampleE.addFieldNames(field);
        }
        System.out.println(sampleE.toString());
        return sampleE;
    }

    public static JSONArray getTableInfoVer3(final String  dbConnName, final String tableName) throws SQLException {
        List<Entity> l = getSchema(dbConnName, tableName);
        final Entity sampleE = Entity.create(tableName);
        JSONArray fields=new JSONArray();
        for (Entity entity : l) {
            String fieldName = entity.getStr("Field");
            String fieldType = entity.getStr("Type");
//            sampleE.addFieldNames(field);
            JSONObject field=new JSONObject();
            field.put("fieldName",fieldName);
            field.put("fieldType",fieldType);
            fields.add(field);
        }
        System.out.println(sampleE.toString());
        return fields;
    }

    public static Entity getTableInfoNew(String url, String user, String password,String tableName) throws SQLException {
        List<Entity> l = getSchemaNew(url,user,password,tableName);
        final Entity sampleE = Entity.create(tableName);
        for (Entity entity : l) {
            String field = entity.getStr("Field");
            sampleE.addFieldNames(field);
        }
        return sampleE;
    }

    /**
     * 获取表结构
     * @param dbConnName 数据库
     * @param tableName 数据表
     * @return 表结构信息
     * @throws SQLException ss
     */
    public static List<Entity> getSchema(final String  dbConnName, final String tableName) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        return  dbUse.query("desc " + tableName);
    }
    public static String getSchema4Order(Db dbUse, final String tableName) throws SQLException {
        final List<Entity> query = dbUse.query("desc " + tableName);
        String str = "id";
        for ( Entity en :query) {
            if(str.equals(en.getStr("Field"))){
                return str;
            }
        }
        return  query.get(0).getStr("Field");
    }

    public static List<Entity> getSchemaNew(String url, String user, String password,String tableName) throws SQLException {
        final DataSource dsSource = new SimpleDataSource(url, user, password);
        final Db dbUse = DbUtil.use(dsSource);
        return  dbUse.query("desc " + tableName);
    }
    public static List<Entity> actionSqlStatements(final String  dbConnName, final String sqlStatements) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        return  dbUse.query(sqlStatements);
    }

    public static Entity getFieldInformation(final String  dbConnName, final String tableName,final String fieldName) throws SQLException {
        List<Entity> l = getSchema(dbConnName, tableName);
        for (Entity entity : l) {
            if(fieldName.equals(entity.getStr("Field"))){
                return entity;
            }
        }
        return null;
    }

    public static Entity convert(final Entity sampleE, final JSONObject obj) {
        for (String fieldName : sampleE.getFieldNames()) {
            String value = obj.getString(fieldName);
            if (StringUtils.isNotBlank(value)) {
                sampleE.set(fieldName, value);
            }
        }
        sampleE.set("id", IDUtil.getId());
        StaticLog.info(JsonTool.formatJson(sampleE));
        return sampleE;
    }

    /**
     * 插入一条数据
     * @param dbConnName
     * @param tableName
     * @param obj
     * @return
     * @throws SQLException
     */
    public static Entity insert(final String dbConnName, final String tableName, final JSONObject obj)
            throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity sampleE= convert(getTableInfo(dbConnName, tableName), obj);
        dbUse.insert(sampleE);
        return sampleE;
    }

    /**
     * 批量插入数据
     * @param dbConnName
     * @param entities
     * @return
     * @throws SQLException
     */
    public static int[] insert(final String dbConnName,ArrayList<Entity> entities)
            throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        return dbUse.insert(entities);
    }

    public static int insertOrUpdateBatch(final String dbConnName,ArrayList<Entity> entities,final String prikey)
            throws SQLException {
        int i=0;
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        for ( Entity en: entities ) {
            i+=(dbUse.insertOrUpdate(en, prikey));
        }
        return i;
    }

    public static int insertOrUpdate(final String jdbc,final String user,final String password,List<Entity> entity,final String prikey)throws SQLException {
        final DataSource dsSource = new SimpleDataSource(jdbc,user,password);
        final Db dbUse = DbUtil.use(dsSource);
        int i=0;
        for ( Entity en: entity ) {
            i+=(dbUse.insertOrUpdate(en, prikey));
        }

        return i;
    }

    /**
     * 根据id更新数据中的版本version,默认版本为1，1版本代表最新数据，0为历史数据
     * @param dbConnName aa
     * @param tableName aa
     * @return aa
     * @throws SQLException aa
     */
    public static int update(final String dbConnName, final String tableName, final String fieldName,final String fieldValue,
                             final String prikey,final String prikeyValue)throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        return dbUse.update(Entity.create(tableName).set(fieldName,fieldValue),Entity.create(tableName).set(prikey,prikeyValue));
    }

    public static int updateBatch(final String jdbc,final String user,final String password,final String tableName,
                                  final String fieldName,final String prikey,Map<Long,String> map)throws SQLException {
        final DataSource dsSource = new SimpleDataSource(jdbc,user,password);
        final Db dbUse = DbUtil.use(dsSource);
        int i=0;
        for (Long ma : map.keySet()) {
            final int update = dbUse.update(Entity.create(tableName).set(fieldName, map.get(ma)), Entity.create(tableName).set(prikey, ma));
            i+= update;
        }
        return i ;
    }
    public static String getOnlyPriKey(final String dbConnName, final String tableName) throws SQLException {
        final List<Entity> schemaNew =getSchema(dbConnName,tableName);
        for ( Entity en :schemaNew) {
            if("PRI".equals(en.getStr("Key"))){
                return en.getStr("Field");
            }
        }
        return null;
    }
    /**
     * 根据id删除数据
     * @param dbConnName
     * @param tableName
     * @param id
     * @return
     * @throws SQLException
     */
    public static int del(final String dbConnName, final String tableName, final String id) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        return dbUse.del(tableName, "id", id);
    }

    public static List<Entity> findAll(final String dbConnName, final String tableName, int page, int numPerPage)
            throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
//		numPerPage = numPerPage < 10 ? 10 : numPerPage;
        page = page < 1 ? 1 : page;
        return dbUse.pageForEntityList(whereE, page, numPerPage);
    }

    public static PageResult<Entity> findAllOrderByIdDesc(final String dbConnName, final String tableName, int pageNum,
                                                          int pageSize) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        final Page page = genPage(pageNum, pageSize, getSchema4Order(dbUse,tableName));
        return dbUse.page(whereE, page);
    }

    /**
     * 指定字段进行倒序排序
     * @param dbConnName aa
     * @param tableName aa
     * @param orderField aa
     * @param pageNum aa
     * @param pageSize aa
     * @return  aa
     * @throws SQLException aa
     */
    public static PageResult<Entity> findAllOrderByFieldDesc(final String dbConnName, final String tableName, final String orderField,
                                                             int pageNum,int pageSize) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        final Page page = genPage(pageNum, pageSize, orderField);
        return dbUse.page(whereE, page);
    }

    public static PageResult<Entity> findAllOrderByIdDesc(final String url ,final String user,final String password,
                                                          final String tableName, int pageNum,int pageSize) throws SQLException {
        final DataSource dsSource = new SimpleDataSource(url,user,password);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        final Page page = genPage(pageNum, pageSize, getSchema4Order(dbUse,tableName));
        return dbUse.page(whereE, page);
    }

    public static PageResult<Entity> findFieldNamesOrderByIdDesc(final String dbConnName, final String tableName,
                                                                 int pageNum, int pageSize, final String... fieldNames) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        whereE.addFieldNames(fieldNames);
        final Page page = genPage(pageNum, pageSize, getSchema4Order(dbUse,tableName));
        return dbUse.page(whereE, page);
    }

    public static PageResult<Entity> findFieldNames(final String dbConnName, final String tableName,
                                                    int pageNum, int pageSize, final List<String > fieldNames) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        for(String s:fieldNames){
            whereE.addFieldNames(s);
        }
        final Page page = genPage(pageNum, pageSize, getSchema4Order(dbUse,tableName));
        return dbUse.page(whereE, page);
    }

    public static PageResult<Entity> findFieldNamesNew(final String url,final String user,final String password, final String tableName,
                                                       int pageNum, int pageSize, final List<String > fieldNames) throws SQLException {
        final DataSource dsSource = new SimpleDataSource(url,user,password);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        String s1 = fieldNames.get(0);
        for(String s:fieldNames){
            whereE.addFieldNames(s);
        }
        // final Page page = genPage(pageNum, pageSize, "id");
        final Page page = genPage(pageNum, pageSize, s1);
        return dbUse.page(whereE, page);
    }

    public static List<Entity> findAllBySql(final String url,final String user,final String password, final String sql) throws SQLException {
        final DataSource dsSource = new SimpleDataSource(url,user,password);
        final Db dbUse = DbUtil.use(dsSource);
        return dbUse.query(sql);
    }

    public static List<Entity> findAllBySql(final String dbConnName,final String sql) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        return dbUse.query(sql);
    }

    /**
     * 根据字段条件进行查询
     * @return
     * @throws SQLException
     */
    public static PageResult<Entity> conditionsForQuery(final String dbConnName, final String tableName,
                                                        String fieldName,final String fieldValue, int pageNum, int pageSize) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        whereE.set(fieldName,fieldValue);
        final Page page = genPage(pageNum, pageSize, getSchema4Order(dbUse,tableName));
        return dbUse.page(whereE, page);
    }

    public static Page genPage(int pageNum, int pageSize, final String order4FieldName) {
        final Order order = new Order();
        order.setField(order4FieldName);
        order.setDirection(Direction.DESC);
        pageSize = pageSize < 5 ? 5 : pageSize;
        pageNum = pageNum < 1 ? 1 : pageNum;
        final Page page = new Page(pageNum, pageSize, order);
        return page;
    }

    public static int countAll(final String dbConnName, final String tableName) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        return dbUse.count(whereE);
    }

    public static int countAllNew(final String url ,final String user,final String password, final String tableName) throws SQLException {
        final DataSource dsSource = new SimpleDataSource(url, user, password);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        return dbUse.count(whereE);
    }



    /**
     * 将excel中的数据导入到数据库中
     * @param dbConnName
     * @param tableName
     * @param file
     * @return
     * @throws SQLException
     */
    public static int[] loadExcel(final String dbConnName, final String tableName, MultipartFile file)
            throws SQLException {

        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final List<Entity> entity = new ArrayList<Entity>();
        try {
            InputStream inputStream = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            List<Map<String, Object>> li = reader.readAll();
            for (Map<String, Object> map : li) {
                Entity e = Entity.create(tableName);
                for (String s : map.keySet()) {
                    e.set(s, map.get(s));
                }
                entity.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbUse.insert(entity);
    }

    public static int[] loadCsv(final String dbConnName, final String tableName, MultipartFile file)
            throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final List<Entity> entity = new ArrayList<Entity>();
        final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        final List<String> li = new ArrayList<String>();
        String[] s = null;
        try {
            InputStreamReader isr = new InputStreamReader(file.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            int i = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                if (i == 0) {
                    s = line.split(",");
                    i++;
                } else {
                    li.add(line);
                }
            }
            for (String l : li) {
                String[] s1 = l.split(",");
                Map<String, Object> map = new HashMap<String, Object>(16);
                for (int j = 0; j < s1.length; j++) {
                    map.put(s[j], s1[j]);
                }
                list.add(map);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (Map<String, Object> map : list) {
            Entity e = Entity.create(tableName);
            for (String str : map.keySet()) {
                e.set(str, map.get(str));
            }
            entity.add(e);
        }
        return dbUse.insert(entity);
    }

    /**
     * 从数据库中导出数据到本地Excel
     *
     * @param dbConnName
     * @param tableName
     * @param pageNum
     * @param pageSize
     * @return
     * @throws SQLException
     */
   /* public static String exportExcel(final String dbConnName, final String tableName, int pageNum, int pageSize)
            throws SQLException {

        final PageResult<Entity> fiel = findFieldNamesOrderByIdDesc(dbConnName, tableName, pageNum, pageSize);
        List<Entity> entity = new ArrayList<Entity>();
        for (int i = 0; i < fiel.size(); i++) {
            Entity sampleE = getTableInfo(dbConnName, tableName);
            for (String fieldName : sampleE.getFieldNames()) {
                final String value = fiel.get(i).getStr(fieldName);
                if (StringUtils.isNotBlank(value)) {
                    sampleE.set(fieldName, value);
                }
            }
            entity.add(sampleE);
        }
        //将entity写入到表格中并返回结果
        return writeExcel(entity, tableName);
    }*/

    /**
     * 将数据库中的数据写出到excel表格中
     *
     * @param entity
     */
/*    private static String writeExcel(List<Entity> entity, String tableName) {
        String str = null;
        //获取文件写入路径
        final String systemExcelHome = LocalFileUtil.getSystemImageHome("var\\apigateway\\excel\\");
        //用表名加随机id的后6位作为表格名称
        String excelName = tableName + String.valueOf(IDUtil.getId()).substring(12) + ".xls";
        //文件路径
        String fileName = systemExcelHome + excelName;
        LogFactory.get().info("excelName:" + excelName);
        //创建excel文件
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();//如果文件存在就删除
        }
        try {
            file.createNewFile();
            //创建文件，设置文件名
            WritableWorkbook workbookA = Workbook.createWorkbook(file);
            //生成名为“sheet1”的工作表，参数0表示这是第一页
            WritableSheet sheet1 = workbookA.createSheet("sheet1", 0);
            //在Label对象的构造子中指名单元格
            Label label = null;
            final Set<String> fieldNames = entity.get(0).getFieldNames();
            LogFactory.get().info("fieldNames:" + fieldNames);
            //获取数据源
            for (int i = 0; i < entity.size(); i++) {
                int l = 0;
                for (String en : fieldNames) {
                    //设置列名
                    label = new Label(l, 0, en);
                    sheet1.addCell(label);
                    //获取数据
                    label = new Label(l++, i + 1, entity.get(i).getStr(en));
                    sheet1.addCell(label);
                }
            }
            //写入数据 
            workbookA.write();
            //关闭连接
            workbookA.close();
            str = "数据导出成功,路径：" + systemExcelHome + ",文件名为： " + excelName;
            LogFactory.get().info(str);
        } catch (Exception e) {
            str = "数据导出失败...";
            LogFactory.get().info(str + "   " + e);
        }
        return str;
    }*/

    /**
     * 生成模型数据，不切词
     * @param dbConnName 数据源
     * @param tableName 表
     * @return String
     */
/*    public static String exportToTxt2(final String dbConnName, final String tableName, final String tableField) throws SQLException {
        int pageNum=0;
        int pageSize=500;
        final String systemExcelHome = FastTextModelList.loadSetting().getFasttextPath()+"/data/";
        String excelName = tableName + "_" + tableField + String.valueOf(IDUtil.getId()).substring(12) + ".txt";
        String filePath = systemExcelHome + excelName;
        //获取数据
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        final String schema4Order = getSchema4Order(dbUse, tableName);
        while (true){
            pageNum++;
            final Page page = genPage(pageNum, pageSize,schema4Order);
            final PageResult<Entity> entity = dbUse.page(whereE, page);
            StringBuffer aa = new StringBuffer();
            for (Entity en : entity) {
                final String names = en.getStr(tableField);
                if(StringUtils.isNotBlank(names)){
                    final String fastTextLabel = en.getStr("fast_text_label");
                    final String label = StringUtils.isBlank(fastTextLabel)?"others":fastTextLabel;
                    aa.append("__label__").append(label).append(" ").append(names).append("\r\n");
                }
            }
            saveAsFileWriter(filePath,aa.toString());
            aa.setLength(0);
            if(entity.size()<500){
                break;
            }
        }
        return filePath;
    }
  */


/*
    public static String exportToTxt(final String dbConnName, final String tableName, final String tableField) throws SQLException {
        int pageNum=0;
        int pageSize=500;
        final String systemExcelHome = FastTextModelList.loadSetting().getFasttextPath()+"/data/";
        String excelName = tableName + "_" + tableField + String.valueOf(IDUtil.getId()).substring(12) + ".txt";
        String filePath = systemExcelHome + excelName;
        //获取数据
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        final String schema4Order = getSchema4Order(dbUse, tableName);
        while (true){
            pageNum++;
            final Page page = genPage(pageNum, pageSize,schema4Order);
            final PageResult<Entity> entity = dbUse.page(whereE, page);
            StringBuffer aa = new StringBuffer();
            for (Entity en : entity) {
                final String names = WpByHanlpWithStopwords.extractWords(en.getStr(tableField));
                if(StringUtils.isNotBlank(names)){
                    final String fastTextLabel = en.getStr("fast_text_label");
                    final String label = StringUtils.isBlank(fastTextLabel)?"others":fastTextLabel;
                    aa.append("__label__").append(label).append(" ").append(names).append("\r\n");
                }
            }
            saveAsFileWriter(filePath,aa.toString());
            aa.setLength(0);
            if(entity.size()<500){
                break;
            }
        }
        return filePath;
    }
*/

    /**
     * 数据脱敏
     */

/*    public static String dataDesensitization(final String dbConnName, final String tableName, final String tableField,final String rule,
                                             final String dataFormate, final String hdfsPath, final String fileName,final String hdfsUrl,final String hdfsUser) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        final List<Entity> entity = dbUse.findAll(whereE);
        int i=0;
        for (Entity en : entity) {
            StringBuffer aa = new StringBuffer();
            //获取原文
            final String old = en.getStr(tableField);
            String newContent = DesensitizationUtil.toRun(rule, old);
            //转为libsvm格式
            aa.append(i++).append(" ").append("1:").append(newContent).append("\r\n");
            String str1 = HdfsTool.writeToHdfs(hdfsUrl,hdfsUser,hdfsPath + fileName, aa.toString());
            if("写入失败".equals(str1)){
                return "采集失败";
            }
        }
        return "采集成功";
    }*/

    public static String exportToTxt(final String dbConnName, final String tableName, final String tableField,
                                     final String labelField,final String exportFileName,final String path) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        final Entity whereE = Entity.create(tableName);
        //获取数据
        final List<Entity> entity = dbUse.findAll(whereE);
        //文件名称用表名加随机id的后6位作为表格名称
        String excelName = exportFileName + "_"  + String.valueOf(IDUtil.getId()).substring(12) + ".txt";
        //文件全路径
        String filePath = path + excelName;
        int i = 0;
        StringBuffer aa = new StringBuffer();
        for (Entity en : entity) {
            i++;
            //获取标签字段的值
            final String label = en.getStr(labelField);
            //获取names字段的值
            final String names = en.getStr(tableField);
            aa.append(label).append("-_-_").append(names).append("\r\n");
            if(i>5000){
                saveAsFileWriter(filePath,aa.toString());
                aa.setLength(0);
                i=0;
            }
        }
        saveAsFileWriter(filePath,aa.toString());
        return filePath;
    }

    /**
     * 写到指定目录
     * @param content
     */
    private static void saveAsFileWriter(String filePath,String content) {
        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter(filePath, true);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取linux服务器中某路径下的文件及目录
     * @param
     */
    public static File[] getFileName(final String path) {
        File[] ls = FileUtil.ls(path);
        return ls;
    }

    /**
     * 获取服务器下的所有数据库
     * @return
     * @throws SQLException
     */
    public static List<String> getDatabases(final String jdbc,final String user,final String password) throws SQLException {
        final List<String> arr = new ArrayList<>();
        DruidDataSource ds2 = new DruidDataSource();
        ds2.setUrl(jdbc);
        ds2.setUsername(user);
        ds2.setPassword(password);
        Db use = DbUtil.use(ds2);
        String sql="show DATABASES";
        List<Entity> databases = use.query(sql);
        for (Entity en :databases){
            arr.add(en.getStr("Database"));
        }
        return arr;
    }

    /**
     * 查找对应数据库中所有的表
     * @param
     * @return
     * @throws SQLException
     */
    public static List<String> getTableName(final String dbConnName) throws SQLException {
        final List<String> arr = new ArrayList<>();
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final DatabaseMetaData m = dsSource.getConnection().getMetaData();
        final ResultSet tables = m.getTables(dsSource.getConnection().getCatalog(), "%", "%", new String[]{"TABLE"});
        while (tables.next()) {
            arr.add(tables.getString("TABLE_NAME"));
        }
        return arr;
    }

    /**
     * 获取数据库下的所有表
     * @param url jdbc
     * @param user 用户
     * @param password 密码
     * @return
     */
    public static List<String> getTableNameNew(String url, String user, String password) throws SQLException {
        final List<String> arr = new ArrayList<>();
        final DataSource dsSource = new SimpleDataSource(url, user, password);
        final DatabaseMetaData m = dsSource.getConnection().getMetaData();
        final ResultSet tables = m.getTables(dsSource.getConnection().getCatalog(), "%", "%", new String[]{"TABLE"});
        while (tables.next()) {
            arr.add(tables.getString("TABLE_NAME"));
        }
        return arr;
    }

    /**
     * keyword数据
     * @param dbConnName
     * @param tableName
     * @return
     */
    public static List<Entity> keyword(final String dbConnName, final String tableName) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        //获取数据
        String sql=" select * from "+tableName;
        return dbUse.query(sql);
    }
    /**
     * 推荐结果查询
     *
     * @param dbConnName
     * @param tableName
     * @param pageNum
     * @param pageSize
     * @return
     * @throws SQLException
     */
    public static List<Entity> findRecommendedResult(final String dbConnName, final String tableName , int pageNum, int pageSize) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        String sql;
        sql="select clue_id,sign,case_list,lastmodified_time,similar,title from "+tableName +" where sign not in (select clue_id  from  "+tableName+" ) limit " +(pageNum-1)*pageSize+","+pageSize ;
        return  dbUse.query(sql);
    }

    /**
     * 获取总页数
     * @param dbConnName
     * @param tableName
     * @return
     * @throws SQLException
     */
    public static int totalCount(final String dbConnName, final String tableName) throws SQLException {
        final DataSource dsSource = GetMysqlConn.getConn(dbConnName);
        final Db dbUse = DbUtil.use(dsSource);
        String sql="select clue_id,sign from "+tableName +" where sign not in (select clue_id  from  "+tableName+")";
        return dbUse.query(sql).size();
    }
    public static void main(String[] args) throws SQLException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "123");
        StaticLog.info(JsonTool.formatJson(findFieldNamesOrderByIdDesc("group_db_target","tdkj_case_t",1,5,"id")));
        StaticLog.info(JsonTool.formatJson(findFieldNamesOrderByIdDesc("group_db_target","tdkj_case_t",2,5,"id")));
//		PageResult<Entity> a = findAllOrderByIdDesc("group_db_target","tdkj_case_t",1,5);
//		PageResult<Entity> b = findAllOrderByIdDesc("group_db_target","tdkj_case_t",2,5);
//		a.forEach(x -> System.out.println(x.getStr("id")));
//		b.forEach(x -> System.out.println(x.getStr("id")));
    }



}
