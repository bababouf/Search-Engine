package modules.documents;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a document that is saved as a simple text file in the local file system.
 */
public class TextFileDocument implements FileDocument
{
    private final int documentID;
    private final Path filePath;

    /**
     * Constructs a TextFileDocument with the given document ID representing the file at the given
     * absolute file path.
     */
    public TextFileDocument(int id, Path absoluteFilePath)
    {
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
     * Returns the documents ID
     */
    @Override
    public int getId()
    {
        return documentID;
    }

    /**
     * Returns a reader for the content of the document
     */
    @Override
    public Reader getContent()
    {
        try
        {
            return Files.newBufferedReader(filePath);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the filename (title) of the document
     */
    @Override
    public String getTitle()
    {
        return filePath.getFileName().toString();
    }

    /**
     * Returns null since TEXT documents do not have URLs
     */
    @Override
    public String getURL()
    {
        return null;
    }

    /**
     * Returns a TextFileDocument
     */
    public static FileDocument loadTextFileDocument(Path absolutePath, int documentId, String contentKey)
    {
        return new TextFileDocument(documentId, absolutePath);
    }
}
