package root.configure;

import java.io.File;
import java.net.InetAddress;

public class AppConstants {
    //    private String reportPath;//文件服务路径
//    private String staticReportPath;//静态报表路径
//    private String dynamicReportPath;//动态报表路径
//    private String excelFilePath;
//    private String mobileSubjectPath;//手机主题路径
//    private String templatePath;//报表Excel模板路径
//    private String fillTemplatePath;
//    private String userSqlPath;//SQL模板文件服务路径
//    private String userFunctionPath;
//    private String userDictionaryPath;
//    private String webServicePath;
//    private String mongoTemplate;//mongdb元数据保存路径
//    private String appFilePath;
//    private String lambdaUrl;//LambdaAddress计算地址
//    private String clientInstallFile;
//    private String report2;//前端路径
    private static String workPath = System.getProperty("user.dir") + File.separator + "work";
    private static String uploadPath = System.getProperty("user.dir") + File.separator + "upload";
    private static String appPath = System.getProperty("user.dir") + File.separator + "app";

    public static String getReportPath() {
        return workPath + File.separator + "report";
    }

    public static String getUploadPath() {
        return uploadPath;
    }
    public static String getStaticReportPath() {
        return workPath + File.separator + "webpage" + File.separator + "staticReport";
    }

    public static String getDynamicReportPath() {
        return workPath + File.separator + "webpage" + File.separator + "dynamicReport";
    }

    public static String getExcelFilePath() {
        return workPath + File.separator + "excel";
    }

    public static String getAppFilePath() {
        return workPath + File.separator + "webpage" + File.separator + "mobile" + File.separator + "app";
    }

    public static String getMobileSubjectPath() {
        return workPath + File.separator + "webpage" + File.separator + "mobile";
    }

    public static String getTemplatePath() {
        return workPath + File.separator + "template";
    }

    public static String getFillTemplatePath() {
        return workPath + File.separator + "filltemplate";
    }

    public static String getUserSqlPath() {
        return workPath + File.separator + "dbtemplate" + File.separator + "query";
    }

    public static String getUserFunctionPath() {
        return workPath + File.separator + "dbtemplate" + File.separator + "function";
    }

    public static String getUserDictionaryPath() {
        return workPath + File.separator + "dbtemplate" + File.separator + "dictionary";
    }

    public static String getWebServicePath() {
        return workPath + File.separator + "dbtemplate" + File.separator + "webservice";
    }

    public static String getMongoTemplate() {
        return workPath + File.separator + "mongotemplate";
    }

    public static String getLambdaUrl() {
        String ip = "127.0.0.1";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "http://" + ip + ":8090/lambda";
    }

    public static String getClientInstallFile() {
        return appPath + File.separator + "excel";
    }

    public static String getReport2() {
        return appPath + File.separator + "web";
    }
    public static String getPhoneapp() {
        return appPath + File.separator + "app";
    }

    // function 默认文件前缀名称
    public static String FunctionPrefix = "func_";

    // query 默认文件前缀名称
    public static String QueryPrefix = "qry_";
}
