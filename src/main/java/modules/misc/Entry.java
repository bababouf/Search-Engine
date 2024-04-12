package modules.misc;

public class Entry {
    Integer docID;
    Double Ad;

    public Entry(Integer id, Double accumulatorValue) {
        docID = id;
        Ad = accumulatorValue;
    }
    public Integer getDocID(){
        return docID;
    }
    public Double getAd(){
        return Ad;
    }

}
