package SearchFoundations_Java.testing;

import SearchFoundations_Java.cecs429.algorithms.DefaultRanked;
import SearchFoundations_Java.cecs429.algorithms.OkapiRanked;
import SearchFoundations_Java.cecs429.algorithms.TermFreqInvDocFreqRanked;
import SearchFoundations_Java.cecs429.algorithms.WakyRanked;
import SearchFoundations_Java.cecs429.indexing.DiskPositionalIndex;
import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.indexing.Posting;
import SearchFoundations_Java.cecs429.queries.QueryComponent;
import SearchFoundations_Java.cecs429.queries.RankedQueryParser;
import SearchFoundations_Java.edu.csulb.DiskPositionalIndexer;
import org.testng.annotations.Test;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class QueryIndexTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private DirectoryCorpus corpus;
    private DiskPositionalIndex index;

    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    public void setUpIndexAndCorpus(){
        BuildIndexTest buildIndex = new BuildIndexTest();
        Path absolutePathToIndex = buildIndex.readInPathTest();
        corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToIndex, ".json");
        index = new DiskPositionalIndex(absolutePathToIndex, corpus.getCorpusSize());
    }
    private void setInput(String input)
    {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    public void readInQueryModeTest() {
        setUpStreams();
        setInput("invalid\nalsoInvalid\n1\n");

        try {
            int result = DiskPositionalIndexer.readInSystemMode(new Scanner(System.in)); // Replace YourClassName with the actual class name
            assertEquals(1, result);

        } finally {
            restoreStreams();

        }
    }
    @Test
    public void booleanQuery_SingleTermTest(){
        List<Integer> documentIDs = new ArrayList<>();
        try{
            setUpIndexAndCorpus();
            List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery("dogs", index );
            for (Posting queryPosting : queryPostings) {
                documentIDs.add(queryPosting.getDocumentId());
            }

            List<Integer> expectedIDs = new ArrayList<>();
            expectedIDs.add(0);
            expectedIDs.add(4);
            assertEquals(expectedIDs, documentIDs);

        }catch (IOException e){
            e.printStackTrace();
        }

    }
    @Test
    public void booleanQuery_ANDQueryTest(){
        List<Integer> documentIDs = new ArrayList<>();
        try{
            setUpIndexAndCorpus();
            List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery("dogs cats",  index );
            for (Posting queryPosting : queryPostings) {
                documentIDs.add(queryPosting.getDocumentId());
            }

            List<Integer> expectedIDs = new ArrayList<>();
            expectedIDs.add(4);
            assertEquals(expectedIDs, documentIDs);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void booleanQuery_ORQueryTest(){
        List<Integer> documentIDs = new ArrayList<>();
        try{
            setUpIndexAndCorpus();
            List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery("dogs + walruses", index );
            for (Posting queryPosting : queryPostings) {
                documentIDs.add(queryPosting.getDocumentId());
            }

            List<Integer> expectedIDs = new ArrayList<>();
            expectedIDs.add(0);
            expectedIDs.add(3);
            expectedIDs.add(4);
            assertEquals(expectedIDs, documentIDs);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void booleanQuery_MixTermsTest(){
        List<Integer> documentIDs = new ArrayList<>();
        try{
            setUpIndexAndCorpus();
            List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery("dogs cats + walruses", index );
            for (Posting queryPosting : queryPostings) {
                documentIDs.add(queryPosting.getDocumentId());
            }

            List<Integer> expectedIDs = new ArrayList<>();
            expectedIDs.add(3);
            expectedIDs.add(4);
            assertEquals(expectedIDs, documentIDs);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void booleanQuery_TermNotFoundTest(){
        List<Integer> documentIDs = new ArrayList<>();
        try{
            setUpIndexAndCorpus();
            List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery("amigo", index );
            for (Posting queryPosting : queryPostings) {
                documentIDs.add(queryPosting.getDocumentId());
            }

            List<Integer> expectedIDs = new ArrayList<>();
            assertEquals(expectedIDs, documentIDs);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void booleanQuery_PhraseQueryTest(){
        List<Integer> documentIDs = new ArrayList<>();
        try{
            setUpIndexAndCorpus();
            List<Posting> queryPostings = DiskPositionalIndexer.processBooleanQuery("\"recognized for their\"", index );
            for (Posting queryPosting : queryPostings) {
                documentIDs.add(queryPosting.getDocumentId());
            }

            List<Integer> expectedIDs = new ArrayList<>();
            expectedIDs.add(3);
            assertEquals(expectedIDs, documentIDs);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void defaultRankedQuery_SingleTermTest(){
        DefaultRanked sup = new DefaultRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 2.12);
        expectedADMap.put(4, 2.62);


        Map<Integer, Double> ADMap = sup.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }
    @Test
    public void defaultRankedQuery_MultipleTermsTest(){
        DefaultRanked defaultRanked = new DefaultRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 2.12);
        expectedADMap.put(2, 2.62);
        expectedADMap.put(4, 5.25);


        Map<Integer, Double> ADMap = defaultRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }

    /**
     * In this test, a common word "the" is used in the query in order to show how the
     * ranking scheme handles words that appear frequently in many documents.
     */
    @Test
    public void defaultRankedQuery_CommonTermTest(){
        DefaultRanked defaultRanked = new DefaultRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "the cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, .69);
        expectedADMap.put(1, 1.65);
        expectedADMap.put(2, 3.32);
        expectedADMap.put(3, 1.65);
        expectedADMap.put(4, 4.08);


        Map<Integer, Double> ADMap = defaultRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }
    @Test
    public void tfidfRankedQuery_SingleTermTest(){
        TermFreqInvDocFreqRanked tfidfRanked = new TermFreqInvDocFreqRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 1.83);
        expectedADMap.put(4, 2.74);


        Map<Integer, Double> ADMap = tfidfRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);
    }
    @Test
    public void tfidfRankedQuery_MultipleTermsTest(){
        TermFreqInvDocFreqRanked tfidfRanked = new TermFreqInvDocFreqRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 1.83);
        expectedADMap.put(2, 2.74);
        expectedADMap.put(4, 5.49);


        Map<Integer, Double> ADMap = tfidfRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }

    /**
     * In this test, a common word "the" is used in the query in order to show how the
     * ranking scheme handles words that appear frequently in many documents. For the TFIDF
     * ranking scheme, words that appear often are given very little weight. In this case, since
     * "the" appears in all documents, it is gives 0 weight (ln(corpusSize = 5/ docFreq = 5)) = ln(1) = 0
     */
    @Test
    public void tfidfRankedQuery_CommonTermTest(){
        TermFreqInvDocFreqRanked tfidfRanked = new TermFreqInvDocFreqRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "the cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.0);
        expectedADMap.put(1, 0.0);
        expectedADMap.put(2, 2.74);
        expectedADMap.put(3, 0.0);
        expectedADMap.put(4, 2.74);


        Map<Integer, Double> ADMap = tfidfRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }
    @Test
    public void okapiRankedQuery_SingleTermTest(){
        OkapiRanked okapiRanked = new OkapiRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.49);
        expectedADMap.put(4, 0.52);


        Map<Integer, Double> ADMap = okapiRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);
    }




    @Test
    public void okapiRankedQuery_MultipleTermsTest(){
        OkapiRanked okapiRanked = new OkapiRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.49);
        expectedADMap.put(2, 0.53);
        expectedADMap.put(4, 1.05);


        Map<Integer, Double> ADMap = okapiRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }


    @Test
    public void okapiRankedQuery_CommonTermTest(){
        OkapiRanked okapiRanked = new OkapiRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "the cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.11);
        expectedADMap.put(1, 0.17);
        expectedADMap.put(2, 0.63);
        expectedADMap.put(3, 0.16);
        expectedADMap.put(4, 0.68);


        Map<Integer, Double> ADMap = okapiRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }

    @Test
    public void wakyRankedQuery_SingleTermTest(){
        WakyRanked wakyRanked = new WakyRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.55);
        expectedADMap.put(4, 0.63);


        Map<Integer, Double> ADMap = wakyRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);
    }




    @Test
    public void wakyRankedQuery_MultipleTermsTest(){
        WakyRanked wakyRanked = new WakyRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "dog cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.55);
        expectedADMap.put(2, 0.66);
        expectedADMap.put(4, 1.26);


        Map<Integer, Double> ADMap = wakyRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }


    @Test
    public void wakyRankedQuery_CommonTermTest(){
        WakyRanked wakyRanked = new WakyRanked();
        RankedQueryParser rankedParser = new RankedQueryParser();
        String query = "the cat";

        setUpIndexAndCorpus();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        Map<Integer, Double> expectedADMap = new HashMap<>();

        expectedADMap.put(0, 0.0);
        expectedADMap.put(1, 0.0);
        expectedADMap.put(2, 0.66);
        expectedADMap.put(3, 0.0);
        expectedADMap.put(4, 0.63);


        Map<Integer, Double> ADMap = wakyRanked.calculate(literals, index, corpus);

        for (Integer docID: ADMap.keySet()) {

            double truncatedToTwoDecimalPlaces = Math.floor(ADMap.get(docID) * 100) / 100;
            ADMap.put(docID, truncatedToTwoDecimalPlaces);

        }

        assertEquals(expectedADMap, ADMap);


    }



}
