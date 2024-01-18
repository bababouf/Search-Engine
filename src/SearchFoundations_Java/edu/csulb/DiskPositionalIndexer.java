
package SearchFoundations_Java.edu.csulb;

import SearchFoundations_Java.cecs429.algorithms.*;
import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.documents.Document;
import SearchFoundations_Java.cecs429.indexing.*;
import SearchFoundations_Java.cecs429.queries.*;
import opennlp.tools.stemmer.PorterStemmer;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class DiskPositionalIndexer {

    /** Below is the driver for this program. The program operates in two modes. One of the modes will create an index
     * from a user-entered corpus directory (should be a corpus of all text files or all JSON files). The other mode will
     * allow the user to query the index. The program allows for two different types of queries, boolean and ranked. For
     * boolean queries, the program supports only normal disjunctive form (one of more AND queries joined with ORs). For ranked queries,
     * the user can choose one of four different algorithms: default (using frequencies of query terms), term frequency inverse document frequency, okapi bm25, or Waky.
     * More about these algorithms and the project as a whole can be found in the .readME.
     */
    public static void main(String[] args) throws SQLException, IOException {

        Scanner readIn = new Scanner(System.in);
        int userChoice = readInSystemMode(readIn);

        // User enters '1' to build an index from a directory
        if (userChoice == 1) {
            Path absolutePath = readInPath(readIn);
            buildOnDiskIndex(absolutePath);

            // User enters '2' to query an existing index
        } else if (userChoice == 2) {
            int queryMode = readInQueryMode(readIn);
            Path absolutePathToIndex = checkForValidIndex(readIn); // Cannot pass this point without a valid index to query from
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePathToIndex, ".json");
            DiskPositionalIndex index = new DiskPositionalIndex(absolutePathToIndex, corpus.getCorpusSize());

            queryDispatch(queryMode, index, corpus, readIn);
        } else {
            endProgram(readIn);
        }

    }

    /**
     * This method prompts the user to choose between 3 options; 1 to build the index, 2 to process queries over an
     * existing index, and 3 to exit. Invalid inputs will again prompt the user.
     */
    public static int readInSystemMode(Scanner readIn) {
        int userChoice = 0;

        do {
            System.out.print(
                    """
                            [1] Build on-disk index for a specified directory\s
                            [2] Process queries over directory\s
                            [3] Exit\s
                            Choice:\s""");

            // Check if the input is an integer
            if (readIn.hasNextInt()) {
                userChoice = readIn.nextInt();

                // Check if the input is within the valid range
                if (userChoice < 1 || userChoice > 3) {
                    System.out.println("Invalid choice. Enter another choice: ");
                }
            } else {
                System.out.println("Invalid input. Enter another choice: ");
                readIn.nextLine(); // Consume invalid input
            }

        } while (userChoice < 1 || userChoice > 3);

        return userChoice;

    }

    /**
     * Prompts user to enter a path, which is returned as an absolute path. No checking is done here.
     */
    public static Path readInPath(Scanner readIn) {
        System.out.print("Enter corpus path: ");
        String pathString = readIn.next();
        Path path = Paths.get(pathString);
        path = path.toAbsolutePath();
        return path;
    }

    /**
     * Here the on disk index is built from the positional index. The positionalInvertedIndex is an in-memory hashmap where the
     * terms are mapped to posting lists. Each posting contains the document the term is found in, as well as the positions.
     * A single term may have a very long posting list (shows up in many documents), and may have many positions (shows up many times
     * in that document). After being written to disk, a SQLite local database is created that holds the terms and the byte position
     * where they can be found. This makes for an efficient lookup during querying. Ex: "dog" begins at bytePosition 2000000
     */
    public static void buildOnDiskIndex(Path absolutePath) {
        try {
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePath, ".json");
            PositionalInvertedIndex index = PositionalInvertedIndexer.indexCorpus(corpus, absolutePath);
            DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
            List<Long> bytePositions = diskIndexWriter.writeIndex(index, absolutePath);
            diskIndexWriter.writeTermBytePositionsToDatabase(index, bytePositions);

        } catch (IOException | SQLException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }
    }

    /**
     * In order to check that an index exists, the program loads the documents that match .json from the path that the
     * user entered. If no documents are found, there is no directory, and the user is prompted to enter a new directory.
     * Otherwise, the path is returned.
     */
    public static Path checkForValidIndex(Scanner readIn) {

        do {
            Path absolutePath = readInPath(readIn);
            DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePath, ".json");
            int corpusSize = corpus.getCorpusSize();

            if (corpusSize == 0) {
                System.out.println("Index does not exist on disk.");
            } else {
                readIn.nextLine();
                return absolutePath;
            }
        } while (true);


    }

    /**
     * Different functions are called depending on what type of mode the user wants to query in. If the user chose 1, the
     * program will process queries in booleanRetrieval mode. Otherwise, the user will pick from one of four different
     * rankedRetrieval modes (default, TFIDF, okapi, and wacky).
     */
    public static void queryDispatch(int queryMode, DiskPositionalIndex DPIndex, DirectoryCorpus corpus, Scanner readIn) throws IOException {
        //Boolean query
        if (queryMode == 1) {
            booleanRetrieval(DPIndex, corpus, readIn);
        } else {
            List<Entry> top10Ranked = null;
            int choice;
            do {

                choice = readInAlgorithmMode(readIn);
                RankedDispatch rankedAlgorithm = new RankedDispatch(DPIndex, corpus);

                switch (choice) {
                    case 1 -> top10Ranked = rankedAlgorithm.calculate(new DefaultRanked());
                    case 2 -> top10Ranked = rankedAlgorithm.calculate(new TermFreqInvDocFreqRanked());
                    case 3 -> top10Ranked = rankedAlgorithm.calculate(new OkapiRanked());
                    case 4 -> top10Ranked = rankedAlgorithm.calculate(new WackyRanked());
                }
                if (top10Ranked != null) {
                    printTop10Ranked(top10Ranked, corpus);
                }

            } while (choice != 0);
            endProgram(readIn);
        }

    }


    public static void endProgram(Scanner readIn) {
        System.out.println("Ending program.");
        readIn.close();
        System.exit(0);
    }

    /**
     * This function prompts the user to choose between 2 options; 1 to enter booleanRetrieval mode, and 2 for rankedRetrieval. Invalid inputs will again prompt the user.
     */
    public static int readInQueryMode(Scanner readIn) {
        int queryMode;
        do {
            System.out.print("""
                    [1] Boolean Retrieval\s
                    [2] Ranked Retrieval (Not functioning)
                    Choose Mode :\s""");
            //readIn.nextLine();

            while (!readIn.hasNextInt()) {
                System.out.println("Invalid input.");
                System.out.print("Choose Mode: ");
                readIn.next();
            }

            queryMode = readIn.nextInt();
            if (queryMode < 1 || queryMode > 2) {
                System.out.println("Invalid choice. ");
            }
        } while (queryMode != 1 && queryMode != 2);

        return queryMode;
    }

    /**
     * Print function used to display results for the 4 different ranking algorithms.
     */
    public static void printTop10Ranked(List<Entry> top10Results, DirectoryCorpus corpus) throws IOException {
        Scanner readIn = new Scanner(System.in);
        for (Entry top10Result : top10Results) {
            Integer docID = top10Result.docID;
            System.out.println("DocID: " + docID);
            System.out.println("ADValue: " + top10Result.Ad);
        }

        //start
        int documentToShow;
        System.out.print("Enter a documentID to show: ");
        documentToShow = readIn.nextInt();
        Document documentShow = corpus.getDocument(documentToShow);
        Reader jsonReader = documentShow.getContent();
        StringBuilder textBuilder = new StringBuilder();
        int a;
        int count = 0;

        while ((a = jsonReader.read()) != -1) {
            textBuilder.append((char) a);
            count++;
            if (count == 120) {
                String oi = textBuilder.toString();
                System.out.println(oi);
                textBuilder.delete(0, textBuilder.length());
                count = 0;
            }

        }
        String oi = textBuilder.toString();
        System.out.println(oi);


    }

    /**
     * This function allows the user to continue to enter queries until they enter 'END' and the program exits. In addition to
     * boolean queries, the user can choose to stem a term or list off the first 100 vocabulary terms in the index.
     * Note: The program allows AND/OR queries. NOT/AND NOT are not allowed since these aren't relevant for searching (imagine
     * searching "AND NOT Walruses") <-- Want to see everything but walruses?
     * In addition to booleanQueries, phrase queries are allowed. "Fires in yosemite" will provide documents with this phrase.
     */
    public static void booleanRetrieval(DiskPositionalIndex index, DirectoryCorpus corpus, Scanner readIn) throws IOException {

        String query;
        do {

            query = readInQuery();

            if (query.contains("STEM")) {
                stemQuery(query);
            } else if (query.contains("VOCAB")) {
                printVocabulary(index);

            } else if (!query.contains("END")) {
                processBooleanQuery(query, corpus, index, readIn); // Main function called to processBooleanQueries
            }

        } while (!query.equals("END"));
        endProgram(readIn);
    }

    /**
     * Uses a Porter Stemmer to get the stem of the term passed to the method. For example,
     * generously -> gener
     */

    public static void stemQuery(String query) {
        String termToStem = query.split(" ")[1];
        System.out.println("Term to Stem: " + termToStem);
        PorterStemmer stemmer = new PorterStemmer();
        String stemmedTerm = stemmer.stem(termToStem);
        System.out.println(termToStem + "--> " + stemmedTerm);
    }

    /**
     * Prints the first 1000 vocabulary terms in ascending order.
     */
    public static void printVocabulary(DiskPositionalIndex index) {
        List<String> vocabulary = index.getVocabulary();
        List<String> first1000Vocab = vocabulary.subList(0, 1000);
        System.out.println("First 1000 terms (sorted): ");

        for (String term : first1000Vocab) {
            System.out.println(term);
        }

        System.out.println("Total vocabulary terms: " + vocabulary.size());
    }

    /**
     * This function will take a user query and provide resulting documents that satisfy the query. For all queries except for
     * phrase queries, positions aren't needed. That is, the position that the term shows up in a document isn't necessary.
     * This is why getPostingsWithPositions is called when the query is determined to be a phrase query, and getPostings is called
     * for all other queries.
     */
    public static void processBooleanQuery(String query, DirectoryCorpus corpus, DiskPositionalIndex index, Scanner readIn) throws IOException {
        BooleanQueryParser booleanParser = new BooleanQueryParser();
        QueryComponent queryComponent = booleanParser.parseQuery(query);

        if (queryComponent instanceof PhraseLiteral phraseLiteral) {
            List<Posting> queryPostings = phraseLiteral.getPostingsWithPositions(index);
            printResults(corpus, queryPostings, readIn);
        } else {
            List<Posting> queryPostings = queryComponent.getPostings(index);
            printResults(corpus, queryPostings, readIn);
        }
    }

    /**
     * Prints the resulting document IDs for the query, and prompts the user to choose a document to show.
     */
    public static void printResults(DirectoryCorpus corpus, List<Posting> queryPostings, Scanner readIn) throws IOException {
        if (queryPostings == null) {
            System.out.println("\n Term not found in corpus \n");
        } else {
            for (Posting queryPosting : queryPostings) {
                int documentID = queryPosting.getDocumentId();
                Document d = corpus.getDocument(documentID);
                System.out.println(d.getTitle() + " (ID " + d.getId() + ")");
            }
            int documentToShow;
            System.out.print("Enter a documentID to show: ");
            documentToShow = readIn.nextInt();
            Document documentShow = corpus.getDocument(documentToShow);
            Reader jsonReader = documentShow.getContent();
            StringBuilder textBuilder = new StringBuilder();
            int a;
            int count = 0;

            while ((a = jsonReader.read()) != -1) {
                textBuilder.append((char) a);
                count++;
                if (count == 120) {
                    String oi = textBuilder.toString();
                    System.out.println(oi);
                    textBuilder.delete(0, textBuilder.length());
                    count = 0;
                }

            }
            String oi = textBuilder.toString();
            System.out.println(oi);
        }
    }

    /**
     * Prompts user to enter in 1, 2, 3, or 4 to choose the ranking algorithm that will be used
     * for ranked queries. If the user enters 0 the program exits. All other inputs are rejects, prompting
     * the user to try again.
     */
    public static int readInAlgorithmMode(Scanner readIn) {
        int userChoice;

        do {
            System.out.print("""
                    [0] Exit\s
                    [1] Default\s
                    [2] TF-IDF\s
                    [3] Okapi BM 25\s
                    [4] Wacky\s
                    Choice:\s""");
            while (!readIn.hasNextInt()) {
                System.out.println("Invalid input.");
                readIn.nextLine();
                System.out.print("Choice: ");
            }
            userChoice = readIn.nextInt();
            if (userChoice < 1 || userChoice > 3) {
                System.out.println("Invalid choice. ");
            }

        } while (userChoice > 4 || userChoice < 1);
        return userChoice;

    }

    /**
     * Prompts user to enter a query. Not checked.
     */

    public static String readInQuery() {

        Scanner readIn = new Scanner(System.in);
        System.out.print("Enter a query ('quit' to escape): ");
        String query = readIn.nextLine();
        System.out.print("You have entered: " + query + "\n");
        return query;

    }


}
