package drivers;

import modules.documents.DirectoryCorpus;
import modules.indexing.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * This driver program will allow for a directory to be read in (which should be located in the project root), and it will
 * proceed to create an in-memory positional index. The positional index is a hashmap that maps all the unique terms in the
 * directory's files to their corresponding posting lists (the documents and positions within the document that the term
 * appears). Once the positional index is created, it will be serialized and stored in an Azure Blob file (cloud storage).
 * In addition to the index being stored, two other Azure Blob files will be created that store meta-data needed for processing
 * queries.
 */
public class AzureBlobPositionalIndexer
{

    // The name of the Azure Blob Storage container that holds default corpus files
    public static String defaultCorpusContainer = "se-indexing-files";

    public static void main(String[] args) throws SQLException, IOException
    {
        Scanner readIn = new Scanner(System.in);
        Path absolutePathToCorpus = readInPath(readIn);
        buildIndex(absolutePathToCorpus, defaultCorpusContainer);
    }

    /**
     * This method reads in the name of the directory that will be indexed. It MUST be located in the root directory.
     */
    public static Path readInPath(Scanner readIn)
    {
        System.out.print("Enter corpus path: ");
        String pathString = readIn.next();
        Path path = Paths.get(pathString).toAbsolutePath();
        System.out.println("Absolute path from read in: " + path);
        return path;
    }

    /**
     * This method will first create the corpus, then build the positional index in-memory. Once the index is built, it will
     * be serialized and uploaded to an Azure Blob file. During the indexing process, meta-data will be collected from each
     * document and additional blob files will be uploaded. Lastly, the starting byte positions (where each of the serialized
     * terms begin in the index blob file) will be stored in a Postgres database. This allows for efficient lookup of terms
     * when processing queries.
     */
    public static void buildIndex(Path absolutePathToCorpus, String containerName)
    {
        try
        {
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToCorpus, ".json");
            AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient(containerName);


            PositionalInvertedIndex index = PositionalInvertedIndex.indexCorpus(corpus, blobStorageClient);
            List<Long> bytePositions = BlobStorageWriter.serializeAndUploadIndex(index, blobStorageClient);
            BlobStorageWriter.writeBytePositions(index, bytePositions, "default_directory");
        }
        catch (IOException | SQLException e)
        {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }


}
