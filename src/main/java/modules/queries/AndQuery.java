package modules.queries;

import modules.indexing.Index;
import modules.indexing.Posting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent
{

    // The components of the ANDQuery
    private final List<QueryComponent> components;

    public AndQuery(List<QueryComponent> components)
    {
        this.components = components;
    }

    /**
     * An ANDQuery could have 2 or more literals to merge together. The merge that is taking place is with the posting lists
     * for each of the literals. For example, if "dog cat" is the query, the posting list for dog and the posting list for cat
     * would need to be merged. Similarly, if the query was "dog cat elephant lion", all four posting lists would need to be merged.
     * The end result would be one posting list of the intersecting document IDs.
     */
    @Override
    public List<Posting> getPostings(Index index) throws IOException
    {

        // Initialize result with the merged postings of the first two components
        QueryComponent listOne = components.get(0);
        QueryComponent listTwo = components.get(1);

        // Call mergeLists to find the intersection of both of the posting lists
        List<Posting> resultsList = mergeLists(listOne.getPostings(index), listTwo.getPostings(index));

        // Iterate through the remaining components and merge their postings with the current result
        for (int i = 2; i < components.size(); i++)
        {
            resultsList = mergeLists(resultsList, components.get(i).getPostings(index));
        }

        return resultsList;
    }

    /**
     * This method takes the posting lists from two terms and finds the intersection (the documents that both lists have
     * in common). The documentIDs in each of these list are stored in ascending order. This allows for two pointers to be
     * used to traverse the lists, checking for a documentID match at each iteration. If there is a match, the documentID
     * is added to the results list. If not, the list with the lesser documentID is incremented. This process is continued
     * until both lists have been traversed.
     */
    public List<Posting> mergeLists(List<Posting> listOne, List<Posting> listTwo)
    {
        List<Posting> resultsList = new ArrayList<>();
        int l1Index = 0;
        int l2Index = 0;

        while (l1Index < listOne.size() && l2Index < listTwo.size())
        {
            int currentDocL1 = listOne.get(l1Index).getDocumentId();
            int currentDocL2 = listTwo.get(l2Index).getDocumentId();

            // If the documentIDs match, add to results list
            if (currentDocL1 == currentDocL2)
            {
                resultsList.add(listOne.get(l1Index));
                l1Index++;
                l2Index++;
            }
            // Otherwise, increment list with lesser documentID
            else if (currentDocL1 < currentDocL2)
            {
                l1Index++;
            }
            // This selection statement is used when one list finishes; a match can still be found, and the list must be searched
            else
            {
                l2Index++;
            }
        }

        return resultsList;
    }

    @Override
    public String toString()
    {
        return
                String.join(" ", components.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
