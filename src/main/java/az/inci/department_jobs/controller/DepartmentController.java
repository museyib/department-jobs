package az.inci.department_jobs.controller;

import az.inci.department_jobs.model.User;
import az.inci.department_jobs.service.DepartmentService;
import az.inci.department_jobs.model.ReportData;
import az.inci.department_jobs.service.security.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
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

        if(user.getScope().equals("company"))
            fileList = departmentService.getDepartments();
        else
            fileList = departmentService.getDepartmentsByScope(user.getScope());

        model.addAttribute("files", fileList);

        return "index";
    }

    @GetMapping("/content")
    public String getContent(Model model,
                             @RequestParam("department") String department,
                             @RequestParam(value = "password", required = false) String password,
                             @RequestParam(value = "encoded", required = false) boolean encoded)
    {
        User user = userService.getCurrentuser();
        String userScope = user.getScope();
        if(userScope.equals("company") || department.equals(userScope + ".xlsx"))
        {
            if(departmentService.fileIsEncrypted(department))
            {
                if(password == null)
                    password = departmentService.getPassword(department);

                if(password != null)
                {
                    ReportData content = departmentService.getContentWithPassword(department, password, encoded);
                    if(content != null)
                    {
                        model.addAttribute("contentData", content);
                        return "content";
                    }
                }
                model.addAttribute("error", true);
                return "input-password";
            }
            model.addAttribute("contentData", departmentService.getContent(department));
            return "content";
        }

        return "error/403";
    }
}
