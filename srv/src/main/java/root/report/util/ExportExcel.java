package root.report.util;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*** 
 * @author lsf
 */
public class ExportExcel {

    /***************************************************************************
     * @param fileName EXCEL文件名称
     * @param listTitle EXCEL文件第一行列标题集合
     * @param listContent EXCEL文件正文数据集合
     * @return
     * @throws IOException
     */
    public  final static Map exportExcel(String titleHeader, String fileName, List listTitle, List listColumn, List<Object> listContent) throws IOException {
        //基础路径  E:/springboot-upload/image/
        String folder=System.getProperty("java.io.tmpdir")+ File.separator ;
        System.out.println(folder);
        String basePath =folder+ "reportExcel";
      //  String basePath =System.getProperty("user.dir") + File.separator + "upload/excel";
        File destFile = new File(basePath);
        if (!destFile.exists()) {
            destFile.mkdirs();
        }
        String result="系统提示：Excel文件导出成功！";
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCellStyle style = wb.createCellStyle();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet=wb.createSheet(titleHeader);
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row1=sheet.createRow(0);
        //创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
        /** ***************以下是EXCEL第一行列标题********************* */
		   
		/*HSSFCell cell=row1.createCell(0);
		  //设置单元格内容
		cell.setCellValue(titleHeader);
		//合并单元格CellRangeAddress构造参数依次表示起始行，截至行，起始列， 截至列
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,3));*/
        //在sheet里创建第二行
        //row1=sheet.createRow(1);
        for (int i = 0; i < listTitle.size(); i++) {
            // 创建单元格，设置值
            row1.createCell(i).setCellValue(listTitle.get(i).toString());
        }
        //在sheet里创建第三行
        for (int i = 0; i < listContent.size(); i++) {
            Row rows = sheet.createRow((int) i + 1);
            rows.setHeightInPoints(25);
            Map list=  (Map) listContent.get(i);
            for (int j = 0; j < listColumn.size(); j++) {
                // 创建单元格，设置值
                rows.createCell(j).setCellValue(list.get(listColumn.get(j))!=null?list.get(listColumn.get(j)).toString():null);
            }
        }

        Format format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fileNames="/Excel"+format.format(new Date())+".xls";
        String filepath=basePath+fileNames;
        String usefilepath="reportExcel/"+fileNames;
        FileOutputStream out =new FileOutputStream(filepath);
        wb.write(out);
        out.close();
        Map<String,String> map=new HashMap<String,String>();
        map.put("fileName", "Excel"+fileName+".xls");
        map.put("filePath", usefilepath);
        return map;
    }

    public static HSSFWorkbook workbooks(String name, String[] titles, String[] column, List<Map<String, Object>> data) {
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet spreadsheet = workbook.createSheet(name);

        HSSFRow titlesRow = spreadsheet.createRow(0);

        HSSFCellStyle cellStyle = workbook.createCellStyle();

        for (int i = 0; i < titles.length; i++) {
            titlesRow.createCell(i).setCellValue(titles[i]);
        }

        for (int i = 0; i < data.size(); i++) {
            HSSFRow row = spreadsheet.createRow(i + 1);
            for (int j = 0; j < column.length; j++) {
                Object value = data.get(i).get(column[j]);

                String stringValue = "";
                if (value instanceof BigDecimal) {
                    stringValue = value.toString();
                }else if(value instanceof String) {
                    stringValue = (String) value;
                }else if (value instanceof Date){
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                    stringValue = sdf.format(value);
                }else {
                    System.out.println(column[j] + "--------------" + value);
                }

                row.createCell(j).setCellValue(stringValue);
            }
        }

        for (int i = 0; i < titles.length; i++) {
            spreadsheet.autoSizeColumn(i);
        }

        return  workbook;
    }
}