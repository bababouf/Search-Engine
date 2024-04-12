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
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}

	/**
	 * Phrase Literals are a special kind of query component in that they require positions. For example, if the query was
	 * "the dog", we are looking for a specific order (specific positions where those terms are found in a document). Thus,
	 * this getPostings method is not used for phrase literal components.
	 */
	@Override
	public List<Posting> getPostings(Index index) throws IOException {

		String firstTerm = mTerms.get(0);
		List<Posting> firstTermPostings = index.getPostings(firstTerm);
		String secondTerm = mTerms.get(1);
		List<Posting> nextTermPostings = index.getPostings(secondTerm);
		List<Posting> result = MergeLists(firstTermPostings, nextTermPostings, 1);

		for(int i = 2; i < mTerms.size(); i++)
		{
			String nthTerm = mTerms.get(i);
			List<Posting> nthTermPostings = index.getPostings(nthTerm);
			result = MergeLists(result, nthTermPostings, i);
		}

		return result;
	}


	/**
	 * The getPostingsWithPositions method will obtain a list of postings for a phrase literal components. The phrase literal
	 * can contain many terms, all of which must be merged together for the end list that's returned. In this method, terms in the
	 * query must be found in the same document, and in addition, their positions must be appropriate.
	 */
	@Override
	public List<Posting> getPostingsWithPositions(Index index) throws IOException {

		List<Posting> result = new ArrayList<>();
		String firstTerm = mTerms.get(0);
		List<Posting> firstTermPostings = index.getPostingsWithPositions(firstTerm);
		String secondTerm = mTerms.get(1);
		List<Posting> nextTermPostings = index.getPostingsWithPositions(secondTerm);

		result = MergeLists(firstTermPostings, nextTermPostings, 1); // Merge lists called with both lists, as well as "1" to indicate they must be 1 position away

		for(int i = 2; i < mTerms.size(); i++)
		{
			String nthTerm = mTerms.get(i);
			List<Posting> nthTermPostings = index.getPostingsWithPositions(nthTerm);
			result = MergeLists(result, nthTermPostings, i);
		}

		return result;
	}


	public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral, int termsApart)
	{

		List<Posting> result = new ArrayList<>();
		// Two pointers, j and k, used to traverse each of the lists passed to this method
		int j = 0;
		int k = 0;

		while (true) {
			if (j >= literal.size() || k >= nextLiteral.size()) // End of the list is reached, break
			{
				break;
			}
			if(literal.get(j).getDocumentId() == nextLiteral.get(k).getDocumentId()) // Matching documents found
			{
				Posting listA = literal.get(j);
				Posting listB = nextLiteral.get(k);
				Posting resultingPosting;
				resultingPosting = checkPostingPositions(listA, listB, termsApart); // Must check for appropriate positions of terms in the document
				if (resultingPosting != null) // Proper positions were found if this selection statement is true
				{
					result.add(resultingPosting);
				}

				// Continue moving through each list
				j++;
				k++;

			}
			else if (literal.get(j).getDocumentId() < nextLiteral.get(k).getDocumentId()) // For each posting list, docIDs are in order. Thus, we continue with smaller docID list first
			{
				j++;

			} else {
				k++;

			}

		}

		return result;
	}

	/**
	 * This method ensures that the two postings passed to it have the appropriate separation of term positions.
	 */
	public Posting checkPostingPositions(Posting p, Posting d, int sup)
	{
		List<Integer> firstList = p.getPosition();
		List<Integer> secondList = d.getPosition();
		List<Integer> positions = new ArrayList<>();

		// Pointers to traverse each of the lists passed
		int l = 0;
		int k = 0;
		while(true)
		{
			if (l >= firstList.size() || k >= secondList.size()) // End of one of the lists found
			{
				break;
			}
			if(firstList.get(l) + sup ==  secondList.get(k) ) // Appropriate positions for terms found
			{
				positions.add(firstList.get(l));
				l++;
				k++;

			}
			else if(firstList.get(l) + sup < secondList.get(k))
			{

				l++;
			}
			else
			{
				k++;
			}
		}
		if(positions.size() != 0)
		{
			return new Posting(p.getDocumentId(), positions ); // Return the posting for the two terms found
		}
		else
		{
			return null;
		}


	}
	@Override
	public String toString() {
		String terms = 
			mTerms.stream()
			.collect(Collectors.joining(" "));
		return "\"" + terms + "\"";
	}
}
