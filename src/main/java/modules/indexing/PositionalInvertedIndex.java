package modules.indexing;

import modules.documents.Document;
import modules.documents.DocumentCorpus;
import modules.text.EnglishTokenStream;
import modules.text.NonBasicTokenProcessor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * A PositionalInvertedIndex is creating during the indexing process, and contains a hashmap mapping every unique term
 * to a list of postings. A posting simply contains the documentID, a list of positions within the document where the term
 * appears, and the total count of the occurences of the term in the document.
 */
public class PositionalInvertedIndex implements Index
{

    private final HashMap<String, List<Posting>> invertedIndex;
    private final List<String> vocabulary;

    /**
     * Constructs a PositionalInvertedIndex; contains a Hashmap and a vocabulary list
     */
    public PositionalInvertedIndex()
    {
        invertedIndex = new HashMap<>();
        vocabulary = new ArrayList<>();
    }

    /**
     * This method adds a term to the index. If the term has already been added to the index, it will append the newly
     * discovered position to the terms posting list. If not, a new Posting will be created, and the first position will
     * be added to that Posting's position list.
     */
    public void addTerm(String term, int documentId, int position)
    {
        List<Posting> postingList = invertedIndex.get(term);

        // The posting list is null when no occurrences for the term passed have been discovered thus far
        if (postingList == null)
        {
            postingList = new ArrayList<>();

            // A new posting is created, passing the documentID and creating a new list for positions
            Posting posting = new Posting(documentId, new ArrayList<>(List.of(position)));

            // The newly created posting is added to the posting list for the term passed
            postingList.add(posting);

            // The term and associated posting list is placed into the index
            invertedIndex.put(term, postingList);

            // The term is added to the vocabulary list
            vocabulary.add(term);
        }
        // If the posting list is not null, this means there has already been at least one occurence of the term in the document
        else
        {
            // Retrieves the posting so that the new position can be added to the positions list
            Posting lastPosting = postingList.get(postingList.size() - 1);

            // Ensure the last posting is from the same document as the current document
            if (lastPosting.getDocumentId() == documentId)
            {
                // Add the position to the list of positions
                lastPosting.getPositions().add(position);
            }
            // In the event the last posting is NOT from the same document, create a new posting and position list
            else
            {
                Posting posting = new Posting(documentId, new ArrayList<>(List.of(position)));
                postingList.add(posting);
            }
        }
    }

    /**
     * This method creates and populates the index. For every document in the corpus that is passed as a parameter, each
     * term is examined. Before a term is added to the index (using the addTerm method above), each term undergoes basic
     * preprocessing (lowercasing, splitting hyphenated words, etc). In addition to creating the index, this method accumulates
     * meta-data about each document that is critical for ranked retrieval. This includes the number of tokens, bytes, and
     * the positions of each term in the document. Methods are called to write this data to an Azure Blob Storage file.
     */
    public static PositionalInvertedIndex indexCorpus(DocumentCorpus corpus, AzureBlobStorageClient blobStorageClient) throws IOException
    {
        System.out.println("Indexing...");
        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
        PositionalInvertedIndex positionalIndex = new PositionalInvertedIndex();
        BlobStorageWriter blobStorageWriter = new BlobStorageWriter();

        int initialBufferSize = 20 * 1024 * 1024;
        // ByteArrayOutputStream to accumulate all document weights

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(initialBufferSize);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Tracks the total number of tokens across the entire corpus; used to determine average tokens per document
        double corpusTokenCount = 0;


        // Loop through each of the documents in the corpus
        for (Document document : corpus.getDocuments())
        {

            System.out.println("Looping through document: " + document.getId());
            // Each of the integers below is tracked; this is critical meta-data used in ranked retrieval
            int currentDocumentTokens = 0;
            int currentDocumentBytes = 0;
            int currentDocumentPosition = 0;
            int currentDocumentID = document.getId();

            // Hashmap of terms -> frequency;
            Map<String, Integer> termFrequency = new HashMap<>();

            // Creates a stream of tokens from the associated Reader for a document
            EnglishTokenStream documentTokenStream = new EnglishTokenStream(document.getContent());

            // Converts stream of tokens to iterable of strings
            Iterable<String> tokens = documentTokenStream.getTokens();

            // Loop through each term in the document
            for (String term : tokens)
            {
                // Further processes tokens (splitting hyphenated terms and additional preprocessing of terms)
                List<String> tokenList = processor.processToken(term);

                // If hyphenated term, loop through each of the individual terms returned from processing.
                for (String token : tokenList)
                {
                    // Gets the UTF16 bytes representing the term
                    byte[] UTF16BYTES = term.getBytes(StandardCharsets.US_ASCII);

                    currentDocumentBytes = currentDocumentBytes + UTF16BYTES.length;
                    positionalIndex.addTerm(token, document.getId(), currentDocumentPosition);
                    currentDocumentPosition++;
                    currentDocumentTokens++;
                    corpusTokenCount = corpusTokenCount + 1;

                    // If it's the first encounter of a term, set value to 1. Otherwise, set to current frequency + 1
                    termFrequency.merge(token, 1, Integer::sum);
                }
            }

            // The meta-data is written to Azure Blob Storage to be retrieved during querying
            byte[] documentWeights = blobStorageWriter.serializeDocumentWeights(termFrequency, currentDocumentTokens, currentDocumentBytes);

            System.out.println("DocumentWeightLength: " + documentWeights.length);
            dataOutputStream.write(documentWeights);
        }

        // Upload the doc-weights to Azure Blob storage
        byte[] accumulatedData = byteArrayOutputStream.toByteArray();
        blobStorageClient.uploadFile("doc-weights.bin", accumulatedData);

        // As a final step, once all documents are processed, the average tokens per document is calculated
        double averageTokens = corpusTokenCount / corpus.getCorpusSize();

        // Average tokens is appended to the same Azure Blob Storage file containing all the meta-data
        byte[] serializedAverageTokens = blobStorageWriter.serializeAverageTokens(averageTokens);
        blobStorageClient.uploadFile("average-tokens.bin", serializedAverageTokens);

        System.out.println("Indexing Complete. \n");
        return positionalIndex;

    }


    /**
     * This method takes as a parameter a single term, and returns the posting list
     */
    @Override
    public List<Posting> getPostings(String term)
    {
        List<Posting> postingsForTerm = invertedIndex.get(term);

        // If the posting list is null, return an empty array
        if (postingsForTerm == null)
        {
            return new ArrayList<>();
        }
        // If not, return an array list of the postings
        else
        {
            return new ArrayList<>(postingsForTerm);
        }
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term)
    {

        return null;
    }

    /**
     * Returns the sorted vocabulary list of terms.
     */
    public List<String> getVocabulary()
    {
        List<String> sortedVocabulary = new ArrayList<>(vocabulary);
        Collections.sort(sortedVocabulary);
        return sortedVocabulary;
    }


}
