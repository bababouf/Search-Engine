package servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MultipartConfig
@WebServlet(name = "UploadDirServlet", value = "/upload")
public class UploadDirServlet extends HttpServlet
{

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            // Get servlet context
            ServletContext context = getServletContext();

            // Create the container name (unique ID + "-" + directoryName)
            String containerName = setContainerName(request);

            // Create a temporary directory in the project root to hold uploaded files
            String uploadedDirectory = ServletUtilities.createTemporaryUploadDirectory(containerName);

            // Store files sent across HTTP in the temporary directory
            ServletUtilities.handleUploadingFiles(request, uploadedDirectory, context);

            // Creates the index and stores all files in the Azure Blob container (using the container name created above)
            ServletUtilities.buildAndStoreIndexFiles(uploadedDirectory, containerName, request, context);

            // Zip the user uploaded directory, then upload to the blob container
            Path zippedFilePath = ServletUtilities.zipUploadDirectory(uploadedDirectory);
            ServletUtilities.uploadZipDirectory(zippedFilePath, containerName);

            // Set the user directories session variable
            setUserDirectories(request);

            // Cleanup; remove temporary uploaded-directory
            File uploadDir = new File(uploadedDirectory);
            removeTemporaryDirectory(uploadDir);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Files uploaded successfully");
        }
        catch (Exception e)
        {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during file upload");
        }
    }

    // Creates the container name (uniquely identifies a user's uploaded directory)
    private String setContainerName(HttpServletRequest request)
    {
        // Get the directory name
        String directoryName = request.getParameter("directoryName");

        // Ensure an active HTTP session
        HttpSession session = request.getSession(false);
        if (session != null)
        {
            // Obtain user's unique ID
            String id = (String) session.getAttribute("uniqueID");

            // Ensure there is both a unique ID and directoryName
            if (id != null && directoryName != null)
            {
                // Create and return the container name
                return id + "-" + directoryName;
            }
            else
            {
                System.err.println("Unique ID or Directory Name is null");
            }
        }
        else
        {
            System.err.println("Session is null");
        }
        return null;
    }

    public void setUserDirectories(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String uniqueID = (String) session.getAttribute("uniqueID");

        List<ServletUtilities.Directory> userDirectories = ServletUtilities.getUserDirectories(uniqueID);
        session.setAttribute("userDirectories", userDirectories);
    }

    public void removeTemporaryDirectory(File file)
    {
        File[] contents = file.listFiles();
        if (contents != null)
        {
            for (File f : contents)
            {
                if (!Files.isSymbolicLink(f.toPath()))
                {
                    removeTemporaryDirectory(f);
                }
            }
        }
        file.delete();
    }

}