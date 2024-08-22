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

public class TermFreqInvDocFreqRanked implements RankingStrategy
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

        // Will store document IDs -> accumulator values
        Map<Integer, Double> ADMap = new HashMap<>();

        try
        {
            // Loop through each term in the query
            for (QueryComponent literal : literals)
            {
                // Obtain the postings for the term
                List<Posting> postingsForTerm = literal.getPostings(index);

                // Obtain the number of documents the term shows up in
                int documentFrequency = postingsForTerm.size();

                // Calculate the weight of the term in the query
                Double weightOfTermInQuery = Math.log((corpusSize / documentFrequency));

                // Loop through each posting
                for (Posting posting : postingsForTerm)
                {
                    // Obtain the document ID for the posting
                    Integer id = posting.getDocumentId();

                    // Calculate the weight of the term in the document
                    Double weightOfTermInDocument = (double) posting.getTermFrequency();

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

            // Create priority queue for storing accumulator values in descending order
            PriorityQueue<Entry> descendingAccumulators = new PriorityQueue<>(new EntryComparator());

            // Creates streams for accessing specific bytes within document weights
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentWeights);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

            // Reset stream to the beginning of document weights
            byteArrayInputStream.mark(0);

            // Loop through each key in the ADMap
            for (Integer ID : ADMap.keySet())
            {
                // Obtain the current accumulator value
                Double accumulatorValue = ADMap.get(ID);

                /*
                Normalize the accumulator value by diving it by the LD value obtained in the document weights. For this scheme,
                the LD value is the euclidian norm of the term frequencies in that document, which is the square root of the summation
                of term frequency for a term squared. (sqrt(summation(tftd^2))
                 */
                if (accumulatorValue != 0)
                {
                    byteArrayInputStream.reset();

                    // Skip to the proper byte for the given document to read the LD value
                    dataInputStream.skipBytes(32 * ID);

                    // Read the LD value
                    Double Ld = dataInputStream.readDouble();

                    // Calculate new accumulator value
                    accumulatorValue = accumulatorValue / Ld;

                    // Create entry object to store ID and accumulator value
                    Entry rankedDocument = new Entry(ID, accumulatorValue);

                    // Add the entry object to the priority queue (organizing the values in descending order)
                    descendingAccumulators.add(rankedDocument);

                    // Put the accumulator value back into the ADMap
                    ADMap.put(ID, accumulatorValue);

                }
            }

            int count = 0;
            List<Entry> topRankedDocuments = new ArrayList<>();

            // Obtain the top 10 documents from the priority queue
            while (!descendingAccumulators.isEmpty() && count < 10)
            {
                Entry rankedDocument = descendingAccumulators.poll();
                topRankedDocuments.add(rankedDocument);
                count++;
            }

            return topRankedDocuments;

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

}
