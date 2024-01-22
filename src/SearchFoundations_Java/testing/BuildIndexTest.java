package SearchFoundations_Java.testing;
import SearchFoundations_Java.edu.csulb.DiskPositionalIndexer;
import org.testng.annotations.Test;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class BuildIndexTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }

    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    /**
     * Tests the DiskPositionalIndexer.readInSystemMode method to ensure it rejects multiple non-numerical inputs until
     * a numerical input (1) is entered.
     */
    @Test
    public void readInSystemModeTest() {
        setUpStreams();
        setInput("invalid\nanotherInvalid\nyetAnotherInvalidInput\n1\n");

        try {
            int result = DiskPositionalIndexer.readInSystemMode(new Scanner(System.in)); // Replace YourClassName with the actual class name
            assertEquals(1, result);

        } finally {
            restoreStreams();
        }
    }

    /**
     * Tests the DiskPositionalIndexer.readInPath method to ensure it returns the absolute path from the
     * user entered directory name. This function doesn't work properly unless the directory specified by
     * the user is at the root directory of the program. For example, test-corpus and all-nps-sites-extracted
     * are both at the root directory, so when the user enters either of those directories the absolute path
     * is found and the program functions as expected. If another corpus were to be added (in order to query it)
     * it would also need to be at the root of the program.
     */
    @Test
    public Path readInPathTest() {
        setUpStreams();
        setInput("test-corpus");
        Path path;

        try {
            path = DiskPositionalIndexer.readInPath(new Scanner(System.in));
            String expectedRelativePath = "test-corpus";
            String expectedAbsolutePath = Paths.get(expectedRelativePath).toAbsolutePath().toString();
            String actualPathAsString = path.toString();
            assertEquals(expectedAbsolutePath, actualPathAsString);

        } finally {
            restoreStreams();
        }

        return path;

    }

    /**
     * Tests the DiskPositionalIndexer.buildOnDiskIndex method. The index is build using the test-corpus directory, which contains
     * three .json test files. The buildOnDiskIndex method builds the on disk index, but more importantly, it writes several crucial
     * double values to the docWeights.bin file located within test-corpus/index. This test ensures these values (LD, documentTokens,
     * bytes, avgTFTD) are as expected. In short, this method tests that docWeights.bin values are written and read properly, and that
     * the values are correct.
     */
    @Test
    public void buildIndexAndTestDocumentWeights(){
        setUpStreams();
        Path absolutePath = readInPathTest();
        DiskPositionalIndexer.buildOnDiskIndex(absolutePath); // Builds the index from the test-corpus

        String pathToDocumentWeights = absolutePath.toString() + "/index/docWeights.bin";

        try {
            RandomAccessFile documentWeights =
                    new RandomAccessFile(
                            pathToDocumentWeights, "rw");

            documentWeights.seek(documentWeights.getFilePointer());
            double[][] twoD_arr = new double[5][4];

            for(int i = 0; i <= 4; i++)
            {
                twoD_arr[i][0] = documentWeights.readDouble(); // LD
                twoD_arr[i][1] = documentWeights.readDouble(); // DocumentTokens
                twoD_arr[i][2] = documentWeights.readDouble(); // Bytes
                twoD_arr[i][3] = documentWeights.readDouble(); // avgTFTD

            }

            double[] documentOneWeights = {9.17, 73.0, 440.0, 1.28}; // Manually calculated from test1.json
            double[] documentTwoWeights = {10.16, 88.0, 519.0, 1.35}; // Manually calculated from test2.json
            double[] documentThreeWeights = {10.21, 92.0, 521.0, 1.31}; // Manually calculated from test3.json
            double[] documentFourWeights = {11.30, 111.0, 627.0, 1.37}; // Manually calculated from test4.json
            double[] documentFiveWeights = {10.60, 97.0, 505.0, 1.40}; // Manually calculated from test4.json

            assertArrayEquals(documentOneWeights, twoD_arr[0], .01);
            assertArrayEquals(documentTwoWeights, twoD_arr[1], .01);
            assertArrayEquals(documentThreeWeights, twoD_arr[2], .01);
            assertArrayEquals(documentFourWeights, twoD_arr[3], .01);
            assertArrayEquals(documentFiveWeights, twoD_arr[4], .01);

            documentWeights.seek(documentWeights.length() - 8);
            double averageTokens = documentWeights.readDouble();
            assertEquals(92.2, averageTokens, .01);
            documentWeights.seek(documentWeights.length());
            documentWeights.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            restoreStreams();
        }

    }


}

