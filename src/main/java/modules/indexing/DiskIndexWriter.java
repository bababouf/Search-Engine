package modules.indexing;

import modules.database.MySQLDB;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiskIndexWriter {

    /**
     * This method writes the contents of the positionalInvertedIndex to disk. The pattern for writing
     * terms is as follows: documentFreqForTerm, documentID, termFreqForTermInDoc, Position1, Position2, PositionX...
     * If a term shows up in multiple documents, documentFreqForTerm will be > 1, and the rest of the pattern (documentID,
     * termFreqForTermInDoc, etc.) will be repeated for each document the term is in.
     * It's important to note that the IDs and positions are written in gaps. Ex: Doc IDS 5, 8, 15 will be
     * written as 5, 3, 7 (difference between consecutive documents). Gaps typically allow more compact storage.
     *
     * @param onDiskIndex This is the path to the corpus that is input from the user. The postings.bin file is created within
     *                    that directory, in its own /index directory.
     */
    public List<Long> writeIndex(PositionalInvertedIndex PII, Path onDiskIndex) throws IOException, SQLException {

        String onDisk = onDiskIndex.toString() + "/index/postings.bin";
        System.out.println("Ondisk path: " + onDisk);
        List<Long> termBytePositions = null;
        try {
            RandomAccessFile onDiskFile =
                    new RandomAccessFile(
                            onDisk, "rw");

            List<String> vocabulary = PII.getVocabulary();
            termBytePositions = new ArrayList<>();
            System.out.println("Moving positional index... \n");

            for (String term : vocabulary) {
                long bytePosition = onDiskFile.getFilePointer();
                termBytePositions.add(bytePosition); // Add each term's bytePosition to list
                int lastDocID = 0; // Will keep track of lastDocID in order to write each ID as a gap
                List<Posting> postingsForTerm = PII.getPostings(term);
                int documentFrequencyForTerm = postingsForTerm.size(); // How many documents the term is found in
                onDiskFile.writeInt(documentFrequencyForTerm);

                for (Posting posting : postingsForTerm) {
                    int docID = posting.getDocumentId();
                    int docIDGap = docID - lastDocID; // Gap is current docID - last docID
                    lastDocID = docID; // Set current ID to last
                    onDiskFile.writeInt(docIDGap);
                    List<Integer> positionsForPosting = posting.getPosition();
                    int termFrequencyInDocument = positionsForPosting.size(); // Number of times a term is found in a document
                    onDiskFile.writeInt(termFrequencyInDocument);
                    int lastPosition = 0; // Will keep track of lastPosition in order to write each position as a gap

                    for (Integer currentPosition : positionsForPosting) {
                        int positionGap = currentPosition - lastPosition;
                        onDiskFile.writeInt(positionGap);
                        lastPosition = currentPosition;

                    }
                }

            }
            System.out.println("Move to disk complete. ");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return termBytePositions;

    }

    /**
     * This function connects to a local SQLite DB and inserts a row at a time, consisting of a term and its corresponding
     * long byte position.
     *
     * @param termBytePositions A list of long byte positions written in ascending alphabetical order for each term
     */
    public void writeTermBytePositionsToDatabase(PositionalInvertedIndex PII, List<Long> termBytePositions) {
        List<String> vocabulary = PII.getVocabulary(); // Ascending alphabetical order
        System.out.println("Writing term byte positions to database.");
        MySQLDB database = new MySQLDB(); // This connects to the DB (hardcoded to sample.db at project root location)
        database.dropTable(); // If the program is run multiple times this ensures the DB doesn't grow infinitely
        database.createTable();
        int count = 0;

        for (int i = 0; i < vocabulary.size(); i++) {
            String term = vocabulary.get(i);
            long bytePosition = termBytePositions.get(i);

            /*
              In an effort to increase efficient insertion of terms to the DB, instead of operating in auto-commit mode
              SQLite transactions are used, allowing 1000 terms to be commited to the DB at a time.
             */
            database.insertTerm(term, bytePosition);
            if (count == 1000) {
                System.out.println("Commiting 1000 terms to DB");
                count = 0;
                database.commit();
            }

            count++;

        }
        database.commit();
        System.out.println("wee here?");
    }

    /**
     * Sets the docWeights.bin file (at the location specified by absolutePath) to length 0, erasing its contents
     */
    public void clearFileContents(Path absolutePath) {
        String pathToDocWeights = absolutePath.toString() + "/index/docWeights.bin";
        try {
            RandomAccessFile documentWeights = new RandomAccessFile(pathToDocWeights, "rw");
            documentWeights.setLength(0);
            documentWeights.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method is called for each document in the corpus, and has the purpose of writing 4 different double values
     * to the docWeights.bin file. These include LD (used to normalize very short documents/very long documents), the number of tokens, number of ASCII bytes, and the average term
     * frequency of terms in the document.
     */
    public void calculateAndWriteDocumentWeights(Map<String, Integer> termFrequency, Path absolutePath, int id, int documentTokens, int bytes) {
        String pathToDocWeights = absolutePath.toString() + "/index/docWeights.bin";

        try {
            RandomAccessFile documentWeights =
                    new RandomAccessFile(
                            pathToDocWeights, "rw");

            Set<String> terms = termFrequency.keySet();
            double LD = 0.0;
            double averageTFTD = 0.0; // averageTermFrequencyOfTermsInDocument

            for (String term : terms) {
                int frequencyOfTerm = termFrequency.get(term);
                averageTFTD = averageTFTD + frequencyOfTerm;

                if (frequencyOfTerm >= 1) {
                    Double weightOfTermInDocument = (1 + (Math.log(frequencyOfTerm))); // Terms that appear more are given a higher weight
                    LD = LD + (weightOfTermInDocument * weightOfTermInDocument);

                }
            }

            averageTFTD = averageTFTD / (termFrequency.values().size());
            LD = Math.sqrt(LD);
            documentWeights.seek(32L * id);
            documentWeights.writeDouble(LD);
            documentWeights.writeDouble(documentTokens);
            documentWeights.writeDouble(bytes);
            documentWeights.writeDouble(averageTFTD);
            documentWeights.seek(documentWeights.length());
            documentWeights.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function will write the average tokens per document for the corpus at the end of the docWeights.bin file
     */
    public void writeAverageTokensForCorpus(String pathToDocWeights, double averageTokens) {
        try {
            RandomAccessFile documentWeights =
                    new RandomAccessFile(
                            pathToDocWeights, "rw");

            documentWeights.seek(documentWeights.length());
            documentWeights.writeDouble(averageTokens);
            documentWeights.seek(documentWeights.length());
            documentWeights.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}






