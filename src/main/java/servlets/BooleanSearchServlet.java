package servlets;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.AzureBlobPositionalIndex;
import modules.indexing.AzureBlobStorageClient;
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
    public AzureBlobPositionalIndex createDiskPositionalIndex() throws IOException {

        System.out.println("In the create positional index");
        ServletContext context = getServletContext();

        String queryMode = "boolean";
        // Accesses the context variable containing the directory type (indicating which corpus is being used)
        String directoryType = (String) context.getAttribute("directoryType");

        String containerName = (String) context.getAttribute("containerName");

        // Creates the index based on which directory the user is attempting to query
        return new AzureBlobPositionalIndex(directoryType, containerName, queryMode);
    }


    public DirectoryCorpus createCorpus() {
        ServletContext context = getServletContext();
        String directoryType = (String) context.getAttribute("directoryType");

        if (directoryType == "default_directory") {
            corpus = handleDefaultCorpusCreation(context);
        } else {
            corpus = handleUploadedCorpusCreation(context);
        }
        corpus.getDocuments();
        System.out.println("After Corpus Creation, number of files found: " + corpus.getCorpusSize());
        return corpus;
    }

    public DirectoryCorpus handleDefaultCorpusCreation(ServletContext context) {
        String pathString = (String) context.getAttribute("directoryPath");
        System.out.println("Path string: " + pathString);
        Path absolutePathToCorpus = Paths.get(pathString).toAbsolutePath();
        System.out.println("Absolute path: " + absolutePathToCorpus);
        return DirectoryCorpus.loadJsonDirectory(absolutePathToCorpus, ".json");

    }
    public DirectoryCorpus handleUploadedCorpusCreation(ServletContext context) {
        String containerName = (String) context.getAttribute("containerName");
        AzureBlobStorageClient client = new AzureBlobStorageClient(containerName);

        // Download the zip file as a byte array
        byte[] serializedCorpus = client.downloadFile("zipped-directory.zip");

        // Create a temporary directory to unzip the contents
        Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("unzippedCorpus");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }

        String fileExtension = null;

        // Unzip the downloaded file into the temporary directory
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedCorpus);
             ZipInputStream zis = new ZipInputStream(bais)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    Path filePath = tempDirectory.resolve(zipEntry.getName());

                    System.out.println("Filename: " + filePath);
                    // Ensure the parent directories exist
                    Files.createDirectories(filePath.getParent());

                    // Write the unzipped file to the temporary directory
                    try (OutputStream os = Files.newOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }

                    // Get the file extension of the first file
                    if (fileExtension == null) {
                        String fileName = filePath.getFileName().toString();
                        fileExtension = fileName.substring(fileName.lastIndexOf("."));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip the file", e);
        }

        System.out.println("File extension: " + fileExtension);
        if(Objects.equals(fileExtension, ".json")) {
            System.out.println("We should be in here or big issue. ");
            corpus = DirectoryCorpus.loadJsonDirectory(tempDirectory, ".json");
        }
        else{
            System.out.println("No way we in here");
            corpus = DirectoryCorpus.loadTextDirectory(tempDirectory, ".txt");
    }
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
        queryPostings = sortPostingsByTermFrequency(queryPostings);

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

    public List<Posting> sortPostingsByTermFrequency(List<Posting> queryPostings) {
        // Sort the list in descending order based on termFrequency
        Collections.sort(queryPostings, new Comparator<Posting>() {
            @Override
            public int compare(Posting p1, Posting p2) {
                return p2.getTermFrequency().compareTo(p1.getTermFrequency());
            }
        });
        return queryPostings;
    }
    // Encapsulates a document
    public static class Page {
        String title;
        String url;
    }
}