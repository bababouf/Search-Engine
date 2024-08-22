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

public class DefaultRanked implements RankingStrategy
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
            // Loop through each literal (each term in the query)
            for (QueryComponent literal : literals)
            {
                // Obtain the posting list for the term
                List<Posting> postingsForTerm = literal.getPostings(index);

                // Obtain the number of documents the term appears in
                int documentFrequency = postingsForTerm.size();

                // Calculate weight for the term
                Double weightOfTermInQuery = Math.log(1 + (corpusSize / documentFrequency));

                // Loop through each posting (the object representing a single document where the term appears in)
                for (Posting posting : postingsForTerm)
                {
                    // Obtain the document ID
                    Integer id = posting.getDocumentId();

                    // Calculate weight for that term in the document (based on how many times the term appears)
                    Double weightOfTermInDocument = (1 + (Math.log(posting.getTermFrequency())));

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

    /*
    Normalizes the accumulator values that were calculated in the calculate method. This method is important because it allows
    for documents of varying lengths to be compared properly. In other words, longer documents that would typically have far
    higher accumulator values will be adjusted so that they can be compared to far shorter documents.
     */
    @Override
    public List<Entry> normalizeAccumulatorValues(Map<Integer, Double> ADMap, AzureBlobPositionalIndex index)
    {

        try
        {
            // Obtain the document weights
            byte[] documentWeights = index.getDocumentWeights();

            // Create a priority queue that will organize the highest accumulator values entries at the front
            PriorityQueue<Entry> descendingAccumulators = new PriorityQueue<>(new EntryComparator());

            // Create streams for accessing specific bytes in the document weights
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentWeights);
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

            // Set stream to the beginning
            byteArrayInputStream.mark(0);

            // Go through each document that is in the ADMap
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

                    // Skip to read the proper LD value for the given document
                    dataInputStream.skipBytes(32 * ID);

                    // Read the LD value
                    Double Ld = dataInputStream.readDouble();

                    // Calculate the normalized accumulator value
                    accumulatorValue = accumulatorValue / Ld;

                    // Create entry object to hold ID and accumulator value
                    Entry rankedDocument = new Entry(ID, accumulatorValue);

                    // Add the entry object to the priority queue (to organize in descending order based on accumulator value)
                    descendingAccumulators.add(rankedDocument);

                    // Put new accumulator value back into the ADMap
                    ADMap.put(ID, accumulatorValue);
                }
            }


            int count = 0;
            List<Entry> topRankedDocuments = new ArrayList<>();

            // Obtain the top 10 documents by removing from the priority queue
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
