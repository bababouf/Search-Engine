package modules.indexing;

import modules.database.PostgresDB;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Once the Positional Inverted Index is created in memory, additional information is extracted in the methods below to make
 * the querying process more efficient. Azure Blob Storage is used to store data in binary format for later access.
 */
public class BlobStorageWriter {

    /**
     * This method converts the given positional inverted index into a byte stream and uploads it to Azure Blob Storage.
     * It writes the metadata and postings lists of terms in binary format, using gap encoding for document IDs and
     * term positions to optimize storage space.
     */
    public static List<Long> serializeAndUploadIndex(PositionalInvertedIndex index, AzureBlobStorageClient blobStorageClient) throws IOException, SQLException {

        // Will save the starting byte position for each term
        List<Long> bytePositions = new ArrayList<>();
        int initialBufferSize = 60 * 1024 * 1024;
        //ByteArrayOutputStream is the underlying stream that DataOutputStream will be writing to
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(initialBufferSize);

             // DataOutputStream allows for methods like writeInt() which easily convert primitive data to binary
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream))

        {
            String blobFileName = "default-directory-postings.bin";
            List<String> vocabulary = index.getVocabulary();

            // Loop through each term in the index
            for (String term : vocabulary)
            {
                long bytePosition = byteArrayOutputStream.size();
                bytePositions.add(bytePosition);
                List<Posting> postings = index.getPostings(term);

                // documentFrequency is the number of documents a term appears in
                int documentFrequency = postings.size();
                dataOutputStream.writeInt(documentFrequency);

                // Will be used for storing the gap instead of each documentID (explained further below)
                int lastDocID = 0;

                /*
                Loop through each of the postings (documents). It's important to note here that the documentIDs for each
                of the documents are not written, but instead the documentIDGap (the difference between the current docID
                and the last docID written) is written. This usually allows for more efficient storage.
                 */
                for (Posting posting : postings)
                {
                    int docID = posting.getDocumentId();
                    int docIDGap = docID - lastDocID;
                    lastDocID = docID;
                    dataOutputStream.writeInt(docIDGap);

                    // The getPositions method returns the positions within a document that a term appears in
                    List<Integer> positions = posting.getPositions();
                    int termFrequency = positions.size();
                    dataOutputStream.writeInt(termFrequency);
                    int lastPosition = 0;

                    // Just like documentIDs, positions are written as gaps
                    for (Integer currentPosition : positions)
                    {
                        int positionGap = currentPosition - lastPosition;
                        dataOutputStream.writeInt(positionGap);
                        lastPosition = currentPosition;
                    }
                }
            }

            // Upload the byte array to Azure Blob Storage
            byte[] data = byteArrayOutputStream.toByteArray();
            blobStorageClient.uploadFile(blobFileName, data);

            System.out.println("Upload postings.bin to Azure Blob Storage complete.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytePositions;
    }

    /**
     * Once the serializeAndUploadIndex is complete, it returns a list of byte positions where each term starts. This allows
     * for efficient lookup and access to the desired term during querying. This method has the task of storing each term
     * and its associated starting byte position (in the blob file) in a Postgres database. Again, this allows for a given
     * term to first be looked up in the Postgres database, finding the starting byte position, and then accessing the blob
     * file at that byte position for efficient retrieval of information on that term.
     */
    public static void writeBytePositions(PositionalInvertedIndex index, List<Long> bytePositions, String databaseName) {

        /*
        // Obtain a list of all the unique terms in the index
        List<String> vocabulary = index.getVocabulary();
        System.out.println("Writing term byte positions to database.");

        // Create the Postgre database
        PostgresDB database = new PostgresDB(databaseName);
        database.dropTable();
        database.createTable();

        int count = 0;

        // Loop through each term, adding it and its associated byte position to the database
        for (int i = 0; i < vocabulary.size(); i++)
        {
            String term = vocabulary.get(i);
            if(term.length() >= 254)
            {
                term = term.substring(0, 254);
            }
            long bytePosition = bytePositions.get(i);
            database.insertTerm(term, bytePosition);

            // Commits terms to the database in batches of 1000 (helps with efficiency)
            if (count == 1000)
            {
                System.out.println("Commiting 1000 terms to DB");
                count = 0;
                database.commit();
            }

            count++;
        }

        database.commit();

         */
        List<String> vocabulary = index.getVocabulary();
        System.out.println("Writing term byte positions to database.");

        PostgresDB database = new PostgresDB(databaseName);
        database.dropTable();
        database.createTable();
        System.out.println("After create table");

        database.insertTermsBatch(vocabulary, bytePositions);

    }

    /**
     * This method has the task of calculating meta-data for a document and serializing it so that it can be uploaded after
     * all the documents have been processed. In specific, there are four values that are calculated for each document: a
     * so-called "LD" value, the total number of tokens (terms), number of bytes, and average term frequency. The LD value
     * that is calculated represents the Euclidean Norm of a document. This allows for a consistent measure of document
     * length that allows for comparing documents of different lengths.
     */
    public byte[] serializeDocumentWeights(Map<String, Integer> termFrequency, int documentTokens, int bytes) {

        //ByteArrayOutputStream is the underlying stream that DataOutputStream will be writing to.
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

             // DataOutputStream allows for methods like writeInt() which easily convert primitive data to binary
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream))

        {

            // Obtain the set of terms for a document
            Set<String> terms = termFrequency.keySet();
            double LD = 0.0;
            double totalTermFrequency = 0.0;

            // Loop through all the terms for a document
            for (String term : terms)
            {
                // Obtain the frequency of that term in the document
                int frequencyOfTerm = termFrequency.get(term);

                // Accumulate the current frequencyOfTerm with the running total
                totalTermFrequency = totalTermFrequency + frequencyOfTerm;

                if (frequencyOfTerm >= 1)
                {
                    /*
                    Logarithmic scaling helps to normalize the term frequencies, allowing for weights of terms to be
                    more comparable across documents of different lengths. In addition, since the log function grows more
                    slowly than a linear function, the impact of common words (like "the", "and", "a") is reduced.
                     */
                    Double weightOfTermInDocument = (1 + (Math.log(frequencyOfTerm)));

                    // The LD is calculating by first taking the summation of each of the terms squared
                    LD = LD + (weightOfTermInDocument * weightOfTermInDocument);
                }
            }

            double averageTermFrequency = totalTermFrequency / (termFrequency.values().size());

            // Finally, to get the Euclidian Norm of the document the LD is squared
            LD = Math.sqrt(LD);

            // Writes each of the primitive double values in their binary representation
            dataOutputStream.writeDouble(LD);
            dataOutputStream.writeDouble(documentTokens);
            dataOutputStream.writeDouble(bytes);
            dataOutputStream.writeDouble(averageTermFrequency);

            return byteArrayOutputStream.toByteArray();

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new byte[0];
        }

    }

    /**
     * This method serializes and uploads a single value representing the average tokens in a document to an Azure Blob
     * Storage file.
     */
    public void serializeAndUploadAverageTokens(double averageTokens) {
        AzureBlobStorageClient blobStorageClient = new AzureBlobStorageClient();
        String blobFileName = "default-directory-averageTokens.bin";

        try (
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream))
        {
            dataOutputStream.writeDouble(averageTokens);

            // Upload the byte array to Azure Blob Storage
            byte[] documentWeights = byteArrayOutputStream.toByteArray();
            blobStorageClient.uploadFile(blobFileName, documentWeights);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}






