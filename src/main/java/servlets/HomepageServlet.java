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
        String servletContextDir = context.getRealPath("/");
        String projectRoot = ServletUtilities.getProjectRootDir(servletContextDir); // This will give the project root (the directory that contains the pom.xml file).
        String defaultDirectoryPath = projectRoot + File.separator + "all-nps-sites-extracted";
        context.setAttribute("path", defaultDirectoryPath); // setAttribute() allows for setting application-scope vars
    }

}