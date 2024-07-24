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
import modules.indexing.AzureBlobPositionalIndex;
import modules.indexing.Posting;
import com.google.gson.JsonObject;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modules.queries.BooleanQueryParser;
import modules.queries.PhraseLiteral;
import modules.queries.QueryComponent;


@WebServlet(name = "BooleanSearchServlet", value = "/search/booleansearch")
public class BooleanSearchServlet extends HttpServlet {

    public DirectoryCorpus corpus;
    public AzureBlobPositionalIndex index;

    /*
    This servlet is triggered when a GET request is made to the /search/booleansearch endpoint. Specifically, when the user
    selects the boolean retrieval mode option, this request is made.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        index = createDiskPositionalIndex();
        corpus = createCorpus();
    }

    /*
    This servlet is invoked when POST requests are made to the /search/booleansearch endpoint. This request is made when
    the user submits a query in the search bar on the boolean search page.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Parses the HTTP request and obtains the query entered in the searchbar by the user
        String query = getQuery(request);

        // Calls a method that will obtain the documents that satisfy the user's query
        List<Posting> queryPostings = processBooleanQuery(query, index);

        // Converts the list of postings into JSON string to be sent to the browser
        String results = setupResults(queryPostings, corpus);

        // Sends the results obtained above to the browser
        ServletUtilities.sendResultsToBrowser(results, response);
    }

    //Returns the query parameter's value that was sent in the GET request
    public String getQuery(HttpServletRequest request) throws IOException {
        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        return jsonBody.get("query").getAsString();
    }

    /*
    This method creates an AzureBlobPositionalIndex object which contains information regarding which corpus is being used,
    as well as the index representing that corpus
     */
    public AzureBlobPositionalIndex createDiskPositionalIndex() {
        ServletContext context = getServletContext();

        // Accesses the context variable containing the directory type (indicating which corpus is being used)
        String directoryType = (String) context.getAttribute("directoryType");

        // Creates the index based on which directory the user is attempting to query
        return new AzureBlobPositionalIndex(directoryType);
    }


    public DirectoryCorpus createCorpus() {
        ServletContext context = getServletContext();

        // Obtains the context variable containing the path to the corpus that will be queried
        String pathString = (String) context.getAttribute("path");
        Path directoryPath = Path.of(pathString);
        System.out.println("Corpus path:" + directoryPath);

        // Creates the DirectoryCorpus object which is used when displaying the results
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(directoryPath, ".json");
        corpus.getDocuments();
        return corpus;
    }

    // Returns a list of postings that satisfy the boolean query
    public List<Posting> processBooleanQuery(String query, AzureBlobPositionalIndex index) throws IOException {
        BooleanQueryParser booleanParser = new BooleanQueryParser();

        // Parses the query (see parseQuery method for detailed information on how this is done)
        QueryComponent queryComponent = booleanParser.parseQuery(query);
        List<Posting> queryPostings;

        // Phrase queries require positions to be examined when determining which postings should be returned
        if (queryComponent instanceof PhraseLiteral phraseLiteral)
        {
            queryPostings = phraseLiteral.getPostingsWithPositions(index);

        }
        // All other query methods do not require positions
        else
        {
            queryPostings = queryComponent.getPostings(index);
        }

        return queryPostings;
    }

    /*
    Takes the list of postings that was returned from the processBooleanQuery method as a parameter, as well as
    the corpus. For each posting, the title of the document and URL are obtained, and packaged as a "page" object. This
    method returns a list of pages that are then converted into JSON string to be sent to the browser.
     */
    public String setupResults(List<Posting> queryPostings, DirectoryCorpus corpus) {

        List<Page> pages = new ArrayList<>();

        // Traverse through each posting returned in the results (query postings)
        for (Posting queryPosting : queryPostings)
        {
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