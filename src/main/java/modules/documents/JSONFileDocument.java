package modules.documents;

import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

/**
 * Represents a document saved as a json file
 */

public class JSONFileDocument implements FileDocument{
    private int documentID;
    private Path filePath;
    private String title;
    private String URL;

    /**
     * Constructs a JSONFileDocument with a given document ID representing the file at the given absoluteFilePath
     */
    public JSONFileDocument(int id, Path absoluteFilePath) {
        documentID = id;
        filePath = absoluteFilePath;
    }

    @Override
    public Path getFilePath()
    {
        return filePath;
    }

    @Override
    public int getId()
    {
        return documentID;
    }

    @Override
    public Reader getContent() {

        JSONParser parser = new JSONParser();
        String path = filePath.toString();

        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String body = (String)jsonObject.get("body");
            StringReader reader = new StringReader(body);
            return reader;
        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        JSONParser parser = new JSONParser();
        String path = filePath.toString();
        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String title = (String)jsonObject.get("title");
            this.title = title;
            return this.title;

        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getURL() {
        JSONParser parser = new JSONParser();
        String path = filePath.toString();
        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String url = (String)jsonObject.get("url");
            URL = url;
            return URL;

        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId)
    {
        return new JSONFileDocument(documentId, absolutePath);
    }
}
