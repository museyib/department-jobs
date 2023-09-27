package az.inci.department_jobs.department;

import az.inci.department_jobs.model.*;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;

import static az.inci.department_jobs.ExcelUtil.*;
import static az.inci.department_jobs.ExcelUtil.getStringValue;

public class ForeignProcurement extends ReportDataFetcher
{
    @Override
    public ReportData fetchReportData(@NonNull Workbook workbook)
    {
        decimalFormat = "%,.2f";
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

                String rowTitle = null;
                int parentRowId = 0;
                int parentRowNum = 0;

                for(int rowId = firstRowId + 1; rowId <= sheet.getLastRowNum(); rowId++)
                {
                    Row row = sheet.getRow(rowId);
                    RowData rowData = new RowData();
                    rowData.setCellDataList(new ArrayList<>());
                    RowData parentRow = null;
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
                                boolean isChildRow = cell != cellFromMergedRegion;
                                String stringValue = getStringValue(cell);
                                if(stringValue.isEmpty())
                                {
                                    cell = cellFromMergedRegion;
                                    stringValue = getStringValue(cell);
                                }

                                if(columnId == initialColumn + 2)
                                {
                                    if(rowId == firstRowId + 1)
                                    {
                                        rowTitle = stringValue;
                                    }
                                    else
                                    {
                                        if(rowTitle != null && rowTitle.equals(stringValue) && isChildRow)
                                        {
                                            String tag = sheetId + "-" + parentRowId + "-" + stringValue;
                                            rowData.setParentRowId(parentRowId);
                                            rowData.setChild(true);
                                            rowData.setClassName(tag);
                                            parentRow = sheetData.getRowDataList().get(parentRowId);
                                            parentRow.setParent(true);
                                            parentRow.setTag(tag);
                                        }
                                        else
                                        {
                                            rowTitle = stringValue;
                                            parentRowId = rowId - firstRowId - 1 + parentRowNum;
                                        }
                                    }
                                }
                                CellData cellData = new CellData();
                                cellData.setCol(columnId);
                                cellData.setData(stringValue);

                                if(sheetData.getSummableColumns().contains(columnId))
                                    cellData.setSummable(true);

                                if(sheetData.getVisibleColumns().contains(columnId))
                                    cellData.setVisible(true);

                                rowData.addCellData(cellData);

                                if(rowId == sheet.getLastRowNum() && stringValue.equalsIgnoreCase("toplam"))
                                {
                                    rowData.setFooter(true);
                                }
                            }
                        }
                    }
                    List<RowData> rowDataList = sheetData.getRowDataList();
                    if(rowData.isChild() &&
                       rowDataList.size() - rowDataList.indexOf(parentRow) == 1)
                    {
                        sheetData.addRowData(RowData.childCopyOf(parentRow, parentRowId));
                        parentRowNum++;
                    }
                    sheetData.addRowData(rowData);
                }

                reportData.addSheet(sheetData);
            }
        }

        return reportData;
    }
}
