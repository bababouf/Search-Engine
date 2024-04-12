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
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;

	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	/**
	 * An ORQuery may contain two or more components, of which the end result will be the union of all the lists.
	 */
	@Override
	public List<Posting> getPostings(Index index) throws IOException {
		List<Posting> result = new ArrayList<>();

		if(mComponents.size() >= 2)
		{
			QueryComponent firstLiteral = mComponents.get(0);
			QueryComponent secondLiteral = mComponents.get(1);
			result = MergeLists(firstLiteral.getPostings(index), secondLiteral.getPostings(index));

			for(int i = 2; i < mComponents.size(); i++)
			{
				result = MergeLists(result, mComponents.get(i).getPostings(index));
			}
		}
		return result;

	}

	/**
	 * This method will obtain a list that contains the union of the two lists passed to it. In order to do this,
	 * two pointers are used and each list is traversed. The pointers start at the beginning of each list, and the smaller
	 * docID list is always incremented. Each unique docID found is added to the results list. When the end is reached for one of the lists,
	 * the loop continues until the end of the other list is reached.
	 */

	public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral) {
		List<Posting> result = new ArrayList<>();
		int j = 0;
		int k = 0;
		while (true) {
			if (j == literal.size() && k == nextLiteral.size()) { // End of both lists is found
				return result;
			} else if (j == literal.size()) { // End of one of the lists is found
				Posting tempToAdd = nextLiteral.get(k);
				result.add(tempToAdd);
				k++;
			} else if (k == nextLiteral.size()) { // End of the other list is found
				Posting tempToAdd = literal.get(j);
				result.add(tempToAdd);
				j++;
			} else {
				if (literal.get(j).getDocumentId() == nextLiteral.get(k).getDocumentId()) { // Both lists contain same docID, add one
					Posting tempToAdd = literal.get(j);
					result.add(tempToAdd);
					j++;
					k++;
				} else if (literal.get(j).getDocumentId() < nextLiteral.get(k).getDocumentId()) { // Increment list with lesser docID

					Posting tempToAdd = literal.get(j);
					result.add(tempToAdd);
					j++;

				} else
				{
					Posting tempToAdd = nextLiteral.get(k);
					result.add(tempToAdd);

					k++;
				}
			}
		}

	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
				String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
				+ " )";
	}
}

