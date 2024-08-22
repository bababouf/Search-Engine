package modules.rankingSchemes;

import modules.documents.DirectoryCorpus;
import modules.indexing.AzureBlobPositionalIndex;
import modules.indexing.Posting;
import modules.misc.Entry;
import modules.misc.EntryComparator;
import modules.queries.QueryComponent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class OkapiRanked implements RankingStrategy
{
    /*
    Implements the calculate method; this method creates a hashmap which maps documentIDs to accumulator values. These
    accumulator values represent a document's relevance to the query. The accumulator value for any given document is the
    product of two weights: the weight of the term in the query, and the weight of the term in the document.
     */
    @Override
    public Map<Integer, Double> calculate(List<QueryComponent> literals, AzureBlobPositionalIndex index, DirectoryCorpus corpus)
    {

        // Obtain corpus size
        double corpusSize = corpus.getCorpusSize();

        // Will store document IDs -> accumulator values
        Map<Integer, Double> ADMap = new HashMap<>();

        try
        {
            // Obtain the document weights
            byte[] documentWeights = index.getDocumentWeights();

            // Create streams for accessing specific bytes within the document weights
            ByteArrayInputStream byteArrayInputStreamDocWeights = new ByteArrayInputStream(documentWeights);
            DataInputStream dataInputStreamDocWeights = new DataInputStream(byteArrayInputStreamDocWeights);

            // Obtain the average tokens
            byte[] averageTokens = index.getAverageTokens();

            // Create streams for accessing specific bytes within the average tokens
            ByteArrayInputStream byteArrayInputStreamAvgTokens = new ByteArrayInputStream(averageTokens);
            DataInputStream dataInputStreamAvgTokens = new DataInputStream(byteArrayInputStreamAvgTokens);

            // Loop through each term in the query
            for (QueryComponent literal : literals)
            {
                // Obtain the postings for the term
                List<Posting> postingsForTerm = literal.getPostings(index);

                // Obtain the number of documents the term appears in
                int documentFrequency = postingsForTerm.size();

                // Calculate the weight of the term in the query
                Double weightOfTermInQueryCalculation = (corpusSize - documentFrequency + .5) / (documentFrequency + .5);
                Double weightOfTermInQuery = Math.max(.1, Math.log(weightOfTermInQueryCalculation));

                // Loop through each posting
                for (Posting posting : postingsForTerm)
                {
                    // Reset the streams to the start
                    byteArrayInputStreamDocWeights.reset();
                    byteArrayInputStreamAvgTokens.reset();

                    // Get the document ID of the posting
                    Integer id = posting.getDocumentId();

                    // Obtain the number of times the term appears in the document
                    int termFrequencyOfTermInDocument = posting.getTermFrequency();

                    /*
                    Skips to the specific double value in the document weights byte stream. Each document has four weights
                    that are written (LD, documentLength, bytes, and averageTermFrequency). These weights are written in order
                    of document ID, where documentID = 0 would have its four weights written first. To obtain the document length
                    for a document (32 * id) + 8 is used. The (32 * id) will skip to the start of the intended document that is
                    to be read (since 32 bytes is 4 double values), and the + 8 will skip one double value to obtain the document
                    length.
                     */
                    dataInputStreamDocWeights.skipBytes((32 * id) + 8);
                    double docLength = dataInputStreamDocWeights.readDouble();

                    // The averageTokensPerDoc value is simply the only value, so nothing is skipped and the 1st value is read
                    double avgTokensPerDoc = dataInputStreamAvgTokens.readDouble();

                    // Calculate weight of the term in the document using the above values
                    Double weightOfTermInDocument =
                            (2.2 * termFrequencyOfTermInDocument) / (1.2 * (.25 + .75 * (docLength / avgTokensPerDoc)) + posting.getTermFrequency());


                    // Create or accumulate the accumulator value for each document
                    if (ADMap.get(id) == null)
                    {
                        Double accumulatorValue = weightOfTermInDocument * weightOfTermInQuery;
                        ADMap.put(id, accumulatorValue);

                    }
                    else
                    {
                        Double accumulatorValue = ADMap.get(id);
                        accumulatorValue = accumulatorValue + (weightOfTermInDocument * weightOfTermInQuery);
                        ADMap.put(id, accumulatorValue);
                    }
                }
            }

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return ADMap;
    }

    @Override
    public List<Entry> normalizeAccumulatorValues(Map<Integer, Double> ADMap, AzureBlobPositionalIndex index)
    {
        // Priority queue that will hold the top documents with the highest accumulator values
        PriorityQueue<Entry> descendingAccumulators = new PriorityQueue<>(new EntryComparator());

        // Loop through each key in the ADMap
        for (Integer ID : ADMap.keySet())
        {
            // Obtain the current accumulator value
            Double accumulatorValue = ADMap.get(ID);

            // Obtain the new accumulator value (in this scheme LD is set to 1)
            if (accumulatorValue != 0)
            {
                Double Ld = 1.0;

                // Obtain new accumulator value
                accumulatorValue = accumulatorValue / Ld;

                // Create entry object that holds ID and accumulator value
                Entry rankedDocument = new Entry(ID, accumulatorValue);

                // Add entry object to the priority queue (organizing the accumulator values in descending order)
                descendingAccumulators.add(rankedDocument);

                // Put new accumulator value back into ADMap
                ADMap.put(ID, accumulatorValue);
            }
        }

        int count = 0;
        List<Entry> topRankedDocuments = new ArrayList<>();

        // Obtain the top 10 documents from the priority queue (docs with the highest accumulator values)
        while (!descendingAccumulators.isEmpty() && count < 10)
        {
            count++;
            Entry rankedDocument = descendingAccumulators.poll();
            topRankedDocuments.add(rankedDocument);
        }
        return topRankedDocuments;

    }



}
