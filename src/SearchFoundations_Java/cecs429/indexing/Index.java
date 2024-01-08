package SearchFoundations_Java.cecs429.indexing;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {


	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 */
	List<Posting> getPostings(String term) throws IOException;



	/**
	 * A sorted list of all terms in the index vocabulary.
	 */
	Integer getCorpusSize();

	Integer getVocabSize() throws IOException;
	List<String> getVocabulary();

	List<Posting> getPostingsWithPositions(String firstTerm) throws IOException;
}
