package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;


public class ServletUtilities {

    public static JsonObject parseRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();

        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return JsonParser.parseString(requestBody.toString()).getAsJsonObject();
    }


}