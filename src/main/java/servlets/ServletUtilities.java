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

    public static JsonObject parseRequestBody(HttpServletRequest request) throws IOException {
        /*
            BufferedReader and Strinbuilder are essential classes for reading data efficiently.
            BufferedReader is used for reading text from a character-input stream, buffering
            characters so as to provide for efficient reading of chars, arrays, and lines.
            It is commonly used to read text from files.
            It provides methods like read(), readLine(), and ready() for reading chars or lines from
            the input stream.

            Stringbuilder is used to create mutable sequences of chars. It's similar to string in c++
            but is mutable.
            It provides methods to append, insert, or delete chars in the sequence.
            It's particularly useful when needing to build strings dynamically, especially when
            you're reading and concatenating multiple strings.
            Unlike String, which is immutable in Java (once created you cant modify its content),
            Strinbuilder allows you to modify the sequence of chars it holds without creating
            new objects.
         */

        BufferedReader reader = request.getReader();

        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return JsonParser.parseString(requestBody.toString()).getAsJsonObject();
    }

    public static void sendResultsToBrowser(String results, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(results);
        out.flush();
    }
    public static String getProjectRootDir(String servletContextDir) {
        String currentDir = servletContextDir;
        while (currentDir != null && !new File(currentDir + File.separator + "pom.xml").exists()) {
            currentDir = new File(currentDir).getParent();
        }
        return currentDir;
    }
    public static String getFileName(Part part) {
        String filename = part.getSubmittedFileName();
        System.out.println(filename);
        String[] parts = filename.split("/");
        return parts[parts.length - 1];

    }

}