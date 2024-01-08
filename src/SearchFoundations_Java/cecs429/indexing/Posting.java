package SearchFoundations_Java.cecs429.indexing;

import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPosition;
	private Integer TFtd;

	public Posting(int documentId, List<Integer> position) {

		mDocumentId = documentId;
		mPosition = position;
	}

	public void setTFTD(Integer termFreqDoc){TFtd = termFreqDoc;}
	public Integer getTFtd(){return TFtd;}
	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getPosition() {
		return mPosition;
	}
}