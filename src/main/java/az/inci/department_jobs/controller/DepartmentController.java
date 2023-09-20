package az.inci.department_jobs.controller;

import az.inci.department_jobs.model.ReportData;
import az.inci.department_jobs.model.User;
import az.inci.department_jobs.service.DepartmentService;
import az.inci.department_jobs.service.security.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DepartmentController
{
    private final DepartmentService departmentService;
    private final UserService userService;

    @GetMapping("/")
    public String getDepartments(Model model)
    {
        User user = userService.getCurrentuser();
        List<String> fileList;
        fileList = departmentService.getDepartments(user.getScope());

        model.addAttribute("files", fileList);

        return "index";
    }

    @GetMapping("/content")
    public String getContent(Model model,
                             @RequestParam("department") String fileName,
                             @RequestParam(value = "password", required = false) String password,
                             @RequestParam(value = "encoded", required = false) boolean encoded)
    {
        User user = userService.getCurrentuser();
        String userScope = user.getScope();
        String department = fileName.substring(0, fileName.length() - 5);
        String template = departmentService.assignDepartment(department);

        if(userScope.equals("company") || department.equals(userScope))
        {
            if(departmentService.fileIsEncrypted(fileName))
            {
                if(password == null)
                    password = departmentService.getPassword(fileName);

                if(password != null)
                {
                    ReportData content = departmentService.getContentWithPassword(fileName, password, encoded);
                    if(content != null)
                    {
                        model.addAttribute("contentData", content);
                        return template;
                    }
                }
                model.addAttribute("error", true);
                return "input-password";
            }
            model.addAttribute("contentData", departmentService.getContent(fileName));
            return template;
        }

        return "error/403";
    }
}
