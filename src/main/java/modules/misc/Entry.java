package modules.misc;

public class Entry {
    private final Integer docID;
    private final Double accumulatorValue;

    /**
     * Constructs an Entry with the specified document ID and accumulator value.
     * @param id The document ID.
     * @param accumulatorValue The accumulator value.
     */
    public Entry(Integer id, Double accumulatorValue) {
        this.docID = id;
        this.accumulatorValue = accumulatorValue;
    }

    /**
     * Gets the document ID.
     * @return The document ID.
     */
    public Integer getDocID() {
        return docID;
    }

    /**
     * Gets the accumulator value.
     * @return The accumulator value.
     */
    public Double getAccumulatorValue() {
        return accumulatorValue;
    }
}