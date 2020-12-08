package root.report.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.text.DecimalFormat;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
	
	//Excel扩展名的两种格式
	public static final String EXCEL_2003="xls";
	public static final String EXCEL_2007="xlsx";
	
	/**
	 * 创建一个基于接口的Excel实例 
	 * @param inp
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static Workbook create(InputStream inp) throws IOException,InvalidFormatException {
	    if (!inp.markSupported()) {
	        inp = new PushbackInputStream(inp, 8);
	    }
	    if (POIFSFileSystem.hasPOIFSHeader(inp)) {
	        return new HSSFWorkbook(inp);
	    }
	    if (POIXMLDocument.hasOOXMLHeader(inp)) {
	        return new XSSFWorkbook(OPCPackage.open(inp));
	    }
	    throw new IllegalArgumentException("你的excel版本目前poi解析不了");
	}
	
	
	/**
	 * 获取一个Excel文件的类型，即2003还是2007
	 * @param inp
	 * @return
	 * @throws Exception
	 */
	public static String getExcelType(InputStream inp) throws Exception{
		if (!inp.markSupported()) {
	        inp = new PushbackInputStream(inp, 8);
	    }
	    if (POIFSFileSystem.hasPOIFSHeader(inp)) {
	    	inp=null;
	        return EXCEL_2003;
	    }
	    if (POIXMLDocument.hasOOXMLHeader(inp)) {
	    	inp=null;
	        return EXCEL_2007;
	    }
	    throw new IllegalArgumentException("你的excel版本目前poi解析不了");
	}
	
	
	/***
	 * 计算Excel公式的值
	 * @param book
	 * @return
	 * @throws Exception
	 */
	public static Workbook evaluateExcel(Workbook book) throws Exception{
		if(book instanceof HSSFWorkbook){
			HSSFWorkbook hbook=(HSSFWorkbook)book;
			HSSFFormulaEvaluator.evaluateAllFormulaCells(hbook);
			return hbook;
		}
		if(book instanceof XSSFWorkbook){
			XSSFWorkbook xbook=(XSSFWorkbook)book;
			XSSFFormulaEvaluator.evaluateAllFormulaCells(xbook);
			return xbook;
		}
		return book;
		
	}
	
	/**
	 * @param cell
	 * @return
	 */
	public static String getCellValue(Cell cell) {
		if(cell == null) return "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			return "";
		case Cell.CELL_TYPE_BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case Cell.CELL_TYPE_NUMERIC:
			DecimalFormat df = new DecimalFormat("###.######");
			return df.format(cell.getNumericCellValue());
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue() != null ? cell.getStringCellValue().trim() : "";
		case Cell.CELL_TYPE_FORMULA:
			return cell.getCellFormula();
		default:
			break;
		}
		return null;
	}
}
