package servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import modules.database.PostgresDB;
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
            // Create a temporary directory in the project root to hold uploaded files
            String uploadedDirectory = createTemporaryUploadDirectory();

            // Store files sent across HTTP in the temporary directory
            handleUploadingFiles(request, uploadedDirectory);

            // Create the container name (unique ID + "-" + directoryName)
            String containerName = setContainerName(request);

            // Creates the index and stores all files in the Azure Blob container (using the container name created above)
            buildAndStoreIndexFiles(uploadedDirectory, containerName);

            // Zip the user uploaded directory, then upload to the blob container
            Path zippedFilePath = zipUploadDirectory(uploadedDirectory);
            uploadZipDirectory(zippedFilePath, containerName);

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
    private String setContainerName(HttpServletRequest request) {

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

    // Create the temporary upload directory in the root of the project
    private String createTemporaryUploadDirectory() {
        String projectRoot = "C:/Users/agreg/Desktop/CopyOfProject/search-engine";

        // Append "uploaded-dir" to the root path
        String uploadedDirectoryPath = projectRoot + File.separator + "uploaded-dir";
        File uploadDir = new File(uploadedDirectoryPath);


        // If the directory doesn't exist, create it
        if (!uploadDir.exists())
        {
            boolean created = uploadDir.mkdirs();
            if (!created)
            {
                System.err.println("Failed to create directory.");
            }
        }
        else
        {
            System.out.println("Directory already exists.");
        }

        return uploadedDirectoryPath.replace("\\", "/");
    }

    // Retrieves each file from the form data sent in the HTTP request. Each file is store in the temporary upload directory
    private void handleUploadingFiles(HttpServletRequest request, String uploadedDirectoryPath) throws ServletException, IOException {
        String directoryName = request.getParameter("directoryName");
        HttpSession session = request.getSession();
        session.setAttribute("directoryName", directoryName);

        int count = 0;
        for (Part part : request.getParts())
        {
            String fileName = getFileName(part);

            if (fileName != null && !fileName.isEmpty() && count == 0)
            {
                String filePath = uploadedDirectoryPath + File.separator + fileName;
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                ServletContext context = getServletContext();
                context.setAttribute("fileExtension", fileExtension);
                count++;
                try
                {
                    part.write(filePath);
                    System.out.println("File saved successfully: " + fileName);
                }
                catch (IOException e)
                {
                    System.err.println("Error saving file " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    /*
    In a form data HTTP request, the content-disposition header contains information about each file (part) this is being
    sent. Documentation on content-disposition header: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null)
        {
            return null;
        }

        // Obtains the filename from content disposition header
        for (String cd : contentDisposition.split(";"))
        {
            if (cd.trim().startsWith("filename"))
            {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return new File(fileName).getName();
            }
        }
        return null;
    }

    /*
    This method will take the files from the uploaded directory, index them, and store the serialized index and its associated
    meta data blobs in the blob store container with the passed containerName.
     */
    public void buildAndStoreIndexFiles(String uploadedDirectory, String containerName) throws IOException, SQLException {

        // Connects to blob storage client
        AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(containerName);

        List<Long> bytePositions = null;
        PositionalInvertedIndex index = null;

        // Retrieves the absolute path the the uploaded directory containing the user's uploaded files
        Path absolutePathToDirectory = Paths.get(uploadedDirectory).toAbsolutePath();

        // Creates the corpus object that will be used in the indexing process
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToDirectory, ".json");

        // Creates the index and meta data files; stores files in blob storage
        index = PositionalInvertedIndex.indexCorpus(corpus, blobStorageClient);
        bytePositions = BlobStorageWriter.serializeAndUploadIndex(index, blobStorageClient);


        writeBytePositions(index, "user_directory", bytePositions, containerName);

    }
    private void writeBytePositions(PositionalInvertedIndex index, String databaseName, List<Long> bytePositions, String containerName){
        List<String> vocabulary = index.getVocabulary();
        PostgresDB database = new PostgresDB(databaseName);
        database.setTableName(containerName);
        database.dropTable();
        database.createTable();
        database.insertTermsBatch(vocabulary, bytePositions);
    }

    private Path zipUploadDirectory(String uploadedDirectory) throws IOException {
        Path zippedDirPath = createZipDirectory().resolve("uploadedDirectory.zip");

        Path uploadedDirectoryPath = Paths.get(uploadedDirectory).toAbsolutePath();
        logger.info("Zipping directory: " + uploadedDirectoryPath.toString());

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zippedDirPath))) {
            Files.walkFileTree(uploadedDirectoryPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Path targetFile = uploadedDirectoryPath.relativize(file);
                    logger.info("Adding file to zip: " + targetFile.toString());
                    zipOutputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    Path targetDir = uploadedDirectoryPath.relativize(dir);
                    if (!targetDir.toString().isEmpty()) {
                        logger.info("Adding directory to zip: " + targetDir.toString());
                        zipOutputStream.putNextEntry(new ZipEntry(targetDir.toString() + "/"));
                        zipOutputStream.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            logger.info("Directory zipped successfully.");
        } catch (IOException e) {
            logger.error("Error zipping directory: ", e);
            throw e;
        }

        return zippedDirPath;
    }

    private Path createZipDirectory() {
        String projectRoot = System.getProperty("java.io.tmpdir"); // Use the system temp directory
        String zippedDirectoryPath = projectRoot + File.separator + "zipped-upload-dir";
        File uploadDir = new File(zippedDirectoryPath);

        // If the directory doesn't exist, create it
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                logger.error("Failed to create directory.");
                throw new RuntimeException("Failed to create directory.");
            }
        } else {
            logger.info("Directory already exists.");
        }

        return Paths.get(zippedDirectoryPath).toAbsolutePath();
    }

    private void uploadZipDirectory(Path zippedFilePath, String containerName) {
        AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(containerName);
        BlobContainerClient containerClient = blobStorageClient.getContainerClient();

        // Ensure containerClient is properly initialized
        if (containerClient == null) {
            System.err.println("Container client is null. Check container name and connection.");
            return;
        }

        // Set the blob name to be a valid and simple name
        String blobName = "zipped-directory.zip";
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // Log and normalize file path
        String normalizedFilePath = zippedFilePath.toString().replace("\\", "/");
        System.out.println("Normalized file path: " + normalizedFilePath);

        // Check if the file exists
        File file = new File(normalizedFilePath);
        if (!file.exists()) {
            System.err.println("File does not exist: " + normalizedFilePath);
            return;
        }

        try {
            // Upload the file
            blobClient.uploadFromFile(normalizedFilePath, true);
            System.out.println("File uploaded successfully: " + normalizedFilePath);
        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUserDirectories(HttpServletRequest request){
        HttpSession session = request.getSession();
        String uniqueID = (String) session.getAttribute("uniqueID");

        List<ServletUtilities.Directory> userDirectories = ServletUtilities.getUserDirectories(uniqueID);
        session.setAttribute("userDirectories", userDirectories);
    }

    public void removeTemporaryDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null)
        {
            for (File f : contents)
            {
                if (! Files.isSymbolicLink(f.toPath()))
                {
                    removeTemporaryDirectory(f);
                }
            }
        }
        file.delete();
    }

}