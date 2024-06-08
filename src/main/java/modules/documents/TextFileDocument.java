package modules.documents;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a document that is saved as a simple text file in the local file system.
 */
public class TextFileDocument implements FileDocument {
	private int documentID;
	private Path filePath;
	private String URL;
	private String title;
	
	/**
	 * Constructs a TextFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public TextFileDocument(int id, Path absoluteFilePath)
	{
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

	@Override
	public String getTitle()
	{
		 title = filePath.getFileName().toString();
		 return title;
	}

	@Override
	public String getURL()
	{
		return null;
	}

	public static FileDocument loadTextFileDocument(Path absolutePath, int documentId)
	{
		return new TextFileDocument(documentId, absolutePath);
	}
}
