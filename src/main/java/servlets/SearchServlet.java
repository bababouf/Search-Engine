package servlets;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(name = "SupServletServlet", value = "/search")
public class SearchServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        // Parse the JSON request body
        JsonObject jsonBody = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
        String query = jsonBody.get("query").getAsString();

        // Now you can access the query parameter value directly
        System.out.println("Received query: " + query);

        // You can also send a response back if needed
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("Received query: " + query);
    }
}