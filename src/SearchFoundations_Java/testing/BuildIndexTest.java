package SearchFoundations_Java.testing;

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

public class BuildIndexTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }
    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }
    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }


    @Test
    public void readInSystemModeTest() {

        setInput("invalid\nalsoInvalid\n1\n");
        try {
            int result = DiskPositionalIndexer.readInSystemMode(new Scanner(System.in)); // Replace YourClassName with the actual class name
            //assertTrue(outputStream.toString().contains("Invalid input. Enter another choice: "));
            assertEquals(1, result);

        } finally {
            restoreStreams();

        }
    }
    @Test
    public Path readInPathTest() {
        String input = "test-corpus";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Path path;

        try {
            path = DiskPositionalIndexer.readInPath(new Scanner(System.in));
            String expectedRelativePath = "test-corpus";
            String expectedAbsolutePath = Paths.get(expectedRelativePath).toAbsolutePath().toString();
            String actualPathAsString = path.toString();
            assertEquals(expectedAbsolutePath, actualPathAsString);

        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);

        }

        return path;

    }

    @Test
    public void buildIndexAndTestDocumentWeights(){
        Path absolutePath = readInPathTest();
        DiskPositionalIndexer.buildOnDiskIndex(absolutePath);
        String pathToDocumentWeights = absolutePath.toString() + "/index/docWeights.bin";

        try {
            RandomAccessFile documentWeights =
                    new RandomAccessFile(
                            pathToDocumentWeights, "rw");

            documentWeights.seek(0);
            double[][] twoD_arr = new double[3][4];

            for(int i = 0; i <= 2; i++)
            {
                twoD_arr[i][0] = documentWeights.readDouble(); // LD
                twoD_arr[i][1] = documentWeights.readDouble(); // DocumentTokens
                twoD_arr[i][2] = documentWeights.readDouble(); // Bytes
                twoD_arr[i][3] = documentWeights.readDouble(); // avgTFTD

            }
            double[] documentOneWeights = {3.16, 10.0, 37.0, 1.0}; // Manually calculated from test1.json
            double[] documentTwoWeights = {3.31, 11.0, 47.0, 1.0}; // Manually calculated from test2.json
            double[] documentThreeWeights = {3.60, 13.0, 54.0, 1.0}; // Manually calculated from test3.json

            assertArrayEquals(documentOneWeights, twoD_arr[0], .01);
            assertArrayEquals(documentTwoWeights, twoD_arr[1], .01);
            assertArrayEquals(documentThreeWeights, twoD_arr[2], .01);

            documentWeights.seek(documentWeights.length() - 8);
            double averageTokens = documentWeights.readDouble();
            assertEquals(11.33, averageTokens, .01);

            documentWeights.seek(documentWeights.length());
            documentWeights.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }


}

