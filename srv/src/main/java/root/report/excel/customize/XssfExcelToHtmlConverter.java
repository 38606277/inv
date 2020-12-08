package root.report.excel.customize;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hwpf.converter.HtmlDocumentFacade;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XssfExcelToHtmlConverter extends XssfAbstractExcelConverter
{
    private static final POILogger logger = POILogFactory
            .getLogger( ExcelToHtmlConverter.class );

    /**
     * Java main() interface to interact with {@link ExcelToHtmlConverter}
     * 
     * <p>
     * Usage: ExcelToHtmlConverter infile outfile
     * </p>
     * Where infile is an input .xls file ( Word 97-2007) which will be rendered
     * as HTML into outfile
     */
    public static void main( String[] args )
    {
        if ( args.length < 2 )
        {
            System.err
                    .println( "Usage: ExcelToHtmlConverter <inputFile.xls> <saveTo.html>" );
            return;
        }

        System.out.println( "Converting " + args[0] );
        System.out.println( "Saving output to " + args[1] );
        try
        {
            Document doc = XssfExcelToHtmlConverter.process( new File( args[0] ) );

            FileWriter out = new FileWriter( args[1] );
            DOMSource domSource = new DOMSource( doc );
            StreamResult streamResult = new StreamResult( out );

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            
            serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            serializer.setOutputProperty( OutputKeys.INDENT, "no" );
            serializer.setOutputProperty( OutputKeys.METHOD, "html" );
            serializer.transform( domSource, streamResult );
            out.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Converts Excel file (97-2007) into HTML file.
     * 
     * @param xlsFile
     *            file to process
     * @return DOM representation of result HTML
     */
    public static Document process( File xlsFile ) throws Exception
    {
        final XSSFWorkbook workbook = XssfExcelToHtmlUtils.loadXls( xlsFile );
        XssfExcelToHtmlConverter excelToHtmlConverter = new XssfExcelToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        excelToHtmlConverter.processWorkbook( workbook );
        return excelToHtmlConverter.getDocument();
    }

    private String cssClassContainerCell = null;

    private String cssClassContainerDiv = null;

    private String cssClassPrefixCell = "c";

    private String cssClassPrefixDiv = "d";

    private String cssClassPrefixRow = "r";

    private String cssClassPrefixTable = "t";

    private Map<Short, String> excelStyleToClass = new LinkedHashMap<Short, String>();

    private final HtmlDocumentFacade htmlDocumentFacade;

    private boolean useDivsToSpan = false;

    public XssfExcelToHtmlConverter( Document doc )
    {
        htmlDocumentFacade = new HtmlDocumentFacade( doc );
    }

    public XssfExcelToHtmlConverter( HtmlDocumentFacade htmlDocumentFacade )
    {
        this.htmlDocumentFacade = htmlDocumentFacade;
    }

    protected String buildStyle( XSSFWorkbook workbook, XSSFCellStyle cellStyle )
    {
        StringBuilder style = new StringBuilder();

        style.append( "white-space:pre-wrap;" );
        XssfExcelToHtmlUtils.appendAlign( style, cellStyle.getAlignmentEnum().getCode() );

        if ( cellStyle.getFillPatternEnum().getCode() == 0 )
        {
            // no fill
        }
        else if ( cellStyle.getFillPatternEnum().getCode() == 1 )
        {
            final XSSFColor foregroundColor = cellStyle
                    .getFillForegroundColorColor();
            if ( foregroundColor != null )
                style.append( "background-color:"
                        + getRGBHex(foregroundColor.getRGB()) + ";" );
        }
        else
        {
            final XSSFColor backgroundColor = cellStyle
                    .getFillBackgroundColorColor();
            if ( backgroundColor != null )
                style.append( "background-color:"
                        + getRGBHex(backgroundColor.getRGB()) + ";" );
        }

        buildStyle_border( workbook, style, "top", cellStyle.getBorderTopEnum().getCode(),
                cellStyle.getTopBorderColor(),cellStyle);
        buildStyle_border( workbook, style, "right",
                cellStyle.getBorderRightEnum().getCode(), cellStyle.getRightBorderColor() ,cellStyle);
        buildStyle_border( workbook, style, "bottom",
                cellStyle.getBorderBottomEnum().getCode(), cellStyle.getBottomBorderColor() ,cellStyle);
        buildStyle_border( workbook, style, "left", cellStyle.getBorderLeftEnum().getCode(),
                cellStyle.getLeftBorderColor() ,cellStyle);

        //XSSFFont font = cellStyle.getFont( workbook );//x
        XSSFFont font = cellStyle.getFont();
        buildStyle_font( workbook, style, font );

        return style.toString();
    }

    private void buildStyle_border( XSSFWorkbook workbook, StringBuilder style,
            String type, short xlsBorder, short borderColor , XSSFCellStyle cellStyle)
    {
        if ( xlsBorder == BorderStyle.NONE.getCode() )
            return;

        StringBuilder borderStyle = new StringBuilder();
        borderStyle.append( XssfExcelToHtmlUtils.getBorderWidth( xlsBorder ) );
        borderStyle.append( ' ' );
        borderStyle.append( XssfExcelToHtmlUtils.getBorderStyle( xlsBorder ) );

//        final XSSFColor color = workbook.getCustomPalette().getColor(
//                borderColor );
        XSSFColor color = null;
        if("top".equals(type))
        {
            color = cellStyle.getTopBorderXSSFColor();
        }
        else if("right".equals(type))
        {
            color = cellStyle.getRightBorderXSSFColor();
        }
        else if("bottom".equals(type))
        {
            color = cellStyle.getBottomBorderXSSFColor();
        }
        else
        {
            color = cellStyle.getLeftBorderXSSFColor();
        }
        
//              borderColor );
        if ( color != null )
        {
            borderStyle.append( ' ' );
            borderStyle.append( getRGBHex(color.getRGB()) );
        }

        style.append( "border-" + type + ":" + borderStyle + ";" );
    }

    void buildStyle_font( XSSFWorkbook workbook, StringBuilder style,
            XSSFFont font )
    {
        if(font.getBold()){
        	style.append( "font-weight:bold;" );
        }
        
//        final XSSFColor fontColor = workbook.getCustomPalette().getColor(
//                font.getColor() );
        final XSSFColor fontColor = font.getXSSFColor();
        if ( fontColor != null )
            style.append( "color: " + getRGBHex(fontColor.getRGB())
                    + "; " );

        if ( font.getFontHeightInPoints() != 0 )
            style.append( "font-size:" + font.getFontHeightInPoints() + "pt;" );

        if ( font.getItalic() )
        {
            style.append( "font-style:italic;" );
        }
    }

    public String getCssClassPrefixCell()
    {
        return cssClassPrefixCell;
    }

    public String getCssClassPrefixDiv()
    {
        return cssClassPrefixDiv;
    }

    public String getCssClassPrefixRow()
    {
        return cssClassPrefixRow;
    }

    public String getCssClassPrefixTable()
    {
        return cssClassPrefixTable;
    }

    public Document getDocument()
    {
        return htmlDocumentFacade.getDocument();
    }

    protected String getStyleClassName( XSSFWorkbook workbook,
            XSSFCellStyle cellStyle )
    {
        final Short cellStyleKey = Short.valueOf( cellStyle.getIndex() );

        String knownClass = excelStyleToClass.get( cellStyleKey );
        if ( knownClass != null )
            return knownClass;

        String cssStyle = buildStyle( workbook, cellStyle );
        String cssClass = htmlDocumentFacade.getOrCreateCssClass(
                cssClassPrefixCell, cssStyle );
        excelStyleToClass.put( cellStyleKey, cssClass );
        return cssClass;
    }

    public boolean isUseDivsToSpan()
    {
        return useDivsToSpan;
    }

    protected boolean processCell( XSSFCell cell, Element tableCellElement,
            int normalWidthPx, int maxSpannedWidthPx, float normalHeightPt )
    {
        final XSSFCellStyle cellStyle = cell.getCellStyle();

        String value;
        switch ( cell.getCellTypeEnum())
        {
        case STRING:
            value = cell.getRichStringCellValue().getString();
            break;
        case FORMULA:
            switch ( cell.getCachedFormulaResultTypeEnum() )
            {
            case STRING:
                XSSFRichTextString str = cell.getRichStringCellValue();
                if ( str != null && str.length() > 0 )
                {
                    value = ( str.toString() );
                }
                else
                {
                    value = XssfExcelToHtmlUtils.EMPTY;
                }
                break;
            case NUMERIC:
                XSSFCellStyle style = cellStyle;
                if ( style == null )
                {
                    value = String.valueOf( cell.getNumericCellValue() );
                }
                else
                {
                    value = ( _formatter.formatRawCellContents(
                            cell.getNumericCellValue(), style.getDataFormat(),
                            style.getDataFormatString() ) );
                }
                break;
            case BOOLEAN:
                value = String.valueOf( cell.getBooleanCellValue() );
                break;
            case ERROR:
                value = ErrorEval.getText( cell.getErrorCellValue() );
                break;
            default:
                logger.log(
                        POILogger.WARN,
                        "Unexpected cell cachedFormulaResultType ("
                                + cell.getCachedFormulaResultTypeEnum() + ")" );
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
            logger.log( POILogger.WARN,
                    "Unexpected cell type (" + cell.getCellTypeEnum() + ")" );
            return true;
        }

        final boolean noText = XssfExcelToHtmlUtils.isEmpty( value );
        final boolean wrapInDivs = !noText && isUseDivsToSpan()
                && !cellStyle.getWrapText();

        final short cellStyleIndex = cellStyle.getIndex();
        if ( cellStyleIndex != 0 )
        {
            XSSFWorkbook workbook = cell.getRow().getSheet().getWorkbook();
            String mainCssClass = getStyleClassName( workbook, cellStyle );
            if ( wrapInDivs )
            {
                tableCellElement.setAttribute( "class", mainCssClass + " "
                        + cssClassContainerCell );
            }
            else
            {
                tableCellElement.setAttribute( "class", mainCssClass );
            }

            if ( noText )
            {
                /*
                 * if cell style is defined (like borders, etc.) but cell text
                 * is empty, add "&nbsp;" to output, so browser won't collapse
                 * and ignore cell
                 */
                value = "\u00A0";
            }
        }

        if ( isOutputLeadingSpacesAsNonBreaking() && value.startsWith( " " ) )
        {
            StringBuilder builder = new StringBuilder();
            for ( int c = 0; c < value.length(); c++ )
            {
                if ( value.charAt( c ) != ' ' )
                    break;
                builder.append( '\u00a0' );
            }

            if ( value.length() != builder.length() )
                builder.append( value.substring( builder.length() ) );

            value = builder.toString();
        }

        Text text = htmlDocumentFacade.createText( value );

        if ( wrapInDivs )
        {
            Element outerDiv = htmlDocumentFacade.createBlock();
            outerDiv.setAttribute( "class", this.cssClassContainerDiv );

            Element innerDiv = htmlDocumentFacade.createBlock();
            StringBuilder innerDivStyle = new StringBuilder();
            innerDivStyle.append( "position:absolute;min-width:" );
            innerDivStyle.append( normalWidthPx );
            innerDivStyle.append( "px;" );
            if ( maxSpannedWidthPx != Integer.MAX_VALUE )
            {
                innerDivStyle.append( "max-width:" );
                innerDivStyle.append( maxSpannedWidthPx );
                innerDivStyle.append( "px;" );
            }
            innerDivStyle.append( "overflow:hidden;max-height:" );
            innerDivStyle.append( normalHeightPt );
            innerDivStyle.append( "pt;white-space:nowrap;" );
            XssfExcelToHtmlUtils.appendAlign( innerDivStyle,
                    cellStyle.getAlignmentEnum().getCode());
            htmlDocumentFacade.addStyleClass( outerDiv, cssClassPrefixDiv,
                    innerDivStyle.toString() );

            innerDiv.appendChild( text );
            outerDiv.appendChild( innerDiv );
            tableCellElement.appendChild( outerDiv );
        }
        else
        {
            tableCellElement.appendChild( text );
        }

        return XssfExcelToHtmlUtils.isEmpty( value ) && cellStyleIndex == 0;
    }

    protected void processColumnHeaders( XSSFSheet sheet, int maxSheetColumns,
            Element table )
    {
        Element tableHeader = htmlDocumentFacade.createTableHeader();
        table.appendChild( tableHeader );

        Element tr = htmlDocumentFacade.createTableRow();

        if ( isOutputRowNumbers() )
        {
            // empty row at left-top corner
            tr.appendChild( htmlDocumentFacade.createTableHeaderCell() );
        }

        for ( int c = 0; c < maxSheetColumns; c++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( c ) )
                continue;

            Element th = htmlDocumentFacade.createTableHeaderCell();
            String text = getColumnName( c );
            th.appendChild( htmlDocumentFacade.createText( text ) );
            tr.appendChild( th );
        }
        tableHeader.appendChild( tr );
    }

    /**
     * Creates COLGROUP element with width specified for all columns. (Except
     * first if <tt>{@link #isOutputRowNumbers()}==true</tt>)
     */
    protected void processColumnWidths( XSSFSheet sheet, int maxSheetColumns,
            Element table )
    {
        // draw COLS after we know max column number
        Element columnGroup = htmlDocumentFacade.createTableColumnGroup();
        if ( isOutputRowNumbers() )
        {
            columnGroup.appendChild( htmlDocumentFacade.createTableColumn() );
        }
        for ( int c = 0; c < maxSheetColumns; c++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( c ) )
                continue;

            Element col = htmlDocumentFacade.createTableColumn();
            col.setAttribute( "width",
                    String.valueOf( getColumnWidth( sheet, c ) ) );
            columnGroup.appendChild( col );
        }
        table.appendChild( columnGroup );
    }

    protected void processDocumentInformation(
            SummaryInformation summaryInformation )
    {
        if ( XssfExcelToHtmlUtils.isNotEmpty( summaryInformation.getTitle() ) )
            htmlDocumentFacade.setTitle( summaryInformation.getTitle() );

        if ( XssfExcelToHtmlUtils.isNotEmpty( summaryInformation.getAuthor() ) )
            htmlDocumentFacade.addAuthor( summaryInformation.getAuthor() );

        if ( XssfExcelToHtmlUtils.isNotEmpty( summaryInformation.getKeywords() ) )
            htmlDocumentFacade.addKeywords( summaryInformation.getKeywords() );

        if ( XssfExcelToHtmlUtils.isNotEmpty( summaryInformation.getComments() ) )
            htmlDocumentFacade
                    .addDescription( summaryInformation.getComments() );
    }

    /**
     * @return maximum 1-base index of column that were rendered, zero if none
     */
    protected int processRow( CellRangeAddress[][] mergedRanges, XSSFRow row,
            Element tableRowElement )
    {
        final XSSFSheet sheet = row.getSheet();
        final short maxColIx = row.getLastCellNum();
        if ( maxColIx <= 0 )
            return 0;

        final List<Element> emptyCells = new ArrayList<Element>( maxColIx );

        if ( isOutputRowNumbers() )
        {
            Element tableRowNumberCellElement = htmlDocumentFacade
                    .createTableHeaderCell();
            processRowNumber( row, tableRowNumberCellElement );
            emptyCells.add( tableRowNumberCellElement );
        }

        int maxRenderedColumn = 0;
        for ( int colIx = 0; colIx < maxColIx; colIx++ )
        {
            if ( !isOutputHiddenColumns() && sheet.isColumnHidden( colIx ) )
                continue;

            CellRangeAddress range = XssfExcelToHtmlUtils.getMergedRange(
                    mergedRanges, row.getRowNum(), colIx );

            if ( range != null
                    && ( range.getFirstColumn() != colIx || range.getFirstRow() != row
                            .getRowNum() ) )
                continue;

            XSSFCell cell = row.getCell( colIx );

            int divWidthPx = 0;
            if ( isUseDivsToSpan() )
            {
                divWidthPx = getColumnWidth( sheet, colIx );

                boolean hasBreaks = false;
                for ( int nextColumnIndex = colIx + 1; nextColumnIndex < maxColIx; nextColumnIndex++ )
                {
                    if ( !isOutputHiddenColumns()
                            && sheet.isColumnHidden( nextColumnIndex ) )
                        continue;

                    if ( row.getCell( nextColumnIndex ) != null
                            && !isTextEmpty( row.getCell( nextColumnIndex ) ) )
                    {
                        hasBreaks = true;
                        break;
                    }

                    divWidthPx += getColumnWidth( sheet, nextColumnIndex );
                }

                if ( !hasBreaks )
                    divWidthPx = Integer.MAX_VALUE;
            }

            Element tableCellElement = htmlDocumentFacade.createTableCell();

            if ( range != null )
            {
                if ( range.getFirstColumn() != range.getLastColumn() )
                    tableCellElement.setAttribute(
                            "colspan",
                            String.valueOf( range.getLastColumn()
                                    - range.getFirstColumn() + 1 ) );
                if ( range.getFirstRow() != range.getLastRow() )
                    tableCellElement.setAttribute(
                            "rowspan",
                            String.valueOf( range.getLastRow()
                                    - range.getFirstRow() + 1 ) );
            }

            boolean emptyCell;
            if ( cell != null )
            {
                emptyCell = processCell( cell, tableCellElement,
                        getColumnWidth( sheet, colIx ), divWidthPx,
                        row.getHeight() / 20f );
            }
            else
            {
                emptyCell = true;
            }

            if ( emptyCell )
            {
                emptyCells.add( tableCellElement );
            }
            else
            {
                for ( Element emptyCellElement : emptyCells )
                {
                    tableRowElement.appendChild( emptyCellElement );
                }
                emptyCells.clear();

                tableRowElement.appendChild( tableCellElement );
                maxRenderedColumn = colIx;
            }
        }

        return maxRenderedColumn + 1;
    }

    protected void processRowNumber( XSSFRow row,
            Element tableRowNumberCellElement )
    {
        tableRowNumberCellElement.setAttribute( "class", "rownumber" );
        Text text = htmlDocumentFacade.createText( getRowName( row ) );
        tableRowNumberCellElement.appendChild( text );
    }

    protected void processSheet( XSSFSheet sheet )
    {
        processSheetHeader( htmlDocumentFacade.getBody(), sheet );

        final int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        if ( physicalNumberOfRows <= 0 )
            return;

        Element table = htmlDocumentFacade.createTable();
        htmlDocumentFacade.addStyleClass( table, cssClassPrefixTable,
                "border-collapse:collapse;border-spacing:0;" );

        Element tableBody = htmlDocumentFacade.createTableBody();

        final CellRangeAddress[][] mergedRanges = XssfExcelToHtmlUtils
                .buildMergedRangesMap( sheet );

        final List<Element> emptyRowElements = new ArrayList<Element>(
                physicalNumberOfRows );
        int maxSheetColumns = 1;
        for ( int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++ )
        {
            XSSFRow row = sheet.getRow( r );

            if ( row == null )
                continue;

            if ( !isOutputHiddenRows() && row.getZeroHeight() )
                continue;

            Element tableRowElement = htmlDocumentFacade.createTableRow();
            htmlDocumentFacade.addStyleClass( tableRowElement,
                    cssClassPrefixRow, "height:" + ( row.getHeight() / 20f )
                            + "pt;" );

            int maxRowColumnNumber = processRow( mergedRanges, row,
                    tableRowElement );

            if ( maxRowColumnNumber == 0 )
            {
                emptyRowElements.add( tableRowElement );
            }
            else
            {
                if ( !emptyRowElements.isEmpty() )
                {
                    for ( Element emptyRowElement : emptyRowElements )
                    {
                        tableBody.appendChild( emptyRowElement );
                    }
                    emptyRowElements.clear();
                }

                tableBody.appendChild( tableRowElement );
            }
            maxSheetColumns = Math.max( maxSheetColumns, maxRowColumnNumber );
        }

        processColumnWidths( sheet, maxSheetColumns, table );

        if ( isOutputColumnHeaders() )
        {
            processColumnHeaders( sheet, maxSheetColumns, table );
        }

        table.appendChild( tableBody );

        htmlDocumentFacade.getBody().appendChild( table );
    }

    protected void processSheetHeader( Element htmlBody, XSSFSheet sheet )
    {
        Element h2 = htmlDocumentFacade.createHeader2();
        h2.appendChild( htmlDocumentFacade.createText( sheet.getSheetName() ) );
        htmlBody.appendChild( h2 );
    }

    public void processWorkbook( XSSFWorkbook workbook )
    {
//        final SummaryInformation summaryInformation = workbook.get
//                .getSummaryInformation();
//        if ( summaryInformation != null )
//        {
//            processDocumentInformation( summaryInformation );
//        }

        if ( isUseDivsToSpan() )
        {
            // prepare CSS classes for later usage
            this.cssClassContainerCell = htmlDocumentFacade
                    .getOrCreateCssClass( cssClassPrefixCell,
                            "padding:0;margin:0;align:left;vertical-align:top;" );
            this.cssClassContainerDiv = htmlDocumentFacade.getOrCreateCssClass(
                    cssClassPrefixDiv, "position:relative;" );
        }

        for ( int s = 0; s < workbook.getNumberOfSheets(); s++ )
        {
            XSSFSheet sheet = workbook.getSheetAt( s );
            processSheet( sheet );
        }

        htmlDocumentFacade.updateStylesheet();
    }

    public void setCssClassPrefixCell( String cssClassPrefixCell )
    {
        this.cssClassPrefixCell = cssClassPrefixCell;
    }

    public void setCssClassPrefixDiv( String cssClassPrefixDiv )
    {
        this.cssClassPrefixDiv = cssClassPrefixDiv;
    }

    public void setCssClassPrefixRow( String cssClassPrefixRow )
    {
        this.cssClassPrefixRow = cssClassPrefixRow;
    }

    public void setCssClassPrefixTable( String cssClassPrefixTable )
    {
        this.cssClassPrefixTable = cssClassPrefixTable;
    }

    /**
     * Allows converter to wrap content into two additional DIVs with tricky
     * styles, so it will wrap across empty cells (like in Excel).
     * <p>
     * <b>Warning:</b> after enabling this mode do not serialize result HTML
     * with INDENT=YES option, because line breaks will make additional
     * (unwanted) changes
     */
    public void setUseDivsToSpan( boolean useDivsToSpan )
    {
        this.useDivsToSpan = useDivsToSpan;
    }
    
    public String getRGBHex( byte[] rgb) {
        StringBuffer sb = new StringBuffer();
        sb.append( '#' );
        //rgb==null,��ʱindex=64
        if(rgb == null) {
           return "black";
        }
        for(byte c : rgb) {
           int i = (int)c;
           if(i < 0) {
              i += 256;
           }
           String cs = Integer.toHexString(i);
           if(cs.length() == 1) {
              sb.append('0');
           }
           sb.append(cs);
        }
        
        String result = sb.toString();
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
}

