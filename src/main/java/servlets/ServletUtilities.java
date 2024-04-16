package servlets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;


public class ServletUtilities {

    public static JsonObject parseRequestBody(BufferedReader reader) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return JsonParser.parseString(requestBody.toString()).getAsJsonObject();
    }


}