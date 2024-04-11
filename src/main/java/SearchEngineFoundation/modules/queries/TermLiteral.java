package SearchEngineFoundation.modules.queries;
import SearchEngineFoundation.modules.indexing.*;


import java.io.IOException;
import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	
	public TermLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) throws IOException {
		return index.getPostings(mTerm);
	}



	@Override
	public String toString() {
		return mTerm;
	}
}
