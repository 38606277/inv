package root.report.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
 
/**
 * 利用POI读取excel,转换为html
 *
 */
public class POIReadExcel {
    public static void main(String[] args) {
        try {
            POIReadExcel poire = new POIReadExcel();
            String path = "/Users/wangjian/Documents/test/湖北报表成本计算表.xls";
            File sourcefile = new File(path);
            InputStream is = new FileInputStream(sourcefile);
            POIFSFileSystem fs = new POIFSFileSystem(is);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            System.out.println(poire.getExcelInfo(wb));
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public String getExcelInfo(HSSFWorkbook wb) throws Exception {
        StringBuffer sb = new StringBuffer();
        Sheet sheet = wb.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        Map map[] = getRowSpanColSpanMap(sheet);
        sb.append(" ");
        HSSFRow row = null;
        HSSFCell cell = null;
        // System.out.println(sheet.getPhysicalNumberOfRows());
        for (int rowNum = sheet.getFirstRowNum(); rowNum < lastRowNum; rowNum++) {
            row = (HSSFRow) sheet.getRow(rowNum);
            if (row == null) {
                sb.append(" ");
                continue;
            }
            sb.append(" ");
            int lastColNum = row.getLastCellNum();
            for (int colNum = 0; colNum < lastColNum; colNum++) {
                cell = row.getCell(colNum);
                if (cell == null) {
                    sb.append(" ");
                    continue;
                }
                String stringValue = getCellValue(cell);
                if (map[0].containsKey(rowNum + "," + colNum)) {
                    String pointString = (String) map[0].get(rowNum + "," + colNum);
                    map[0].remove(rowNum + "," + colNum);
                    int bottomeRow = Integer.valueOf(pointString.split(",")[0]);
                    int bottomeCol = Integer.valueOf(pointString.split(",")[1]);
                    int rowSpan = bottomeRow - rowNum + 1;
                    int colSpan = bottomeCol - colNum + 1;
                    sb.append(" ");
                } else if (map[1].containsKey(rowNum + "," + colNum)) {
                    map[1].remove(rowNum + "," + colNum);
                    continue;
                } else {
                    sb.append(" ");
                }
                HSSFCellStyle cellStyle = cell.getCellStyle();
                if (cellStyle != null) {
                    short alignment = cellStyle.getAlignment();
                    sb.append("align='" + convertAlignToHtml(alignment) + "' ");
                    short verticalAlignment = cellStyle.getVerticalAlignment();
                    sb.append("valign='"
                            + convertVerticalAlignToHtml(verticalAlignment)
                            + "' ");
                    HSSFFont hf = cellStyle.getFont(wb);
                    short boldWeight = hf.getBoldweight();
                    short fontColor = hf.getColor();
                    sb.append("style='");
                    HSSFPalette palette = wb.getCustomPalette(); // 类HSSFPalette用于求的颜色的国际标准形式
                    HSSFColor hc = palette.getColor(fontColor);
                    sb.append("font-weight:" + boldWeight + ";"); // 字体加粗
                    // System.out.println(hf.getFontHeight());
                    sb.append("font-size: " + hf.getFontHeight() / 2 + "%;"); // 字体大小
                    String fontColorStr = convertToStardColor(hc);
                    if (fontColorStr != null && !"".equals(fontColorStr.trim())) {
                        sb.append("color:" + fontColorStr + ";"); // 字体颜色
                    }
                    short bgColor = cellStyle.getFillForegroundColor();
                    hc = palette.getColor(bgColor);
                    String bgColorStr = convertToStardColor(hc);
                    if (bgColorStr != null && !"".equals(bgColorStr.trim())) {
                        sb.append("background-color:" + bgColorStr + ";"); // 背景颜色
                    }
                    short borderColor = cellStyle.getBottomBorderColor();
                    hc = palette.getColor(borderColor);
                    String borderColorStr = convertToStardColor(hc);
                    if (borderColorStr != null
                            && !"".equals(borderColorStr.trim())) {
                        sb.append("border-color:" + borderColorStr + ";"); // 边框颜色
                    }
                    // boolean borderBoolean = cellStyle.getWrapText();
                    //
                    // if(borderBoolean){
                    // sb.append("border-style: inset;");
                    // }
                    sb.append("' ");
                }
                sb.append(">");
                if (stringValue == null || "".equals(stringValue.trim())) {
                    sb.append(" ");
                } else {
                    // 将ascii码为160的空格转换为html下的空格（&nbsp;）
                    sb.append(stringValue.replace(String.valueOf((char) 160),
                            "&nbsp;"));
                }
                sb.append(" ");
            }
            sb.append(" ");
        }
        sb.append(" ");
        return sb.toString();
    }
 
    @SuppressWarnings("unchecked")
    private Map[] getRowSpanColSpanMap(Sheet sheet) {
        Map map0 = new HashMap();
        Map map1 = new HashMap();
        int mergedNum = sheet.getNumMergedRegions();
        CellRangeAddress range = null;
        for (int i = 0; i < mergedNum; i++) {
            range = sheet.getMergedRegion(i);
            int topRow = range.getFirstRow();
            int topCol = range.getFirstColumn();
            int bottomRow = range.getLastRow();
            int bottomCol = range.getLastColumn();
            map0.put(topRow + "," + topCol, bottomRow + "," + bottomCol);
            // System.out.println(topRow + "," + topCol + "," + bottomRow + ","
            // + bottomCol);
            int tempRow = topRow;
            while (tempRow <= bottomRow) {
                int tempCol = topCol;
                while (tempCol <= bottomCol) {
                    map1.put(tempRow + "," + tempCol, "");
                    tempCol++;
                }
                tempRow++;
            }
            map1.remove(topRow + "," + topCol);
        }
        Map[] map = { map0, map1 };
        return map;
    }
 
    private String convertAlignToHtml(short alignment) {
        String align = "left";
        switch (alignment) {
        case HSSFCellStyle.ALIGN_LEFT:
            align = "left";
            break;
        case HSSFCellStyle.ALIGN_CENTER:
            align = "center";
            break;
        case HSSFCellStyle.ALIGN_RIGHT:
            align = "right";
            break;
        default:
            break;
        }
        return align;
    }
 
    private String convertVerticalAlignToHtml(short verticalAlignment) {
        String valign = "middle";
        switch (verticalAlignment) {
        case HSSFCellStyle.VERTICAL_BOTTOM:
            valign = "bottom";
            break;
        case HSSFCellStyle.VERTICAL_CENTER:
            valign = "center";
            break;
        case HSSFCellStyle.VERTICAL_TOP:
            valign = "top";
            break;
        default:
            break;
        }
        return valign;
    }
 
    private String convertToStardColor(HSSFColor hc) {
        StringBuffer sb = new StringBuffer("");
        if (hc != null) {
            if (HSSFColor.AUTOMATIC.index == hc.getIndex()) {
                return null;
            }
            sb.append("#");
            for (int i = 0; i < hc.getTriplet().length; i++) {
                sb.append(fillWithZero(Integer.toHexString(hc.getTriplet()[i])));
            }
        }
        return sb.toString();
    }
 
    private String fillWithZero(String str) {
        if (str != null && str.length() < 2) {
            return "0" + str;
        }
        return str;
    }
 
    private String getCellValue(HSSFCell cell) {
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_NUMERIC:
            DecimalFormat format = new DecimalFormat("#0.##");
            return format.format(cell.getNumericCellValue());
            // return String.valueOf(cell.getNumericCellValue());
        case HSSFCell.CELL_TYPE_STRING:
            return cell.getStringCellValue();
            // case HSSFCell.CELL_TYPE_FORMULA:
            //
            // return cell.getCellFormula();
        default:
            return "";
        }
    }
}
