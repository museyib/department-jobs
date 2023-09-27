package az.inci.department_jobs.department;

import az.inci.department_jobs.ExcelUtil;
import az.inci.department_jobs.model.*;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;

import static az.inci.department_jobs.ExcelUtil.*;
import static az.inci.department_jobs.ExcelUtil.getStringValue;

public class ReportDataFetcher
{
    public ReportData fetchReportData(@NonNull Workbook workbook)
    {
        ReportData reportData = new ReportData();
        reportData.setSheetDataList(new ArrayList<>());
        int firstRow;
        for(int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            Sheet sheet = workbook.getSheetAt(i);
            firstRow = sheet.getFirstRowNum();

            if(firstRow >= 0)
            {
                SheetData sheetData = getSheetData(firstRow, i, sheet);

                for(int r = firstRow + 1; r <= sheet.getLastRowNum(); r++)
                {
                    Row row = sheet.getRow(r);
                    RowData rowData = new RowData();
                    rowData.setCellDataList(new ArrayList<>());
                    if (row != null)
                    {
                        rowData.setHeight(row.getHeightInPoints());
                        int initialColumn = getInitialColumn(row);
                        for (int n = initialColumn; n < row.getLastCellNum(); n++)
                        {
                            Cell cell = row.getCell(n);
                            if (cell != null)
                            {
                                Cell cellFromMergedRegion = getFirstCellFromMergedRegion(sheet, cell);
                                String stringValue = getStringValue(cell);
                                if(stringValue.isEmpty())
                                {
                                    cell = cellFromMergedRegion;
                                    stringValue = getStringValue(cell);
                                }
                                CellData cellData = new CellData();
                                cellData.setCol(n);
                                cellData.setData(stringValue);
                                rowData.addCellData(cellData);

                                if(r == sheet.getLastRowNum() && stringValue.equalsIgnoreCase("toplam"))
                                {
                                    rowData.setFooter(true);
                                }
                            }
                        }
                    }
                    sheetData.addRowData(rowData);
                }

                reportData.addSheet(sheetData);
            }
        }
        return reportData;
    }

    SheetData getSheetData(int firstRow, int i, Sheet sheet)
    {
        SheetData sheetData = new SheetData();
        sheetData.setRowDataList(new ArrayList<>());
        sheetData.setHeaders(new ArrayList<>());
        sheetData.setName(sheet.getSheetName());
        sheetData.setPriority(i);
        Row headerRow = sheet.getRow(firstRow);
        int initialColumn = getInitialColumn(headerRow);
        for(int n = initialColumn; n < headerRow.getLastCellNum(); n++)
        {
            Cell cell = headerRow.getCell(n);
            if(cell != null)
            {
                HeaderData headerData = new HeaderData();
                headerData.setCol(n);
                headerData.setText(getStringValue(cell));
                headerData.setWidth(sheet.getColumnWidthInPixels(n));
                sheetData.addHeader(headerData);
            }
        }
        return sheetData;
    }
}
