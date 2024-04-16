package servlets;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import drivers.DiskPositionalIndexer;
import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.Posting;

import com.google.gson.JsonObject;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modules.indexing.DiskPositionalIndex;
import modules.queries.BooleanQueryParser;
import modules.queries.PhraseLiteral;
import modules.queries.QueryComponent;


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
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(path, ".json");
        corpus.getDocuments();
        List<Posting> queryPostings = processBooleanQuery(query, index);

        // somethin like this



        List<Page> pages = new ArrayList<>();

        for (Posting queryPosting : queryPostings) {

            int documentID = queryPosting.getDocumentId();
            Document document = corpus.getDocument(documentID);
            Page page = new Page();
            page.title = document.getTitle();
            page.url = document.getURL();
            pages.add(page);
        }


        Gson gson = new Gson();
        String json = gson.toJson(pages);
        System.out.println(json);

        response.setContentType("application/json");

        // Write JSON array string to response output stream
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();



    }

    public static List<Posting> processBooleanQuery(String query, DiskPositionalIndex index) throws IOException {
        BooleanQueryParser booleanParser = new BooleanQueryParser();
        QueryComponent queryComponent = booleanParser.parseQuery(query);
        List<Posting> queryPostings;
        if (queryComponent instanceof PhraseLiteral phraseLiteral) {

            queryPostings = phraseLiteral.getPostingsWithPositions(index);

        } else {
            queryPostings = queryComponent.getPostings(index);
        }
        return queryPostings;
    }

    public class Page{
        String title;
        String url;
    }
}