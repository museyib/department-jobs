package az.inci.department_jobs.department;

import az.inci.department_jobs.model.*;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Workbook;

import static az.inci.department_jobs.ExcelUtil.decimalFormat;

public class Analytics extends ReportDataFetcher
{
    @Override
    public ReportData fetchReportData(@NonNull Workbook workbook)
    {
        decimalFormat = "%.0f";
        return super.fetchReportData(workbook);
    }
}
