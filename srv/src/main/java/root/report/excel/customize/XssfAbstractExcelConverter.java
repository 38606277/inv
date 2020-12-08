package root.report.excel.customize;

import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hwpf.converter.DefaultFontReplacer;
import org.apache.poi.hwpf.converter.FontReplacer;
import org.apache.poi.hwpf.converter.NumberFormatter;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.w3c.dom.Document;

public abstract class XssfAbstractExcelConverter
{
    protected static int getColumnWidth( XSSFSheet sheet, int columnIndex )
    {
        return XssfExcelToHtmlUtils.getColumnWidthInPx( sheet
                .getColumnWidth( columnIndex ) );
    }

    protected static int getDefaultColumnWidth( XSSFSheet sheet )
    {
        return XssfExcelToHtmlUtils.getColumnWidthInPx( sheet
                .getDefaultColumnWidth() );
    }

    protected final HSSFDataFormatter _formatter = new HSSFDataFormatter();

    private FontReplacer fontReplacer = new DefaultFontReplacer();

    private boolean outputColumnHeaders = true;

    private boolean outputHiddenColumns = false;

    private boolean outputHiddenRows = false;

    private boolean outputLeadingSpacesAsNonBreaking = true;

    private boolean outputRowNumbers = true;

    /**
     * Generates name for output as column header in case
     * <tt>{@link #isOutputColumnHeaders()} == true</tt>
     * 
     * @param columnIndex
     *            0-based column index
     */
    protected String getColumnName( int columnIndex )
    {
        return NumberFormatter.getNumber( columnIndex + 1, 3 );
    }

    protected abstract Document getDocument();

    public FontReplacer getFontReplacer()
    {
        return fontReplacer;
    }

    /**
     * Generates name for output as row number in case
     * <tt>{@link #isOutputRowNumbers()} == true</tt>
     */
    protected String getRowName( XSSFRow row )
    {
        return String.valueOf( row.getRowNum() + 1 );
    }

    public boolean isOutputColumnHeaders()
    {
        return outputColumnHeaders;
    }

    public boolean isOutputHiddenColumns()
    {
        return outputHiddenColumns;
    }

    public boolean isOutputHiddenRows()
    {
        return outputHiddenRows;
    }

    public boolean isOutputLeadingSpacesAsNonBreaking()
    {
        return outputLeadingSpacesAsNonBreaking;
    }

    public boolean isOutputRowNumbers()
    {
        return outputRowNumbers;
    }

    protected boolean isTextEmpty( XSSFCell cell )
    {
        final String value;
        switch ( cell.getCellTypeEnum() )
        {
        case STRING:
            value = cell.getRichStringCellValue().getString();
            break;
        case FORMULA:
            switch ( cell.getCachedFormulaResultTypeEnum() )
            {
            case STRING:
                XSSFRichTextString str = cell.getRichStringCellValue();
                if ( str == null || str.length() <= 0 )
                    return false;

                value = str.toString();
                break;
            case NUMERIC:
                XSSFCellStyle style = cell.getCellStyle();
                if ( style == null )
                {
                    return false;
                }

                value = ( _formatter.formatRawCellContents(
                        cell.getNumericCellValue(), style.getDataFormat(),
                        style.getDataFormatString() ) );
                break;
            case BOOLEAN:
                value = String.valueOf( cell.getBooleanCellValue() );
                break;
            case ERROR:
                value = ErrorEval.getText( cell.getErrorCellValue() );
                break;
            default:
                value = XssfExcelToHtmlUtils.EMPTY;
                break;
            }
            break;
        case BLANK:
            value = XssfExcelToHtmlUtils.EMPTY;
            break;
        case NUMERIC:
            value = _formatter.formatCellValue( cell );
            break;
        case BOOLEAN:
            value = String.valueOf( cell.getBooleanCellValue() );
            break;
        case ERROR:
            value = ErrorEval.getText( cell.getErrorCellValue() );
            break;
        default:
            return true;
        }

        return XssfExcelToHtmlUtils.isEmpty( value );
    }

    public void setFontReplacer( FontReplacer fontReplacer )
    {
        this.fontReplacer = fontReplacer;
    }

    public void setOutputColumnHeaders( boolean outputColumnHeaders )
    {
        this.outputColumnHeaders = outputColumnHeaders;
    }

    public void setOutputHiddenColumns( boolean outputZeroWidthColumns )
    {
        this.outputHiddenColumns = outputZeroWidthColumns;
    }

    public void setOutputHiddenRows( boolean outputZeroHeightRows )
    {
        this.outputHiddenRows = outputZeroHeightRows;
    }

    public void setOutputLeadingSpacesAsNonBreaking(
            boolean outputPrePostSpacesAsNonBreaking )
    {
        this.outputLeadingSpacesAsNonBreaking = outputPrePostSpacesAsNonBreaking;
    }

    public void setOutputRowNumbers( boolean outputRowNumbers )
    {
        this.outputRowNumbers = outputRowNumbers;
    }

}
