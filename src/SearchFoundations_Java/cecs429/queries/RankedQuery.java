package SearchFoundations_Java.cecs429.queries;

import SearchFoundations_Java.cecs429.indexing.Index;
import SearchFoundations_Java.cecs429.indexing.Posting;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class RankedQuery implements QueryComponent{
    private List<QueryComponent> mComponents;

    public RankedQuery(List<QueryComponent> components) {
        mComponents = components;
    }


    @Override
    public List<Posting> getPostings(Index index) throws IOException {

        //RandomAccessFile docWeights = new RandomAccessFile("rw");
        Integer corpusSize = index.getCorpusSize();
        Double wqt = 0.0;

        Map<Integer, Double> ADMap = new HashMap<>();
        for(QueryComponent literal: mComponents) {
            List<Posting> postingForTerm = literal.getPostings(index);
            Integer docFreq = postingForTerm.size();
            wqt = Math.log(1 + (corpusSize / docFreq));

            for (Posting p : postingForTerm) {
                Integer id = p.getDocumentId();
                Double wdt = 1 + (Math.log(p.getTFtd()));
                Double AD = ADMap.get(id);
                if(AD == null)
                {
                    AD = (wdt * wqt);
                    ADMap.put(id, AD);
                }
                else
                {
                    AD = AD + (wdt * wqt);
                    ADMap.put(id, AD);

                }



            }


        }
        for (Map.Entry<Integer, Double> entry : ADMap.entrySet()) {
            Double ADValue = entry.getValue();
            if(ADValue != 0)
            {

            }
            System.out.println("Document ID: " + entry.getKey()+" \n "+ "Ad Value: " + entry.getValue());

        }



        return null;
    }


}
