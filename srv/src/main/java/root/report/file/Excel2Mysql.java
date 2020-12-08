package root.report.file;

import org.apache.ibatis.session.SqlSession;
import root.report.db.DbFactory;
import root.report.util.XxlsAbstract;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Excel2Mysql extends XxlsAbstract {
	private String dataType = "", company = "", createBy = "";
	private PreparedStatement pres = null;
	private Connection conn = null;
	private String dateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	private Integer totalRows=0;
	// 从excel中加载数据并保存到数据库
	public Integer loadAndSave(String sourceFile, String dataType, String company, String createBy) throws Exception{
		this.dataType = dataType;
		this.company = company;
		this.createBy = createBy;
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
	private void deleteOldVersionData() throws SQLException {
		try (Statement stm = conn.createStatement()){
			String delSql = "delete from excel_data where data_type='" + dataType + "' and company ='"+company+"'";
			stm.execute(delSql);
			conn.commit();
		}
	}

	private String getPrepareSql(){
		String insertSql = "insert into excel_data (#columns#) values (#values#)";
		StringBuffer columns = new StringBuffer("data_type,company,create_by,create_date");
		StringBuffer values = new StringBuffer("?,?,?,?");
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
		pres.setString(1, dataType);
		pres.setString(2, company);
		pres.setString(3, createBy);
		pres.setString(4, dateNow);
		for(int i=0;i<80;i++){
			String colV = rowlist.size()>i ? rowlist.get(i) : null;
			if(colV != null && colV.length()>128) colV = colV.substring(0, 127);
			pres.setString(i+5, colV);
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
		String filePath = "D:\\文档\\201706报表开发\\导入数据库的\\CMCC_成本调整报表分列.xlsx";
		try {
			long time = System.currentTimeMillis();
			Integer tot = this.loadAndSave(filePath, "3","济南","admin");
			System.out.println("use:"+(System.currentTimeMillis()-time)/1000+"共解析到："+tot+"行");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
