package modules.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A positional inverted index maintains a mapping of terms to their corresponding posting lists,
 * and an array of vocabulary terms.
 */
public class PositionalInvertedIndex implements Index {

    private final HashMap<String, List<Posting>> invertedIndex;
    private final List<String> vocabulary;

    /**
     * Constructs a PositionalInvertedIndex with an empty index and vocabulary list.
     */
    public PositionalInvertedIndex() {
        invertedIndex = new HashMap<>();
        vocabulary = new ArrayList<>();
    }

    /**
     * Adds a term to the positional index.
     * @param term The term to add.
     * @param documentId The document ID where the term is found.
     * @param position The position of the term in the document.
     */
    public void addTerm(String term, int documentId, int position) {
        List<Posting> postingListForTerm = invertedIndex.get(term);

        if (postingListForTerm == null) {
            postingListForTerm = new ArrayList<>();
            Posting p = new Posting(documentId, new ArrayList<>(List.of(position)));
            postingListForTerm.add(p);
            invertedIndex.put(term, postingListForTerm);
            vocabulary.add(term);
        } else {
            Posting lastPosting = postingListForTerm.get(postingListForTerm.size() - 1);

            if (lastPosting.getDocumentId() == documentId) {
                lastPosting.getPositions().add(position);
            } else {
                Posting p = new Posting(documentId, new ArrayList<>(List.of(position)));
                postingListForTerm.add(p);
            }
        }
    }

    /**
     * Returns the posting list for a single term.
     * @param term The term to retrieve postings for.
     * @return The posting list for the term.
     */
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

    /**
     * Returns the sorted vocabulary list of terms.
     * @return The sorted vocabulary list.
     */
    public List<String> getVocabulary() {
        List<String> sortedVocabulary = new ArrayList<>(vocabulary);
        Collections.sort(sortedVocabulary);
        return sortedVocabulary;
    }

    @Override
    public Integer getCorpusSize() {
        return vocabulary.size();
    }


}
