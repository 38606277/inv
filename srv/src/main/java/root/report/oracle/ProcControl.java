package root.report.oracle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import oracle.jdbc.OracleCallableStatement;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.db.DbFactory;
import root.report.util.JsonUtil;

import java.sql.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/proc")
public class ProcControl {

	// 得到存储过程的名称
	@RequestMapping(value = "/GetProcName", produces = "text/plain;charset=UTF-8")
	public String GetProcName() {

		List<Map> segments;
		try {
			segments = DbFactory.Open(DbFactory.SYSTEM).selectList("proc.GetProcName");
		} catch (Exception ex) {
			return ex.toString();
		}
		return JSON.toJSONString(segments);

	}

	private static SerializerFeature[] features = { SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue };

	// 得到存储过程的参数
	@RequestMapping(value = "/GetProcParam", produces = "text/plain;charset=UTF-8")
	public String GetProcParam(@RequestBody String pJson) {

		Map<String, String> pMap = (Map<String, String>) JSON.parse(pJson);

		List<Map> rList;
		try {
			rList = DbFactory.Open(DbFactory.SYSTEM).selectList("proc.GetProcParam", pMap);
		} catch (Exception ex) {
			return ex.toString();
		}
		return JSON.toJSONString(rList, features);
	}

	// private String Get(CallableStatement stmt) {
	//
	//
	// }
	// 执行存储过程返回游标
	@RequestMapping(value = "/ExecProc", produces = "text/plain;charset=UTF-8")
	public String ExecProc(@RequestBody String pJson) {

		String  ajson="";
		JSONObject jsonObject = (JSONObject) JSON.parse(pJson);
		String procName = jsonObject.getString("proc_name");
		JSONArray procParams = jsonObject.getJSONArray("params");
		StringBuilder marks = new StringBuilder();

		Connection conn = DbFactory.Open(DbFactory.SYSTEM).getConnection();
        int pSize=procParams.size();
		for (int i = 0; i < pSize; i++) {
			JSONObject aParam = (JSONObject) procParams.get(i);

			if (i != (pSize-1)) {
				marks.append("?,");
			} else {
				marks.append("?");
			}

		}

		try {

			CallableStatement stmt = conn.prepareCall("call " + procName + "(" + marks + ")");
            int iCursor=-1;
			for (int i = 0; i < procParams.size(); i++) {
				JSONObject aParam = (JSONObject) procParams.get(i);

				// aParam.getString("parameter_name");
				// aParam.getString("parameter_type");
				// aParam.getString("parameter_class");
				// aParam.getString("parameter_value");

				if (aParam.getString("parameter_class").equals("IN")) {

					if (aParam.getString("parameter_type").equals("VARCHAR2")) {
						stmt.setString(i+1, aParam.getString("parameter_value"));
					} else if (aParam.getString("parameter_type").equals("DATE")) {
						stmt.setDate(i+1, (Date) aParam.getDate("parameter_value"));
					} else if (aParam.getString("parameter_type").equals("NUMBER")) {
						stmt.setDouble(i+1,aParam.getDouble("parameter_value"));
					}
				} else if (aParam.getString("parameter_class").equals("OUT")) {

					if (aParam.getString("parameter_type").equals("VARCHAR2")) {
						stmt.registerOutParameter(i+1, oracle.jdbc.OracleTypes.VARCHAR);
					} else if (aParam.getString("parameter_type").equals("DATE")) {
						stmt.registerOutParameter(i+1, oracle.jdbc.OracleTypes.DATE);
					} else if (aParam.getString("parameter_type").equals("NUMBER")) {
						stmt.registerOutParameter(i+1, oracle.jdbc.OracleTypes.NUMBER);
					} else if (aParam.getString("parameter_type").equals("REF CURSOR")) {
						stmt.registerOutParameter(i+1, oracle.jdbc.OracleTypes.CURSOR);
						//标注此处用于取出cursor
						iCursor=i+1;
					}

				}

			}

			// //设置in参数
			// stmt.setString(4, "");
			// //设置out输出参数
			// stmt.registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR);

			stmt.executeUpdate();

			ResultSet rs = ((OracleCallableStatement) stmt).getCursor(iCursor);
			
			ajson=JsonUtil.resultSetToJson(rs);
			return ajson;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ajson;
		
	}

}
