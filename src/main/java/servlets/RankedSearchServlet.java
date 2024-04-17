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

    private DirectoryCorpus corpus;
    private final Path defaultPath = Paths.get("C:/Users/agreg/IdeaProjects/search-engine/all-nps-sites-extracted");


    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Im in the ranked servlet ooooooooooooooaaaaaaaass");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("IM IN THE RANKED SERVLET!!!!");
        BufferedReader reader = request.getReader();
        JsonObject jsonBody = ServletUtilities.parseRequestBody(reader);
        String query = jsonBody.get("query").getAsString();
        String mode = jsonBody.get("mode").getAsString();
        System.out.println("Mode: " + mode);

        List<Page> results = rankedModeDispatch(mode, query);
        Gson gson = new Gson();
        String json = gson.toJson(results);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();

    }

    public List<Page> rankedModeDispatch(String mode, String query) {
        List<Entry> top10Ranked = null;
        Path path = defaultPath;

        DiskPositionalIndex index = new DiskPositionalIndex(path);
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(path, ".json");

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
            System.out.println("first step");
            List<Page> pages = new ArrayList<>();
            System.out.println("top10rankedsize: " + top10Ranked.size());
            for (Entry top10Result : top10Ranked) {
                System.out.println("In the for loop");
                Integer docID = top10Result.getDocID();
                System.out.println("Docid: " + docID);
                Document document = corpus.getDocument(docID);
                Page page = new Page();
                page.title = document.getTitle();
                page.url = document.getURL();
                pages.add(page);
                System.out.println("End of for loop");
            }
            return pages;
        }
        else {
            return null;
        }
    }

    public static class Page {
        String title;
        String url;
    }
}