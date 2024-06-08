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
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	/**
	 * An ANDQuery could have 2 or more literals to merge together. The merge that is taking place is with the posting lists
	 * for each of the literals. For example, if "dog cat" is the query, the posting list for dog and the posting list for cat
	 * would need to be merged. Similarly, if the query was "dog cat elephant lion", all four posting lists would need to be merged.
	 * The end result would be one posting list of the intersecting document IDs.
	 */
	@Override
	public List<Posting> getPostings(Index index) throws IOException {
		// Initialize result with the merged postings of the first two components
		QueryComponent firstLiteral = mComponents.get(0);
		QueryComponent secondLiteral = mComponents.get(1);
		List<Posting> result = MergeLists(firstLiteral.getPostings(index), secondLiteral.getPostings(index));

		// Iterate through the remaining components and merge their postings with the current result
		for (int i = 2; i < mComponents.size(); i++) {
			result = MergeLists(result, mComponents.get(i).getPostings(index));
		}

		return result;
	}

	/**
	 * This method will merge two posting lists. This is done by traversing each list until the end of the list is reached.
	 * Two pointers (j and k) are used to keep track of the position in the list, and these pointers start at the beginning of
	 * each list. Each iteration, the docID is checked for both lists. If it's a match, the document is added to the results list
	 * that will be returned. If the end of either list is reached, exit loop. For iterating through the lists, increment the pointer
	 * whose docID is the lesser of the two.
	 */
	public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral) {
		List<Posting> result = new ArrayList<>();
		int j = 0;
		int k = 0;

		while (j < literal.size() && k < nextLiteral.size()) {
			int docId1 = literal.get(j).getDocumentId();
			int docId2 = nextLiteral.get(k).getDocumentId();

			if (docId1 == docId2) { // If the document IDs match, add to the result list
				result.add(literal.get(j));
				j++;
				k++;
			} else if (docId1 < docId2) { // Increment the list with the lesser docID
				j++;
			} else {
				k++;
			}
		}

		return result;
	}
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
