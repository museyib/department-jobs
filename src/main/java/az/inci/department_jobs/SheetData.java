package az.inci.department_jobs;

import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class SheetData
{
    private int priority;
    private String name;
    private List<String> headers;
    private List<RowData> rowDataList;

    public void addHeader(String header)
    {
        headers.add(header);
    }

    public void addRowData(RowData rowData)
    {
        rowDataList.add(rowData);
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
                    amount = Double.parseDouble(rowData.getCellDataList().get(col).getData());
                }
                catch(NumberFormatException e)
                {
                    amount = 0;
                }
                total += amount;
            }
        }

        return String.format(Locale.ENGLISH, "%.2f", total);
    }
}
