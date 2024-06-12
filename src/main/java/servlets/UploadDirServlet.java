package servlets;

import java.io.*;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import drivers.PositionalInvertedIndexer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import modules.documents.DirectoryCorpus;
import modules.indexing.DiskIndexWriter;
import modules.indexing.PositionalInvertedIndex;

/*
The @MultipartConfig is an annotation that may be specified on a Servlet class, indicating that instances
of the Servlet expect requests that conform to the multipart/form-data MIME type.

The multipart/form-data MIME type is typically used for file uploads. When a client sends a multipart request,
the request is broken up into multiple parts. Each part represents a piece of the request, which typically
corresponds to a file upload. One method that is provided by Multiconfig is .getParts(), which allows
access to each part of the multipart request. This allows for access to file names, content types,
and file contents, and the ability to perform actions like saving the uploaded files to this server.

 */
@MultipartConfig
@WebServlet(name = "UploadDirServletServlet", value = "/upload")
public class UploadDirServlet extends HttpServlet {

    private String uploadedDirectoryPath = "";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        createUploadDirectory();
        handleUploadingFiles(request);
        createIndex(uploadedDirectoryPath);

        setUploadDirectoryPath();


    }
    /*
    Jakarta EE provides a method called getServletContext that returns the real path of the root
    directory of a web application. This project uses Maven, however, and when Maven builds the project
    it compiles the sources code into a WAR (Web Application Archive) and places it in the "target" directory.
    As a result, getServletContext points to this "target" directory.

    To get to the actual root directory (search-engine) this method will traverse up the tree until it finds
    the directory where the pom.xml file is. The pom.xml file is in the project root.
   */

    /*
     This method gets the absolute path to the project root directory, and creates a directory
     within it to hold the uploaded files.
     */
    private void createUploadDirectory() {
        String servletContextDir = getServletContext().getRealPath("/");
        String projectRoot = ServletUtilities.getProjectRootDir(servletContextDir);
        uploadedDirectoryPath = projectRoot + File.separator + "uploaded-dir";
        File uploadDir = new File(uploadedDirectoryPath);

        if (!uploadDir.exists()) {
            System.out.println("Directory doesn't exist. Creating directory...");
            boolean created = uploadDir.mkdirs(); // Create the uploaded-dir folder
            if (!created) {
                System.err.println("Failed to create directory.");
            }
        } else {
            System.out.println("Directory already exists.");
        }
    }

    /*

     In HTTP, when a form contains <input type="file"> elements and the form is submitted with
     enctype="multipart/form-data", the request is sent as a multipart request, and each file
     uploaded is treated as a separate part .
     */
    private void handleUploadingFiles(HttpServletRequest request) throws ServletException, IOException {

        // Each part represents one uploaded file.
        for (Part part : request.getParts()) {
            String fileName = ServletUtilities.getFileName(part);
            if (fileName != null && !fileName.isEmpty()) {

                String filePath = uploadedDirectoryPath + File.separator + fileName;

                try {
                    part.write(filePath);
                    System.out.println("File saved successfully: " + fileName);
                } catch (IOException e) {
                    System.err.println("Error saving file " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    private void createIndex(String uploadDirPath) throws IOException {
        try {
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(Path.of(uploadDirPath), ".json");
            PositionalInvertedIndex index = PositionalInvertedIndexer.indexCorpus(corpus, Path.of(uploadDirPath)); // Creates positionalInvertedIndex
            DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
            List<Long> bytePositions = diskIndexWriter.writeIndex(index, Path.of(uploadDirPath));
            diskIndexWriter.writeTermBytePositionsToDatabase(index, bytePositions, uploadDirPath); // Write byte positions to SQLite DB
        } catch (IOException | SQLException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }

    }
    private void setUploadDirectoryPath(){
        ServletContext context = getServletContext();

        // Set a global variable
        context.setAttribute("directory", "uploaded");
        context.setAttribute("path", uploadedDirectoryPath);
    }

}