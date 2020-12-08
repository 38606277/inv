package root.report.util;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;  
  
/** 
 * XSSF and SAX (Event API) 
 */  
public abstract class XxlsAbstract extends DefaultHandler {
    private SharedStringsTable sst;
    private String lastContents;
    private boolean nextIsString;
  
    private int sheetIndex = -1;
    private List<String> rowlist = new ArrayList<String>();
    private int curRow = 0;
    private int curCol = 0;
    private Integer totolColumCount = 0;
    //excel记录行操作方法，以行索引和行元素列表为参数，对一行元素进行操作，元素为String类型  
//  public abstract void optRows(int curRow, List<String> rowlist) throws SQLException ;  
      
    //excel记录行操作方法，以sheet索引，行索引和行元素列表为参数，对sheet的一行元素进行操作，元素为String类型  
    public abstract void optRows(int sheetIndex,int curRow, List<String> rowlist) throws SQLException;
      
    //只遍历一个sheet，其中sheetId为要遍历的sheet索引，从1开始，1-3  
    public void processOneSheet(String filename,int sheetId) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        this.readAndParse(pkg, sheetId);
    }
  
    public void processOneSheet(InputStream inputStream,int sheetId) throws Exception {
        OPCPackage pkg = OPCPackage.open(inputStream);
        this.readAndParse(pkg, sheetId);
    }
    
    private void readAndParse(OPCPackage pkg, int sheetId) throws Exception{
    	XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst);
        // rId2 found by processing the Workbook  
        // 根据 rId# 或 rSheet# 查找sheet  
        InputStream sheet2 = r.getSheet("rId"+sheetId);
        sheetIndex++;
        InputSource sheetSource = new InputSource(sheet2);
        parser.parse(sheetSource);
        sheet2.close();
    }
    
    /** 
     * 遍历 excel 文件 
     */  
    public void process(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst);
        Iterator<InputStream> sheets = r.getSheetsData();
        while (sheets.hasNext()) {
            sheetIndex++;
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
        }  
    }  
  
    public XMLReader fetchSheetParser(SharedStringsTable sst)throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
        this.sst = sst;
        parser.setContentHandler(this);
        return parser;
    }  
  
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    	if (name.equals("c")) {// c => 单元格  
            // 如果下一个元素是 SST 的索引，则将nextIsString标记为true
            String cellType = attributes.getValue("t");
            if (cellType != null && cellType.equals("s")) {
                nextIsString = true;
            } else {  
                nextIsString = false;
            }
            String cellNum = StringUtil.findStrWithRegex("([A-Z]+)\\d+", attributes.getValue("r"), 1);
            curCol = StringUtil.getLetterIntValue(cellNum)-1;
        }else if(name.equals("row")){
        	rowlist = new ArrayList<String>(totolColumCount);
        	curRow = Integer.parseInt(attributes.getValue("r"));
        }else if(name.equals("dimension")){//获得总列数
    		String ref = attributes.getValue("ref");
    		String regex = "[A-Z]+\\d+:([A-Z]+)\\d+";
    		String endColumn = StringUtil.findStrWithRegex(regex, ref, 1);
    		totolColumCount = endColumn != null ? StringUtil.getLetterIntValue(endColumn) : 1;
    	}
        // 置空  
        lastContents = "";
    }  
  
    public void endElement(String uri, String localName, String name)throws SAXException {  
        // 根据SST的索引值的到单元格的真正要存储的字符串 
        // 这时characters()方法可能会被调用多次 
        if (nextIsString) {
            try {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();  
            } catch (Exception e) {
  
            }  
        }  
        // v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引  
        // 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符  
        if (name.equals("v")) {
            if(rowlist.size()<curCol){
            	for(int i=rowlist.size() ;i<curCol; i++){
            		rowlist.add(i, null);
            	}
            }
            String value = lastContents.trim();
            value = value.equals("")?" ":value;
            rowlist.add(curCol, value);
        }else {  
            //如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法  
            if (name.equals("row")) {
                try {
                    optRows(sheetIndex,curRow,rowlist);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
  
    public void characters(char[] ch, int start, int length) throws SAXException {  
        //得到单元格内容的值 
        lastContents += new String(ch, start, length);
    }
    
} 