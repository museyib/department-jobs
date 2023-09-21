package az.inci.department_jobs.department;

import az.inci.department_jobs.model.ReportData;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Workbook;

public class Finance extends ReportDataFetcher
{
    @Override
    public ReportData fetchReportData(@NonNull Workbook workbook)
    {
        return super.fetchReportData(workbook);
    }
}
