package servlets;

import java.io.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "HomepageServlet", value = "/home")
public class HomepageServlet extends HttpServlet {

   // This servlet is invoked when a GET request is sent to the /home endpoint
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        setResponseHeaders(response);
        setDefaultPath();
        setDefaultDirectoryType();
        System.out.println("hi");
    }

    /*
    This method sets the response header to control caching in the browser. In the first call to the setHeader method,
    "Cache-Control" is the HTTP header, and the directive is set to no-cache, meaning the browser will not cache the response.
    This is necessary because without it, the browser would simply use the cached response when multiple requests are made to the servlet.
     */
    public void setResponseHeaders(HttpServletResponse response){
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Expires", "0");
    }

    /*
    This method will set the path to the default corpus "all-nps-sites-extracted". It does this by setting an application-scope variable "path".
    getServletContext() is a critical method in Jakarta EE applications. It will return an object that represents the entire web application, and
    provides useful information about the application environment.
     */
    public void setDefaultPath() {
        ServletContext context = getServletContext();
        String defaultDirectoryPath;


        // Check if running on Azure or locally
        String azurePath = System.getenv("AZURE_PATH");

        System.out.println("Azure Path: " + azurePath);// Environment variable for Azure path
        if (azurePath != null && !azurePath.isEmpty()) {
            // Use Azure path if available
            defaultDirectoryPath = azurePath + "/all-nps-sites-extracted";
        } else {
            // Use local path

            String realPath = context.getRealPath("/");
            defaultDirectoryPath = "C:/Users/agreg/Desktop/Copy of Project/search-engine/all-nps-sites-extracted";
        }

        // Optionally, check if this path exists
        File dir = new File(defaultDirectoryPath);
        if (!dir.exists()) {
            System.err.println("Directory does not exist: " + defaultDirectoryPath);
        } else {
            context.setAttribute("path", defaultDirectoryPath);
            System.out.println("defaultDirectoryPath = " + defaultDirectoryPath);
        }
    }
    public void setDefaultDirectoryType(){
        ServletContext context = getServletContext();
        context.setAttribute("directoryType", "default_directory");
    }

}