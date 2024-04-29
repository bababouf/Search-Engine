package drivers;

import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.documents.DocumentCorpus;
import modules.indexing.DiskIndexWriter;
import modules.indexing.Index;
import modules.indexing.PositionalInvertedIndex;
import modules.indexing.Posting;
import modules.queries.BooleanQueryParser;
import modules.queries.QueryComponent;
import modules.text.EnglishTokenStream;
import modules.text.NonBasicTokenProcessor;
import opennlp.tools.stemmer.PorterStemmer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PositionalInvertedIndexer {

    /**
     * This program allows for an in-memory index to be created (positionalInvertedIndex) and queried. The program
     * supports only normal disjunctive form (one of more AND queries joined with ORs) for the boolean queries. The user
     * can also choose to "STEM term", which uses a Porter Stemmer to get the stem of a term. Ex: generously -> gener. In addition,
     * the first 1000 vocabulary words can be printed in ascending alphabetical order.
     */
    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, SQLException {
        Scanner readIn = new Scanner(System.in);
        Path absolutePath = readInDirectoryToIndex(readIn);
        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePath, ".json");
        Index index = indexCorpus(corpus, absolutePath);

        String query;
        do {
            query = readInQuery(readIn);

            if (query.contains("STEM")) {
                stemQuery(query);

            } else if (query.contains("VOCAB")) {
                printVocabulary(index);

            } else if (!query.contains("END")) {
                processBooleanQuery(query, index, corpus);
            }
        } while (!query.equals("END"));
        endProgram(readIn);
    }

    /**
     * Prompts user to enter the directory name that will be indexed
     *
     * @param readIn Scanner is passed to avoid opening many System.in streams.
     */
    public static Path readInDirectoryToIndex(Scanner readIn) {
        System.out.print("Enter corpus path: ");
        String path = readIn.nextLine();
        return Paths.get(path).toAbsolutePath();
    }

    /**
     * Displays the various query forms that are accepted, and prompts user to enter a query.
     */
    public static String readInQuery(Scanner readIn) {
        System.out.print(
                """
                        Query Forms:\s
                        \tOR QUERY | Ex: 'Term1 + Term2 + ...' \s
                        \tAND QUERY | Ex: 'Term1 Term2 ...'\s
                        \tPHRASE QUERY | Ex: "Term1 term2 ..."\s
                        \tCombinations of the above are accepted.

                        Special Commands: 'END', 'STEM', 'VOCAB'\s
                        \t'END' to terminate program.\s
                        \t'STEM ____' to stem a term.\s
                        \t'VOCAB' to list first 100 vocabulary terms.

                        Choice:\s""");
        return readIn.nextLine();
    }

    /**
     * Closes the scanner and ends the program.
     */
    public static void endProgram(Scanner readIn) {
        System.out.println("Ending program.");
        readIn.close();
        System.exit(0);
    }

    /**
     * Uses PorterScanner to stem terms.
     */
    public static void stemQuery(String query) {
        String termToStem = query.split(" ")[1];
        PorterStemmer stemmer = new PorterStemmer();
        String stemmedTerm = stemmer.stem(termToStem);
        System.out.println(termToStem + " --> " + stemmedTerm);
    }

    /**
     * Prints the first 100 vocabulary terms.
     */
    public static void printVocabulary(Index index) {
        List<String> vocabulary = index.getVocabulary();
        List<String> first1000Vocab = vocabulary.subList(0, 1000);
        System.out.println("First 100 terms (sorted): ");

        for (String term : first1000Vocab) {
            System.out.println(term);
        }
        System.out.println("Total vocabulary terms: " + vocabulary.size());

    }

    /**
     * This method handles boolean queries entered by the user. The first function called, parseQuery, will determine the various parts of the query (the query could be a combination of AND, OR, (and) phrase queries.
     * For example: "Term1 Term2 + Term3"
     * This would have both an AND + OR query (Term1 and Term2) or (Term3).
     * More details can be found in the comment above parseQuery method signature.
     * The getPostings method returns the List of postings satisfying the query, and this list is displayed to the user.
     * The user can then choose to view the content of one of the documents.
     */
    public static void processBooleanQuery(String query, Index index, DocumentCorpus corpus) throws IOException {
        BooleanQueryParser booleanParser = new BooleanQueryParser();
        QueryComponent queryComponent = booleanParser.parseQuery(query);
        List<Posting> queryPostings = queryComponent.getPostings(index);

        if (queryPostings == null) {
            System.out.println("\n Term not found in corpus \n");
        } else {

            for (Posting queryPosting : queryPostings) {
                int documentID = queryPosting.getDocumentId();
                Document document = corpus.getDocument(documentID);
                System.out.println(document.getTitle() + " (ID " + document.getId() + ")");
            }
            System.out.println("Query Postings Size: " + queryPostings.size());
            int documentToShow;
            Scanner readIn = new Scanner(System.in);
            System.out.print("Enter a documentID to show: ");
            documentToShow = readIn.nextInt();
            System.out.print("You have entered: " + documentToShow + "\n");
            Document documentShow = corpus.getDocument(documentToShow);
            Reader jsonReader = documentShow.getContent();
            StringBuilder textBuilder = new StringBuilder();
            int character;
            int count = 0;

            while ((character = jsonReader.read()) != -1) {
                textBuilder.append((char) character);
                count++;

                if (count == 120) {
                    String sequenceFromBody = textBuilder.toString();
                    System.out.println(sequenceFromBody);
                    textBuilder.delete(0, textBuilder.length());
                    count = 0;
                }

            }
            String bodyOfDocument = textBuilder.toString();
            System.out.println(bodyOfDocument);

        }
    }

    /**
     * A PositionalInvertedIndex is a hashmap that consists of all unique terms found in the corpus of documents. The hashmap
     * maps unique terms to a list of postings. Each posting contains the documentID and the positions (locations in the document)
     * where the term is found. For example, the term "dog" might be mapped to many  documents. In each document, it might appear
     * many times and thus have a list of positions per document. In addition, this function calls calculateAndWriteDocumentWeights
     * for each document in order to write document weights.
     */
    public static PositionalInvertedIndex indexCorpus(DocumentCorpus corpus, Path absolutePath) throws IOException {

        System.out.println("Indexing...");
        File theDir = new File(absolutePath + "/index");
        if (!theDir.exists()){
            theDir.mkdirs();
        }


        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
        List<String> tokenList;
        PositionalInvertedIndex positionalIndex = new PositionalInvertedIndex();

        DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
        double averageTokens = 0;


        diskIndexWriter.clearFileContents(absolutePath);

        for (Document document : corpus.getDocuments()) {
            int documentTokens = 0;
            int bytes = 0;
            int position = 0;
            int id = document.getId();

            Iterable<String> tokens;
            Map<String, Integer> termFrequency = new HashMap<>();
            EnglishTokenStream ETS = new EnglishTokenStream(document.getContent());
            tokens = ETS.getTokens(); // Text in the document is split by whitespace into iterable strings

            for (String term : tokens) {
                tokenList = processor.processToken(term); // Tokens are further processed
                for (String token : tokenList) {

                    byte[] UTF16BYTES = term.getBytes(StandardCharsets.US_ASCII);
                    bytes = bytes + UTF16BYTES.length;
                    positionalIndex.addTerm(token, document.getId(), position); // Each term is added to the positional index
                    position++; // Each term added increases position by one
                    documentTokens++; // Calculating total terms in a document
                    averageTokens = averageTokens + 1;

                    Integer count = termFrequency.get(token);
                    if (count == null) {
                        termFrequency.put(token, 1);

                    } else {
                        termFrequency.put(token, count + 1);

                    }
                }
            }
            diskIndexWriter.calculateAndWriteDocumentWeights(termFrequency, absolutePath, id, documentTokens, bytes);

        }

        averageTokens = averageTokens / corpus.getCorpusSize();
        String pathToDocWeights = absolutePath + "/index/docWeights.bin";
        diskIndexWriter.writeAverageTokensForCorpus(pathToDocWeights, averageTokens);
        System.out.println("Indexing Complete. \n");
        return positionalIndex;


    }

}
