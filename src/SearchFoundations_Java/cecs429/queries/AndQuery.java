package SearchFoundations_Java.cecs429.queries;
import SearchFoundations_Java.cecs429.indexing.*;

import javax.management.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
	private static final boolean booleanPositions = false;
	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) throws IOException {


		QueryComponent firstLiteral = mComponents.get(0);
		List<Posting> result = new ArrayList<>();
		QueryComponent secondLiteral = mComponents.get(1);
		result = MergeLists(firstLiteral.getPostings(index), secondLiteral.getPostings(index));

		for(int i = 2; i < mComponents.size(); i++)
		{
			result = MergeLists(result, mComponents.get(i).getPostings(index));

		}

		return result;
	}


	public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral)
	{
		List<Posting> result = new ArrayList<>();
		int j = 0;
		int k = 0;
		while (true) {
			if (j >= literal.size() || k >= nextLiteral.size()) {
				break;
			}
			if(literal.get(j).getDocumentId() == nextLiteral.get(k).getDocumentId())
			{
				Posting tempToAdd = literal.get(j);
				result.add(tempToAdd);
				j++;
				k++;
			}
			else if (literal.get(j).getDocumentId() < nextLiteral.get(k).getDocumentId())
			{
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
