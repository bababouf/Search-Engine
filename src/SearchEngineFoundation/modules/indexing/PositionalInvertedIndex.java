package SearchEngineFoundation.modules.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PositionalInvertedIndex implements Index{

    private final HashMap<String, List<Posting>> invertedIndex;

    private final List<String> mVocabulary;
    private int mCorpusSize;


    /**
     * A positional inverted index maintains two variables: one is the hashmap of strings to their corresponding posting lists,
     * and the other is the array of vocabulary terms.
     */
    public PositionalInvertedIndex() {

        invertedIndex = new HashMap<String, List<Posting>>();
        mVocabulary = new ArrayList<String>();
    }

    /**
     * This function adds a term to the positional index.
     */
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

            }
            else
            {
                postingList.add(position);
                Posting p = new Posting(documentId, postingList);
                postingListForTerm.add(p);
                invertedIndex.put(term, postingListForTerm);

            }
        }





    }

    /**
     * Returns posting list for a single term
     */
    @Override
    public List<Posting> getPostings(String term) {

        List<Posting> results = new ArrayList<>();
        List<Posting> postingsForTerm = invertedIndex.get(term);
        if(postingsForTerm == null)
        {
            return results;
        }
        else
        {
            for(Posting posting : postingsForTerm)
            {
                results.add(posting);
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

    /**
     * Returns the sorted vocabulary list of terms
     */
    public List<String> getVocabulary() {

        Collections.sort(mVocabulary);
        return mVocabulary;
    }

    @Override
    public Integer getCorpusSize() {
        return mVocabulary.size();
    }


}
