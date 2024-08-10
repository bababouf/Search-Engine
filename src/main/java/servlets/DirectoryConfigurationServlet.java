package servlets;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

import static servlets.ServletUtilities.parseRequestBody;

@WebServlet(name = "DirectoryConfigurationServlet", value = "/configure")

public class DirectoryConfigurationServlet extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String path = setDefaultDirectoryPath();
        setDefaultContextVariables(path);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        JsonObject requestBody = parseRequestBody(request);
        String containerName = requestBody.get("containerName").getAsString();
        updateContextVariables(containerName);
    }

    public void setDefaultContextVariables(String path)
    {
        ServletContext context = getServletContext();
        context.setAttribute("queryMode", "boolean");
        context.setAttribute("directoryType", "default_directory");
        context.setAttribute("containerName", "se-indexing-files");
        context.setAttribute("directoryPath", path);
    }

    public String setDefaultDirectoryPath()
    {
        String defaultDirectoryPath = "";

        String azurePath = System.getenv("AZURE_PATH");
        System.out.println("AzurePath: " + azurePath);

        if (azurePath != null && !azurePath.isEmpty())
        {
            defaultDirectoryPath = azurePath;
        }
        else
        {
            defaultDirectoryPath = "C:/Users/agreg/Desktop/CopyOfProject/search-engine/all-nps-sites-extracted";
        }

        return defaultDirectoryPath;
    }

    public void updateContextVariables(String containerName)
    {
        ServletContext context = getServletContext();
        context.setAttribute("directoryType", "user_directory");
        context.setAttribute("containerName", containerName);
        context.setAttribute("directoryPath", "none");
    }


}