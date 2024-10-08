package servlets;

import java.io.IOException;
import java.io.PrintWriter;
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
public class RetrieveProfile extends HttpServlet
{


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        // Obtain the HTTPSession object
        HttpSession session = request.getSession();

        // Obtain user information from session variables
        String firstName = (String) session.getAttribute("firstName");
        String profileURL = (String) session.getAttribute("profileURL");
        String uniqueID = (String) session.getAttribute("uniqueID");
        //List<ServletUtilities.Directory> userDirectories = (List<ServletUtilities.Directory>) session.getAttribute("userDirectories");
        List<ServletUtilities.Directory> userDirectories = ServletUtilities.getUserDirectories(uniqueID);
        //List<String> directoryNames = getDirectoryNames(userDirectories);

        // Store all of this information in a Profile object
        Profile userProfile = new Profile(uniqueID, firstName, profileURL, userDirectories);

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
    public static class Profile
    {
        String id;
        String firstname;
        String url;
        List<ServletUtilities.Directory> directories;

        Profile(String uniqueID, String firstName, String profileURL, List<ServletUtilities.Directory> userDirectories)
        {
            firstname = firstName;
            url = profileURL;
            id = uniqueID;
            directories = userDirectories;
        }

    }


}
