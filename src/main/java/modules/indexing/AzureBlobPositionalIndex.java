package modules.indexing;

import modules.database.PostgresDB;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods to interact and obtain postings from the Azure Blob Storage index.
 */
public class AzureBlobPositionalIndex  implements Index{

    private final String directoryType;
    private final AzureBlobStorageClient client;
    private final String blobIndexName = "default-directory-postings.bin";
    private final byte[] postingsData;

    public AzureBlobPositionalIndex (String directory)
    {
        directoryType = directory;
        client = new AzureBlobStorageClient();
        System.out.println("Starting file download...");
        postingsData = client.downloadFile(blobIndexName);
        System.out.println("Finished file download");
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) throws IOException {


        // Convert the byte array to a DataInputStream
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(postingsData);
             DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {

            // Get the byte position for the term from the database
            PostgresDB database = new PostgresDB(directoryType);
            Long bytePosition = database.selectTerm(term);

            // Skip to the byte position of the term's postings
            dataInputStream.skipBytes(bytePosition.intValue());

            // Read the document frequency
            int docFrequency = dataInputStream.readInt();
            List<Posting> postings = new ArrayList<>();
            int docID = 0;
            List<Integer> positions = new ArrayList<>();

            // Read postings data
            for (int i = 0; i < docFrequency; i++) {
                docID = docID + dataInputStream.readInt();
                int termFrequency = dataInputStream.readInt();
                Integer position = 0;

                for (int j = 0; j < termFrequency; j++) {
                    Integer positionGap = dataInputStream.readInt();
                    position = position + positionGap;
                    positions.add(position);
                }

                Posting posting = new Posting(docID, positions);
                postings.add(posting);
                positions = new ArrayList<>();
            }

            return postings;
        }

    }

    @Override
    public List<Posting> getPostings(String term) throws IOException {

        // Convert the byte array to a DataInputStream
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(postingsData);
             DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {

            PostgresDB database = new PostgresDB(directoryType);
            Long bytePosition = database.selectTerm(term);
            List<Posting> postings = new ArrayList<>();

            if (bytePosition != null) {
                dataInputStream.skipBytes(bytePosition.intValue());

                int documentFrequency = dataInputStream.readInt();
                int docID = 0;

                for (int i = 0; i < documentFrequency; i++) {
                    docID = docID + dataInputStream.readInt();
                    int termFrequency = dataInputStream.readInt();
                    dataInputStream.skipBytes(termFrequency * Integer.BYTES); // Skip the term frequency positions
                    Posting posting = new Posting(docID, null);
                    posting.setTermFrequency(termFrequency);
                    postings.add(posting);
                }
            }

            return postings;

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (logging, rethrowing, etc.)
            throw e; // Re-throwing the exception here
        }
    }


    @Override
    public List<String> getVocabulary() {

        PostgresDB database = new PostgresDB(directoryType);
        return database.retrieveVocabulary();

    }

}

