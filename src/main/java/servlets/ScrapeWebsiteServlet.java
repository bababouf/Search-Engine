package servlets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modules.crawler.WebCrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;


@WebServlet(name = "ScrapeWebsiteServlet", value = "/scrapeWebsite")
public class ScrapeWebsiteServlet extends HttpServlet
{
    private static String OUTPUT_DIR = "C:\\Users\\agreg\\Desktop\\CopyOfProject\\search-engine\\uploaded_dir_scraped";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        JsonObject json = ServletUtilities.parseRequestBody(request);
        String baseURL = json.get("baseURL").getAsString();
        int maxDepth = json.get("depth").getAsInt();

        // Setup proxy authentication
        configureProxyAuthentication();


        WebCrawler crawler = initializeCrawler(baseURL, maxDepth);

        // Crawl the website and process results
        List<WebCrawler.PageContent> pageContents = crawler.crawl();
        try
        {
            processResults(request, baseURL, pageContents);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        // Send response back to the client
        sendCrawlCompletionResponse(response, pageContents.size());
    }

    private void configureProxyAuthentication()
    {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "false");
        System.setProperty("jdk.http.auth.proxying.disabledSchemes", "false");
    }

    private WebCrawler initializeCrawler(String baseURL, int maxDepth)
    {
        String username = System.getenv("SMART_PROXY_USERNAME");
        String password = System.getenv("SMART_PROXY_PASSWORD");

        try
        {
            return new WebCrawler(baseURL, maxDepth, username, password);
        }
        catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new RuntimeException("Error initializing WebCrawler", e);
        }
    }

    private void processResults(HttpServletRequest request, String baseURL, List<WebCrawler.PageContent> pageContents) throws IOException, SQLException
    {
        String containerName = createContainerName(baseURL, request);

        ServletContext context = getServletContext();
        context.setAttribute("fileExtension", "json");

        savePageContents(pageContents);
        Path tempDir = Files.createTempDirectory("uploaded-dir-scraped");
        OUTPUT_DIR = tempDir.toString();

        Path zippedDir = ServletUtilities.zipUploadDirectory(OUTPUT_DIR);
        ServletUtilities.uploadZipDirectory(zippedDir, containerName);
        ServletUtilities.buildAndStoreIndexFiles(OUTPUT_DIR, containerName, request, context);
    }

    private String createContainerName(String baseURL, HttpServletRequest request) throws MalformedURLException
    {
        HttpSession session = request.getSession(false);
        String id = (String) session.getAttribute("uniqueID");
        String host = extractHostFromURL(baseURL);
        return id + "-" + host;
    }

    private String extractHostFromURL(String baseURL) throws MalformedURLException
    {
        URL url = new URL(baseURL);
        String host = url.getHost();

        // Remove common subdomains
        if (host.startsWith("www."))
        {
            host = host.substring(4);
        }

        // Remove the top-level domain
        int lastDotIndex = host.lastIndexOf('.');
        if (lastDotIndex != -1)
        {
            host = host.substring(0, lastDotIndex);
        }

        return host.replace(".", "");
    }

    private void savePageContents(List<WebCrawler.PageContent> pageContents)
    {
        Gson gson = new Gson();
        File outputDir = createOutputDirectory();

        for (int i = 0; i < pageContents.size(); i++)
        {
            WebCrawler.PageContent content = pageContents.get(i);
            savePageContentToFile(content, gson, outputDir, i + 1);
        }
    }

    private File createOutputDirectory()
    {

        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists())
        {
            outputDir.mkdirs();
        }
        return outputDir;
    }

    private void savePageContentToFile(WebCrawler.PageContent content, Gson gson, File outputDir, int index)
    {
        JsonObject jsonContent = new JsonObject();
        jsonContent.addProperty("title", content.getTitle());
        jsonContent.addProperty("body", content.getBody());
        jsonContent.addProperty("url", content.getUrl());

        String filename = index + ".json";
        try (FileWriter writer = new FileWriter(new File(outputDir, filename)))
        {
            gson.toJson(jsonContent, writer);
        }
        catch (IOException e)
        {
            System.err.println("Error writing file " + filename + ": " + e.getMessage());
        }
    }

    private void sendCrawlCompletionResponse(HttpServletResponse response, int pageCount) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Crawling completed. " + pageCount + " pages processed.\"}");
    }
}