package SearchFoundations_Java.cecs429.queries;
import SearchFoundations_Java.cecs429.indexing.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap.*;



import javax.management.Query;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;

	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}

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


	public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral) {
		List<Posting> result = new ArrayList<>();
		int j = 0;
		int k = 0;
		while (true) {
			if (j == literal.size() && k == nextLiteral.size()) {
				return result;
			} else if (j == literal.size()) {
				Posting tempToAdd = nextLiteral.get(k);
				result.add(tempToAdd);
				k++;
			} else if (k == nextLiteral.size()) {
				Posting tempToAdd = literal.get(j);
				result.add(tempToAdd);
				j++;
			} else {
				if (literal.get(j).getDocumentId() == nextLiteral.get(k).getDocumentId()) {
					Posting tempToAdd = literal.get(j);
					result.add(tempToAdd);
					j++;
					k++;
				} else if (literal.get(j).getDocumentId() < nextLiteral.get(k).getDocumentId()) {

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

