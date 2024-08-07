package servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import com.azure.storage.blob.BlobContainerClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import modules.documents.DirectoryCorpus;
import modules.indexing.AzureBlobStorageClient;
import modules.indexing.BlobStorageWriter;
import modules.indexing.PositionalInvertedIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@MultipartConfig
@WebServlet(name = "UploadDirServlet", value = "/upload")
public class UploadDirServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UploadDirServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String uploadedDirectory = createTemporaryUploadDirectory();
            handleUploadingFiles(request, uploadedDirectory);
            String containerName = setContainerName(request);
            logger.info("Container Name : " + containerName);

            // Index the uploaded directory
            // Upload index.bin and other meta data (.bin files) to Blob Storage Container (using directoryIdentifier)
            // Zip the upload directory and upload this to Blob Storage Container (using same directoryIdentifier)


            buildAndStoreIndexFiles(uploadedDirectory, containerName);






            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Files uploaded successfully");
        } catch (Exception e) {
            logger.error("Error during file upload", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during file upload");
        }
    }

    private String setContainerName(HttpServletRequest request) {
        String directoryName = request.getParameter("directoryName");
        HttpSession session = request.getSession(false);
        if (session != null) {
            String id = (String) session.getAttribute("uniqueID");
            if (id != null && directoryName != null) {
                String fullDirectory = id + "-" + directoryName;
                System.out.println("Full Directory: " + fullDirectory);
                return fullDirectory;
            } else {
                System.err.println("Unique ID or Directory Name is null");
            }
        } else {
            System.err.println("Session is null");
        }
        return null;
    }

    private String createTemporaryUploadDirectory() {
        String projectRoot = "C:/Users/agreg/Desktop/Copy of Project/search-engine";
        String uploadedDirectoryPath = projectRoot + File.separator + "uploaded-dir";
        File uploadDir = new File(uploadedDirectoryPath);

        if (!uploadDir.exists()) {
            System.out.println("Directory doesn't exist. Creating directory...");
            boolean created = uploadDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory.");
            }
        } else {
            System.out.println("Directory already exists.");
        }

        return uploadedDirectoryPath;
    }

    private void handleUploadingFiles(HttpServletRequest request, String uploadedDirectoryPath) throws ServletException, IOException {
        String directoryName = request.getParameter("directoryName");
        HttpSession session = request.getSession();
        session.setAttribute("directoryName", directoryName);

        for (Part part : request.getParts()) {
            String fileName = getFileName(part);
            System.out.println("File Name: " + fileName);

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

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return null;
        }

        for (String cd : contentDisposition.split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return new File(fileName).getName();
            }
        }
        return null;
    }

    public void buildAndStoreIndexFiles(String uploadedDirectory, String containerName) throws IOException, SQLException {
        AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient();
        BlobContainerClient containerClient = blobStorageClient.createContainer(containerName);

        if(containerClient != null)
        {
            Path absolutePathToDirectory = Paths.get(uploadedDirectory).toAbsolutePath();
            System.out.println("Uploaded Directory Absolute Path: " + absolutePathToDirectory);
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToDirectory, ".json");
            System.out.println("Corpus Size: " + corpus.getCorpusSize());
            PositionalInvertedIndex index = PositionalInvertedIndex.indexCorpus(corpus, blobStorageClient);

            List<Long> bytePositions = BlobStorageWriter.serializeAndUploadIndex(index, blobStorageClient);
        }



        //BlobStorageWriter.writeBytePositions(index, bytePositions, "default_directory");
    }
}