package modules.documents;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * A DirectoryCorpus represents a single directory
 */
public class DirectoryCorpus implements DocumentCorpus {

	// A hashmap that maps documentIDs to Document objects
	private HashMap<Integer, Document> documents;
	
	// A hashmap that maps fileExtensions to their corresponding methods (.JSON extensions -> loadJsonFileDocument() )
	private final HashMap<String, FileDocumentFactory> factories = new HashMap<>();
	
	// A filtering function for identifying documents that should get loaded.
	private final Predicate<String> fileFilter;

	// The path to the directory
	private final Path directoryPath;

    /**
	 * Constructs a corpus over an absolute path
	 */
	public DirectoryCorpus(Path directoryPath) {

		this(directoryPath, s->true);
	}
	
	/**
	 * Constructs a corpus over an absolute directory path, only loading files whose file names satisfy the given
	 * predicate filter.
	 */
	public DirectoryCorpus(Path dirPath, Predicate<String> fileFilter)
	{
		this.fileFilter = fileFilter;
		directoryPath = dirPath;
	}

	/**
	 * Constructs a corpus over a directory of simple text documents
	 * @param fileExtension The extension of the text documents to load, e.g., ".txt"
	 */
	public static DirectoryCorpus loadTextDirectory(Path absolutePath, String fileExtension)
	{
		DirectoryCorpus corpus = new DirectoryCorpus(absolutePath);

		// Registers the fileExtension (.TXT) to the "loadTextFileDocument" method
		corpus.registerFileDocumentFactory(fileExtension, TextFileDocument::loadTextFileDocument);
		return corpus;
	}

	/**
	 * Constructs a corpus over a directory of JSON documents
	 */
	public static DirectoryCorpus loadJsonDirectory(Path absolutePath, String fileExtension)
	{
		DirectoryCorpus corpus = new DirectoryCorpus(absolutePath);

		// Registers the fileExtension (.JSON) to the "loadJsonFileDocument" method
		corpus.registerFileDocumentFactory(fileExtension, JSONFileDocument::loadJsonFileDocument);
		return corpus;
	}

	/**
	 * The load<FILETYPE>Directory methods above will call this method before any other methods are called. Initially,
	 * the corpus does not know what to do with any file extension it comes across. This method simply associates the passed
	 * fileExtension with the corresponding method to "create" files for the type.
	 */
	public void registerFileDocumentFactory(String fileExtension, FileDocumentFactory factory)
	{
		factories.put(fileExtension, factory);
	}

	/**
	 * Reads all documents in the corpus into a map from ID to document object.
	 */
	private HashMap<Integer, Document> readDocuments() throws IOException
	{
		Iterable<Path> allFiles = findFiles();

		// Next build the mapping from document ID to document.
		HashMap<Integer, Document> result = new HashMap<>();
		int nextId = 0;

		for (Path file : allFiles)
		{
			System.out.println("Found file. " + file.getFileName());
			result.put(nextId, factories.get(getFileExtension(file)).createFileDocument(file, nextId));
			nextId++;
		}
		return result;
	}
	
	/**
	 * Finds all file names that match the corpus filter predicate and have a known file extension.
	 */
	private Iterable<Path> findFiles() throws IOException
	{
		System.out.println("Finding all text files!!");
		List<Path> allFiles = new ArrayList<>();
		
		// First discover all the files in the directory that match the filter.
		Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>()
		{
			
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			{
				// Make sure we only process the current working directory
				if (directoryPath.equals(dir))
				{
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}
			
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			{
				String extension = getFileExtension(file);
				if (fileFilter.test(file.toString()) && factories.containsKey(extension))
				{
					System.out.println("Found text file " + file.getFileName());
					allFiles.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
			
			// Don't throw exceptions if files are locked/other errors occur
			public FileVisitResult visitFileFailed(Path file, IOException e)
			{
				return FileVisitResult.CONTINUE;
			}
		});
		return allFiles;
	}

	/**
	 * Returns the fileExtension for the passed path to a file
	 */
	private static String getFileExtension(Path file)
	{
		String fileName = file.getFileName().toString();
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		return "." + extension;
	}
	
	@Override
	public Iterable<Document> getDocuments()
	{
		if (documents == null)
		{
			try
			{
				documents = readDocuments();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return documents.values();
	}

	/**
	 * Returns the size of the corpus
	 */
	@Override
	public int getCorpusSize()
	{
		if (documents == null)
		{
			try
			{
				documents = readDocuments();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return documents.size();
	}

	/**
	 * Returns the Document object associated with the passed documentID
	 */
	@Override
	public Document getDocument(int documentID)
	{
		return documents.get(documentID);
	}

}
