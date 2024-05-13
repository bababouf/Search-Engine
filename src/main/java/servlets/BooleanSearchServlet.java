package servlets;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
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


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("in bollean servlet");
        String query = getQuery(request);
        DiskPositionalIndex index = createDiskPositionalIndex();
        DirectoryCorpus corpus = createCorpus();
        List<Posting> queryPostings = processBooleanQuery(query, index);
        System.out.println("Query Postings: " + queryPostings.size());
        String results = setupResults(queryPostings, corpus);
        ServletUtilities.sendResultsToBrowser(results, response);
    }

    public String getQuery(HttpServletRequest request) throws IOException {
        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        return jsonBody.get("query").getAsString();
    }
    public DiskPositionalIndex createDiskPositionalIndex() {
        Path defaultPath = getDefaultPath();
        return new DiskPositionalIndex(defaultPath);
    }
    public DirectoryCorpus createCorpus() {
        Path defaultPath = getDefaultPath();
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(defaultPath, ".json");
        corpus.getDocuments();
        return corpus;
    }

    public List<Posting> processBooleanQuery(String query, DiskPositionalIndex index) throws IOException {
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

    public Path getDefaultPath(){
        ServletContext context = getServletContext();
        String path = (String) context.getAttribute("path");
        System.out.println(path);
        return Paths.get(path);
    }


    public String setupResults(List<Posting> queryPostings, DirectoryCorpus corpus) {
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
        return gson.toJson(pages);
    }

    public static class Page {
        String title;
        String url;
    }
}