package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.AzureBlobPositionalIndex;
import modules.misc.Entry;
import modules.rankingSchemes.*;
import servlets.ServletUtilities.FileResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static servlets.ServletUtilities.createFileResult;

@WebServlet(name = "RankedSearchServlet", value = "/search/rankedsearch")
public class RankedSearchServlet extends HttpServlet
{
    public DirectoryCorpus corpus;
    public AzureBlobPositionalIndex index;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Get servlet context, and set queryMode context variable
        ServletContext context = getServletContext();
        context.setAttribute("queryMode", "ranked");

        // Create positional index
        index = ServletUtilities.createPositionalIndex(context);

        // Create directory corpus
        corpus = ServletUtilities.createCorpus(context);
        System.out.println("Corpus Size in the GET: " + corpus.getCorpusSize());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // Parses HTTP request for the query and mode parameters passed in the body
        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        String query = jsonBody.get("query").getAsString();
        String mode = jsonBody.get("mode").getAsString();

        // Calls a dispatch method to handle the query (based on which mode was selected)
        String results = rankedModeDispatch(mode, query);

        // Send results obtained to the browser
        ServletUtilities.sendResultsToBrowser(results, response);
    }

    /*
    This method takes two strings as parameters: mode and query. A switch statement dispatches the work of
    ranking and returning the top 10 documents to various methods depending on the mode passed.
     */
    public String rankedModeDispatch(String mode, String query)
    {
        List<Entry> top10Ranked = null;

        // RankedDispatch class is created, passing the index and corpus
        RankedDispatch rankedAlgorithm = new RankedDispatch(index, corpus);

        // Call the calculate method for the appropriate mode that was selected
        switch (mode)
        {
            case "default-ranked" ->
            {
                System.out.println("In the default ranked. ");
                DefaultRanked defaultRanked = new DefaultRanked();
                top10Ranked = rankedAlgorithm.calculate(defaultRanked, query);
            }

            case "tfidf-ranked" ->
            {
                TermFreqInvDocFreqRanked tfidfRanked = new TermFreqInvDocFreqRanked();
                top10Ranked = rankedAlgorithm.calculate(tfidfRanked, query);
            }
            case "okapi-ranked" ->
            {
                OkapiRanked okapiRanked = new OkapiRanked();
                top10Ranked = rankedAlgorithm.calculate(okapiRanked, query);
            }
            case "wacky-ranked" ->
            {
                WakyRanked wakyRanked = new WakyRanked();
                top10Ranked = rankedAlgorithm.calculate(wakyRanked, query);
            }
        }

        // Convert list of entries to pages, and then convert pages to JSON string to be sent to browser
        if (top10Ranked != null)
        {
            List<FileResult> pages = new ArrayList<>();
            for (Entry top10Result : top10Ranked)
            {
                System.out.println("Found top10 result: " + top10Result.getDocID());
                Document document = corpus.getDocument(top10Result.getDocID());
                FileResult result = createFileResult(document);
                pages.add(result);
            }

            // Convert pages to JSON string
            Gson gson = new Gson();
            return gson.toJson(pages);
        }
        // No results were found
        else
        {
            return null;
        }
    }

}