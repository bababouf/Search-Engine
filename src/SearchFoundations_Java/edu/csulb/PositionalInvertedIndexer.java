package SearchFoundations_Java.edu.csulb;
import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.documents.Document;
import SearchFoundations_Java.cecs429.documents.DocumentCorpus;
import SearchFoundations_Java.cecs429.indexing.Index;
import SearchFoundations_Java.cecs429.indexing.PositionalInvertedIndex;
import SearchFoundations_Java.cecs429.indexing.Posting;
import SearchFoundations_Java.cecs429.text.EnglishTokenStream;
import SearchFoundations_Java.cecs429.queries.*;
import SearchFoundations_Java.cecs429.text.NonBasicTokenProcessor;
import opennlp.tools.stemmer.PorterStemmer;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class PositionalInvertedIndexer {
    public static void main(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, SQLException {
        Scanner readIn = new Scanner(System.in);
        Path absolutePath = readInDirectoryToIndex(readIn);
        DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePath, ".json");
        Index index = indexCorpus(corpus);

        String query = "";
        do {
            query = readInQuery(readIn);

             if (query.contains("STEM")) {
                stemQuery(query);

            } else if (query.contains("VOCAB")) {
                printVocabulary(index);

            } else if(!query.contains("END")){
                processBooleanQuery(query, index, corpus);
            }
        } while (!query.equals("END"));
            endProgram(readIn);
    }

    /**
     * Prompts user to enter the directory name that will be indexed
     * @param readIn Scanner is passed to avoid opening many System.in streams.
     */
    public static Path readInDirectoryToIndex(Scanner readIn) {
        System.out.print("Enter corpus path: ");
        String path = readIn.nextLine();
        Path absolutePath = Paths.get(path).toAbsolutePath();
        return absolutePath;
    }

    /**
     * Displays the various query forms that are accepted, and prompts user to enter a query.
     */
    public static String readInQuery(Scanner readIn) {
        String query = "";
        System.out.print(
                "Query Forms: \n" +
                    "\tOR QUERY | Ex: 'Term1 + Term2 + ...'  \n" +
                    "\tAND QUERY | Ex: 'Term1 Term2 ...' \n" +
                    "\tPHRASE QUERY | Ex: \"Term1 term2 ...\" \n" +
                    "\tCombinations of the above are accepted.\n\n" +

                "Special Commands: 'END', 'STEM', 'VOCAB' \n" +
                    "\t'END' to terminate program. \n" +
                    "\t'STEM ____' to stem a term. \n" +
                    "\t'VOCAB' to list first 100 vocabulary terms.\n\n" +
                "Choice: ");
        query = readIn.nextLine();
        return query;
    }

    /**
     * Closes the scanner and ends the program.
     */
    public static void endProgram(Scanner readIn){
        System.out.println("Ending program.");
        readIn.close();
        System.exit(0);
    }

    /**
     * Uses PorterScanner to stem terms.
     * @param query
     */
    public static void stemQuery(String query){
        String termToStem = query.split(" ")[1];
        PorterStemmer stemmer = new PorterStemmer();
        String stemmedTerm = stemmer.stem(termToStem);
        System.out.println(termToStem + " --> " + stemmedTerm);
    }

    /**
     * Prints the first 100 vocabulary terms.
     * @param index
     */
    public static void printVocabulary(Index index){
        List<String> vocabulary = index.getVocabulary();
        List<String> first100Vocab = vocabulary.subList(0, 1000);
        System.out.println("First 100 terms (sorted): ");

        for (String term : first100Vocab) {
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

            for (int i = 0; i < queryPostings.size(); i++) {
                int documentID = queryPostings.get(i).getDocumentId();
                Document d = corpus.getDocument(documentID);
                System.out.println(d.getTitle() + " (ID " + d.getId() + ")");
                StringBuilder textBuilder = new StringBuilder();
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
            int a = 0;
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
     * A PositionalInvertedIndex is a hashmap that consists of all unique terms found in the corpus of documents.
     * The hashmap maps unique terms to a list of postings. Each posting contains the documentID and the positions (locations in the document) where the term is found.
     */

    public static PositionalInvertedIndex indexCorpus(DocumentCorpus corpus) {
        System.out.println("Indexing...");
        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
        List<String> tokenList;
        PositionalInvertedIndex positionalIndex = new PositionalInvertedIndex();

        for (Document d : corpus.getDocuments()) {
            int num = 0;
            Iterable<String> tokens = null;
            EnglishTokenStream ETS = new EnglishTokenStream(d.getContent());
            tokens = ETS.getTokens();

            for (String term : tokens) {
                tokenList = processor.processToken(term);
                for (String token : tokenList) {
                    positionalIndex.addTerm(token, d.getId(), num);
                    num++;
                }
            }
            positionalIndex.getVocabulary();


        }
        System.out.println("Indexing Complete. \n");

        return positionalIndex;


    }

}
