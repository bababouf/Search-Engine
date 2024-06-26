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
 * A DirectoryCorpus represents a corpus found in a single directory on a local file system.
 */
public class DirectoryCorpus implements DocumentCorpus {
	// The map from document ID to document.
	private HashMap<Integer, Document> documents;
	
	// Maintains a map of registered file types that the corpus knows how to load.
	private HashMap<String, FileDocumentFactory> factories = new HashMap<>();
	
	// A filtering function for identifying documents that should get loaded.
	private Predicate<String> fileFilter;
	
	private Path directoryPath;
	
	/**
	 * Constructs a corpus over an absolute directory path.
	 * Before calling GetDocuments(), you must register a FileDocumentFactory with the RegisterFileDocumentFactory
	 * method. Otherwise, the corpus will not know what to do with the files it finds. The LoadTextDirectory facade
	 * method can simplify this initialization.
	 * @see
	 */
	public DirectoryCorpus(Path directoryPath) {
		this(directoryPath, s->true);
	}
	
	/**
	 * Constructs a corpus over an absolute directory path, only loading files whose file names satisfy
	 * the given predicate filter.
	 */
	public DirectoryCorpus(Path dirPath, Predicate<String> fileFilter)
	{
		this.fileFilter = fileFilter;
		directoryPath = dirPath;
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
			// Use the registered factory for the file's extension.
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
		List<Path> allFiles = new ArrayList<>();
		
		// First discover all the files in the directory that match the filter.
		Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>()
		{
			
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			{
				// make sure we only process the current working directory
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
					allFiles.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
			
			// don't throw exceptions if files are locked/other errors occur
			public FileVisitResult visitFileFailed(Path file, IOException e)
			{
				return FileVisitResult.CONTINUE;
			}
		});
		return allFiles;
	}

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
	
	@Override
	public Document getDocument(int id)
	{
		return documents.get(id);
	}
	
	/**
	 * Registers a factory method for loading documents of the given file extension. By default, a corpus
	 * does not know how to load any files -- this method must be called prior to getDocuments().
	 */
	public void registerFileDocumentFactory(String fileExtension, FileDocumentFactory factory)
	{
		factories.put(fileExtension, factory);
	}
	
	/**
	 * Constructs a corpus over a directory of simple text documents.
	 * @param fileExtension The extension of the text documents to load, e.g., ".txt".
	 */
	public static DirectoryCorpus loadTextDirectory(Path absolutePath, String fileExtension)
	{
		DirectoryCorpus corpus = new DirectoryCorpus(absolutePath);
		corpus.registerFileDocumentFactory(fileExtension, TextFileDocument::loadTextFileDocument);
		return corpus;
	}

	public static DirectoryCorpus loadJsonDirectory(Path absolutePath, String fileExtension)
	{
		DirectoryCorpus corpus = new DirectoryCorpus(absolutePath);
		corpus.registerFileDocumentFactory(fileExtension, JSONFileDocument::loadJsonFileDocument);
		return corpus;
	}

}
