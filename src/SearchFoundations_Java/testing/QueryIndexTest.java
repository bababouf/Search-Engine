package SearchFoundations_Java.testing;

import SearchFoundations_Java.cecs429.indexing.DiskPositionalIndex;
import SearchFoundations_Java.testing.BuildIndexTest;
import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.edu.csulb.DiskPositionalIndexer;
import org.junit.After;
import org.junit.Before;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class QueryIndexTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    private  Path absolutePathToIndex;
    private DirectoryCorpus corpus;
    private DiskPositionalIndex index;

    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }
    public void setUpIndexAndCorpus(){
        BuildIndexTest buildIndex = new BuildIndexTest();
        absolutePathToIndex = buildIndex.readInPathTest();
        corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToIndex, ".json");
        index = new DiskPositionalIndex(absolutePathToIndex, corpus.getCorpusSize());
    }
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    private void setInput(String input)
    {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    public void readInQueryModeTest() {
        setInput("invalid\nalsoInvalid\n1\n");
        setUpStreams();

        try {
            int result = DiskPositionalIndexer.readInSystemMode(new Scanner(System.in)); // Replace YourClassName with the actual class name
            assertEquals(1, result);

        } finally {
            restoreStreams();

        }
    }


    @Test
    public void booleanQueryStemTest(){

        setUpStreams();
        DiskPositionalIndexer.stemQuery("STEM generously");
        assertTrue(outputStream.toString().contains("gener"));
        restoreStreams();
    }
    @Test
    public void booleanQueryVocab(){
        setUpIndexAndCorpus();
        setUpStreams();
        DiskPositionalIndexer.printVocabulary(index);
        restoreStreams();

    }

    @Test
    public void defaultRankedQueryTest(){

    }
    @Test
    public void TermFreqInvDocFreqRankedQueryTest(){

    }
    @Test
    public void OkapiRankedQueryTest(){

    }
    @Test
    public void WakyRankedQueryTest(){

    }



}
