package az.inci.department_jobs.model;

import az.inci.department_jobs.ExcelUtil;
import lombok.Data;

import java.util.List;
import java.util.Locale;

import static az.inci.department_jobs.ExcelUtil.decimalFormat;

@Data
public class SheetData
{
    private int priority;
    private String name;
    private List<HeaderData> headers;
    private List<RowData> rowDataList;
    private List<Integer> summableColumns;
    private List<Integer> visibleColumns;

    public void addHeader(HeaderData header)
    {
        headers.add(header);
    }

    public void addRowData(RowData rowData)
    {
        rowDataList.add(rowData);
    }

    public void addSummableColumn(Integer columnId)
    {
        summableColumns.add(columnId);
    }

    public void addVisibleColumn(Integer columnId)
    {
        visibleColumns.add(columnId);
    }

    public String getTotalAmount(String tag, int col)
    {
        double total = 0;

        for (RowData rowData : rowDataList)
        {
            if(tag.equals(rowData.getClassName()))
            {
                double amount;
                try
                {
                    amount = Double.parseDouble(rowData.getCellDataList().get(col).getData().replace(",", ""));
                }
                catch(NumberFormatException e)
                {
                    amount = 0;
                }
                total += amount;
            }
        }

        return String.format(Locale.ENGLISH, decimalFormat, total);
    }
}
