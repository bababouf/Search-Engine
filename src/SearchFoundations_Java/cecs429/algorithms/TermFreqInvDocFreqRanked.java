package SearchFoundations_Java.cecs429.algorithms;

import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.indexing.DiskPositionalIndex;
import SearchFoundations_Java.cecs429.indexing.Posting;
import SearchFoundations_Java.cecs429.queries.QueryComponent;
import SearchFoundations_Java.edu.csulb.Entry;
import SearchFoundations_Java.edu.csulb.EntryComparator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class TermFreqInvDocFreqRanked implements RankingStrategy{


    public List<Entry> calculateAccumulatorValue(Map<Integer, Double> ADMap, DiskPositionalIndex onDiskIndex) {
        try {
            RandomAccessFile documentWeights = new RandomAccessFile(onDiskIndex.pathToWeights, "r");
            PriorityQueue<Entry> descendingAccumulators = new PriorityQueue<>(new EntryComparator());
            long startOfFile = documentWeights.getFilePointer();

            for (Integer ID : ADMap.keySet()) {
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

            while (!descendingAccumulators.isEmpty() && count < 10) {
                Entry rankedDocument = descendingAccumulators.poll();
                topRankedDocuments.add(rankedDocument);
                count++;
            }
            return topRankedDocuments;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Entry> calculate(List<QueryComponent> literals, DiskPositionalIndex onDiskIndex, DirectoryCorpus corpus) {
        double corpusSize = corpus.getCorpusSize();
        // ADMAP maps docIDs to corresponding accumulator value
        Map<Integer, Double> ADMap = new HashMap<>();

        try {
            for (QueryComponent literal : literals) {
                List<Posting> postingsForTerm = literal.getPostings(onDiskIndex);
                int documentFrequency = postingsForTerm.size();
                Double weightOfTermInQuery = Math.log((corpusSize / documentFrequency));

                for (Posting posting : postingsForTerm) {
                    Integer id = posting.getDocumentId();
                    Double weightOfTermInDocument = (double) posting.getTFtd();

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
            System.out.println("Shit not looking good");
        }
        return calculateAccumulatorValue(ADMap, onDiskIndex);
    }

}
