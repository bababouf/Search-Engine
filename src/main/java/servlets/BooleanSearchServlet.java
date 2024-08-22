package servlets;

import java.io.*;
import java.util.*;

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
import servlets.ServletUtilities.Page;


@WebServlet(name = "BooleanSearchServlet", value = "/search/booleansearch")

public class BooleanSearchServlet extends HttpServlet
{

    public DirectoryCorpus corpus;
    public AzureBlobPositionalIndex index;

    /*
    This servlet is triggered when a GET request is made to the /search/booleansearch endpoint. Specifically, when the user
    selects the boolean retrieval mode option, this request is made.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Get servlet context, and set queryMode context variable
        ServletContext context = getServletContext();
        context.setAttribute("queryMode", "boolean");

        // Create positional index
        index = ServletUtilities.createPositionalIndex(context);

        // Create directory corpus
        corpus = ServletUtilities.createCorpus(context);
    }

    /*
    This servlet is invoked when POST requests are made to the /search/booleansearch endpoint. This request is made when
    the user submits a query in the search bar on the boolean search page.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // Parses the HTTP request and obtains the query entered in the searchbar by the user
        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        String query = jsonBody.get("query").getAsString();

        // Calls a method that will obtain the documents that satisfy the user's query
        List<Posting> queryPostings = processBooleanQuery(query, index);

        // Converts the list of postings into JSON string to be sent to the browser
        String results = setupResults(queryPostings, corpus);

        // Sends the results obtained above to the browser
        ServletUtilities.sendResultsToBrowser(results, response);
    }

    // Returns a list of postings that satisfy the boolean query
    public List<Posting> processBooleanQuery(String query, AzureBlobPositionalIndex index) throws IOException
    {
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
    public String setupResults(List<Posting> queryPostings, DirectoryCorpus corpus)
    {

        List<Page> pages = new ArrayList<>();

        // Traverse through each posting returned in the results (query postings)
        for (Posting queryPosting : queryPostings)
        {
            Document document = corpus.getDocument(queryPosting.getDocumentId());
            Page page = new Page(document.getTitle(), document.getURL());
            pages.add(page);
        }

        // Converts the list of "pages" into JSON
        Gson gson = new Gson();
        return gson.toJson(pages);
    }


}