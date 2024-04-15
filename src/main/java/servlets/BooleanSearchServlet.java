package servlets;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import drivers.DiskPositionalIndexer;
import modules.indexing.Posting;

import com.google.gson.JsonObject;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modules.indexing.DiskPositionalIndex;


@WebServlet(name = "BooleanSearchServlet", value = "/search/booleansearch")
public class BooleanSearchServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello World!";
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        BufferedReader reader = request.getReader();
        JsonObject jsonBody = ServletUtilities.parseRequestBody(reader);
        String query = jsonBody.get("query").getAsString();
        Path path = Paths.get("C:/Users/agreg/IdeaProjects/search-engine/all-nps-sites-extracted");

        DiskPositionalIndex index = new DiskPositionalIndex(path);
        List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery(query, index);
        System.out.println("Oi" );

    }

    public void destroy() {
    }
}