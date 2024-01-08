package SearchFoundations_Java.cecs429.indexing;

import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.documents.Document;
import SearchFoundations_Java.cecs429.indexing.PositionalInvertedIndex;
import SearchFoundations_Java.cecs429.database.SQLiteDB;
import SearchFoundations_Java.cecs429.text.EnglishTokenStream;
import SearchFoundations_Java.cecs429.text.NonBasicTokenProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.sql.*;

public class DiskIndexWriterCopy {

    /**
     * In this function, the contents of the positionalInvertedIndex are written to disk. The pattern for writing
     * terms is as follows: documentFreqForTerm, documentID, termFreqForTermInDoc, Position1, Position2, PositionX...
     * If a term shows up in multiple documents, documentFreqForTerm will be > 1, and the rest of the pattern (documentID,
     * termFreqForTermInDoc, etc) will be repeated for each document the term is in.
     * <p>
     * It's important to note that the IDs and positions are written in gaps. Ex: Doc IDS 5, 8, 15 will be
     * written as 5, 3, 7 (difference between consecutive documents). Gaps typically allow more compact storage.
     */
    public List<Long> writeIndex(PositionalInvertedIndex PII, Path onDiskIndex) throws IOException, SQLException {

        String onDisk = onDiskIndex.toString() + "/index/postings.bin";
        RandomAccessFile onDiskFile =
                new RandomAccessFile(
                        onDisk, "rw");

        List<String> vocabulary = PII.getVocabulary();
        List<Long> termBytePositions = new ArrayList<>();

        System.out.println("Moving positional index... \n");
        int total = 0;
        for (String term : vocabulary) {

            total++;
            long bytePosition = onDiskFile.getFilePointer();
            termBytePositions.add(bytePosition);
            int lastDocID = 0;
            List<Posting> postingsForTerm = PII.getPostings(term);
            int DFT = postingsForTerm.size();
            onDiskFile.writeInt(DFT);

            for (Posting p : postingsForTerm) {
                int docID = p.getDocumentId();
                int docIDGap = docID - lastDocID;
                lastDocID = docID;
                onDiskFile.writeInt(docIDGap);
                List<Integer> positionsForPosting = p.getPosition();

                int TFTD = positionsForPosting.size();
                onDiskFile.writeInt(TFTD);
                int lastPosition = 0;
                for (int i = 0; i < positionsForPosting.size(); i++) {
                    int positionGap = positionsForPosting.get(i) - lastPosition;
                    onDiskFile.writeInt(positionGap);
                    lastPosition = positionsForPosting.get(i);

                }
            }

        }
        System.out.println("Move to disk complete. ");
        return termBytePositions;

    }

    /**
     * LOOK INTO SQLITEDB MORE. RELATIONAL DB? HOW IS IT STORED?? SAMPLE.DB FILE
     * This function uses SQLiteDB (locally stored) to hold the terms and their corresponding byte positions.
     * The parameter termBytePositions contains all of the long bytePositions for each term, as returned
     * by the writeIndex function above.
     * <p>
     * SQLite DB allows for batch processing of prepareStatements; although still taking a substantial amount of time,
     * a batch size of 100 seemed to offer the fastest execution.
     */

    public void writeTermBytePositionsToDatabaseCopy(PositionalInvertedIndex PII, List<Long> termBytePositions) {
        List<String> vocabulary = PII.getVocabulary();
        System.out.println("Writing term byte positions to database.");
        SQLiteDB database = new SQLiteDB();
        database.dropTable();
        database.createTable();
        //SQLiteDB.connect(); // Creates the sample.db file

        int count = 0;
        for (int i = 0; i < vocabulary.size(); i++) {
            String term = vocabulary.get(i);
            long bytePosition = termBytePositions.get(i);
            if(count == 1000)
            {
                database.insertTerm(term, bytePosition);
                System.out.println("Commiting 1000 terms to DB");
                count = 0;
                database.commit();
            }
            else{
                database.insertTerm(term, bytePosition);
            }


            count++;

        }
        database.commit();

    }

    /**
     * This function will create a hashmap for each document, containing the terms mapped to their integer frequencies.
     * Each document will have its own hashmap for the unique terms in the document.
     *
     */
    public void createTermFrequencyHashmap(DirectoryCorpus corpus, Path absolutePath) throws IOException {

        double averageTokens = 0;
        for (Document document : corpus.getDocuments()) {


            int documentTokens = 0;
            int bytes = 0;
            EnglishTokenStream englishTokenStream = new EnglishTokenStream(document.getContent());
            Iterable<String> tokens = englishTokenStream.getTokens();
            Map<String, Integer> termFrequency = new HashMap<>();
            NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
            int id = document.getId();

            for (String token : tokens) {
                List<String> processedTokens = processor.processToken(token);

                for (String term : processedTokens) {

                    final byte[] UTF16BYTES = term.getBytes(StandardCharsets.UTF_16BE);
                    bytes = bytes + UTF16BYTES.length;
                    documentTokens++;
                    averageTokens = averageTokens + documentTokens;
                    Integer count = termFrequency.get(token);

                    if (count == null) {
                        termFrequency.put(term, 1);

                    } else {
                        termFrequency.put(term, count + 1);

                    }
                }
            }

            calculateAndWriteDocumentWeights(termFrequency, absolutePath, id, documentTokens, bytes);

        }

        averageTokens = averageTokens / corpus.getCorpusSize();
        String pathToDocWeights = absolutePath.toString() + "/index/docWeights.bin";

        try {
            RandomAccessFile documentWeights =
                    new RandomAccessFile(
                            pathToDocWeights, "rw");

            documentWeights.seek(documentWeights.length());
            documentWeights.writeDouble(averageTokens);
            documentWeights.seek(documentWeights.length());
            documentWeights.close();
        }catch (IOException e) {
            e.printStackTrace();
        }


        //avgDocLength = avgDocLength / corpus.getCorpusSize();
        //docLengthAvg.writeDouble(avgDocLength);
        //System.out.println("Average Doc Length: " + avgDocLength);

    }

    public void calculateAndWriteDocumentWeights(Map<String, Integer> termFrequency, Path absolutePath, int id, int documentTokens, int bytes) throws IOException {
        String pathToDocWeights = absolutePath.toString() + "/index/docWeights.bin";

        try {
            RandomAccessFile documentWeights =
                    new RandomAccessFile(
                            pathToDocWeights, "rw");
            Set<String> keys = termFrequency.keySet();
            Double LD = 0.0;
            Double avgTFTD = 0.0;
            for (String key : keys) {

                int frequencyOfTerm = termFrequency.get(key);
                avgTFTD = avgTFTD + frequencyOfTerm;
                if (frequencyOfTerm >= 1) {

                    Double wdt = (1 + (Math.log(frequencyOfTerm)));

                    //termWeights.add(wdt);
                    LD = LD + (wdt * wdt);
                }


            }



            //avgDocLength = avgDocLength + docLength;

            avgTFTD = avgTFTD / (termFrequency.values().size());
            LD = Math.sqrt(LD);
            //documentWeights.seek(8 * id);  // Position the file pointer at the correct location
            documentWeights.seek(32 * id);

            documentWeights.writeDouble(LD);
            documentWeights.writeDouble(documentTokens);
            documentWeights.writeDouble(bytes);
            documentWeights.writeDouble(avgTFTD);
            documentWeights.seek(documentWeights.length());
            documentWeights.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
