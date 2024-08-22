package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modules.documents.DirectoryCorpus;
import modules.indexing.AzureBlobPositionalIndex;
import modules.indexing.AzureBlobStorageClient;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        return new AzureBlobPositionalIndex(directoryType, containerName, queryMode);
    }

    public static DirectoryCorpus createCorpus(ServletContext context)
    {

        DirectoryCorpus corpus = null;
        String directoryType = (String) context.getAttribute("directoryType");

        if (directoryType == "default_directory")
        {
            corpus = handleDefaultCorpusCreation(corpus, context);
        }
        else
        {
            corpus = handleUploadedCorpusCreation(corpus, context);
        }

        corpus.getDocuments();
        return corpus;
    }

    public static DirectoryCorpus handleDefaultCorpusCreation(DirectoryCorpus corpus, ServletContext context)
    {
        String pathString = (String) context.getAttribute("directoryPath");
        System.out.println("Path string: " + pathString);
        Path absolutePathToCorpus = Paths.get(pathString).toAbsolutePath();
        System.out.println("Absolute path: " + absolutePathToCorpus);
        return DirectoryCorpus.loadJsonDirectory(absolutePathToCorpus, ".json");

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
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to unzip the file", e);
        }

        System.out.println("File extension: " + fileExtension);
        if (Objects.equals(fileExtension, ".json"))
        {
            System.out.println("We should be in here or big issue. ");
            corpus = DirectoryCorpus.loadJsonDirectory(tempDirectory, ".json");
        }
        else
        {
            System.out.println("No way we in here");
            corpus = DirectoryCorpus.loadTextDirectory(tempDirectory, ".txt");
        }
        return corpus;
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

    public static class Page
    {
        String title;
        String url;

        Page(String title, String url)
        {
            this.title = title;
            this.url = url;

        }
    }

}

