package SearchFoundations_Java.testing;

import SearchFoundations_Java.cecs429.indexing.DiskPositionalIndex;
import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.edu.csulb.DiskPositionalIndexer;
import org.testng.annotations.Test;
import java.io.*;
import java.nio.file.Path;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

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
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    public void setUpIndexAndCorpus(){
        BuildIndexTest buildIndex = new BuildIndexTest();
        absolutePathToIndex = buildIndex.readInPathTest();
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
    public void booleanQueryTest_SingleTerm(){
        setUpStreams();
        try{
            DiskPositionalIndexer.processBooleanQuery("dogs", corpus, index, new Scanner(System.in) );


        }catch (IOException e){
            e.printStackTrace();
        }



    }
    @Test
    public void booleanQueryTest_ANDQuery(){

    }
    @Test
    public void booleanQueryTest_ORQuery(){

    }
    @Test
    public void booleanQueryTest_MIX(){

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
