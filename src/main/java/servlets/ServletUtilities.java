package servlets;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import modules.database.PostgresDB;
import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.AzureBlobPositionalIndex;
import modules.indexing.AzureBlobStorageClient;
import modules.indexing.BlobStorageWriter;
import modules.indexing.PositionalInvertedIndex;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ServletUtilities
{

    /*
     This method takes the request passed as a parameter and reads it line by line. It then converts it into a JSON
     object and returns it.
     */
    public static JsonObject parseRequestBody(HttpServletRequest request) throws IOException
    {
        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            requestBody.append(line);
        }

        return JsonParser.parseString(requestBody.toString()).getAsJsonObject();
    }


    // This method writes the JSON data to the response stream, and sends the data to the browser
    public static void sendResultsToBrowser(String results, HttpServletResponse response) throws IOException
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(results);
        out.flush();
    }

    /*
    Obtains the directories associated with the passed user ID. Returns a list of the directory names.
     */
    public static List<Directory> getUserDirectories(String uniqueID)
    {
        // Connect to Azure Storage and find user directories associated with the hashed ID
        AzureBlobStorageClient client = new AzureBlobStorageClient();

        // Obtain the directories associated with the user ID
        List<String> containerNames = client.listContainers();

        List<Directory> userDirectories = new ArrayList<>();

        // Add each container name to a list
        for (String containerName : containerNames)
        {
            if (containerName.contains(uniqueID))
            {
                String directoryName = containerName.substring(containerName.indexOf("-") + 1);
                Directory directory = new Directory(containerName, directoryName);
                userDirectories.add(directory);
            }
        }
        return userDirectories;
    }

    /*
    This method obtains the directoryType and containerName of the directory that will be queried, and then passes these
    variables to the AzureBlobPositionalIndex so that the directory files can be downloaded
     */
    public static AzureBlobPositionalIndex createPositionalIndex(ServletContext context) throws IOException
    {
        // Accesses the context variable containing the directory type (indicating which corpus is being used)
        String directoryType = (String) context.getAttribute("directoryType");
        String containerName = (String) context.getAttribute("containerName");
        String queryMode = (String) context.getAttribute("queryMode");

        // Creates the index based on which directory the user is attempting to query
        System.out.println("Creating Azure Blob Positional Index under Directory: " + directoryType);
        return new AzureBlobPositionalIndex(directoryType, containerName, queryMode);
    }

    public static DirectoryCorpus createCorpus(ServletContext context)
    {

        DirectoryCorpus corpus = null;
        String directoryType = (String) context.getAttribute("directoryType");

        if (directoryType == "default_directory")
        {
            corpus = handleDefaultCorpusCreation(context);
        }
        else
        {
            corpus = handleUploadedCorpusCreation(corpus, context);
        }

        corpus.getDocuments();
        return corpus;
    }

    public static DirectoryCorpus handleDefaultCorpusCreation( ServletContext context)
    {
        String pathString = (String) context.getAttribute("directoryPath");
        System.out.println("Path string: " + pathString);
        Path absolutePathToCorpus = Paths.get(pathString).toAbsolutePath();
        System.out.println("Absolute path: " + absolutePathToCorpus);

        return DirectoryCorpus.loadDefaultJsonDirectory(absolutePathToCorpus, ".json");

    }

    public static DirectoryCorpus handleUploadedCorpusCreation(DirectoryCorpus corpus, ServletContext context)
    {
        String containerName = (String) context.getAttribute("containerName");
        AzureBlobStorageClient client = new AzureBlobStorageClient(containerName);

        // Download the zip file as a byte array
        byte[] serializedCorpus = client.downloadFile("zipped-directory.zip");

        // Create a temporary directory to unzip the contents
        Path tempDirectory;
        try
        {
            tempDirectory = Files.createTempDirectory("unzippedCorpus");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create temporary directory", e);
        }

        String fileExtension = null;

        // Unzip the downloaded file into the temporary directory
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedCorpus);
             ZipInputStream zis = new ZipInputStream(bais))
        {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null)
            {
                if (!zipEntry.isDirectory())
                {
                    Path filePath = tempDirectory.resolve(zipEntry.getName());


                    System.out.println("Filename: " + filePath);
                    // Ensure the parent directories exist
                    Files.createDirectories(filePath.getParent());

                    // Write the unzipped file to the temporary directory
                    try (OutputStream os = Files.newOutputStream(filePath))
                    {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0)
                        {
                            os.write(buffer, 0, len);
                        }
                    }

                    // Get the file extension of the first file
                    if (fileExtension == null)
                    {
                        String fileName = filePath.getFileName().toString();
                        fileExtension = fileName.substring(fileName.lastIndexOf("."));
                        context.setAttribute("fileExtension", fileExtension);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to unzip the file", e);
        }


        if (Objects.equals(fileExtension, ".json"))
        {
            byte[] keyFileData = client.downloadFile("selectedKey.txt");

            // Convert the byte array to a string (the selected key)
            String selectedKey = new String(keyFileData, StandardCharsets.UTF_8).trim();
            corpus = DirectoryCorpus.loadDefaultJsonDirectory(tempDirectory, ".json");
            corpus.setJSONType(selectedKey);
        }
        else
        {
            System.out.println("No way we in here");
            corpus = DirectoryCorpus.loadTextDirectory(tempDirectory, ".txt");
        }
        return corpus;
    }
    public static String createTemporaryUploadDirectory(String containerName) throws IOException {

        // Check if the environment variable is set
        String tempUploadDir = System.getenv("TEMP_UPLOAD_DIR");
        Path uploadDirPath;

        // If the environment variable is not set, use Java's temporary directory
        if (tempUploadDir == null) {
            // Create a temporary directory in the default temp directory (or use the containerName to create a subdirectory)
            Path tempDir = Files.createTempDirectory(containerName);
            uploadDirPath = tempDir;
            System.out.println("Using system temporary directory: " + tempDir.toString());
        } else {
            // If the environment variable is set, use that directory
            uploadDirPath = Paths.get(tempUploadDir, containerName);
            File uploadDir = new File(uploadDirPath.toString());

            // If the directory doesn't exist, create it
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    System.err.println("Failed to create directory.");
                } else {
                    System.out.println("Created directory at path: " + uploadDirPath.toString());
                }
            } else {
                System.out.println("Directory already exists at path: " + uploadDirPath.toString());
            }
        }

        // Return the directory path as a string, with forward slashes
        return uploadDirPath.toString().replace("\\", "/");
    }
    public static void handleUploadingFiles(HttpServletRequest request, String uploadedDirectoryPath, ServletContext context) throws ServletException, IOException
    {

        String directoryName = request.getParameter("directoryName");
        HttpSession session = request.getSession();
        session.setAttribute("directoryName", directoryName);

        int count = 0;
        for (Part part : request.getParts())
        {
            String fileName = getFileName(part);

            if (fileName != null && !fileName.isEmpty())
            {
                if(count == 0)
                {
                    String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

                    context.setAttribute("fileExtension", fileExtension);
                    System.out.println("THE FILE EXTENSION!!!!: " + fileExtension);
                }
                count++;
                String filePath = uploadedDirectoryPath + File.separator + fileName;
                /*

                System.out.println("File Extension: " + fileExtension);

                 */
                //count++;
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

    public static Path zipUploadDirectory(String uploadedDirectory) throws IOException
    {
        Path zippedDirPath = createZipDirectory().resolve("uploadedDirectory.zip");

        Path uploadedDirectoryPath = Paths.get(uploadedDirectory).toAbsolutePath();


        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zippedDirPath)))
        {
            Files.walkFileTree(uploadedDirectoryPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
                {
                    Path targetFile = uploadedDirectoryPath.relativize(file);

                    zipOutputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException
                {
                    Path targetDir = uploadedDirectoryPath.relativize(dir);
                    if (!targetDir.toString().isEmpty())
                    {

                        zipOutputStream.putNextEntry(new ZipEntry(targetDir + "/"));
                        zipOutputStream.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        }
        catch (IOException e)
        {

            throw e;
        }

        return zippedDirPath;
    }

    public static Path createZipDirectory()
    {
        String projectRoot = System.getProperty("java.io.tmpdir"); // Use the system temp directory
        String zippedDirectoryPath = projectRoot + File.separator + "zipped-upload-dir";
        File uploadDir = new File(zippedDirectoryPath);

        // If the directory doesn't exist, create it
        if (!uploadDir.exists())
        {
            boolean created = uploadDir.mkdirs();
            if (!created)
            {

                throw new RuntimeException("Failed to create directory.");
            }
        }
        else
        {

        }

        return Paths.get(zippedDirectoryPath).toAbsolutePath();
    }

    public static void uploadZipDirectory(Path zippedFilePath, String containerName)
    {
        AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(containerName);
        BlobContainerClient containerClient = blobStorageClient.getContainerClient();

        // Ensure containerClient is properly initialized
        if (containerClient == null)
        {
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
        if (!file.exists())
        {
            System.err.println("File does not exist: " + normalizedFilePath);
            return;
        }

        try
        {
            // Upload the file
            blobClient.uploadFromFile(normalizedFilePath, true);
            System.out.println("File uploaded successfully: " + normalizedFilePath);
        }
        catch (Exception e)
        {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getFileName(Part part)
    {
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

    public static void buildAndStoreIndexFiles(String uploadedDirectory, String containerName, HttpServletRequest request, ServletContext context) throws IOException, SQLException
    {
        // Get the key that was passed
        String selectedKey = request.getParameter("key");
        System.out.println("THE SELECTED KEY!!!: " + selectedKey);
        if(selectedKey == null)
        {
            selectedKey = "body";
            System.out.println("THE SELECTED KEY!!!: " + selectedKey);
        }

        // Connects to blob storage client
        AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(containerName);

        List<Long> bytePositions = null;
        PositionalInvertedIndex index = null;

        // Retrieves the absolute path the the uploaded directory containing the user's uploaded files
        Path absolutePathToDirectory = Paths.get(uploadedDirectory).toAbsolutePath();
        System.out.println("ABSOLUTE PATH TO DIRECTORY: " + absolutePathToDirectory);

        // Creates the corpus object that will be used in the indexing process

        DirectoryCorpus corpus = null;

        String extension = (String) context.getAttribute("fileExtension");
        if(Objects.equals(extension, "json"))
        {
            corpus = DirectoryCorpus.loadDefaultJsonDirectory(absolutePathToDirectory, ".json");
            corpus.setJSONType(selectedKey);
            BlobStorageWriter.uploadSelectedKey(blobStorageClient, selectedKey);
        }
        else
        {
            corpus = DirectoryCorpus.loadTextDirectory(absolutePathToDirectory, ".txt");
        }

        // Creates the index and meta data files; stores files in blob storage
        index = PositionalInvertedIndex.indexCorpus(corpus, blobStorageClient);
        bytePositions = BlobStorageWriter.serializeAndUploadIndex(index, blobStorageClient);


        writeBytePositions(index, "user_directory", bytePositions, containerName);

    }
    public static void writeBytePositions(PositionalInvertedIndex index, String databaseName, List<Long> bytePositions, String containerName)
    {
        List<String> vocabulary = index.getVocabulary();
        PostgresDB database = new PostgresDB(databaseName);
        database.setTableName(containerName);
        database.dropTable();
        database.createTable();
        database.insertTermsBatch(vocabulary, bytePositions);
    }

    public static FileResult createFileResult(Document document)
    {
        FileResult result = null;
        if(document.getURL() == null)
        {
            Reader reader = document.getContent();
            String content = createFileHelper(reader);
            result = new FileResult(document.getTitle(), content, null);
        }
        else
        {
            result = new FileResult(document.getTitle(), null, document.getURL());
        }

        return result;
    }

    public static String createFileHelper(Reader reader){
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder content = new StringBuilder();
        String line;

        // Read each line of the file and append it to the StringBuilder
        try
        {
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line).append("\n"); // Append a newline character
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(); // Handle the exception appropriately
        }
        finally
        {
            try
            {

                bufferedReader.close(); // Ensure the reader is closed
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return content.toString();
    }


    public static class Directory
    {
        String containerName;
        String name;


        Directory(String containerName, String name)
        {
            this.containerName = containerName;
            this.name = name;

        }

    }

    public static class FileResult
    {
        String title;
        String content;
        String url;

        FileResult(String title, String content, String url)
        {

            this.title = title;
            this.content = content;
            this.url = url;

        }
    }

}

