package az.inci.department_jobs;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static org.apache.poi.ss.usermodel.CellType.FORMULA;

public class ExcelUtil
{
    static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    public static String decimalFormat = "%.2f";

    public static String getStringValue(Cell cell)
    {
        String data;

        if(cell.getCellType() == FORMULA)
            data = getForResultType(cell);
        else
            data = getForCellType(cell);

        return data;
    }

    private static String getFromNumeric(Cell cell)
    {
        String data;

        if(DateUtil.isCellDateFormatted(cell))
        {
            if(cell.getNumericCellValue() < 1)
                data = timeFormat.format(cell.getDateCellValue());
            else
                data = dateFormat.format(cell.getDateCellValue());
        }
        else
        {
            double value = cell.getNumericCellValue();
            data = String.format(Locale.ENGLISH, decimalFormat, value);
        }

        return data;
    }

    private static String getForCellType(Cell cell)
    {
        return getForType(cell, cell.getCellType());
    }

    private static String getForResultType(Cell cell)
    {
        return getForType(cell, cell.getCachedFormulaResultType());
    }

    private static String getForType(Cell cell, CellType cellType)
    {
        String data;
        switch(cellType)
        {
            case NUMERIC -> data = getFromNumeric(cell);
            case BOOLEAN -> data = String.valueOf(cell.getBooleanCellValue());
            case STRING -> data = cell.getStringCellValue();
            default -> data = "";
        }
        return data;
    }

    public static Cell getFirstCellFromMergedRegion(Sheet sheet, Cell cell)
    {
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

        for(CellRangeAddress cellAddresses : mergedRegions)
        {
            Cell firstCell = sheet.getRow(cellAddresses.getFirstRow()).getCell(cellAddresses.getFirstColumn());
            if(cell.getRowIndex() >= cellAddresses.getFirstRow() &&
               cell.getRowIndex() <= cellAddresses.getLastRow() &&
               cell.getColumnIndex() >= cellAddresses.getFirstColumn() &&
               cell.getColumnIndex() <= cellAddresses.getLastColumn())
            {
                return firstCell;
            }
        }

        return cell;
    }

    public static boolean isEncrypted(File file)
    {
        try
        {
            try
            {
                new POIFSFileSystem(file);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return true;
        }
        catch(OfficeXmlFileException e)
        {
            return false;
        }
    }
    public static int getInitialColumn(Row row)
    {
        int colNum = 0;

        while (true)
        {
            Cell cell = row.getCell(colNum);
            if (cell != null || colNum >= 16384)
                break;
            colNum++;
        }

        return colNum;
    }
}
