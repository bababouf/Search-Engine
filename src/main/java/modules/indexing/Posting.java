package modules.indexing;

import java.util.List;

/**
 * Represents a posting with a document ID, positions within the document, and term frequency.
 */
public class Posting {
	private final int documentId;
	private final List<Integer> positions;
	private Integer termFrequency;

	/**
	 * Constructs a Posting with the specified document ID and positions.
	 * @param documentId The document ID.
	 * @param positions The positions of the term in the document.
	 */
	public Posting(int documentId, List<Integer> positions) {
		this.documentId = documentId;
		this.positions = positions;
	}

	/**
	 * Sets the term frequency in the document.
	 * @param termFrequency The term frequency to set.
	 */
	public void setTermFrequency(Integer termFrequency) {
		this.termFrequency = termFrequency;
	}

	/**
	 * Gets the term frequency in the document.
	 * @return The term frequency.
	 */
	public Integer getTermFrequency() {
		return termFrequency;
	}

	/**
	 * Gets the document ID.
	 * @return The document ID.
	 */
	public int getDocumentId() {
		return documentId;
	}

	/**
	 * Gets the positions of the term in the document.
	 * @return The positions of the term.
	 */
	public List<Integer> getPositions() {
		return positions;
	}
}