package servlets;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
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

    private DirectoryCorpus corpus;
    private final Path defaultPath = Paths.get("C:/Users/agreg/IdeaProjects/search-engine/all-nps-sites-extracted");

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Hello");

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("IM IN THE BOOLEAN SERVLET!!!!");
        BufferedReader reader = request.getReader();
        JsonObject jsonBody = ServletUtilities.parseRequestBody(reader);
        String query = jsonBody.get("query").getAsString();
        Path path = defaultPath;

        DiskPositionalIndex index = new DiskPositionalIndex(path);
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(path, ".json");
        corpus.getDocuments();

        List<Posting> queryPostings = processBooleanQuery(query, index);
        List <Page> pages = setupResults(queryPostings, corpus);

        Gson gson = new Gson();
        String json = gson.toJson(pages);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(json);
        out.flush();

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

    public List<Page> setupResults(List<Posting> queryPostings, DirectoryCorpus corpus){
        List<Page> pages = new ArrayList<>();

        for (Posting queryPosting : queryPostings)
        {
            int documentID = queryPosting.getDocumentId();
            Document document = corpus.getDocument(documentID);
            Page page = new Page();
            page.title = document.getTitle();
            page.url = document.getURL();
            pages.add(page);
        }
        return pages;
    }

    public static class Page {
        String title;
        String url;
    }
}