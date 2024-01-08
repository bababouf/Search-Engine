/**
package SearchFoundations_Java.edu.csulb;

import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.documents.Document;
import SearchFoundations_Java.cecs429.documents.DocumentCorpus;
import SearchFoundations_Java.cecs429.indexing.*;
import SearchFoundations_Java.cecs429.queries.*;
import SearchFoundations_Java.cecs429.text.EnglishTokenStream;
import SearchFoundations_Java.cecs429.text.NonBasicTokenProcessor;
import opennlp.tools.stemmer.PorterStemmer;
import java.util.Comparator;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.lang.Math;

class Entry{
    Integer docID;
    Double Ad;
    public Entry(Integer id, Double accumulatorValue){
        docID = id;
        Ad = accumulatorValue;
    }

}
class EntryComparator implements Comparator<Entry>{

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    public int compare(Entry s1, Entry s2) {
        if (s1.Ad < s2.Ad)
            return 1;
        else if (s1.Ad > s2.Ad)
            return -1;
        return 0;
    }
}
public class DiskPositionalIndexer {

    public static void main(String[] args) throws SQLException, IOException {

        int userChoice = readInSystemMode();


        if(userChoice == 1)
        {

            Path absolutePath = readInPathToIndex().toAbsolutePath();
            DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePath, ".json");
            Index index = indexCorpus(corpus, absolutePath);
            DiskIndexWriter DIW = new DiskIndexWriter();
            DIW.writeIndex(index, absolutePath);
        }
        else
        {

            Integer queryMode = readInQueryMode();
            Path absolutePath = readInPathToIndex().toAbsolutePath();
            DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(absolutePath, ".json");
            System.out.println("CORPUS SIZE: " + corpus.getCorpusSize());
            Integer corpusSize = corpus.getCorpusSize();
            DiskPositionalIndex DPIndex = new DiskPositionalIndex(absolutePath, corpusSize);

            if(queryMode == 1)
            {
                booleanRetrieval(DPIndex, corpus);
            }
            else if(queryMode == 2)
            {
                List<Entry> top10Results = new ArrayList<>();
                Integer choice = readInAlgorithmMode();
                if(choice == 1)
                {
                    top10Results = rankedRetrievalDefault(DPIndex, corpus);
                    for(int i = 0; i < top10Results.size(); i++)
                    {
                        Integer docID = top10Results.get(i).docID;
                        Document sup = corpus.getDocument(docID);
                        //System.out.println("DocID: " + docID);
                        //System.out.println("ADValue: " + top10Results.get(i).Ad);


                    }



                    //print(List<Entry> top10Results)
                } else if (choice == 2)
                {

                    top10Results = rankedRetrieval_TFIDF(DPIndex, corpus);
                    for(int i = 0; i < top10Results.size(); i++)
                    {
                        Integer docID = top10Results.get(i).docID;
                        Document sup = corpus.getDocument(docID);
                        //System.out.println("DocID: " + docID);
                        //System.out.println("ADValue: " + top10Results.get(i).Ad);

                    }

                }
                else if (choice == 3)
                {
                    top10Results = rankedRetrieval_Okapi(DPIndex, corpus);
                    for(int i = 0; i < top10Results.size(); i++)
                    {
                        Integer docID = top10Results.get(i).docID;
                        Document sup = corpus.getDocument(docID);
                        System.out.println(sup.getTitle() + "ADValue: " + top10Results.get(i).Ad);

                    }
                }
                else{
                    top10Results = rankedRetrieval_Wacky(DPIndex, corpus);
                    for(int i = 0; i < top10Results.size(); i++)
                    {
                        Integer docID = top10Results.get(i).docID;
                        Document sup = corpus.getDocument(docID);
                        System.out.println(sup.getTitle() + "ADValue: " + top10Results.get(i).Ad);

                    }
                }

            }

        }
        System.exit(0);

    }

    public static List<Entry> rankedRetrievalDefault(DiskPositionalIndex index, DocumentCorpus corpus) throws IOException {

        PriorityQueue<Entry> DescendingAccumulators = new PriorityQueue<>(new EntryComparator());
        String query = "";
        RandomAccessFile docWeights = new RandomAccessFile(index.pathToWeights, "r");

        long startOfFile = docWeights.getFilePointer();

        double corpusSize = corpus.getCorpusSize();
        do {
            query = readInQuery();
            RankedQueryParser rankedParser = new RankedQueryParser();
            List<QueryComponent> literals = rankedParser.parseQuery(query);

            Double wqt = 0.0;
            Map<Integer, Double> ADMap = new HashMap<>();
            for (QueryComponent literal : literals) {
                List<Posting> postingForTerm = literal.getPostings(index);
                Double docFreq = (double) postingForTerm.size();

                wqt =  Math.log(1 + (corpusSize / docFreq));


                for (Posting p : postingForTerm) {
                    Integer id = p.getDocumentId();

                    Double wdt =  (1 + (Math.log(p.getTFtd())));
                    //System.out.println("WDT: " + wdt);
                    //System.out.println("DocID: " + id + "\n");

                    if (ADMap.get(id) == null) {
                        double AD = wdt * wqt;
                        ADMap.put(id, AD);
                    } else {
                        double AD = ADMap.get(id);
                        AD = AD + (wdt * wqt);
                        ADMap.put(id, AD);
                    }
                }

            }
            for (Map.Entry<Integer, Double> entry : ADMap.entrySet()) {
                Double ADValue = entry.getValue();
                if (ADValue != 0) {
                    docWeights.seek(startOfFile);
                    docWeights.skipBytes(32 * entry.getKey());
                    Double Ld = docWeights.readDouble();
                    ADValue = ADValue / Ld;
                    Entry sup = new Entry(entry.getKey(), ADValue);
                    DescendingAccumulators.add(sup);
                    ADMap.put(entry.getKey(), ADValue);
                }

            }
            Integer count = 1;
            List<Entry> results = new ArrayList<>();
            while (!DescendingAccumulators.isEmpty() && count <= 50) {
                Entry temp = DescendingAccumulators.poll();
                results.add(temp);

                System.out.println("DocID: " + temp.docID);
                System.out.println("ADValue : " + temp.Ad);
                count++;
            }
            return results;


        }while (query != "exit");
    }
    public static List<Entry> rankedRetrieval_TFIDF(DiskPositionalIndex index, DocumentCorpus corpus) throws IOException {

        PriorityQueue<Entry> DescendingAccumulators = new PriorityQueue<>(new EntryComparator());
        String query = "";
        RandomAccessFile docWeights = new RandomAccessFile(index.pathToWeights, "r");

        long startOfFile = docWeights.getFilePointer();

        double corpusSize = corpus.getCorpusSize();
        do {
            query = readInQuery();
            RankedQueryParser rankedParser = new RankedQueryParser();
            List<QueryComponent> literals = rankedParser.parseQuery(query);

            Double wqt = 0.0;
            Map<Integer, Double> ADMap = new HashMap<>();
            for (QueryComponent literal : literals) {
                List<Posting> postingForTerm = literal.getPostings(index);
                Double docFreq = (double) postingForTerm.size();

                wqt =  Math.log((corpusSize / docFreq));
                System.out.println("WQT: " + wqt);

                for (Posting p : postingForTerm) {
                    Integer id = p.getDocumentId();

                    Double wdt =  Double.valueOf(p.getTFtd());
                    System.out.println("WDT: " + wdt);
                    System.out.println("DocID: " + id + "\n");

                    if (ADMap.get(id) == null) {
                        double AD = wdt * wqt;
                        ADMap.put(id, AD);
                    } else {
                        double AD = ADMap.get(id);
                        AD = AD + (wdt * wqt);
                        ADMap.put(id, AD);
                    }
                }

            }
            for (Map.Entry<Integer, Double> entry : ADMap.entrySet()) {
                Double ADValue = entry.getValue();
                if (ADValue != 0) {
                    docWeights.seek(startOfFile);
                    docWeights.skipBytes(32 * entry.getKey());
                    Double Ld = docWeights.readDouble();
                    ADValue = ADValue / Ld;
                    Entry sup = new Entry(entry.getKey(), ADValue);
                    DescendingAccumulators.add(sup);
                    ADMap.put(entry.getKey(), ADValue);
                }

            }
            List<Entry> results = new ArrayList<>();
            Integer count = 1;
            while (!DescendingAccumulators.isEmpty() && count <= 50) {
                Entry temp = DescendingAccumulators.poll();
                results.add(temp);
                //System.out.println("DocID: " + temp.docID);
                //System.out.println("ADValue : " + temp.Ad);
                count++;
            }

            return results;

        }while (query != "exit");
    }

    public static List<Entry> rankedRetrieval_Okapi(DiskPositionalIndex index, DocumentCorpus corpus) throws IOException {

        PriorityQueue<Entry> DescendingAccumulators = new PriorityQueue<>(new EntryComparator());
        String query = "";
        RandomAccessFile docWeights = new RandomAccessFile(index.pathToWeights, "r");
        RandomAccessFile avgDocLength = new RandomAccessFile(index.pathToAvgDocLength, "r");

        Double averageDocLength = avgDocLength.readDouble();
        long startOfFile = docWeights.getFilePointer();

        double corpusSize = corpus.getCorpusSize();
        do {
            query = readInQuery();
            RankedQueryParser rankedParser = new RankedQueryParser();
            List<QueryComponent> literals = rankedParser.parseQuery(query);

            Double wqt = 0.0;
            Map<Integer, Double> ADMap = new HashMap<>();
            for (QueryComponent literal : literals) {
                List<Posting> postingForTerm = literal.getPostings(index);
                Double docFreq = (double) postingForTerm.size();

                wqt = Math.max(.1, Math.log((corpusSize - docFreq + .5) /( docFreq + .5)));

                System.out.println("WQT: " + wqt);
                //wqt =  Math.log(1 + (corpusSize / docFreq));

                for (Posting p : postingForTerm) {
                    Integer id = p.getDocumentId();

                    docWeights.seek(startOfFile);
                    docWeights.skipBytes(id * 32);
                    docWeights.skipBytes(8);
                    Double docLength = docWeights.readDouble();

                    Double wdt = ((2.2 * p.getTFtd())/ (1.2 * (.25 + .75 * docLength/averageDocLength)) + p.getTFtd());
                    System.out.println("DocID: " + id);
                    System.out.println("WDT: " + wdt);
                    if (ADMap.get(id) == null) {
                        double AD = wdt * wqt;
                        ADMap.put(id, AD);
                    } else {
                        double AD = ADMap.get(id);
                        AD = AD + (wdt * wqt);
                        ADMap.put(id, AD);
                    }
                }

            }
            for (Map.Entry<Integer, Double> entry : ADMap.entrySet()) {
                Double ADValue = entry.getValue();
                if (ADValue != 0) {
                    docWeights.seek(startOfFile);
                    docWeights.skipBytes(8 * entry.getKey());
                    Double Ld = docWeights.readDouble();
                    ADValue = ADValue / Ld;
                    Entry sup = new Entry(entry.getKey(), ADValue);
                    DescendingAccumulators.add(sup);
                    ADMap.put(entry.getKey(), ADValue);
                }

            }
            List<Entry> results = new ArrayList<>();
            Integer count = 1;
            while (!DescendingAccumulators.isEmpty() && count <= 10) {
                Entry temp = DescendingAccumulators.poll();
                results.add(temp);
                System.out.println("DocID: " + temp.docID);
                System.out.println("ADValue : " + temp.Ad);
                count++;
            }
            return results;


        }while (query != "exit");
    }


    public static List<Entry> rankedRetrieval_Wacky(DiskPositionalIndex index, DocumentCorpus corpus) throws IOException {

        PriorityQueue<Entry> DescendingAccumulators = new PriorityQueue<>(new EntryComparator());
        String query = "";
        RandomAccessFile docWeights = new RandomAccessFile(index.pathToWeights, "r");

        long startOfFile = docWeights.getFilePointer();

        double corpusSize = corpus.getCorpusSize();
        do {
            query = readInQuery();
            RankedQueryParser rankedParser = new RankedQueryParser();
            List<QueryComponent> literals = rankedParser.parseQuery(query);

            Double wqt = 0.0;
            Double byteSize = 0.0;
            Double avgTFTD = 0.0;

            Map<Integer, Double> ADMap = new HashMap<>();
            for (QueryComponent literal : literals) {
                List<Posting> postingForTerm = literal.getPostings(index);
                Double docFreq = (double) postingForTerm.size();

                wqt = Math.max(0, Math.log((corpusSize - docFreq)/docFreq));

                for (Posting p : postingForTerm) {
                    Integer id = p.getDocumentId();
                    docWeights.seek(startOfFile);
                    docWeights.skipBytes(id * 32);
                    docWeights.skipBytes(16);
                    //System.out.println("DocID: " + id);

                    byteSize = docWeights.readDouble();
                    avgTFTD = docWeights.readDouble();
                    //.out.println("ByteSize: " + byteSize);
                    //System.out.println("AvgTFTD: " + avgTFTD);
                    Double wdt =  (1 + Math.log(p.getTFtd()) / (1 + Math.log(avgTFTD)));

                    if (ADMap.get(id) == null) {
                        double AD = wdt * wqt;
                        ADMap.put(id, AD);
                    } else {
                        double AD = ADMap.get(id);
                        AD = AD + (wdt * wqt);
                        ADMap.put(id, AD);
                    }
                }

            }
            for (Map.Entry<Integer, Double> entry : ADMap.entrySet()) {
                Double ADValue = entry.getValue();
                if (ADValue != 0) {


                    ADValue = ADValue / (Math.sqrt(byteSize));
                    Entry sup = new Entry(entry.getKey(), ADValue);
                    DescendingAccumulators.add(sup);
                    ADMap.put(entry.getKey(), ADValue);
                }

            }
            Integer count = 1;
            List<Entry> results = new ArrayList<>();
            while (!DescendingAccumulators.isEmpty() && count <= 10) {
                Entry temp = DescendingAccumulators.poll();
                results.add(temp);
                System.out.println("DocID: " + temp.docID);
                System.out.println("ADValue : " + temp.Ad);
                count++;
            }

            return results;

        }while (query != "exit");
    }
    public static void booleanRetrieval(Index index, DocumentCorpus corpus) throws IOException {



        String query = "";
        do {
            query = readInQuery();

            if (query.contains(":q")) {
                System.out.println("Ending program.");
                return;
            } else if (query.contains(":stem")) {

                String termToStem = query.split(" ")[1];
                System.out.println("Term to Stem: " + termToStem.toString());
                PorterStemmer stemmer = new PorterStemmer();
                String stemmedTerm = stemmer.stem(termToStem);
                System.out.println(termToStem + "--> " + stemmedTerm);

            } else if (query.contains("vocab")) {
                List<String> vocabulary = index.getVocabulary();
                List<String> first100Vocab = vocabulary.subList(0, 1000);
                System.out.println("First 100 terms (sorted): ");

                for (String term : first100Vocab) {
                    System.out.println(term);
                }

                System.out.println("Total vocabulary terms: " + vocabulary.size());

            } else if(query.startsWith("\"") && query.endsWith("\"")) {


                List<Posting> sup = index.getPostingsWithPositions("sup");
            }
            
            
            else {
                BooleanQueryParser booleanParser = new BooleanQueryParser();
                QueryComponent queryComponent = booleanParser.parseQuery(query);
                
                List<Posting> queryPostings = queryComponent.getPostings(index);
                System.out.println("Query postings size!!!: " + queryPostings.size());
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

                    while((a = jsonReader.read()) != -1)
                    {
                        textBuilder.append((char)a);
                        count++;
                        //textBuilder.append((char)a);
                        if(count == 120)
                        {

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
        }while (!query.equals("q")) ;
    }
    public static Integer readInSystemMode() {
        Integer userChoice;

        do {
            Scanner readIn = new Scanner(System.in);
            System.out.print("[1] Build on-disk index for a specified directory \n" +
                    "[2] Process queries over directory \n" +
                    "[3] Exit \n" +
                    "Choice: ");
            userChoice = readIn.nextInt();

        } while (userChoice > 3 || userChoice < 1);
        return userChoice;

    }
    public static Integer readInAlgorithmMode() {
        Integer userChoice;

        do {
            Scanner readIn = new Scanner(System.in);
            System.out.print("[1] Default \n" +
                    "[2] TF-IDF \n" +
                    "[3] Okapi BM 25 \n" +
                    "[4] Wacky \n" +
                    "Choice: ");
            userChoice = readIn.nextInt();

        } while (userChoice > 4 || userChoice < 1);
        return userChoice;

    }
    public static Integer readInQueryMode()
    {
        Integer queryMode;
        Scanner readIn = new Scanner(System.in);
        System.out.print("[1] Boolean Retrieval \n" +
                "[2] Ranked Retrieval (Not functioning)\n" +
                "Choose Mode : ");
        queryMode = readIn.nextInt();
        System.out.print("You have entered: " + queryMode + "\n");
        return queryMode;
    }
    public static String readInQuery()
    {
        String query = "";
        Scanner readIn = new Scanner(System.in);
        System.out.print("Enter a query ('quit' to escape): ");
        query = readIn.nextLine();
        System.out.print("You have entered: " + query + "\n");
        return query;

    }
    public static Path readInPathToIndex()
    {

        Scanner readIn = new Scanner(System.in);
        System.out.print("Enter corpus path: ");
        String pathString;
        pathString = readIn.nextLine();
        Path path = Paths.get(pathString);
        System.out.print("You have entered: " + path + "\n");
        return path;

    }
    private static Index indexCorpus(DocumentCorpus corpus, Path path) throws IOException {
        System.out.println("Indexing...");
        String index = path.toAbsolutePath().toString();
        System.out.println("Creating index in " + index);
        index = index + "/index";

        File indexDir = new File(index);
        indexDir.mkdir();
        String docWeights = index +  "/docWeights.bin";
        String avgDoc = index + "/docLengthAvg.bin";
        RandomAccessFile documentWeights = new RandomAccessFile(docWeights, "rw");
        RandomAccessFile docLengthAvg = new RandomAccessFile(avgDoc, "rw");
        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
        List<String> tokenList = new ArrayList<>();
        PositionalInvertedIndex positionalIndex = new PositionalInvertedIndex();


        List<Double> termWeights = new ArrayList<>();
        Double docLength;
        Double avgDocLength = 0.0;
        Double avgTFTD;
        Integer bytes;
        for (Document d : corpus.getDocuments())
        {

            int num = 0;
            Iterable<String> tokens = null;

            EnglishTokenStream ETS = new EnglishTokenStream(d.getContent());

            tokens = ETS.getTokens();

            Map<String, Integer> termFrequency = new HashMap<String, Integer>();
            docLength = 0.0;
            avgTFTD = 0.0;
            bytes = 0;
            for (String term : tokens)
            {
                tokenList = processor.processToken(term);
                Integer tokenCount = tokenList.size();

                for(String token : tokenList)
                {
                    bytes = bytes + (2 * token.length());
                    docLength++;
                    Integer count = termFrequency.get(token);
                    if(count == null)
                    {

                        termFrequency.put(token, 1);
                    }
                    else
                    {
                        termFrequency.put(token, count + 1);
                    }

                    positionalIndex.addTerm(token, d.getId(), num);
                    num++;
                }


            }
            Double LD = 0.0;
            avgDocLength = avgDocLength + docLength;
            for(Map.Entry<String, Integer> mapElement : termFrequency.entrySet())
            {
                 Integer tftd = mapElement.getValue();
                 avgTFTD = avgTFTD + tftd;
                 if(tftd >= 1)
                 {


                     Double wdt = (1 + (Math.log(tftd)));
                     termWeights.add(wdt);
                     LD = LD + (wdt * wdt);
                 }

            }
            avgTFTD = avgTFTD / (termFrequency.values().size());
            LD = Math.sqrt(LD);
            //System.out.println("DocID: " + d.getId());
            //System.out.println("avgTFTD: " + avgTFTD);
            //System.out.println("Bytes: " + bytes);
            documentWeights.writeDouble(LD);
            documentWeights.writeDouble(docLength);
            documentWeights.writeDouble(bytes);
            documentWeights.writeDouble(avgTFTD);



        }

        avgDocLength = avgDocLength / corpus.getCorpusSize();
        docLengthAvg.writeDouble(avgDocLength);
        System.out.println("Average Doc Length: " + avgDocLength);


        return positionalIndex;


    }



}
**/