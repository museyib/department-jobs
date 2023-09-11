package az.inci.department_jobs.service.security;

import az.inci.department_jobs.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class UserService
{
    @Value("${meta-data-file-path}")
    private String metaDataFilePath;

    public User getById(String userId)
    {
        User user = new User();
        ObjectMapper objectMapper = new ObjectMapper();
        List<User> users;
        try
        {
            users = List.of(objectMapper.readValue(new File(metaDataFilePath), User[].class));
            for(User item : users)
            {
                if(item.getUserId().equals(userId))
                    return item;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return user;
    }

    public User getCurrentuser()
    {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails)
            username = ((UserDetails) principal).getUsername();
        else
            username = principal.toString();

        return getById(username);
    }

    public List<User> getUsers() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        return List.of(objectMapper.readValue(new File(metaDataFilePath), User[].class));
    }
}
