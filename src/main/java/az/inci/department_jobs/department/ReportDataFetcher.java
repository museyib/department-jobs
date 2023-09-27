package az.inci.department_jobs.department;

import az.inci.department_jobs.model.*;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.stream.Stream;

import static az.inci.department_jobs.ExcelUtil.*;

public class ReportDataFetcher
{
    public ReportData fetchReportData(@NonNull Workbook workbook)
    {
        ReportData reportData = new ReportData();
        reportData.setSheetDataList(new ArrayList<>());
        int firstRowId;
        for(int sheetId = 0; sheetId < workbook.getNumberOfSheets(); sheetId++)
        {
            Sheet sheet = workbook.getSheetAt(sheetId);
            firstRowId = sheet.getFirstRowNum();

            if(firstRowId >= 0)
            {
                SheetData sheetData = getSheetData(firstRowId, sheetId, sheet);

                for(int rowId = firstRowId + 1; rowId <= sheet.getLastRowNum(); rowId++)
                {
                    Row row = sheet.getRow(rowId);
                    RowData rowData = new RowData();
                    rowData.setCellDataList(new ArrayList<>());
                    if (row != null)
                    {
                        rowData.setHeight(row.getHeightInPoints());
                        int initialColumn = getInitialColumn(row);
                        for (int columnId = initialColumn; columnId < row.getLastCellNum(); columnId++)
                        {
                            Cell cell = row.getCell(columnId);
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
                                cellData.setCol(columnId);
                                cellData.setData(stringValue);
                                rowData.addCellData(cellData);

                                if(rowId == sheet.getLastRowNum() && stringValue.equalsIgnoreCase("toplam"))
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

    SheetData getSheetData(int firstRowId, int sheetId, Sheet sheet)
    {
        SheetData sheetData = new SheetData();
        sheetData.setRowDataList(new ArrayList<>());
        sheetData.setHeaders(new ArrayList<>());
        sheetData.setSummableColumns(new ArrayList<>());
        sheetData.setVisibleColumns(new ArrayList<>());
        sheetData.setName(sheet.getSheetName());
        sheetData.setPriority(sheetId);
        Row headerRow = sheet.getRow(firstRowId);
        int initialColumn = getInitialColumn(headerRow);
        for(int columnId = initialColumn; columnId < headerRow.getLastCellNum(); columnId++)
        {
            Cell cell = headerRow.getCell(columnId);
            if(cell != null)
            {
                String value = getStringValue(cell);
                HeaderData headerData = new HeaderData();
                headerData.setCol(columnId);
                headerData.setText(value);
                headerData.setWidth(sheet.getColumnWidthInPixels(columnId));
                sheetData.addHeader(headerData);

                if(Stream.of("AMOUNT",
                             "DEPOSIT",
                             "BALANCE",
                             "REMAIN",
                             "QALIQ").anyMatch(value::equalsIgnoreCase))
                {
                    sheetData.addSummableColumn(columnId);
                }

                if(Stream.of("PORT",
                             "MIX or FULL").anyMatch(value::equalsIgnoreCase))
                {
                    sheetData.addVisibleColumn(columnId);
                }
            }
        }
        return sheetData;
    }
}
