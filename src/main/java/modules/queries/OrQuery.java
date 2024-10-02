package modules.queries;

import modules.indexing.Index;
import modules.indexing.Posting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent
{

    // The components of the ORQuery
    private final List<QueryComponent> components;

    public OrQuery(List<QueryComponent> components)
    {
        this.components = components;
    }

    /**
     * An ORQuery may contain two or more components, of which the end result will be the union of all the lists.
     */
    @Override
    public List<Posting> getPostings(Index index) throws IOException
    {
        List<Posting> resultsList = new ArrayList<>();

        if (components.size() >= 2)
        {
            // Retrieve postings for the first two components and merge them
            QueryComponent listOne = components.get(0);
            QueryComponent listTwo = components.get(1);
            resultsList = MergeLists(listOne.getPostings(index), listTwo.getPostings(index));

            // Iterate through the remaining components and merge their postings with the current result
            for (int i = 2; i < components.size(); i++)
            {
                resultsList = MergeLists(resultsList, components.get(i).getPostings(index));
            }
        }

        return resultsList;
    }


    /**
     * This method will obtain a list that contains the union of the two lists passed to it. In order to do this,
     * two pointers are used and each list is traversed. The pointers start at the beginning of each list, and the smaller
     * docID list is always incremented. Each unique docID found is added to the results list. When the end is reached for one of the lists,
     * the loop continues until the end of the other list is reached.
     */

    public List<Posting> MergeLists(List<Posting> listOne, List<Posting> listTwo)
    {
        List<Posting> result = new ArrayList<>();
        int l1Index = 0;
        int l2Index = 0;

        while (l1Index < listOne.size() || l2Index < listTwo.size())
        {
            // If the end of listOne is reached, continue to add documents and increment listTwo
            if (l1Index == listOne.size())
            {

                result.add(listTwo.get(l2Index));
                l2Index++;
            }
            // If the end of listTwo is reached, continue to add documents and increment listOne
            else if (l2Index == listTwo.size())
            {
                result.add(listOne.get(l1Index));
                l1Index++;
            }
            // If neither list has reached the end...
            else
            {
                // Get the documentIDs from both of the lists
                int currentDocL1 = listOne.get(l1Index).getDocumentId();
                int currentDocL2 = listTwo.get(l2Index).getDocumentId();

                // If the documents match, add the ID to the resultsList and increment both lists
                if (currentDocL1 == currentDocL2)
                {
                    int l1Frequency = listOne.get(l1Index).getTermFrequency();
                    int l2Frequency = listTwo.get(l2Index).getTermFrequency();
                    int combinedFrequency = l1Frequency + l2Frequency;
                    listOne.get(l1Index).setTermFrequency(combinedFrequency);

                    result.add(listOne.get(l1Index));
                    l1Index++;
                    l2Index++;
                }
                // If they don't match, add the document with the lesser documentID and increment that document
                else if (currentDocL1 < currentDocL2)
                {
                    result.add(listOne.get(l1Index));
                    l1Index++;
                }
                //TODO
                // This should never be reached
                else
                {
                    result.add(listTwo.get(l2Index));
                    l2Index++;
                }
            }
        }

        return result;
    }

    @Override
    public String toString()
    {
        // Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
        return "(" +
                String.join(" + ", components.stream().map(c -> c.toString()).collect(Collectors.toList()))
                + " )";
    }
}

