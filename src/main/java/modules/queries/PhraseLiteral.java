package modules.queries;

import modules.indexing.Index;
import modules.indexing.Posting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent
{

    // The list of individual terms in the phrase
    private final List<String> terms = new ArrayList<>();

    /**
     * Constructs a PhraseLiteral with the given individual phrase terms.
     */
    public PhraseLiteral(List<String> terms)
    {
        this.terms.addAll(terms);
    }

    /**
     * Phrase Literals are a special kind of query component in that they require positions. For example, if the query was
     * "the dog", we are looking for a specific order (specific positions where those terms are found in a document). Thus,
     * this getPostings method is not used for phrase literal components.
     */
    @Override
    public List<Posting> getPostings(Index index) throws IOException
    {

        // Get the postings list for the first term
        String firstTerm = terms.get(0);
        List<Posting> result = index.getPostings(firstTerm);

        // Iterate through the remaining terms and merge their postings lists with the current result
        for (int i = 1; i < terms.size(); i++)
        {
            String nextTerm = terms.get(i);
            List<Posting> nextTermPostings = index.getPostings(nextTerm);

            // Merge the current result with the postings list of the next term
            result = MergeLists(result, nextTermPostings, i);
        }

        return result;
    }


    /**
     * The getPostingsWithPositions method will obtain a list of postings for a phrase literal components. The phrase literal
     * can contain many terms, all of which must be merged together for the end list that's returned. In this method, terms in the
     * query must be found in the same document, and in addition, their positions must be appropriate.
     */
    @Override
    public List<Posting> getPostingsWithPositions(Index index) throws IOException
    {

        // Retrieve the postings list for the first term
        String firstTerm = terms.get(0);
        List<Posting> result = index.getPostingsWithPositions(firstTerm);

        // Iterate through the remaining terms and merge their postings lists with the current result
        for (int i = 1; i < terms.size(); i++)
        {
            String nextTerm = terms.get(i);
            List<Posting> nextTermPostings = index.getPostingsWithPositions(nextTerm);

            // Merge the current result with the postings list of the next term
            // The offset 'i' indicates the position difference between terms
            result = MergeLists(result, nextTermPostings, i);
        }

        return result;
    }


    public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral, int termsApart)
    {
        List<Posting> result = new ArrayList<>();
        int l1Index = 0;
        int l2Index = 0;

        while (l1Index < literal.size() && l2Index < nextLiteral.size())
        {
            int currentDocL1 = literal.get(l1Index).getDocumentId();
            int currentDocL2 = nextLiteral.get(l2Index).getDocumentId();

            // Matching documents found
            if (currentDocL1 == currentDocL2)
            {
                Posting mergedPosting = checkPostingPositions(literal.get(l1Index), nextLiteral.get(l2Index), termsApart);
                if (mergedPosting != null)
                {
                    mergedPosting.setTermFrequency(1);
                    result.add(mergedPosting);
                }
                l1Index++;
                l2Index++;
            }
            // Advance in the first list
            else if (currentDocL1 < currentDocL2)
            {
                l1Index++;
            }
            // Advance in the second list
            else
            {
                l2Index++;
            }
        }

        return result;
    }

    /**
     * This method ensures that the two postings passed to it have the appropriate separation of term positions.
     */
    public Posting checkPostingPositions(Posting p, Posting d, int sup)
    {
        List<Integer> l1Positions = p.getPositions();
        List<Integer> l2Positions = d.getPositions();
        List<Integer> resultsPositions = new ArrayList<>();

        int l1Index = 0;
        int l2Index = 0;

        while (l1Index < l1Positions.size() && l2Index < l2Positions.size())
        {
            int pos1 = l1Positions.get(l1Index);
            int pos2 = l2Positions.get(l2Index);

            if (pos1 + sup == pos2)
            { // Appropriate positions found
                resultsPositions.add(pos1);
                l1Index++;
                l2Index++;
            }
            else if (pos1 + sup < pos2)
            { // Move forward in the first list
                l1Index++;
            }
            else
            { // Move forward in the second list
                l2Index++;
            }
        }

        return resultsPositions.isEmpty() ? null : new Posting(p.getDocumentId(), resultsPositions);
    }

    @Override
    public String toString()
    {
        String terms =
                this.terms.stream()
                        .collect(Collectors.joining(" "));
        return "\"" + terms + "\"";
    }
}
