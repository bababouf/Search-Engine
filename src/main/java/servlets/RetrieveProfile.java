package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
This servlet is responsible for obtaining pieces of user information that will be displayed in the user profile page (when
the user successfully signs in). All of this information is stored in HTTPSession variables (which is carried out in the
LoginCallback servlet).
 */
@MultipartConfig
@WebServlet(name = "retrieveProfile", value = "/retrieveProfile")
public class RetrieveProfile extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Obtain the HTTPSession object
        HttpSession session = request.getSession();

        // Obtain user information from session variables
        String firstName = (String) session.getAttribute("firstName");
        String profileURL = (String) session.getAttribute("profileURL");
        String uniqueID = (String) session.getAttribute("uniqueID");
        List<String> userDirectories = (List<String>) session.getAttribute("userDirectories");

        List<String> directoryNames = getDirectoryNames(userDirectories);

        // Store all of this information in a Profile object
        Profile userProfile = new Profile(uniqueID, firstName, profileURL, directoryNames);

        // Send the profile object (converted to a JSON string) to the browser
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Convert profile object to a JSON string to send in the HTTP response
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(userProfile);
        out.print(jsonResponse);
        out.flush();

    }

    // Used to store all the user information that will be displayed in the user profile page
    public static class Profile {
        String id;
        String firstname;
        String url;
        List<String> directories;

        Profile(String uniqueID, String firstName, String profileURL, List<String> userDirectories)
        {
            firstname = firstName;
            url = profileURL;
            id = uniqueID;
            directories = userDirectories;
        }

    }

    /*
    Returns the directory names which is done by creating a substring starting at the index after the first occurence
    of a "-" (each uploaded directory is stored with the identifier built by concatenating uniqueID + "-" + directory name)
     */
    public List<String> getDirectoryNames(List<String> userDirectories)
    {
        List<String> directoryNames = new ArrayList<String>();
        for (String userDirectory : userDirectories)
        {

            try
            {
                System.out.println("Directory: " + userDirectory);
                String directoryName = userDirectory.substring(userDirectory.indexOf("-") + 1);
                directoryNames.add(directoryName);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return directoryNames;
    }


}
