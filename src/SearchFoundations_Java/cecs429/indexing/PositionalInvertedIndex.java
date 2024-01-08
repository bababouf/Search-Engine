package SearchFoundations_Java.cecs429.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PositionalInvertedIndex implements Index{

    private final HashMap<String, List<Posting>> invertedIndex;

    private final List<String> mVocabulary;
    private int mCorpusSize;



    public PositionalInvertedIndex() {

        invertedIndex = new HashMap<String, List<Posting>>();
        mVocabulary = new ArrayList<String>();
    }
    public void addTerm(String term, int documentId, int position) {

        List<Posting> postingListForTerm;
        postingListForTerm = invertedIndex.get(term);
        List<Integer> postingList = new ArrayList<>();

        if(postingListForTerm == null)
        {
            List<Posting> postingListForTerm2 = new ArrayList<>();
            postingList.add(position);
            Posting p = new Posting(documentId, postingList);
            postingListForTerm2.add(p);
            invertedIndex.put(term, postingListForTerm2);
            mVocabulary.add(term);
        }
        else
        {
            Posting lastIndex = postingListForTerm.get(postingListForTerm.size() - 1);

            if(lastIndex.getDocumentId() == documentId)
            {
                postingList = lastIndex.getPosition();
                postingList.add(position);

                postingListForTerm.set(postingListForTerm.size() - 1, lastIndex);
                invertedIndex.put(term, postingListForTerm);

                return;
            }
            else
            {

                postingList.add(position);
                Posting p = new Posting(documentId, postingList);
                postingListForTerm.add(p);
                invertedIndex.put(term, postingListForTerm);
            }
        }


        return;



    }

    @Override
    public List<Posting> getPostings(String term) {

        List<Posting> results = new ArrayList<>();
        List<Posting> ayy = invertedIndex.get(term);
        if(ayy == null)
        {
            return results;
        }
        else
        {
            for(Posting p : ayy)
            {
                results.add(p);
            }
        }

        return results;
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {

        System.out.println("Im returning null and therefore this shit is fucked");
        return null;
    }


    @Override
    public Integer getVocabSize() throws IOException {
        return null;
    }
    public List<String> getVocabulary() {
        //Map<String, List<Posting>> theMap = new HashMap<>(invertedIndex);
        //TreeMap<String, List<Posting>> sortedMap = new TreeMap<>(theMap);

        Collections.sort(mVocabulary);

        return mVocabulary;
        //return Collections.unmodifiableList(mVocabulary);
    }

    @Override
    public Integer getCorpusSize() {
        return mVocabulary.size();
    }


}
