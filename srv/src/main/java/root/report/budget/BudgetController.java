package root.report.budget;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import root.configure.AppConstants;
import root.form.user.UserModel;
import root.report.budget.bean.BudgetAccount;
import root.report.db.DbFactory;
import root.report.query.SelectControl;
import root.report.sys.SysContext;
import root.report.util.DateUtil;
import root.report.util.StringUtil;
import root.report.util.budget.DESUtil;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/reportServer/budget")
public class BudgetController {
	private SimpleDateFormat sp = new SimpleDateFormat("yyMMddHH24mmss");
	@Autowired
	private SelectControl selectControl;
	@RequestMapping(value = "/getBudgetDetail/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
	public String getBudgetClass(@PathVariable("namespace") String namespace,@PathVariable("sqlid") String sqlid,
								 @PathVariable("type") String type,@RequestBody JSONObject pJson) {
		StringBuilder sb = new StringBuilder();
		Map<String,Object> tableMap = new HashMap<String,Object>();
		String tableName = "budget_account_temp_"+sp.format(new Date());
		tableMap.put("tableName", tableName);
		try{
			Map<String,Object> param1 = prepareParameter(pJson);
			JSONObject param = JSONObject.parseObject(JSONObject.toJSONString(param1));
			JSONArray budgetListTemp = JSONObject.parseObject(getReportDetail(namespace,sqlid,type,param)).getJSONArray("list");
			List<BudgetAccount> budgetList = convertJSONToObject(budgetListTemp);
			if(budgetList.size()>0){
				//动态创建临时表
				DbFactory.Open(DbFactory.FORM).update("budget.createBudgetAccountTempTable",tableMap);
				//将数据导入到mysql临时表
				Map<String,Object> inserteMap = new HashMap<String,Object>();
				inserteMap.put("list", budgetList);
				inserteMap.put("tableName", tableName);
				DbFactory.Open(DbFactory.FORM).insert("budget.batchInsertBudgetAccountInfo", inserteMap);
				int fistCodeLength = budgetList.get(0).getBudget_account_code().length();
				for(int j=14;j-2>=fistCodeLength;j=j-2)
				{
					//将数据进行层级汇总
					Map<String,Object> accumulateMap = new HashMap<String,Object>();
					accumulateMap.put("tableName", tableName);
					accumulateMap.put("budget_account_code_length", j);
					accumulateMap.put("parent_budget_account_code_length", j-2);
					DbFactory.Open(DbFactory.FORM).update("budget.accumulateBudgetAccountAmount",accumulateMap);
				}
				List<Map<String,Object>> budget = DbFactory.Open(DbFactory.FORM).selectList("budget.getBudgetAccountDetail",tableMap);
				//删除临时表
				DbFactory.Open(DbFactory.FORM).selectList("budget.dropBudgetTable",tableMap);
				Map<String,List<Map<String,Object>>> company_budget = converIntoCompanyBudget(budget);
				Set<String> keySet = company_budget.keySet();
				List<Map<String,Object>> companyBudgetList = null;
				int k = 1;//树的起始index
				Stack<Integer> parentId = new Stack<Integer>();
				for(String company_name:keySet){
					int size = parentId.size();
					batchPopStack(parentId,size);
					parentId.push(k);
					companyBudgetList = company_budget.get(company_name);
					Map<String, Object> companySummary = companyBudgetList.get(0);
					sb.append("<tr class=\"treegrid-"+k+"\">"
							+ "<td>"+company_name+"</td>"
							+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("TRANSMIT_BUDGET_AMOUNT").toString())+"</td>"
							+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("APPROVED_BUDGET_AMOUNT").toString())+"</td>"
							+ "<td style='text-align: right'>"+companySummary.get("APPROVED_PRO")+"</td>"
							+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("ACCOUNT_AMOUNT").toString())+"</td>"
							+ "<td style='text-align: right'>"+companySummary.get("ACCOUNT_PRO")+"</td>"
							+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("CLAIM_AMOUNT").toString())+"</td>"
							+ "<td style='text-align: right'>"+companySummary.get("BUDGET_PRO")+"</td></tr>");
					Map<String,Object> map = null;
					for (int i = 1; i <= companyBudgetList.size(); i++) {
						size = parentId.size();
						map = companyBudgetList.get(i-1);
						String budget_account_name = (String)map.get("BUDGET_ACCOUNT_NAME");
						String budget_account_code = (String)map.get("BUDGET_ACCOUNT_CODE");
						BigDecimal tramsit_budget_amount = (BigDecimal)map.get("TRANSMIT_BUDGET_AMOUNT");
						BigDecimal approved_budget_amount = (BigDecimal)map.get("APPROVED_BUDGET_AMOUNT");
						String approved_pro = (String)map.get("APPROVED_PRO");
						BigDecimal account_amount = (BigDecimal)map.get("ACCOUNT_AMOUNT");
						String account_pro = (String)map.get("ACCOUNT_PRO");
						BigDecimal claim_amount = (BigDecimal)map.get("CLAIM_AMOUNT");
						String budget_pro = (String)map.get("BUDGET_PRO");
						int offset = 0;//偏移量
						if(budget_account_code.length()==fistCodeLength){
							batchPopStack(parentId,size-1);
							sb.append("<tr class=\"treegrid-"+(k+i)+" treegrid-parent-"+parentId.lastElement()+"\">");
						}else{
							offset = (budget_account_code.length()-fistCodeLength)/2;
							if(size==offset){
								parentId.push(k+i-1);
							}else if(size!=offset){
								batchPopStack(parentId,size-offset-1);
							}
							sb.append("<tr class=\"treegrid-"+(k+i)+" treegrid-parent-"+parentId.lastElement()+"\">");
						}
						//sb.append("<td>"+budget_account_name+" "+budget_account_code+"</td>"
						sb.append("<td>"+budget_account_name+"</td>"
								+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(tramsit_budget_amount.toString())+"</td>"
								+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(approved_budget_amount.toString())+"</td>"
								+ "<td style='text-align: right'>"+approved_pro+"</td>"
								+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(account_amount.toString())+"</td>"
								+ "<td style='text-align: right'>"+account_pro+"</td>"
								+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(claim_amount.toString())+"</td>"
								+ "<td style='text-align: right'>"+budget_pro+"</td>");
						sb.append("</tr>");
					}
					k+=companyBudgetList.size()+1;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			//删除临时表
			DbFactory.Open(DbFactory.FORM).selectList("budget.dropBudgetTable",tableMap);
		}
		return sb.toString();
	}

	private  List<BudgetAccount> convertJSONToObject(JSONArray budgetAccoutListTemp){
		List<BudgetAccount> budgetAccountList = new ArrayList<BudgetAccount>();
		BudgetAccount budgetAccount = null;
		JSONObject obj = null;
		for(int i=0;i<budgetAccoutListTemp.size();i++){
			obj = budgetAccoutListTemp.getJSONObject(i);
			budgetAccount = new BudgetAccount();
			budgetAccount.setCompany_id(obj.get("COMPANY_ID").toString());
			budgetAccount.setCompany_name(obj.get("COMPANY_NAME").toString());
			budgetAccount.setBudget_account_name(obj.get("BUDGET_ACCOUNT_NAME").toString());
			budgetAccount.setBudget_account_code(obj.get("BUDGET_ACCOUNT_CODE").toString());
			budgetAccount.setTransmit_budget_amount((BigDecimal)obj.get("TRANSMIT_BUDGET_AMOUNT"));
			budgetAccount.setApproved_budget_amount((BigDecimal)obj.get("APPROVED_BUDGET_AMOUNT"));
			budgetAccount.setAccount_amount((BigDecimal)obj.get("ACCOUNT_AMOUNT"));
			budgetAccount.setClaim_amount((BigDecimal)obj.get("CLAIM_AMOUNT"));
			budgetAccountList.add(budgetAccount);
		}
		return budgetAccountList;
	}

	@RequestMapping(value = "/getNetWorkFee/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
	public String getNetWorFee(@PathVariable("namespace") String namespace,@PathVariable("sqlid") String sqlid,
							   @PathVariable("type") String type,@RequestBody JSONObject pJson) {
		StringBuilder sb = new StringBuilder();
		JSONArray budgetListTemp = JSONObject.parseObject(getReportDetail(namespace,sqlid,type,pJson)).getJSONArray("list");
		List<Map<String,Object>> list = convertJSONToList(budgetListTemp);
		Map<String,List<Map<String,Object>>> company_budget = converIntoCompanyBudget(list);
		List<Map<String,Object>> companyBudgetList = null;
		Set<String> keySet = company_budget.keySet();
		int k = 1;//树的起始index
		Stack<Integer> parentId = new Stack<Integer>();
		int fistCodeLength = 0;
		for(String company_name:keySet){
			int size = parentId.size();
			batchPopStack(parentId,size);
			parentId.push(k);
			companyBudgetList = company_budget.get(company_name);
			Map<String, Object> companySummary = companyBudgetList.get(0);
			sb.append("<tr class=\"treegrid-"+k+"\">"
					+ "<td>"+company_name+"</td>"
					+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("TRANSMIT_BUDGET_AMOUNT").toString())+"</td>"
					+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("APPROVED_BUDGET_AMOUNT").toString())+"</td>"
					+ "<td style='text-align: right'>"+companySummary.get("APPROVED_PRO")+"%</td>"
					+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(companySummary.get("OCCUPIED_BUDGET_AMOUNT_SUM").toString())+"</td>"
					+ "<td style='text-align: right'>"+companySummary.get("OCCUPIED_PRO")+"%</td></tr>");
			Map<String,Object> map = null;
			for (int i = 1; i <= companyBudgetList.size(); i++) {
				size = parentId.size();
				map = companyBudgetList.get(i-1);
				if(i==1){
					fistCodeLength = map.get("BUDGET_ACCOUNT_CODE").toString().length();
				}
				String budget_account_code = (String)map.get("BUDGET_ACCOUNT_CODE");
				String budget_account_name = (String)map.get("BUDGET_ACCOUNT_NAME");
				BigDecimal transmit_budget_amount = (BigDecimal)map.get("TRANSMIT_BUDGET_AMOUNT");
				BigDecimal approved_budget_amount = (BigDecimal)map.get("APPROVED_BUDGET_AMOUNT");
				BigDecimal approved_pro=(BigDecimal)map.get("APPROVED_PRO");
				BigDecimal occupied_budget_amount_sum = (BigDecimal)map.get("OCCUPIED_BUDGET_AMOUNT_SUM");
				BigDecimal occupied_pro=(BigDecimal)map.get("OCCUPIED_PRO");
				int offset = 0;//偏移量
				if(budget_account_code.length()==fistCodeLength){
					batchPopStack(parentId,size-1);
					sb.append("<tr class=\"treegrid-"+(k+i)+" treegrid-parent-"+parentId.lastElement()+"\">");
				}else{
					offset = (budget_account_code.length()-fistCodeLength)/2;
					if(size==offset){
						parentId.push(k+i-1);
					}else if(size!=offset){
						batchPopStack(parentId,size-offset-1);
					}
					sb.append("<tr class=\"treegrid-"+(k+i)+" treegrid-parent-"+parentId.lastElement()+"\">");
				}
				sb.append("<td>"+budget_account_name+" "+budget_account_code+"</td>"
						+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(transmit_budget_amount.toString())+"</td>"
						+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(approved_budget_amount.toString())+"</td>"
						+ "<td style='text-align: right'>"+approved_pro+"%</td>"
						+ "<td style='text-align: right'>"+StringUtil.fmtMicrometer(occupied_budget_amount_sum.toString())+"</td>"
						+ "<td style='text-align: right'>"+occupied_pro+"%</td>");
				sb.append("</tr>");
			}
			k+=companyBudgetList.size()+1;
		}
		
		return sb.toString();
	}

	private List<Map<String,Object>> convertJSONToList(JSONArray budgetListTemp){
		List<Map<String,Object>> budgetList = new ArrayList<Map<String,Object>>();
		Map<String,Object> map = null;
		JSONObject obj = null;
		for(int i=0;i<budgetListTemp.size();i++){
			obj = budgetListTemp.getJSONObject(i);
			map = new HashMap<String,Object>();
			map.put("COMPANY_ID",obj.get("COMPANY_ID").toString());
			map.put("COMPANY_NAME",obj.get("COMPANY_NAME").toString());
			map.put("BUDGET_ACCOUNT_NAME",obj.get("BUDGET_ACCOUNT_NAME").toString());
			map.put("BUDGET_ACCOUNT_CODE",obj.get("BUDGET_ACCOUNT_CODE").toString());
			map.put("TRANSMIT_BUDGET_AMOUNT",obj.get("TRANSMIT_BUDGET_AMOUNT"));
			map.put("APPROVED_BUDGET_AMOUNT",obj.get("APPROVED_BUDGET_AMOUNT"));
			map.put("APPROVED_PRO",obj.get("APPROVED_PRO"));
			map.put("OCCUPIED_BUDGET_AMOUNT_SUM",obj.get("OCCUPIED_BUDGET_AMOUNT_SUM"));
			map.put("OCCUPIED_PRO",obj.get("OCCUPIED_PRO"));
			budgetList.add(map);
		}
		return budgetList;
	}
	@RequestMapping(value = "/exportBudgetDetail/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
	public String exportBudgetDetail(@PathVariable("namespace") String namespace,@PathVariable("sqlid") String sqlid,
									 @PathVariable("type") String type,@RequestBody JSONObject pJson) {
		Map<String,Object> tableMap = new HashMap<String,Object>();
		String tableName = "budget_account_temp_"+sp.format(new Date());
		tableMap.put("tableName", tableName);
		List<Map<String,Object>> list = null;
		try{
			Map<String,Object> param1 = prepareParameter(pJson);
			JSONObject param = JSONObject.parseObject(JSONObject.toJSONString(param1));
			JSONArray budgetListTemp = JSONObject.parseObject(getReportDetail(namespace,sqlid,type,param)).getJSONArray("list");
			List<BudgetAccount> budgetList = convertJSONToObject(budgetListTemp);
			//动态创建临时表
			DbFactory.Open(DbFactory.FORM).update("budget.createBudgetAccountTempTable",tableMap);
			//将数据导入到mysql临时表
			Map<String,Object> inserteMap = new HashMap<String,Object>();
			inserteMap.put("list", budgetList);
			inserteMap.put("tableName", tableName);
			DbFactory.Open(DbFactory.FORM).insert("budget.batchInsertBudgetAccountInfo", inserteMap);
			int fistCodeLength = budgetList.get(0).getBudget_account_code().length();
			for(int j=14;j-2>=fistCodeLength;j=j-2)
			{
				//将数据进行层级汇总
				Map<String,Object> accumulateMap = new HashMap<String,Object>();
				accumulateMap.put("tableName", tableName);
				accumulateMap.put("budget_account_code_length", j);
				accumulateMap.put("parent_budget_account_code_length", j-2);
				DbFactory.Open(DbFactory.FORM).update("budget.accumulateBudgetAccountAmount",accumulateMap);
			}
			list = DbFactory.Open(DbFactory.FORM).selectList("budget.getBudgetAccountDetail",tableMap);
			//删除临时表
			DbFactory.Open(DbFactory.FORM).selectList("budget.dropBudgetTable",tableMap);
        }catch(Exception e){
            e.printStackTrace();
            //删除临时表
			DbFactory.Open(DbFactory.FORM).selectList("budget.dropBudgetTable",tableMap);
        }
		return JSON.toJSONString(list);
	}
	
	@RequestMapping(value = "/exportNetWorkFee/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
	public String exportNetWorFee(@PathVariable("namespace") String namespace,@PathVariable("sqlid") String sqlid,
								  @PathVariable("type") String type,@RequestBody JSONObject pJson) {
		return getReportDetail(namespace,sqlid,type,pJson);
	}
	
	private Map<String,List<Map<String,Object>>> converIntoCompanyBudget(List<Map<String,Object>> budget){
		Map<String,List<Map<String,Object>>> companyBudget = new HashMap<String,List<Map<String,Object>>>();
		for(Map<String,Object> temp:budget){
			String companyName = (String)temp.get("COMPANY_NAME");
			if(!companyBudget.containsKey(companyName)){
				companyBudget.put(companyName, new ArrayList<Map<String,Object>>());
			}
			companyBudget.get(companyName).add(temp);
		}
		return companyBudget;
	}
	
	//批量移除栈元素
	private void batchPopStack(Stack<Integer> stack, int num){
		if(num!=0){
			for (int i = 0; i < num; i++) {
				stack.pop();
			}
		}
	}
	
	private Map<String,Object> prepareParameter(JSONObject obj){
		Map<String,Object> param = new HashMap<String,Object>();
		try{
			Integer BUDGET_YEAR = obj.getInteger("budget_year");
			String BUDGET_ACCOUNT_CODE = obj.getString("budget_account_code");
			int MONTH = obj.getInteger("budget_month");//前台查询月份
			Calendar ca = Calendar.getInstance();
			int year = ca.get(Calendar.YEAR);
			int month = ca.get(Calendar.MONTH) + 1;//实际月份
			int day = ca.get(Calendar.DATE);
			DecimalFormat df=new DecimalFormat("00");
			param.put("budget_year", BUDGET_YEAR);
			param.put("period_name", DateUtil.getMonthAbbr(MONTH)+"-"+obj.getString("budget_year").substring(2));
			param.put("budget_account_code", BUDGET_ACCOUNT_CODE);
			String claim_last_update_time_start = "";
			String claim_last_update_time_end = "";
			String reward_last_update_time_start = "";
			String reward_last_update_time_end = "";
			if(Integer.valueOf(BUDGET_YEAR)==year&&MONTH==month){
				if(month==1){
					if(day<10){
						claim_last_update_time_start = BUDGET_YEAR-1 +"-12-10 00:00:00";
						claim_last_update_time_end = BUDGET_YEAR+"-01-05 23:59:59";
					}else{
						claim_last_update_time_start = BUDGET_YEAR+"-01-10 00:00:00";
						claim_last_update_time_end = BUDGET_YEAR+"-02-05 23:59:59";
					}
				}else if(month==12){
					if(day<10){
						claim_last_update_time_start = BUDGET_YEAR+"-11-10 00:00:00";
						claim_last_update_time_end = BUDGET_YEAR+"-12-05 23:59:59";
					}else{
						claim_last_update_time_start = BUDGET_YEAR+"-12-10 00:00:00";
						claim_last_update_time_end = BUDGET_YEAR+1+"-01-05 23:59:59";
					}
				}else{
					if(day<10){
						claim_last_update_time_start = BUDGET_YEAR+"-"+df.format(month-1)+"-10 00:00:00";
						claim_last_update_time_end = BUDGET_YEAR+"-"+df.format(month)+"-05 23:59:59";
					}else{
						claim_last_update_time_start = BUDGET_YEAR+"-"+df.format(month)+"-10 00:00:00";
						claim_last_update_time_end = BUDGET_YEAR+"-"+df.format(month+1)+"-05 23:59:59";
					}
				}
			}else{
				claim_last_update_time_start = BUDGET_YEAR+"-"+DateUtil.getEnMonth(MONTH)+"-10 00:00:00";
				if(MONTH==12){
					claim_last_update_time_end = BUDGET_YEAR+1+"-"+DateUtil.getNextEnMonth(MONTH)+"-05 23:59:59";
				}else{
					claim_last_update_time_end = BUDGET_YEAR+"-"+DateUtil.getNextEnMonth(MONTH)+"-05 23:59:59";
				}
			}
			SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String nowDate = df1.format(ca.getTime());
			Date nowDate1 = df1.parse(nowDate);
			Date date1 = df1.parse(BUDGET_YEAR+"-01-15 00:00:00");
			Date date2 = df1.parse(BUDGET_YEAR+"-05-15 00:00:00");
			Date date3 = df1.parse(BUDGET_YEAR+"-12-15 00:00:00");
			if(Integer.valueOf(BUDGET_YEAR)==year&&MONTH==month){
				if(1<=month&&month<=5){
					if(nowDate1.before(date1)){
						reward_last_update_time_start = BUDGET_YEAR-1+"-12-15 00:00:00";
						reward_last_update_time_end = BUDGET_YEAR+"-01-5 23:59:59";
					}else{
						reward_last_update_time_start = BUDGET_YEAR+"-01-15 00:00:00";
						reward_last_update_time_end = BUDGET_YEAR+"-02-05 23:59:59";
					}
				}else if(6<=month&&month<=11){
					if(nowDate1.before(date2)){
						reward_last_update_time_start = BUDGET_YEAR+"-01-15 00:00:00";
						reward_last_update_time_end = BUDGET_YEAR+"-02-05 23:59:59";
					}else{
						reward_last_update_time_start = BUDGET_YEAR+"-05-15 00:00:00";
						reward_last_update_time_end = BUDGET_YEAR+"-06-05 23:59:59";
					}
				}else if(month==12){
					if(nowDate1.before(date3)){
						reward_last_update_time_start = BUDGET_YEAR+"-05-15 00:00:00";
						reward_last_update_time_end = BUDGET_YEAR+"-06-05 23:59:59";
					}else{
						reward_last_update_time_start = BUDGET_YEAR+"-12-15 00:00:00";
						reward_last_update_time_end = BUDGET_YEAR+1+"-01-05 23:59:59";
					}
				}
			}else{
				if(MONTH>=1&&MONTH<=5){
					reward_last_update_time_start = BUDGET_YEAR+"-01-15 00:00:00";
					reward_last_update_time_end = BUDGET_YEAR+"-02-05 23:59:59";
				}else if(MONTH>=6&&MONTH<=11){
					reward_last_update_time_start = BUDGET_YEAR+"-05-15 00:00:00";
					reward_last_update_time_end = BUDGET_YEAR+"-06-05 23:59:59";
				}else if(MONTH==12){
					reward_last_update_time_start = BUDGET_YEAR+"-12-15 00:00:00";
					reward_last_update_time_end = BUDGET_YEAR+1+"-01-05 23:59:59";
				}
			}
			param.put("claim_last_update_time_start", claim_last_update_time_start);
			param.put("claim_last_update_time_end", claim_last_update_time_end);
			param.put("reward_last_update_time_start", reward_last_update_time_start);
			param.put("reward_last_update_time_end", reward_last_update_time_end);
			param.put("companycodes",obj.get("companycodes"));
			param.put("departmentids",obj.get("departmentids"));
		}catch(Exception e){
			e.printStackTrace();
		}
		return param;
	}

	@RequestMapping(value="/getCompanyByDicItem",produces = "text/plain;charset=UTF-8")
	public String getCompanyByDicItem(){
		JSONObject result = new JSONObject();
		UserModel user = SysContext.getRequestUser();
		String userName = user.getUserName();
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("userName", userName);
		//如果用户最大permisssion是P,则默认给予所有公司查询权限,否则需要赋权才能查看
		String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
		List<Map> companyList = new ArrayList<Map>();
		if(userPermission!=null&&userPermission.equals("P")){
			List<Map<String, String>> userCompanyList = DbFactory.Open(DbFactory.BUDGET).selectList("cache.getAllCompanies");
			Map<String, Object> map = null;
			for (Map<String, String> temp : userCompanyList) {
				map = new HashMap<String, Object>();
				map.put("value", temp.get("COMPANY_CODE"));
				map.put("name", temp.get("COMPANY_NAME"));
				companyList.add(map);
			}
		}else{
			Map<String, String> map = new HashMap<String,String>();
			map.put("userName", user.getUserName());
			map.put("type", "budget_account_ou");
			List<Map<String,String>> list = DbFactory.Open(DbFactory.FORM).selectList("rule.getAuthListByConditions", map);
			if(list.size()>0){
				Map<String, Object> temp = null;
				for (int i = 0; i < list.size(); i++) {
					paramMap.put("company_code", Integer.valueOf(list.get(i).get("funcId")));
					Map<String, Object> company = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getCompanyByCode", paramMap);
					temp = new HashMap<String, Object>();
					temp.put("name", company.get("COMPANY_NAME"));
					temp.put("value", company.get("COMPANY_CODE"));
					companyList.add(temp);
				}
			}
		}
		result.put("companys", companyList);
		return result.toJSONString();
	}

	@RequestMapping(value="/getCompanyAndDepartmentByPermission",produces = "text/plain;charset=UTF-8")
    public String getCompanyAndDepartmentByPermission(){
		JSONObject result = new JSONObject();
        UserModel user = SysContext.getRequestUser();
        String userName = user.getUserName();
        Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("userName", userName);
		String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
		List<Map> companyList = new ArrayList<Map>();
		List<Map> departmentList = new ArrayList<Map>();
		List<Map<String,String>> userCompanyList = null;
		String org_code = null;
		//字典表配置的是特殊情形的账号权限数据
		Map<String,String> kindItem = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKindItem", paramMap);
		if(kindItem != null) {
			String detail = (String) kindItem.get("DETAIL");
			if("ALL".equals(detail)){
				userCompanyList = DbFactory.Open(DbFactory.BUDGET).selectList("cache.getAllCompanies");
				Map<String,Object> map = null;
				for(Map<String,String> temp:userCompanyList){
					map = new HashMap<String,Object>();
					map.put("value", temp.get("COMPANY_CODE"));
					map.put("name", temp.get("COMPANY_NAME"));
					companyList.add(map);
				}
				departmentList = this.getDepartmentByPermission("P",org_code,companyList);
			}else{
				String[] companyIds = detail.split(",");
				Map<String,Object> temp = null;
				for(int i = 0;i < companyIds.length;i++){
					paramMap.put("company_id", Integer.valueOf(companyIds[i]));
					Map<String,Object> company = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getCompanyById", paramMap);
					temp = new HashMap<String,Object>();
					temp.put("name", company.get("COMPANY_NAME"));
					temp.put("value", company.get("COMPANY_CODE"));
					companyList.add(temp);
				}
				departmentList = this.getDepartmentByPermission("C",org_code,companyList);
			}
		}else {
			if (userPermission != null && userPermission.equals("P")) {
				userCompanyList = DbFactory.Open(DbFactory.BUDGET).selectList("cache.getAllCompanies");
			} else {
				org_code = DbFactory.Open(DbFactory.FORM).selectOne("oa.getUserDepartmentNo", paramMap);
				if (org_code != null) {
					userCompanyList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getUserCompany", org_code);
				}
			}
			Map<String,Object> map = null;
			for(Map<String,String> temp:userCompanyList){
				map = new HashMap<String,Object>();
				map.put("value", temp.get("COMPANY_CODE"));
				map.put("name", temp.get("COMPANY_NAME"));
				companyList.add(map);
			}
			departmentList = this.getDepartmentByPermission(userPermission,org_code,companyList);
		}

		result.put("companys", companyList);
		result.put("departments", departmentList);
		return result.toJSONString();
    }
	
	private List<Map> getDepartmentByPermission(String userPermission,String org_code,List<Map> companyList ){
		List<Map> departmentList = new ArrayList<Map>();
		List<Map> userDepartmentList = new ArrayList<Map>();
		Map<String,String> paramMap = new HashMap<String,String>();
		if(userPermission!=null&&(userPermission.equals("P")||userPermission.equals("C"))) {
			String companyCodes = "";
			for (int i = 0; i < companyList.size(); i++) {
				String company_code = companyList.get(i).get("value").toString();
				if (i == companyList.size() - 1) {
					companyCodes = companyCodes + "'" + company_code + "'";
				} else {
					companyCodes = companyCodes + "'" + company_code + "',";
				}
			}
			paramMap.put("companyCodes", companyCodes);
			userDepartmentList = DbFactory.Open(DbFactory.BUDGET).selectList("cache.getDepartmentListByConmpanyCodes", paramMap);
		}else{
			//首先通过角色获取用户有权限查询的部门
			paramMap.put("userName",SysContext.getRequestUser().getUserName());
			paramMap.put("org_code",org_code);
			userDepartmentList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getDepartmentInfoByRole",paramMap);
			//其次通过OA查询其所属部门
			if(userDepartmentList.size()==0&&org_code!=null){
				userDepartmentList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getDepartmentInfo",org_code);
			}
		}

		Map<String, Object> map = null;
		for (Map<String, Object> temp : userDepartmentList) {
			map = new HashMap<String, Object>();
			map.put("value", temp.get("VALUE"));
			map.put("name", temp.get("NAME"));
			departmentList.add(map);
		}
		return departmentList;
	}
	
	@RequestMapping(value="/getDepartmentByCompanyCode",produces = "text/plain;charset=UTF-8")
    public String getDepartmentByCompanyCode(@RequestBody JSONObject pJson){
		String companyCode = pJson.getString("companyCode");
		List<Map> departmentList = new ArrayList<Map>();
		List<Map> userDepartmentList = new ArrayList<Map>();
		UserModel user = SysContext.getRequestUser();
        String userName = user.getUserName();
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("userName", userName);
		String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
		//根据角色对应的PCD来角色能查询的部门信息,P省公司可以查询所有公司和部门权限,C地市公司和部门权限,D所属公司的部门权限
		if(userPermission!=null && !userPermission.isEmpty()){
			if("P".equals(userPermission)||"C".equals(userPermission)){
				Map<String,String> departmentParamMap = new HashMap<String,String>();
				departmentParamMap.put("companyCodes", "'"+companyCode+"'");
				userDepartmentList = DbFactory.Open(DbFactory.BUDGET).selectList("cache.getDepartmentListByConmpanyCodes",departmentParamMap);
			}else{
				String org_code = DbFactory.Open(DbFactory.FORM).selectOne("oa.getUserDepartmentNo",paramMap);
				//首先通过角色获取用户有权限查询的部门
				paramMap.put("org_code",org_code);
				userDepartmentList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getDepartmentInfoByRole",paramMap);
				//其次通过OA查询其所属部门
				if(userDepartmentList.size()==0&&org_code!=null){
					userDepartmentList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getDepartmentInfo",org_code);
				}
			}
		}
		Map<String,Object> map = null;
		for(Map<String,Object> temp:userDepartmentList){
			map = new HashMap<String,Object>();
			map.put("value", temp.get("VALUE"));
			map.put("name", temp.get("NAME"));
			departmentList.add(map);
		}
		return JSONObject.toJSONString(departmentList);
	}
	
	@RequestMapping(value="/getBudgetAccountCode",produces = "text/plain;charset=UTF-8")
    public String getBudgetAccountCode(){
		List<Map> authList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getBudgetAccountCode");
		JSONArray result = new JSONArray();
		JSONObject temp = null;
		for(Map map:authList){
			temp = new JSONObject();
			temp.put("budgetAccountCode", map.get("BUDGET_ACCOUNT_CODE"));
			temp.put("budgetAccountName", map.get("BUDGET_ACCOUNT_NAME"));
			result.add(temp);
		}
		return result.toJSONString();
	}
	
	private void prepareCompanyIds(Map<String,Object> param,JSONObject pJson)
	{
        StringBuilder sb = new StringBuilder();
        JSONArray companycodes = pJson.getJSONArray("companycodes");
        if(companycodes!=null){
        	if(companycodes.size()>0){
		        for (int i=0;i<companycodes.size();i++)
		        {
		            sb.append("'"+companycodes.getJSONObject(i).get("value")+"',");
		        }
		        param.put("companycodes", sb.substring(0, sb.length()-1));
        	}else{
        		param.put("companycodes", null);
        	}
        }else{
        	param.put("companycodes",null);
        }
        sb = new StringBuilder();
        JSONArray departmentids = pJson.getJSONArray("departmentids");
        if(departmentids!=null){
        	if(departmentids.size()>0)
        	{
		        for (int i=0;i<departmentids.size();i++)
		        {
		            sb.append(departmentids.getJSONObject(i).get("value")+",");
		        }
		        param.put("departmentids", sb.substring(0, sb.length()-1));
        	}else{
        		param.put("departmentids", null);
        	}
        }else{
        	param.put("departmentids",null);
        }
	}
	
	private JSONArray prepareQueryParam(Map<String,Object> param)
	{
		JSONArray paramArr = new JSONArray();
	    Set<String> keys = param.keySet();
	    JSONObject obj = null;
	    for (String key:keys) {
	    	obj = new JSONObject();
	    	obj.put("id", key);
	    	obj.put("value", param.get(key));
	    	paramArr.add(obj);
		}
		return paramArr;
	}
	
	@RequestMapping(value = "/report/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
	public String getReportDetail(@PathVariable("namespace") String namespace,
			@PathVariable("sqlid") String sqlid,@PathVariable("type") String type,@RequestBody JSONObject pJson) {
		JSONObject obj = new JSONObject();
        obj.put("namespace", namespace);
        obj.put("sqlid", sqlid);
        //获取sql查询所需的数据库
        JSONObject sqlObj = JSON.parseObject(selectControl.qrySelectSqlDetail(obj.toJSONString()));
        JSONObject commentObj = sqlObj.getJSONObject("comment");
        String db = commentObj.getString("db");
        //构造查询参数
        JSONArray queryparam = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("db", db);
        Map<String,Object> param = new HashMap<String,Object>();
		param.put("budget_year", pJson.getIntValue("budget_year"));
		param.put("company_code", pJson.getString("company_code"));
		param.put("department_id", pJson.getIntValue("department_id"));
		param.put("period_name", pJson.getString("period_name"));
		param.put("claim_last_update_time_start", pJson.getString("claim_last_update_time_start"));
		param.put("claim_last_update_time_end", pJson.getString("claim_last_update_time_end"));
		param.put("reward_last_update_time_start", pJson.getString("reward_last_update_time_start"));
		param.put("reward_last_update_time_end", pJson.getString("reward_last_update_time_end"));
		param.put("budget_account_code", pJson.getString("budget_account_code"));
		param.put("budget_account_id", pJson.getIntValue("budget_account_id"));
		param.put("accounting_subject_name", pJson.getString("accounting_subject_name"));
		param.put("static_budget_account_name", pJson.getString("static_budget_account_name"));
		param.put("user_permission_code", this.getUserPermissionCode());
		param.put("user_name", SysContext.getRequestUser().getUserName().toLowerCase());
		prepareCompanyIds(param,pJson);
		//将Map参数转换为通用查询所需的参数形式
		JSONArray paramArr = prepareQueryParam(param);
        paramObj.put("in",paramArr);
        queryparam.add(paramObj);
        if(type.equals("query")){
        	queryparam.add(pJson.getJSONObject("page"));
        }
        return JSONObject.parseObject(selectControl.execSelect(namespace,sqlid,queryparam.toJSONString())).getJSONObject("data").toJSONString();
	}
	
	@RequestMapping(value = "/getOrgList/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
    public String getOrgList(@PathVariable("namespace") String namespace,
            @PathVariable("sqlid") String sqlid,@PathVariable("type") String type,@RequestBody JSONObject pJson) {
        JSONObject obj = new JSONObject();
        obj.put("namespace", namespace);
        obj.put("sqlid", sqlid);
        //获取sql查询所需的数据库
        JSONObject sqlObj = JSON.parseObject(selectControl.qrySelectSqlDetail(obj.toJSONString()));
        JSONObject commentObj = sqlObj.getJSONObject("comment");
        String db = commentObj.getString("db");
        //构造查询参数
        JSONArray queryparam = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("db", db);
        //转换公司和部门Id
        Map<String,Object> param = new HashMap<String,Object>();
        //预算年份
        param.put("budget_year", pJson.getIntValue("budget_year"));
        //公司名称(ID)
        prepareCompanyIds(param,pJson);
        //部门名称
        param.put("department_id", pJson.getString("department_id"));
        //预算立项编号
        param.put("project_number", pJson.getString("project_number"));
        //预算立项名称
        param.put("project_name", pJson.getString("project_name"));
        //预算科目名称
        param.put("budget_account_name", pJson.getString("budget_account_name"));
        //合同编号
        param.put("document_code", pJson.getString("document_code"));
        //合同名称
        param.put("document_description", pJson.getString("document_description"));
        //订单号
        param.put("order_number", pJson.getString("order_number"));
        //报账单编号
        param.put("bz_document_number", pJson.getString("bz_document_number"));
        param.put("isadmin", SysContext.getRequestUser().getIsAdmin());
        param.put("user_permission_code", this.getUserPermissionCode());
		param.put("user_name", SysContext.getRequestUser().getUserName().toLowerCase());
		//arch_user用户名
		Map<String,String> map = new HashMap<String,String>();
		map.put("userName", SysContext.getRequestUser().getUserName().toLowerCase());
		List<Map<String,String>> result = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getArchUserName",map);
		param.put("userName", result.size()!=0?result.get(0).get("USERNAME"):"");
        //将Map参数转换为通用查询所需的参数形式
        JSONArray paramArr = prepareQueryParam(param);
        paramObj.put("in",paramArr);
        queryparam.add(paramObj);
        if(!type.equals("export")){
            queryparam.add(pJson.getJSONObject("page"));
        }
        return JSONObject.parseObject(selectControl.execSelect(namespace,sqlid,queryparam.toJSONString())).getJSONObject("data").toJSONString();
    }
	
	
	@RequestMapping(value = "/getContractBudgetList/{namespace}/{sqlid}/{type}", produces = "text/plain; charset=utf-8")
    public String getContractBudgetList(@PathVariable("namespace") String namespace,
            @PathVariable("sqlid") String sqlid,@PathVariable("type") String type,@RequestBody JSONObject pJson) {
        String userSqlPath = AppConstants.getUserSqlPath() + File.separator
                + namespace + ".xml";
        JSONObject obj = new JSONObject();
        obj.put("namespace", namespace);
        obj.put("sqlid", sqlid);
        //获取sql查询所需的数据库
        JSONObject sqlObj = JSON.parseObject(selectControl.qrySelectSqlDetail(obj.toJSONString()));
        JSONObject commentObj = sqlObj.getJSONObject("comment");
        String db = commentObj.getString("db");
        //构造查询参数
        JSONArray queryparam = new JSONArray();
        JSONObject paramObj = new JSONObject();
        paramObj.put("db", db);
        //转换公司和部门Id
        Map<String,Object> param = new HashMap<String,Object>();
        //公司名称(ID),部门名称
        prepareCompanyIds(param,pJson);
        //合同编号
        param.put("contract_code", pJson.getString("contract_code"));
        //合同名称
        param.put("contract_name", pJson.getString("contract_name"));
        param.put("isadmin", SysContext.getRequestUser().getIsAdmin());
        //合同开始时间
        param.put("start_Date", pJson.getString("start_Date"));
        //合同结束时间
        param.put("end_Date", pJson.getString("end_Date"));
        //合同预算年份
        param.put("budget_year", pJson.getString("budget_year"));
        //预算科目
        param.put("budget_account_name", pJson.getString("budget_account_name"));
        //预算立项编号
        param.put("project_number", pJson.getString("project_number"));
        //将Map参数转换为通用查询所需的参数形式
        JSONArray paramArr = prepareQueryParam(param);
        paramObj.put("in",paramArr);
        queryparam.add(paramObj);
        if(!type.equals("export")){
            queryparam.add(pJson.getJSONObject("page"));
        }
        return JSONObject.parseObject(selectControl.execSelect(namespace,sqlid,queryparam.toJSONString())).getJSONObject("data").toJSONString();
    }
	
	@RequestMapping(value = "/getContractPayment/{type}", produces = "text/plain; charset=utf-8")
    public String getContractPayment(@PathVariable("type") String type) {
		String userName = SysContext.getRequestUser().getUserName();
		String oaDeptNo = "";//OA部门代码
		String coSegCode = "";//公司代码
		String permission="";  //当前用户人权限
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("userName", userName);
		String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
		List<Map<String,String>> userCompanyList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getUserCompany", paramMap);
		Map<String,String> kindItem = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKindItem", paramMap);
		if(kindItem!=null){
			String remark = (String)kindItem.get("REMARK");
			String detail = (String)kindItem.get("DETAIL");
			if(remark.contains("account")){
				if("ALL".equals(detail)){
					permission = "P";
					if(userCompanyList!=null&&!userCompanyList.isEmpty()){
						oaDeptNo = userCompanyList.get(0).get("ORG_CODE");
						coSegCode = userCompanyList.get(0).get("COMPANY_CODE");
					}
				}else{
					permission = "C";
					if(userCompanyList!=null && !userCompanyList.isEmpty()){
						oaDeptNo = (String)userCompanyList.get(0).get("ORG_CODE");
					}
					String[] companyIds = kindItem.get("DETAIL").split(",");
					for(int i = 0;i < companyIds.length;i++){
						paramMap.put("company_id", Integer.valueOf(companyIds[i]));
						String companyCode = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getCompanyCodeById", paramMap);
						if(i==companyIds.length-1){
							coSegCode = coSegCode + companyCode;
						}else{
							coSegCode = coSegCode + companyCode + ",";
						}
					}
				}
			}else{
				if(userPermission!=null && !userPermission.equals("")){
					permission =  userPermission;
				}
				if(userCompanyList!=null&&!userCompanyList.isEmpty()){
					oaDeptNo = userCompanyList.get(0).get("ORG_CODE");
					coSegCode = userCompanyList.get(0).get("COMPANY_CODE");
				}
			}
		}else{
			if(userPermission!=null && !userPermission.isEmpty()){
				permission =  userPermission;
			}
			if(userCompanyList!=null&&!userCompanyList.isEmpty()){
				oaDeptNo = userCompanyList.get(0).get("ORG_CODE");
				coSegCode = userCompanyList.get(0).get("COMPANY_CODE");
			}
		}
		String url = "";
		if("summary".equals(type)){
			url = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKeyValue", "CONTRACT_ACCOUNT_PAYMENT_SUMMARY_URL");
		}else{//type="detail"
			url = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKeyValue", "ACCOUNT_PAYMENT_DATAILS_URL");
		}
		return url+"?username="+userName+"&permission="+permission+"&oaDeptNo="+oaDeptNo+"&coSegCode="+coSegCode;
	}
	
	@RequestMapping(value = "/getCertified/{type}", produces = "text/plain; charset=utf-8")
    public String getCertified(@PathVariable("type") String type) {
        String userName = SysContext.getRequestUser().getUserName();
        String permission="";  //当前用户人权限
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("userName", userName);
        String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
        Map<String,String> kindItem = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKindItem", paramMap);
        if(kindItem!=null){
            permission =  "C";
        }else{
            permission =  "E";
        }
        String url = "";
        if("certified".equals(type)){
            url = DbFactory.Open(DbFactory.FORM).selectOne("budget.getDicValueByKey", type);
        }else{
            url = DbFactory.Open(DbFactory.FORM).selectOne("budget.getDicValueByKey", type);
        }
        return url+"?username="+userName+"&permission="+permission;
    }
	
	@RequestMapping(value = "/getStockBudgetInfo", produces = "text/plain; charset=utf-8")
    public String getStockBudgetInfo() throws Exception {
		String userName = SysContext.getRequestUser().getUserName();//当前用户名
		String authenticator = DESUtil.encrypt(userName.toLowerCase()+"$");//DES加密
		String permission = "";//permission：当前用户人权限
		String companyCode = "";// companyCode：当前用户所属公司编码
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("userName", userName);
		String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
		List<Map<String,String>> userCompanyList = DbFactory.Open(DbFactory.BUDGET).selectList("budget.getUserCompany", paramMap);
		Map<String,String> kindItem = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKindItem", paramMap);
		if(kindItem != null){
			String remark = (String)kindItem.get("REMARK");
			String detail = (String)kindItem.get("DETAIL");
			if(remark.contains("account")){
				if("ALL".equals(detail)){
					permission = "P";
					if(userCompanyList!=null && !userCompanyList.isEmpty()){
						companyCode = userCompanyList.get(0).get("COMPANY_CODE");
					}
				 }else{
					permission = "C";
					String[] companyIds = detail.split(",");
					for(int i = 0;i < companyIds.length;i++){
						paramMap.put("company_id", Integer.valueOf(companyIds[i]));
						String company_Code = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getCompanyCodeById", paramMap);
						if(i==companyIds.length-1){
							companyCode = companyCode+company_Code;
						}else{
							companyCode = companyCode+company_Code+",";
						}
					}
				}
			}else{
				if(userPermission!=null && !userPermission.isEmpty()){
					permission =  userPermission;
					if(!permission.equals("P")){
						permission = "C";
					}
				}
				if(userCompanyList!=null && !userCompanyList.isEmpty()){
					companyCode = userCompanyList.get(0).get("COMPANY_CODE");
				}
			}
		}else{
			if(userPermission!=null && !userPermission.isEmpty()){
				permission =  userPermission;
				if(!permission.equals("P")){
					permission = "C";
				}
			}
			if(userCompanyList!=null && !userCompanyList.isEmpty()){
				companyCode = userCompanyList.get(0).get("COMPANY_CODE");
			}
		}
		  
		 //获取URL与SCOMMAND
		String url =  DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKeyValue", "STOCK_BUDGET_INFO_URL");
		String SCOMMAND = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKeyValue", "SCOMMAND");
		 
		// 重定向到新地址
		return url+"?SCOMMAND="+SCOMMAND+"&auth="+authenticator+"&Permisson="+permission+"&COMPANYCODE="+companyCode;
	}
	
	private String getUserPermissionCode(){
		String userName = SysContext.getRequestUser().getUserName();//当前用户名
		String permissionCode = "E";
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("userName", userName);
		String userPermission = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getUserPermission", paramMap);
		Map<String,String> kindItem = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getKindItem", paramMap);
		if(kindItem != null){
			String detail = (String)kindItem.get("DETAIL");
			if("ALL".equals(detail)){
				permissionCode = "P";
			}else{
				permissionCode = "C";
			}
		}else{
			if(userPermission!=null && !userPermission.isEmpty()){
				permissionCode =  userPermission;
			}
		}
		return permissionCode;
	}
	
	@RequestMapping(value = "/isFinish", produces = "text/plain; charset=utf-8")
    public String isFinish(@RequestBody JSONObject pJson) {
	    String falg = "false";
	    int MONTH = pJson.getInteger("budget_month");//前台查询月份
	    String period_name = DateUtil.getMonthAbbr(MONTH)+"-"+pJson.getString("budget_year").substring(2);
	    int count = DbFactory.Open(DbFactory.BUDGET).selectOne("budget.getCount",period_name);
        int erpcount = DbFactory.Open(DbFactory.SYSTEM).selectOne("procure.getERPCount",period_name);
        if(count==erpcount){
            falg="true";
        }
        return falg;
    }
}
