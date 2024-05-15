package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import drivers.DiskPositionalIndexer;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.DiskPositionalIndex;
import modules.indexing.Posting;
import modules.misc.Entry;
import modules.rankingSchemes.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "RankedSearchServlet", value = "/search/rankedsearch")
public class RankedSearchServlet extends HttpServlet {

    private final Path defaultPath = Paths.get("C://Users//agreg//Desktop//Copy of Project//search-engine//all-nps-sites-extracted");

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonObject jsonBody = ServletUtilities.parseRequestBody(request);
        String query = jsonBody.get("query").getAsString();
        String mode = jsonBody.get("mode").getAsString();
        String results = rankedModeDispatch(mode, query);
        ServletUtilities.sendResultsToBrowser(results, response);
    }

    /*
    This method takes two strings as parameters: mode and query. A switch statement dispatches the work of
    ranking and returning the top 10 documents to various methods depending on the mode passed.
     */
    public String rankedModeDispatch(String mode, String query) {
        List<Entry> top10Ranked = null;

        DiskPositionalIndex index = new DiskPositionalIndex(defaultPath);
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(defaultPath, ".json");
        RankedDispatch rankedAlgorithm = new RankedDispatch(index, corpus);

        switch (mode) {
            case "default-ranked" -> {
                DefaultRanked defaultRanked = new DefaultRanked();
                rankedAlgorithm.calculate(defaultRanked, query);
                top10Ranked = rankedAlgorithm.calculateAccumulatorValue(defaultRanked);
            }

            case "tfidf-ranked" -> {
                TermFreqInvDocFreqRanked tfidfRanked = new TermFreqInvDocFreqRanked();
                rankedAlgorithm.calculate(tfidfRanked, query);
                top10Ranked = rankedAlgorithm.calculateAccumulatorValue(tfidfRanked);
            }
            case "okapi-ranked" -> {
                OkapiRanked okapiRanked = new OkapiRanked();
                rankedAlgorithm.calculate(okapiRanked, query);
                rankedAlgorithm.calculateAccumulatorValue(okapiRanked);
            }
            case "wacky-ranked" -> {
                WakyRanked wakyRanked = new WakyRanked();
                rankedAlgorithm.calculate(wakyRanked, query);
                rankedAlgorithm.calculateAccumulatorValue(wakyRanked);
            }
        }
        if (top10Ranked != null) {
            List<Page> pages = new ArrayList<>();
            for (Entry top10Result : top10Ranked) {
                Integer docID = top10Result.getDocID();
                Document document = corpus.getDocument(docID);
                Page page = new Page();
                page.title = document.getTitle();
                page.url = document.getURL();
                pages.add(page);
            }
            Gson gson = new Gson();
            return gson.toJson(pages);
        } else {
            return null;
        }
    }

    public static class Page {
        String title;
        String url;
    }
}