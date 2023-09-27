package az.inci.department_jobs.department;

import az.inci.department_jobs.model.ReportData;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Workbook;

import static az.inci.department_jobs.ExcelUtil.decimalFormat;

public class Production extends ReportDataFetcher
{
    @Override
    public ReportData fetchReportData(@NonNull Workbook workbook)
    {
        decimalFormat = "%,.2f";
        return super.fetchReportData(workbook);
    }
}
