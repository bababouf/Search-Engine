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

public class DefaultJSONFileDocument implements FileDocument
{
    private final int documentID;
    private final Path filePath;
    private final String key;

    /**
     * Constructs a JSONFileDocument with a given document ID representing the file at the given absoluteFilePath
     */
    public DefaultJSONFileDocument(int id, Path absoluteFilePath, String contentKey)
    {
        documentID = id;
        filePath = absoluteFilePath;
        key = contentKey;
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
    public Reader getContent()
    {

        System.out.println("In the get content method." );
        System.out.println("The key is: " + key);
        JSONParser parser = new JSONParser();
        String path = filePath.toString();

        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject) obj;
            String body = (String) jsonObject.get(key);
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
    public String getTitle()
    {
        JSONParser parser = new JSONParser();
        String path = filePath.toString();
        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject) obj;
            return (String) jsonObject.get("title");

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
    public String getURL()
    {
        JSONParser parser = new JSONParser();
        String path = filePath.toString();
        try
        {
            Object obj = parser.parse(new FileReader(path));
            JSONObject jsonObject = (JSONObject) obj;
            return (String) jsonObject.get("url");

        }
        catch (ParseException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method returns a JSONFileDocument
     */
    public static FileDocument loadDefaultJsonFileDocument(Path absolutePath, int documentId, String contentKey)
    {
        System.out.println("Surely this is being called right");
        return new DefaultJSONFileDocument(documentId, absolutePath, contentKey);
    }
}
