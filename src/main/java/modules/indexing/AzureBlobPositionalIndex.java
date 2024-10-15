package modules.indexing;

import modules.database.PostgresDB;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods to interact and obtain postings from the Azure Blob Storage index
 */
public class AzureBlobPositionalIndex implements Index
{

    // The directory type contains a string indicating which corpus will be queried
    private final String directoryType;

    // Holds the byte stream containing all the postings in the index
    private byte[] postingsData;

    // Holds the byte stream containing the document weights
    private byte[] documentWeights;

    // Holds the byte stream containing the average tokens across the corpus
    private byte[] averageTokens;

    private final String tableName;

    public AzureBlobPositionalIndex(String directory, String containerName, String queryMode) throws IOException
    {
        directoryType = directory;

        if (containerName.equals("se-indexing-files"))
        {
            tableName = "byte_positions";
        }
        else
        {
            tableName = containerName;
        }

        downloadFiles(queryMode, containerName);

    }


    /*
    This method is necessary for processing phrase queries, where the position a term appears in a document is relevant.
    The postings data that is retrieved from Azure Blob Storage is stored as a byte array. The PostgresDB contains information
    that maps each term to a byte position (where the byte position is the position within the postings data byte array that
    the term begins at). This allows for fast retrieval of data for each term.
     */
    @Override
    public List<Posting> getPostingsWithPositions(String term) throws IOException
    {

        // Convert the byte array to a DataInputStream
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(postingsData);
             DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream))
        {

            // Get the byte position for the term from the database

            PostgresDB database = new PostgresDB(directoryType);
            if(directoryType.equals("user_directory"))
            {
                database.setTableName(tableName);
            }
            //database.setTableName(tableName);
            Long bytePosition = database.selectTerm(term);

            // Skip to the byte position of the term's postings
            dataInputStream.skipBytes(bytePosition.intValue());

            // Read the document frequency
            int docFrequency = dataInputStream.readInt();
            List<Posting> postings = new ArrayList<>();
            int docID = 0;
            List<Integer> positions = new ArrayList<>();

            // Read postings data
            for (int i = 0; i < docFrequency; i++)
            {
                docID = docID + dataInputStream.readInt();
                int termFrequency = dataInputStream.readInt();
                Integer position = 0;

                // Obtain each position within a document where the term is found, add it to positions list
                for (int j = 0; j < termFrequency; j++)
                {
                    Integer positionGap = dataInputStream.readInt();
                    position = position + positionGap;
                    positions.add(position);
                }

                // Store information (ID and positions list) in a posting object, and add it to a list of postings
                Posting posting = new Posting(docID, positions);
                postings.add(posting);
                positions = new ArrayList<>();
            }

            return postings;
        }

    }

    /*
    Similar to the above getPostingsWithPositions method, this method is identical except for it doesn't access the positions
    where a term appears in a given document.
     */
    @Override
    public List<Posting> getPostings(String term) throws IOException
    {

        // Convert the byte array to a DataInputStream
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(postingsData);
             DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream))
        {

            System.out.println("Directory type in the get postings: " + directoryType);
            PostgresDB database = new PostgresDB(directoryType);
            if(directoryType.equals("user_directory"))
            {
                database.setTableName(tableName);
            }
            //database.setTableName(tableName);
            Long bytePosition = database.selectTerm(term);
            List<Posting> postings = new ArrayList<>();

            if (bytePosition != null)
            {
                dataInputStream.skipBytes(bytePosition.intValue());

                int documentFrequency = dataInputStream.readInt();
                int docID = 0;

                for (int i = 0; i < documentFrequency; i++)
                {
                    docID = docID + dataInputStream.readInt();
                    int termFrequency = dataInputStream.readInt();
                    dataInputStream.skipBytes(termFrequency * Integer.BYTES); // Skip the term frequency positions
                    Posting posting = new Posting(docID, null);
                    posting.setTermFrequency(termFrequency);
                    postings.add(posting);
                }
            }

            return postings;

        }
        catch (IOException e)
        {
            e.printStackTrace();

            throw e; // Re-throwing the exception here
        }
    }

    public void downloadFiles(String queryMode, String containerName) throws IOException
    {

        AzureBlobStorageClient client = new AzureBlobStorageClient(containerName);
        postingsData = client.downloadFile("postings.bin");
        System.out.println("Downloaded postings data. ");
        if (queryMode.equals("ranked"))
        {
            documentWeights = client.downloadFile("doc-weights.bin");
            averageTokens = client.downloadFile("average-tokens.bin");
        }

    }

    @Override
    // Simply returns a list of the unique vocabulary terms found in the corpus
    public List<String> getVocabulary()
    {
        PostgresDB database = new PostgresDB(directoryType);
        //database.setTableName("byte_positions");
        return database.retrieveVocabulary();

    }


    public byte[] getDocumentWeights()
    {
        return documentWeights;
    }

    public byte[] getAverageTokens()
    {
        return averageTokens;
    }
}

