package az.inci.department_jobs;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributes;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static az.inci.department_jobs.ExcelUtil.*;

@Service
public class DepartmentService
{
    @Value("${data-source-folder}")
    private String rootFolder;

    public List<String> getDepartments()
    {
        File root = new File(rootFolder);
        List<String> fileList = new ArrayList<>();
        File[] files = root.listFiles(file -> {
            DosFileAttributes attributes;
            try
            {
                attributes = Files.readAttributes(file.toPath(), DosFileAttributes.class);
            }
            catch(IOException e)
            {
                return false;
            }
            return file.getName().endsWith(".xlsx")
                   && !attributes.isSystem()
                   && !attributes.isHidden();

        });
        if(files != null)
        {
            for(File file : files)
            {
                fileList.add(file.getName());
            }
        }

        return fileList;
    }

    public ReportData getContent(String department)
    {
        File file = new File(rootFolder, department);
        ReportData reportData = new ReportData();
        reportData.setSheetDataList(new ArrayList<>());

        try (Workbook workbook = new XSSFWorkbook(file)) {
            gatReportData(reportData, workbook);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(InvalidFormatException e)
        {
            throw new RuntimeException(e);
        }

        return reportData;
    }

    public ReportData getContentWithPassword(String department, String password)
    {
        File file = new File(rootFolder, department);
        ReportData reportData = new ReportData();
        reportData.setSheetDataList(new ArrayList<>());

        try  {
            Workbook workbook;
            if(isEncrypted(file))
            {
                POIFSFileSystem fileSystem = new POIFSFileSystem(new FileInputStream(file));
                EncryptionInfo encryptionInfo = new EncryptionInfo(fileSystem);
                Decryptor decryptor = Decryptor.getInstance(encryptionInfo);
                boolean b = decryptor.verifyPassword(new String(Base64.getDecoder().decode(password)));
                if(b)
                    workbook = new XSSFWorkbook(decryptor.getDataStream(fileSystem));
                else
                    return null;
            }
            else
                workbook = new XSSFWorkbook(file);

            gatReportData(reportData, workbook);
        } catch (IOException | GeneralSecurityException | InvalidFormatException e) {
            e.printStackTrace();
        }

        return reportData;
    }

    private void gatReportData(ReportData reportData, Workbook workbook)
    {
        int firstRow;
        int initialColumn;
        for(int i = 0; i < workbook.getNumberOfSheets(); i++)
        {
            Sheet sheet = workbook.getSheetAt(i);
            firstRow = sheet.getFirstRowNum();

            if(firstRow >= 0)
            {
                SheetData sheetData = new SheetData();
                sheetData.setRowDataList(new ArrayList<>());
                sheetData.setHeaders(new ArrayList<>());
                sheetData.setName(sheet.getSheetName());
                sheetData.setPriority(i);
                Row headerRow = sheet.getRow(firstRow);
                initialColumn = getInitialColumn(headerRow);
                for(int n = initialColumn; n < headerRow.getLastCellNum(); n++)
                {
                    Cell cell = headerRow.getCell(n);
                    if(cell != null)
                        sheetData.addHeader(getStringValue(cell));
                }

                String lastCompany = null;
                int parentRowId = 0;
                int parentRowNum = 0;

                for(int r = firstRow + 1; r < sheet.getLastRowNum(); r++)
                {
                    Row row = sheet.getRow(r);
                    RowData rowData = new RowData();
                    rowData.setCellDataList(new ArrayList<>());
                    RowData parentRow = null;
                    if (row != null)
                    {
                        initialColumn = getInitialColumn(row);
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
                                rowData.addCellData(cellData);
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
    }

    public boolean fileIsEncrypted(String department)
    {
        return isEncrypted(new File(rootFolder, department));
    }

    private int getInitialColumn(Row row)
    {
        int colNum = 0;

        while (true)
        {
            Cell cell = row.getCell(colNum);
            if (cell != null || colNum >= 16384)
                break;
            colNum++;
        }

        return colNum;
    }
}
