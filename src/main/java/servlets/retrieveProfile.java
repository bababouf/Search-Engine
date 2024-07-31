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

@MultipartConfig
@WebServlet(name = "retrieveProfile", value = "/retrieveProfile")
public class retrieveProfile extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        String firstName = (String) session.getAttribute("firstName");
        String profileURL = (String) session.getAttribute("profileURL");
        String uniqueID = (String) session.getAttribute("uniqueID");
        List<String> userDirectories = (List<String>) session.getAttribute("userDirectories");
        System.out.println("Userdirectories: " + userDirectories);
        Profile userProfile = new Profile(uniqueID, firstName, profileURL, userDirectories);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(userProfile);
        out.print(jsonResponse);
        out.flush();

    }

    public static class Profile {
        String id;
        String firstname;
        String url;
        List<String> directories;

        Profile(String uniqueID, String firstName, String profileURL, List<String> userDirectories) {
            firstname = firstName;
            url = profileURL;
            id = uniqueID;
            directories = userDirectories;

        }

    }


}
