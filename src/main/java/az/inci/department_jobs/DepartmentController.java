package az.inci.department_jobs;

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
    private final DepartmentService service;

    @GetMapping
    public String getDepartments(Model model)
    {
        List<String> fileList = service.getDepartments();
        model.addAttribute("files", fileList);

        return "index";
    }

    @GetMapping("/content")
    public String getContent(Model model,
                             @RequestParam("department") String department,
                             @RequestParam(value = "password", required = false) String password)
    {
        if(service.fileIsEncrypted(department))
        {
            if(password != null)
            {
                ReportData content = service.getContentWithPassword(department, password);
                if(content != null)
                {
                    model.addAttribute("contentData", service.getContentWithPassword(department, password));
                    return "content";
                }
                else
                {
                    model.addAttribute("error", true);
                    return "input-password";
                }
            }
            model.addAttribute("error", false);
            return "input-password";
        }
        model.addAttribute("contentData", service.getContent(department));
        return "content";
    }
}
