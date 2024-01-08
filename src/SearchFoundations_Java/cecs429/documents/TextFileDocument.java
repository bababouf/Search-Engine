package SearchFoundations_Java.cecs429.documents;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a document that is saved as a simple text file in the local file system.
 */
public class TextFileDocument implements FileDocument {
	private int mDocumentId;
	private Path mFilePath;

	private String title;
	
	/**
	 * Constructs a TextFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public TextFileDocument(int id, Path absoluteFilePath) {
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
		System.out.println("In here????");
		try {
			return Files.newBufferedReader(mFilePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		 title = mFilePath.getFileName().toString();
			return title;
	}

	public static FileDocument loadTextFileDocument(Path absolutePath, int documentId) {
		return new TextFileDocument(documentId, absolutePath);
	}
}
