package az.inci.department_jobs.model;

import lombok.Data;

import java.util.List;

@Data
public class ReportData {
    private List<SheetData> sheetDataList;

    public void addSheet(SheetData sheetData)
    {
        sheetDataList.add(sheetData);
    }
}
