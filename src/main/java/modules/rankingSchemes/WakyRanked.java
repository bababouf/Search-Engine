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
import static java.lang.Math.sqrt;

public class WakyRanked implements RankingStrategy
{

    /*
    Implements the calculate method; this method creates a hashmap which maps documentIDs to accumulator values. These
    accumulator values represent a document's relevance to the query. The accumulator value for any given document is the
    product of two weights: the weight of the term in the query, and the weight of the term in the document.
     */
    @Override
    public Map<Integer, Double> calculate(List<QueryComponent> literals, AzureBlobPositionalIndex index, DirectoryCorpus corpus)
    {
        // Obtain the corpus size
        double corpusSize = corpus.getCorpusSize();

        // Will store document IDs -> accumulator values//
        Map<Integer, Double> ADMap = new HashMap<>();

        try
        {
            // Loop through each term in the query
            for (QueryComponent literal : literals)
            {
                // Obtain the postings for a term
                List<Posting> postingsForTerm = literal.getPostings(index);

                // Obtain the number of documents the term appears in
                int documentFrequency = postingsForTerm.size();

                // Calculate the weight of the term in the query
                Double weightOfTermInQuery = Math.max(0, Math.log(
                        (corpusSize - documentFrequency) / documentFrequency));

                // Loop through each posting
                for (Posting posting : postingsForTerm)
                {
                    // Obtain the document ID for the posting
                    Integer id = posting.getDocumentId();

                    // Obtain the document weights
                    byte[] documentWeights = index.getDocumentWeights();

                    // Create streams for accessing specific bytes within the document weights
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentWeights);
                    DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

                    // Reset the stream to the beginning
                    byteArrayInputStream.reset();

                    /*
                    Skips to the specific double value in the document weights byte stream. Each document has four weights
                    that are written (LD, documentLength, bytes, and averageTermFrequency). These weights are written in order
                    of document ID, where documentID = 0 would have its four weights written first. To obtain the document length
                    for a document (32 * id) + 24 is used. The (32 * id) will skip to the start of the intended document that is
                    to be read (since 32 bytes is 4 double values), and the + 24 will skip one double value to obtain the document
                    length.
                     */
                    dataInputStream.skipBytes((32 * id) + 24);

                    // Read the averageTFTD value
                    Double avgTFTD = dataInputStream.readDouble();

                    // Calculate the weight of the term in the document
                    Double weightOfTermInDocument = (1 + Math.log(posting.getTermFrequency())) / (1 + Math.log(avgTFTD));

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
        try
        {
            // Obtain the document weights
            byte[] documentWeights = index.getDocumentWeights();

            // Create the priority queue that will hold the accumulator values in descending order
            PriorityQueue<Entry> descendingAccumulators = new PriorityQueue<>(new EntryComparator());

            // Used to access specific bytes within the stream
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentWeights);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

            // Reset stream to the beginning
            byteArrayInputStream.mark(0);

            // Loop through each key in the ADMap
            for (Integer ID : ADMap.keySet())
            {
                // Obtain the current accumulator value
                Double accumulatorValue = ADMap.get(ID);

                /*
                Normalize the accumulator value by diving it by the LD value obtained in the document weights. For this scheme,
                the LD value is the square root of the byte size for the document.
                 */
                if (accumulatorValue != 0)
                {
                    // Reset stream to the start
                    byteArrayInputStream.reset();

                    // Skip to the byte that gives the byte size for the specific document
                    dataInputStream.skipBytes((32 * ID) + 16);

                    // Read the LD value
                    Double Ld = dataInputStream.readDouble();

                    // Square root the LD value
                    Ld = sqrt(Ld);

                    // Calculate the new accumulator value
                    accumulatorValue = accumulatorValue / Ld;

                    // Create entry object to hold ID and accumulator value
                    Entry rankedDocument = new Entry(ID, accumulatorValue);

                    // Add the entry object to the priority queue
                    descendingAccumulators.add(rankedDocument);

                    // Put the accumulator value back into the ADMap
                    ADMap.put(ID, accumulatorValue);

                }
            }

            int count = 0;
            List<Entry> topRankedDocuments = new ArrayList<>();

            // Obtain the top 10 documents that have the highest accumulator values
            while (!descendingAccumulators.isEmpty() && count < 10)
            {
                count++;
                Entry rankedDocument = descendingAccumulators.poll();
                topRankedDocuments.add(rankedDocument);
            }

            return topRankedDocuments;

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

}
