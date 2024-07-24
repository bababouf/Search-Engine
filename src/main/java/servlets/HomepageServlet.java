package servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "HomepageServlet", value = "/home")

public class HomepageServlet extends HttpServlet {

   // This servlet is invoked when a GET request is sent to the /home endpoint
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        setResponseHeaders(response);
        setDefaultDirectoryPath();
        setDefaultDirectoryType();
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
   This method sets a context variable that specifies the directory that the application will use to query. Since we are
   in the homepage servlet, this is initially set to the default directory. If the user chooses to upload their own corpus
   (directory), this context variable will be set to specify that directory. This context variable, which can be accessed
   across all servlets, allows the application to know which corpus should be used to process queries over.
    */
    public void setDefaultDirectoryType(){
        ServletContext context = getServletContext();
        context.setAttribute("directoryType", "default_directory");
    }

    /*
    This method will set the path to the default corpus "all-nps-sites-extracted". The path set depends on whether the
    application is ran locally or on the Azure hosted server. If running locally, the AZURE_PATH variable that is
    retrieved will be null and the hardcoded path to the corpus on my filesystem is used.
     */
    public void setDefaultDirectoryPath() {
        ServletContext context = getServletContext();
        String defaultDirectoryPath = "";

        // Attempt to retrieve AZURE_PATH environment variable
        String azurePath = System.getenv("AZURE_PATH");
        System.out.println("AzurePath: " + azurePath);

        // This selection is triggered if the Azure path exists (application is running on Azure server)
        if (azurePath != null && !azurePath.isEmpty())
        {
            defaultDirectoryPath = azurePath;
        }
        // This selection is triggered if the application is running locally
        else
        {
            defaultDirectoryPath = "C:/Users/agreg/Desktop/Copy of Project/search-engine/all-nps-sites-extracted";
        }

        context.setAttribute("path", defaultDirectoryPath);
        System.out.println("defaultDirectoryPath = " + defaultDirectoryPath);

    }


}