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
    private final int documentID;
    private final Path filePath;

    /**
     * Constructs a JSONFileDocument with a given document ID representing the file at the given absoluteFilePath
     */
    public JSONFileDocument(int id, Path absoluteFilePath) {
        documentID = id;
        filePath = absoluteFilePath;
    }

    /**
     * Returns the absolute file path
     */
    @Override
    public Path getFilePath()
    {
        return filePath;
    }

    /**
     * Returns the document ID
     */
    @Override
    public int getId()
    {
        return documentID;
    }

    /**
     * Returns a reader over the body of the JSON document
     */
    @Override
    public Reader getContent() {

        JSONParser parser = new JSONParser();
        String path = filePath.toString();

        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            String body = (String)jsonObject.get("body");
            return new StringReader(body);
        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the title of the JSON document
     */
    @Override
    public String getTitle() {
        JSONParser parser = new JSONParser();
        String path = filePath.toString();
        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            return (String)jsonObject.get("title");

        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the URL of the JSON document
     */
    @Override
    public String getURL() {
        JSONParser parser = new JSONParser();
        String path = filePath.toString();
        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject)obj;
            return (String)jsonObject.get("url");

        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method returns a JSONFileDocument
     */
    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId)
    {
        return new JSONFileDocument(documentId, absolutePath);
    }
}
