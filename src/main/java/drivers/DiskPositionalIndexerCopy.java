package drivers;

import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.indexing.DiskIndexWriter;
import modules.indexing.DiskPositionalIndex;
import modules.indexing.PositionalInvertedIndex;
import modules.indexing.Posting;
import modules.misc.Entry;
import modules.queries.BooleanQueryParser;
import modules.queries.PhraseLiteral;
import modules.queries.QueryComponent;
import opennlp.tools.stemmer.PorterStemmer;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class DiskPositionalIndexerCopy {

    /**
     * The program operates in two modes. One of the modes will create an index from a user-input corpus directory
     * (should be a corpus of all text files or all JSON files). The other mode will allow the user to query the index.
     * The program allows for two different types of queries, boolean and ranked. For boolean queries, the program supports
     * only normal disjunctive form (one of more AND queries joined with ORs). For ranked queries, the user can choose one
     * of four different algorithms: default (using frequencies of query terms), term frequency inverse document frequency,
     * okapi bm25, or Waky. More about these algorithms and the project as a whole can be found in the .readME.
     */
    public static void main(String[] args) throws SQLException, IOException {

        Scanner readIn = new Scanner(System.in);
        Path absolutePathToCorpus = readInPath(readIn);
        buildOnDiskIndex(absolutePathToCorpus);

    }

    /**
     * Prompts user to enter a path, which is returned as an absolute path. No checking is done here.
     */
    public static Path readInPath(Scanner readIn) {
        System.out.print("Enter corpus path: ");
        String pathString = readIn.next();
        Path path = Paths.get(pathString);
        path = path.toAbsolutePath();
        System.out.println("Absolute path from read in: " + path);
        return path;
    }

    /**
     * Here the on disk index is built from the positional index. The positionalInvertedIndex is an in-memory hashmap where the
     * terms are mapped to posting lists. Each posting contains the document the term is found in, as well as the positions.
     * A single term may have a very long posting list (shows up in many documents), and may have many positions (shows up many times
     * in that document). After being written to disk, a SQLite local database is created that holds the terms and the byte position
     * where they can be found. This makes for an efficient lookup during querying. Ex: "dog" begins at bytePosition 2000000
     */
    public static void buildOnDiskIndex(Path absolutePathToCorpus) {
        try {
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToCorpus, ".json");
            PositionalInvertedIndex index = PositionalInvertedIndexer.indexCorpus(corpus, absolutePathToCorpus); // Creates positionalInvertedIndex
            DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
            List<Long> bytePositions = diskIndexWriter.writeIndex(index, absolutePathToCorpus);
            diskIndexWriter.writeTermBytePositionsToDatabase(index, bytePositions, true); // Write byte positions to SQLite DB

        } catch (IOException | SQLException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }

    /**
     * This function will take a user query and provide resulting documents that satisfy the query. For all queries except for
     * phrase queries, positions aren't needed. That is, the position that the term shows up in a document isn't necessary.
     * This is why getPostingsWithPositions is called when the query is determined to be a phrase query, and getPostings is called
     * for all other queries.
     */
    public static List<Posting> processBooleanQuery(String query, DiskPositionalIndex index) throws IOException{
        BooleanQueryParser booleanParser = new BooleanQueryParser();
        QueryComponent queryComponent = booleanParser.parseQuery(query);
        List<Posting> queryPostings;
        if (queryComponent instanceof PhraseLiteral phraseLiteral) {

            queryPostings = phraseLiteral.getPostingsWithPositions(index);

        } else {
            queryPostings = queryComponent.getPostings(index);
        }
        return queryPostings;
    }



}
