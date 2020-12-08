package root.report.file;

import org.apache.ibatis.session.SqlSession;
import root.report.db.DbFactory;
import root.report.util.ObjectUtil;
import root.report.util.XxlsAbstract;
import root.report.util.annotaion.DBColumn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Excel2MysqlForTax extends XxlsAbstract {
	public static class AssetTaxUploadParam{
		private String dataType;
		private String company;
		private String createBy;
		private String accountPeriod;
		private String assetProperty;
		public AssetTaxUploadParam(String dataType, String company, 
				String createBy, String accountPeriod, String assetProperty) {
			super();
			this.dataType = dataType;
			this.company = company;
			this.createBy = createBy;
			this.accountPeriod = accountPeriod;
			this.assetProperty = assetProperty;
		}
		@DBColumn("data_type")
		public String getDataType() {
			return dataType;
		}
		@DBColumn("company")
		public String getCompany() {
			return company;
		}
		@DBColumn("create_by")
		public String getCreateBy() {
			return createBy;
		}
		@DBColumn("account_period")
		public String getAccountPeriod() {
			return accountPeriod;
		}
		@DBColumn("asset_property")
		public String getAssetProperty() {
			return assetProperty;
		}
	}
	
	private PreparedStatement pres = null;
	private Connection conn = null;
	private String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	AssetTaxUploadParam param = null;
	private Integer totalRows=0;
	// 从excel中加载数据并保存到数据库
	public Integer loadAndSave(String sourceFile, AssetTaxUploadParam param) throws Exception{
		this.param = param;
		SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
		conn = sqlSession.getConnection();
		try {
			conn.setAutoCommit(false);
			this.deleteOldVersionData();
			pres = conn.prepareStatement(this.getPrepareSql());
			this.processOneSheet(sourceFile,1);
			pres.executeBatch();
			conn.commit();
		} finally{
			if(pres != null) pres.close();
			DbFactory.close(DbFactory.FORM);
		}
		return totalRows;
	}
		
	//删除该类型数据的历史数据
	private void deleteOldVersionData() throws Exception {
		try (Statement stm = conn.createStatement()){
			StringBuffer sb = new StringBuffer("delete from excel_data_asset where 1=1 ");
			Map<String, Object> map = ObjectUtil.convertObjectToMap(param);
			if(map != null){
				for(String key : map.keySet()){
					sb.append("and "+key+"='"+map.get(key)+"'");
				}
			}
			stm.execute(sb.toString());
			conn.commit();
		}
	}

	private String getPrepareSql(){
		String insertSql = "insert into excel_data_asset (#columns#) values (#values#)";
		StringBuffer columns = new StringBuffer("data_type,company,create_by,create_date,asset_property,account_period");
		StringBuffer values = new StringBuffer("?,?,?,?,?,?");
		for(int i=1;i<81;i++){
			columns.append(",col"+i);
			values.append(",?");
		}
		insertSql = insertSql.replace("#columns#", columns.toString()).replace("#values#", values.toString());
		return insertSql;
	}
	
	long lastCommitTime = System.currentTimeMillis();
	@Override
	public void optRows(int sheetIndex, int curRow, List<String> rowlist) throws SQLException {
		if(rowlist == null || rowlist.size()<1) return;
		totalRows = curRow;
		pres.setString(1, param.dataType);
		pres.setString(2, param.company);
		pres.setString(3, param.createBy);
		pres.setString(4, dateNow);
		pres.setString(5, param.assetProperty);
		pres.setString(6, param.accountPeriod);
		for(int i=0;i<80;i++){
			String colV = rowlist.size()>i ? rowlist.get(i) : null;
			if(colV != null && colV.length()>128) colV = colV.substring(0, 127);
			pres.setString(i+7, colV);
		}
		pres.addBatch();
		if(curRow%1000==0){
			pres.executeBatch();
			conn.commit();
			System.out.println("use milinseconds to commit 1000 rows:"+(System.currentTimeMillis()-lastCommitTime));
			lastCommitTime = System.currentTimeMillis();
			System.out.println("curRow:"+curRow);
		}
	}
	
	//@Test
	public void test() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				String filePath = "D:\\文档\\201706报表开发\\导入数据库的\\CMCC_资产报废报表分列.xlsx";
				try {
					long time = System.currentTimeMillis();
					AssetTaxUploadParam param = new AssetTaxUploadParam("4", "3221","钟晶","MAY-17","FA");
					Excel2MysqlForTax e = new Excel2MysqlForTax();
					Integer tot = e.loadAndSave(filePath, param);
					System.out.println("共解析到："+tot+"行");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).run();
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		new Thread(new Runnable(){
			@Override
			public void run() {
				String filePath = "D:\\文档\\201706报表开发\\导入数据库的\\CMCC_资产报废报表分列.xlsx";
				try {
					long time = System.currentTimeMillis();
					AssetTaxUploadParam param = new AssetTaxUploadParam("4", "3220","钟晶","MAY-17","FA");
					Excel2MysqlForTax e = new Excel2MysqlForTax();
					Integer tot = e.loadAndSave(filePath, param);
					System.out.println("共解析到："+tot+"行");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).run();
		
	}
}
