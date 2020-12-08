package root.report.excel.customize;

import java.util.Arrays;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class XssfExcelToHtmlUtils extends XssfAbstractExcelUtils
{
    public static void appendAlign( StringBuilder style, short alignment )
    {
        String cssAlign = getAlign( alignment );
        if ( isEmpty( cssAlign ) )
            return;

        style.append( "text-align:" );
        style.append( cssAlign );
        style.append( ";" );
    }

    /**
     * Creates a map (i.e. two-dimensional array) filled with ranges. Allow fast
     * retrieving {@link CellRangeAddress} of any cell, if cell is contained in
     * range.
     * 
     * @see #getMergedRange(CellRangeAddress[][], int, int)
     */
    public static CellRangeAddress[][] buildMergedRangesMap( XSSFSheet sheet )
    {
        CellRangeAddress[][] mergedRanges = new CellRangeAddress[1][];
        for ( int m = 0; m < sheet.getNumMergedRegions(); m++ )
        {
            final CellRangeAddress cellRangeAddress = sheet.getMergedRegion( m );

            final int requiredHeight = cellRangeAddress.getLastRow() + 1;
            if ( mergedRanges.length < requiredHeight )
            {
                CellRangeAddress[][] newArray = new CellRangeAddress[requiredHeight][];
                System.arraycopy( mergedRanges, 0, newArray, 0,
                        mergedRanges.length );
                mergedRanges = newArray;
            }

            for ( int r = cellRangeAddress.getFirstRow(); r <= cellRangeAddress
                    .getLastRow(); r++ )
            {
                final int requiredWidth = cellRangeAddress.getLastColumn() + 1;

                CellRangeAddress[] rowMerged = mergedRanges[r];
                if ( rowMerged == null )
                {
                    rowMerged = new CellRangeAddress[requiredWidth];
                    mergedRanges[r] = rowMerged;
                }
                else
                {
                    final int rowMergedLength = rowMerged.length;
                    if ( rowMergedLength < requiredWidth )
                    {
                        final CellRangeAddress[] newRow = new CellRangeAddress[requiredWidth];
                        System.arraycopy( rowMerged, 0, newRow, 0,
                                rowMergedLength );

                        mergedRanges[r] = newRow;
                        rowMerged = newRow;
                    }
                }

                Arrays.fill( rowMerged, cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn() + 1, cellRangeAddress );
            }
        }
        return mergedRanges;
    }

}

