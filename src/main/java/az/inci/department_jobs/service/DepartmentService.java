package az.inci.department_jobs.service;

import az.inci.department_jobs.department.*;
import az.inci.department_jobs.model.ReportData;
import az.inci.department_jobs.model.User;
import az.inci.department_jobs.service.security.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
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

import static az.inci.department_jobs.ExcelUtil.isEncrypted;
import static az.inci.department_jobs.model.Scope.*;

@Service
@Slf4j
public class DepartmentService
{
    @Value("${data-source-folder}")
    private String rootFolder;

    private final UserService userService;

    private ReportDataFetcher dataFetcher;

    public String assignDepartment(String department)
    {
        String template;
        switch(department)
        {
            case foreign_procurement ->
            {
                template = "contents/foreign_procurement";
                dataFetcher = new ForeignProcurement();
            }
            case human_resources ->
            {
                template = "contents/human_resources";
                dataFetcher = new HumanResources();
            }
            case internal_procurement ->
            {
                template = "contents/internal_procurement";
                dataFetcher = new InternalProcurement();
            }
            case marketing ->
            {
                template = "contents/marketing";
                dataFetcher = new Marketing();
            }
            case analytics ->
            {
                template = "contents/analytics";
                dataFetcher = new Analytics();
            }
            case production ->
            {
                template = "contents/production";
                dataFetcher = new Production();
            }
            case finance ->
            {
                template = "contents/finance";
                dataFetcher = new Finance();
            }
            case law ->
            {
                template = "contents/law";
                dataFetcher = new Law();
            }
            default ->
            {
                template = "contents/default";
                dataFetcher = new ReportDataFetcher();
            }
        }

        return template;
    }

    public DepartmentService(UserService userService)
    {
        this.userService = userService;
    }

    public List<String> getDepartments(String scope)
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
                String fileName = file.getName();
                if(scope.equals(fileName.substring(0, file.getName().length() - 5))
                   || scope.equals("company"))
                    fileList.add(fileName);
            }
        }

        fileList.sort(String::compareToIgnoreCase);

        return fileList;
    }

    public ReportData getContent(String department)
    {
        File file = new File(rootFolder, department);
        ReportData reportData = null;
        try (Workbook workbook = new XSSFWorkbook(file)) {
            if(dataFetcher == null)
                dataFetcher = new ReportDataFetcher();
            reportData = dataFetcher.fetchReportData(workbook);
        } catch (IOException | InvalidFormatException e) {
            log.error(e.getMessage());
        }

        return reportData;
    }

    public ReportData getContentWithPassword(String department, String password, boolean encoded)
    {
        File file = new File(rootFolder, department);
        ReportData reportData = null;

        try  {
            Workbook workbook;
            if(isEncrypted(file))
            {
                POIFSFileSystem fileSystem = new POIFSFileSystem(new FileInputStream(file));
                EncryptionInfo encryptionInfo = new EncryptionInfo(fileSystem);
                Decryptor decryptor = Decryptor.getInstance(encryptionInfo);
                if(encoded)
                    password = new String(Base64.getDecoder().decode(password));
                boolean b = decryptor.verifyPassword(password);
                if(b)
                    workbook = new XSSFWorkbook(decryptor.getDataStream(fileSystem));
                else
                    return null;
            }
            else
                workbook = new XSSFWorkbook(file);

            if(dataFetcher == null)
                dataFetcher = new ReportDataFetcher();
            reportData = dataFetcher.fetchReportData(workbook);
        } catch (IOException | GeneralSecurityException | InvalidFormatException e) {
            log.error(e.getMessage());
        }

        return reportData;
    }

    public boolean fileIsEncrypted(String department)
    {
        return isEncrypted(new File(rootFolder, department));
    }

    public String getPassword(String fileName)
    {
        String password = null;

        try
        {
            for(User user : userService.getUsers())
            {
                if(fileName.equals(user.getScope() + ".xlsx"))
                    password = user.getPassword();
            }
        }
        catch(IOException e)
        {
            log.error(e.getMessage());
        }

        return password;
    }
}
