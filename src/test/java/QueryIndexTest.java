import modules.rankingSchemes.DefaultRanked;
import modules.rankingSchemes.OkapiRanked;
import modules.rankingSchemes.TermFreqInvDocFreqRanked;
import modules.rankingSchemes.WakyRanked;
import modules.indexing.DiskPositionalIndex;
import modules.documents.DirectoryCorpus;
import modules.indexing.Posting;
import modules.queries.QueryComponent;
import modules.queries.RankedQueryParser;
import drivers.DiskPositionalIndexer;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This file tests each of the different query mode options the user can select. For boolean queries, the user can enter a single term,
 * AND queries, OR queries, or a mixed query of ANDs that are OR'd together. For ranked queries, the user can choose one of
 * several schemes, including default, tfidf, okapi bm25, and wacky. For each of these schemes, the user can enter a single term or multiple
 * terms in "bag of words" fashion. The file contains tests for each of these functionalities, using the test-corpus of 5 .json documents to ensure
 * the methods being tested are giving accurate results. The equations for each of the ranking schemes can be found in the readME file.
 */
public class QueryIndexTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private DirectoryCorpus corpus;
    private DiskPositionalIndex index;

    /**
     * Some functions being tested print to the console. This method allows for the redirection of the output stream.
     */
    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }

    /**
     * Sets the streeams (input and output) back to original
     */
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    /**
     * Many of the query tests below call this function to set up the index that queries will be ran on. For all the query tests below,
     * the test-corpus of 5 json files is used.
     */
    public void setUpIndexAndCorpus(){
        BuildIndexTest buildIndex = new BuildIndexTest();

        String relativePath = "test-corpus";
        Path absolutePathToIndex = Paths.get(relativePath).toAbsolutePath();
        corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToIndex, ".json");
        index = new DiskPositionalIndex(absolutePathToIndex, corpus.getCorpusSize());
    }

    /**
     * This method is used to simulate user input; the string passed is what will be input to the function being tested.
     */
    private void setInput(String input)
    {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    /**
     * This method ensures invalid inputs are rejects for the DiskPositionalIndexer.readInSystemMode() method.
     */

    /*
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
*/
    /**
     * Boolean Query mode allows for several types of queries, and this method ensures single term queries are processed correctly.
     * Two of the test-corpus documents contain the word dog, and are added to the expectedIDs list. DocumentIDs are the IDs returned
     * by the processBooleanQuery method we are testing.
     */
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

    /**
     * This method tests boolean AND queries using the input "dogs cats". Only one test-corpus document contains both these terms, and this is
     * added to the expectedIDs list. DocumentIDs are the IDs returned by the processBooleanQuery method we are testing.
     */
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

    /**
     * This method tests boolean OR queries with the input "dogs + walruses". One document contains the term walruses, and
     * two contain the term "dogs". These documents are added to expectedIDs, and documentIDs is what is returned by the method
     * we are testing.
     */
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

    /**
     * Boolean queries can take the form of multiple AND queries that are OR'd together. This method ensure that a mix of
     * AND + OR queries returns the correct result. One document contains the terms "dogs" and "cats", and one document contains the
     * term "walruses". Both of these are added to expectedIDs, and documentIDs are returned by the method being tested.
     */
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

    /**
     * This method tests that the program doesn't break when a term is queried that isn't found in the corpus.
     */
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

    /**
     * This method tests phrase queries using the input "recognized for their" phrase found in one of the test corpus
     * documents. This document is added to the expectedIDs list and tested against what the method we are testing returns.
     */
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

    /**
     * This method tests a single term query for the default ranked scheme. Since "dog" is used as the query, only the two
     * documents that contain "dog" have nonzero accumulator values. The expectedADMap values are calculated manually using the
     * formulas found for the default ranked scheme in the readME file.
     */
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

    /**
     * This method tests multiple terms using the default ranked scheme. The expectedADMap values are calculated manually.
     */
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

    /**
     * This method tests single term queries for the tfidf ranked scheme. The expectedADMap values are calculated manually.
     */
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
    /**
     * This method tests multiple term queries for the tfidf ranked scheme. The expectedADMap values are calculated manually.
     */
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
    /**
     * This method tests single term queries for the okapi bm25 ranked scheme. The expectedADMap values are calculated manually.
     */
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


    /**
     * This method tests multiple term queries for the okapi bm25 ranked scheme. The expectedADMap values are calculated manually.
     */
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

    /**
     * In this test, a common word "the" is used in the query in order to show how the
     * ranking scheme handles words that appear frequently in many documents.
     */
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
    /**
     * This method tests single term queries for the wacky ranked scheme. The expectedADMap values are calculated manually.
     */
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

    /**
     * This method tests multiple term queries for the wacky ranked scheme. The expectedADMap values are calculated manually.
     */

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

    /**
     * In this test, a common word "the" is used in the query in order to show how the
     * ranking scheme handles words that appear frequently in many documents.
     */
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
