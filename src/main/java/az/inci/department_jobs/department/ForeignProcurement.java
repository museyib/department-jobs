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
        int firstRow;
        for(int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            Sheet sheet = workbook.getSheetAt(i);
            firstRow = sheet.getFirstRowNum();

            if(firstRow >= 0)
            {
                SheetData sheetData = getSheetData(firstRow, i, sheet);

                String lastCompany = null;
                int parentRowId = 0;
                int parentRowNum = 0;

                for(int r = firstRow + 1; r <= sheet.getLastRowNum(); r++)
                {
                    Row row = sheet.getRow(r);
                    RowData rowData = new RowData();
                    rowData.setCellDataList(new ArrayList<>());
                    RowData parentRow = null;
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
                                boolean isChildRow = cell != cellFromMergedRegion;
                                String stringValue = getStringValue(cell);
                                if(stringValue.isEmpty())
                                {
                                    cell = cellFromMergedRegion;
                                    stringValue = getStringValue(cell);
                                }

                                if(n == initialColumn + 2)
                                {
                                    if(r == firstRow + 1)
                                    {
                                        lastCompany = stringValue;
                                    }
                                    else
                                    {
                                        if(lastCompany != null && lastCompany.equals(stringValue) && isChildRow)
                                        {
                                            String tag = i + "-" + parentRowId + "-" + stringValue;
                                            rowData.setParentRowId(parentRowId);
                                            rowData.setChild(true);
                                            rowData.setClassName(tag);
                                            parentRow = sheetData.getRowDataList().get(parentRowId);
                                            parentRow.setParent(true);
                                            parentRow.setTag(tag);
                                        }
                                        else
                                        {
                                            lastCompany = stringValue;
                                            parentRowId = r - firstRow - 1 + parentRowNum;
                                        }
                                    }
                                }
                                CellData cellData = new CellData();
                                cellData.setCol(n);
                                cellData.setData(stringValue);
                                if(sheetData.getName().equals("Yükləmə gözləyən") && n >= 4 && n <= 7)
                                    cellData.setSummable(true);
                                if(sheetData.getName().equals("Yolda olanlar") && n >= 5 && n <= 8)
                                    cellData.setSummable(true);

                                rowData.addCellData(cellData);

                                if(r == sheet.getLastRowNum() && stringValue.equalsIgnoreCase("toplam"))
                                {
                                    rowData.setFooter(true);
                                }
                            }
                        }
                    }
                    List<RowData> rowDataList = sheetData.getRowDataList();
                    if(rowData.isChild() &&
                       rowDataList.size()-rowDataList.indexOf(parentRow)==1)
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
