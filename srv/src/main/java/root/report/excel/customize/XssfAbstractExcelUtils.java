package root.report.excel.customize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XssfAbstractExcelUtils
{
    static final String EMPTY = "";
    private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
    private static final int UNIT_OFFSET_LENGTH = 7;

    public static String getAlign( short alignment )
    {
        switch ( HorizontalAlignment.forInt(alignment) )
        {
        case CENTER:
            return "center";
        case CENTER_SELECTION:
            return "center";
        case FILL:
            // XXX: shall we support fill?
            return "";
        case GENERAL:
            return "";
        case JUSTIFY:
            return "justify";
        case LEFT:
            return "left";
        case RIGHT:
            return "right";
        default:
            return "";
        }
    }

    public static String getBorderStyle( short xlsBorder )
    {
        final String borderStyle;
        switch ( BorderStyle.valueOf(xlsBorder) )
        {
        case NONE:
            borderStyle = "none";
            break;
        case DASH_DOT:
        case DASH_DOT_DOT:
        case DOTTED:
        case HAIR:
        case MEDIUM_DASH_DOT:
        case MEDIUM_DASH_DOT_DOT:
        case SLANTED_DASH_DOT:
            borderStyle = "dotted";
            break;
        case DASHED:
        case MEDIUM_DASHED:
            borderStyle = "dashed";
            break;
        case DOUBLE:
            borderStyle = "double";
            break;
        default:
            borderStyle = "solid";
            break;
        }
        return borderStyle;
    }

    public static String getBorderWidth( short xlsBorder )
    {
        final String borderWidth;
        switch ( BorderStyle.valueOf(xlsBorder) )
        {
        case MEDIUM_DASH_DOT:
        case MEDIUM_DASH_DOT_DOT:
        case MEDIUM_DASHED:
            borderWidth = "2pt";
            break;
        case THICK:
            borderWidth = "thick";
            break;
        default:
            borderWidth = "thin";
            break;
        }
        return borderWidth;
    }

    public static String getColor( XSSFColor color )
    {
        StringBuilder stringBuilder = new StringBuilder( 7 );
        stringBuilder.append( '#' );
        for ( short s :color.getRGB() )
        {
            if ( s < 10 )
                stringBuilder.append( '0' );

            stringBuilder.append( Integer.toHexString( s ) );
        }
        String result = stringBuilder.toString();

        if ( result.equals( "#ffffff" ) )
            return "white";

        if ( result.equals( "#c0c0c0" ) )
            return "silver";

        if ( result.equals( "#808080" ) )
            return "gray";

        if ( result.equals( "#000000" ) )
            return "black";

        return result;
    }

    /**
     * See <a href=
     * "http://apache-poi.1045710.n5.nabble.com/Excel-Column-Width-Unit-Converter-pixels-excel-column-width-units-td2301481.html"
     * >here</a> for Xio explanation and details
     */
    public static int getColumnWidthInPx( int widthUnits )
    {
        int pixels = ( widthUnits / EXCEL_COLUMN_WIDTH_FACTOR )
                * UNIT_OFFSET_LENGTH;

        int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
        pixels += Math.round( offsetWidthUnits
                / ( (float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH ) );

        return pixels;
    }

    /**
     * @param mergedRanges
     *            map of sheet merged ranges built with
     *            {@link ExcelToHtmlUtils#buildMergedRangesMap(HSSFSheet)}
     * @return {@link CellRangeAddress} from map if cell with specified row and
     *         column numbers contained in found range, <tt>null</tt> otherwise
     */
    public static CellRangeAddress getMergedRange(
            CellRangeAddress[][] mergedRanges, int rowNumber, int columnNumber )
    {
        CellRangeAddress[] mergedRangeRowInfo = rowNumber < mergedRanges.length ? mergedRanges[rowNumber]
                : null;
        CellRangeAddress cellRangeAddress = mergedRangeRowInfo != null
                && columnNumber < mergedRangeRowInfo.length ? mergedRangeRowInfo[columnNumber]
                : null;

        return cellRangeAddress;
    }

    static boolean isEmpty( String str )
    {
        return str == null || str.length() == 0;
    }

    static boolean isNotEmpty( String str )
    {
        return !isEmpty( str );
    }

    public static XSSFWorkbook loadXls( File xlsFile ) throws IOException
    {
        final FileInputStream inputStream = new FileInputStream( xlsFile );
        try
        {
            return new XSSFWorkbook( inputStream );
        }
        finally
        {
            IOUtils.closeQuietly( inputStream );
        }
    }

}

