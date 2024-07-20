package servlets;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.AzureBlobPositionalIndex;
import modules.indexing.Posting;
import com.google.gson.JsonObject;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modules.indexing.AzureBlobPositionalIndex;
import modules.queries.BooleanQueryParser;
import modules.queries.PhraseLiteral;
import modules.queries.QueryComponent;


@WebServlet(name = "BooleanSearchServlet", value = "/search/booleansearch")
public class BooleanSearchServlet extends HttpServlet {


    // This servlet is invoked when POST requests are made to the /search/booleansearch endpoint
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = getQuery(request);
        AzureBlobPositionalIndex index = createDiskPositionalIndex();
        DirectoryCorpus corpus = createCorpus();
        List<Posting> queryPostings = processBooleanQuery(query, index);
        // returnTop15Postings(queryPostings);

        String results = setupResults(queryPostings, corpus);
        ServletUtilities.sendResultsToBrowser(results, response);
    }

    //Returns the query parameter's value that was sent in the GET request
    public String getQuery(HttpServletRequest request) throws IOException {
        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        return jsonBody.get("query").getAsString();
    }
    // Creates the DiskPositionalIndex object
    public AzureBlobPositionalIndex createDiskPositionalIndex() {
        ServletContext context = getServletContext();
        String directoryType = (String) context.getAttribute("directoryType");

        return new AzureBlobPositionalIndex(directoryType);
    }
    // Creates the DirectoryCorpus object
    public DirectoryCorpus createCorpus() {
        ServletContext context = getServletContext();
        Path directoryPath = (Path) context.getAttribute("path");
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(directoryPath, ".json");
        corpus.getDocuments();
        return corpus;
    }

    // Returns a list of postings that satisfy the boolean query
    public List<Posting> processBooleanQuery(String query, AzureBlobPositionalIndex index) throws IOException {
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

    public List<Posting> returnTop15Postings(List<Posting> queryPostings) {
        List<Posting> top15Postings = new ArrayList<>();


        return top15Postings;


    }

    // Returns the application-scope "path" variable
    public Path getDefaultPath(){
        ServletContext context = getServletContext();
        String path = (String) context.getAttribute("path");
        System.out.println(path);
        return Paths.get(path);
    }


    /*
    Takes the list of postings that was returned from the processBooleanQuery method as a parameter, as well as
    the corpus. For each posting, the title of the document and URL are obtained, and packaged as a "page" object. This
    method returns a list of pages.
     */
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
        return gson.toJson(pages); // Converts the list of "pages" into JSON
    }

    // Encapsulates a document
    public static class Page {
        String title;
        String url;
    }
}