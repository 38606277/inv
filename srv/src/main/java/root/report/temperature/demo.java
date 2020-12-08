package root.report.temperature;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import com.mysql.cj.x.json.JsonArray;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.configure.AppConstants;
import root.report.common.RO;
import root.report.db.DbFactory;

/**
 * Demo
 *
 * @author swh
 * @version 1.0
 */

@RestController
@RequestMapping("/reportServer/temp")
public class demo extends RO {

    @RequestMapping(value = "/getMax", produces = "text/plain;charset=UTF-8")
    public String getMax()
    {
        //发送 POST 请求
        String param = "username=admin&password=admin";
        //取得Key值
        String retMessage = http.sendGet("http://192.168.1.123/v1/user/login", param);
//        System.out.println(retMessage);
        JSONObject jsonMessage=JSONObject.parseObject(retMessage);
        String token=jsonMessage.getJSONObject("Data").getString("Token");
//        System.out.println(token);
        //取温度
        param="[{\"End\":{\"X\":8137,\"Y\":8139},\"Mode\":2,\"Start\":{\"X\":117,\"Y\":10}}]";
        String temptMessage = http.sendPost("http://192.168.1.123/v1/cmd07/allTempGet/GetTpcTemperatureArray",
                token,
                param);
        JSONObject jsonTemptMessage =  JSONObject.parseObject(temptMessage);
        JSONArray dataArray=jsonTemptMessage.getJSONArray("Data");
        String MaxTempt=dataArray.getJSONObject(0).getString("Max");
        //开氏转摄氏度
        Double tMaxTempt= Double.parseDouble(MaxTempt)/10-273.15;

        return new java.text.DecimalFormat("0.0").format(tMaxTempt);
    }



    @RequestMapping(value = "/add", produces = "text/plain;charset=UTF-8")
    public String add(@RequestBody String pJson) throws SQLException
    {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);

        try {
             this.createTemp(pJson,sqlSession);

        } catch (IOException e) {
            sqlSession.getConnection().rollback();
            e.printStackTrace();
            return ErrorMsg("","插入数据失败");
        }
        return SuccessMsg("插入数据成功","");
    }
    @RequestMapping(value = "/getAll", produces = "text/plain;charset=UTF-8")
    public String getAll() {
        List<Map<String, String>> listFunc = new ArrayList<>();
        try {
            listFunc = GetAllTemp();
            return SuccessMsg("", listFunc);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


    public List<Map<String, String>> GetAllTemp() {
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        resultList = sqlSession.selectList("temp.getAllTemp");
        return resultList;
    }



    public void createTemp(String pJson, SqlSession sqlSession) throws IOException {
        Map<String, Object> map = new HashMap<>();
        JSONObject jsonObject=JSONObject.parseObject(pJson);
        map.put("name", jsonObject.getString("name"));
        map.put("vist_date", jsonObject.getString("vist_date"));
        map.put("id_number", jsonObject.getString("id_number"));
        map.put("contact", jsonObject.getString("contact"));
        map.put("corp", jsonObject.getString("corp"));
        map.put("workplace", jsonObject.getString("workplace"));
        map.put("temp", jsonObject.getString("temp"));
        sqlSession.insert("temp.createTemp", map);
        return;
    }

//    public static void main(String[] args) {
//        //发送 POST 请求
//        String param = "username=admin&password=admin";
//        //取得Key值
//        String retMessage = http.sendGet("http://192.168.1.123/v1/user/login", param);
//        System.out.println(retMessage);
//        JSONObject jsonMessage=JSONObject.parseObject(retMessage);
//        String token=jsonMessage.getJSONObject("Data").getString("Token");
//        System.out.println(token);
//        //取温度
//        param="[{\"End\":{\"X\":8137,\"Y\":8139},\"Mode\":2,\"Start\":{\"X\":117,\"Y\":10}}]";
//        String temptMessage = http.sendPost("http://192.168.1.123/v1/cmd07/allTempGet/GetTpcTemperatureArray",
//                token,
//                param);
//        JSONObject jsonTemptMessage =  JSONObject.parseObject(temptMessage);
//        JSONArray dataArray=jsonTemptMessage.getJSONArray("Data");
//        String MaxTempt=dataArray.getJSONObject(0).getString("Max");
//        //开氏转摄氏度
//        Double tMaxTempt= Double.parseDouble(MaxTempt)/10-273.15;
//
//        System.out.println("体温："+ new java.text.DecimalFormat("0.0").format(tMaxTempt));
//
//
//    }
}
