package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import modules.documents.DirectoryCorpus;
import modules.indexing.DiskPositionalIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class ServletUtilities {

    /*
     This method takes the request passed as a parameter and reads it line by line. It then converts it into a JSON
     object and returns it.
     */
    public static JsonObject parseRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
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
        String currentDir = servletContextDir;
        while (currentDir != null && !new File(currentDir + File.separator + "pom.xml").exists()) {
            currentDir = new File(currentDir).getParent();
        }
        return currentDir;
    }
    // Returns the filename (file1.txt) from the part that was passed
    public static String getFileName(Part part) {
        String filename = part.getSubmittedFileName();
        System.out.println(filename);
        String[] parts = filename.split("/");
        return parts[parts.length - 1];

    }

}