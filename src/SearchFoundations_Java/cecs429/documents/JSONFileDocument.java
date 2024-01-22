package SearchFoundations_Java.cecs429.documents;

import java.io.*;
import java.nio.file.Path;
import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * Represents a document saved as a json file
 */

public class JSONFileDocument implements FileDocument{
    private int mDocumentId;
    private Path mFilePath;
    private String mTitle;
    private String URL;

    /**
     * Constructs a JSONFileDocument with a given document ID representing the file at the given absoluteFilePath
     */
    public JSONFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;

    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {

        JSONParser parser = new JSONParser();
        String path = mFilePath.toString();
        try {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String body = (String)jsonObject.get("body");
            StringReader reader = new StringReader(body);

            return reader;

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        JSONParser parser = new JSONParser();
        String path = mFilePath.toString();
        try {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String title = (String)jsonObject.get("title");
            mTitle = title;
            return mTitle;

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String getURL() {
        JSONParser parser = new JSONParser();
        String path = mFilePath.toString();
        try {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String url = (String)jsonObject.get("url");
            URL = url;
            return URL;

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JSONFileDocument(documentId, absolutePath);
    }
}
