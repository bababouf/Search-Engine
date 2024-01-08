package SearchFoundations_Java.cecs429.queries;
import SearchFoundations_Java.cecs429.indexing.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}


	// COMMENT EVENTUALLY / FIX TRASHCAN VAR NAMES
	@Override
	public List<Posting> getPostings(Index index) throws IOException {

		List<Posting> result = new ArrayList<>();
		String firstTerm = mTerms.get(0);
		List<Posting> firstTermPostings = index.getPostings(firstTerm);
		String secondTerm = mTerms.get(1);
		List<Posting> nextTermPostings = index.getPostings(secondTerm);
		result = MergeLists(firstTermPostings, nextTermPostings, 1);

		for(int i = 2; i < mTerms.size(); i++)
		{
			String nthTerm = mTerms.get(i);
			List<Posting> nthTermPostings = index.getPostings(nthTerm);
			result = MergeLists(result, nthTermPostings, i);
		}

		return result;
	}

//COMMENT EVENTUALLY / FIX TRASHCAN VAR NAMES
	@Override
	public List<Posting> getPostingsWithPositions(Index index) throws IOException {
		List<Posting> result = new ArrayList<>();
		String firstTerm = mTerms.get(0);
		List<Posting> firstTermPostings = index.getPostingsWithPositions(firstTerm);
		String secondTerm = mTerms.get(1);
		List<Posting> nextTermPostings = index.getPostingsWithPositions(secondTerm);
		result = MergeLists(firstTermPostings, nextTermPostings, 1);

		for(int i = 2; i < mTerms.size(); i++)
		{
			String nthTerm = mTerms.get(i);
			List<Posting> nthTermPostings = index.getPostingsWithPositions(nthTerm);
			result = MergeLists(result, nthTermPostings, i);
		}

		return result;
	}

	// COMMENT EVENTUALLY / FIX TRASHCAN VAR NAMES
	public List<Posting> MergeLists(List<Posting> literal, List<Posting> nextLiteral, int sup)
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
				Posting temp2 = nextLiteral.get(k);
				Posting resultingPosting;
				resultingPosting = checkPostingPositions(tempToAdd, temp2, sup);
				if (resultingPosting != null) {

					result.add(resultingPosting);
				}
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
	public Posting checkPostingPositions(Posting p, Posting d, int sup)
	{
		List<Integer> firstList = p.getPosition();
		List<Integer> secondList = d.getPosition();
		List<Integer> positions = new ArrayList<>();

		int l = 0;
		int k = 0;
		while(true)
		{
			if (l >= firstList.size() || k >= secondList.size()) {
				break;
			}
			if(firstList.get(l) + sup ==  secondList.get(k) )
			{
				positions.add(firstList.get(l));
				l++;
				k++;
				// good
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
			Posting hello = new Posting(p.getDocumentId(), positions );
			return hello;
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
