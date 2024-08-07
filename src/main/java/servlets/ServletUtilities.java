package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import modules.indexing.AzureBlobStorageClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ServletUtilities {

    /*
     This method takes the request passed as a parameter and reads it line by line. It then converts it into a JSON
     object and returns it.
     */
    public static JsonObject parseRequestBody(HttpServletRequest request) throws IOException {
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
    public static void sendResultsToBrowser(String results, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(results);
        out.flush();
    }

    // Returns the path to the root directory (directory containing pom.xml)
    public static String getProjectRootDir(String servletContextDir) {
        String currentDir = "C:/Users/agreg/Desktop/CopyOfProject/search-engine";
        while (currentDir != null && !new File(currentDir + File.separator + "pom.xml").exists()) {
            System.out.println("Checking directory: " + currentDir); // Debug statement
            currentDir = new File(currentDir).getParent();
        }
        if (currentDir == null) {
            System.out.println("Reached the top of the directory hierarchy without finding pom.xml");
        } else {
            System.out.println("Found pom.xml in directory: " + currentDir); // Debug statement
        }
        return currentDir;
    }

    public static List<String> getUserDirectories(String uniqueID){

        // Connect to Azure Storage and find user directories associated with the hashed ID
        AzureBlobStorageClient client = new AzureBlobStorageClient();

        // Obtain the directories associated with the user ID
        List<String> containerNames = client.listContainers();

        List<String> userDirectories = new ArrayList<>();
        for(String containerName : containerNames)
        {
            if(containerName.contains(uniqueID))
            {
                userDirectories.add(containerName);
            }
        }
        return userDirectories;
    }


}

