package modules.rankingSchemes;

import modules.documents.DirectoryCorpus;
import modules.indexing.DiskPositionalIndex;
import modules.indexing.Posting;
import modules.misc.Entry;
import modules.misc.EntryComparator;
import modules.queries.QueryComponent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class WakyRanked implements RankingStrategy{

    /**
     * This method obtains the finals accumulator values for each document by dividing their previous values
     * by LD. The equation for LD can be found in the readME. Once the final AD value is found, a priority queue to sort
     * the highest ranking documents is used.
     */
    public List<Entry> calculateAccumulatorValue(Map<Integer, Double> ADMap, DiskPositionalIndex onDiskIndex) {
        try {
            RandomAccessFile documentWeights = new RandomAccessFile(onDiskIndex.pathToWeights, "r");
            PriorityQueue<Entry> descendingAccumulators = new PriorityQueue<>(new EntryComparator());
            long startOfFile = documentWeights.getFilePointer();

            for(Integer ID : ADMap.keySet())
            {
                Double accumulatorValue = ADMap.get(ID);

                if(accumulatorValue != 0){
                    documentWeights.seek(startOfFile + 32 * ID);
                    Double Ld = documentWeights.readDouble();
                    accumulatorValue = accumulatorValue / Ld;
                    Entry rankedDocument = new Entry(ID, accumulatorValue);
                    descendingAccumulators.add(rankedDocument);
                    ADMap.put(ID, accumulatorValue);

                }
            }

            int count = 0;
            List<Entry> topRankedDocuments = new ArrayList<>();

            while (!descendingAccumulators.isEmpty() && count < 10)
            {
                count++;
                Entry rankedDocument = descendingAccumulators.poll();
                topRankedDocuments.add(rankedDocument);

            }
            return topRankedDocuments;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This method will calculate an accumulator value for each document and store the result in a hashmap (mapping
     * docID to accumulatorValue). This is done for each literal (term) in the query, and each document that is found in the posting list for
     * that term. The equations for wacky ranked are found in the readME file.
     */
    @Override
    public Map<Integer, Double> calculate(List<QueryComponent> literals, DiskPositionalIndex onDiskIndex, DirectoryCorpus corpus) {
        double corpusSize = corpus.getCorpusSize();
        // ADMAP maps docIDs to corresponding accumulator value
        Map<Integer, Double> ADMap = new HashMap<>();

        try {
            for (QueryComponent literal : literals) {
                List<Posting> postingsForTerm = literal.getPostings(onDiskIndex);
                int documentFrequency = postingsForTerm.size();
                Double weightOfTermInQuery = Math.max(0, Math.log(
                        (corpusSize - documentFrequency)/documentFrequency));

                for (Posting posting : postingsForTerm) {
                    Integer id = posting.getDocumentId();
                    RandomAccessFile docWeights = new RandomAccessFile(onDiskIndex.pathToWeights, "r");
                    docWeights.seek((32 * id) + 24);
                    Double avgTFTD = docWeights.readDouble();
                    Double weightOfTermInDocument = (1 + Math.log(posting.getTFtd()))/(1 + Math.log(avgTFTD));


                    if (ADMap.get(id) == null) {
                        Double accumulatorValue = weightOfTermInDocument * weightOfTermInQuery;
                        ADMap.put(id, accumulatorValue);

                    } else {
                        Double accumulatorValue = ADMap.get(id);
                        accumulatorValue = accumulatorValue + (weightOfTermInDocument * weightOfTermInQuery);
                        ADMap.put(id, accumulatorValue);

                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ADMap;
    }


}
