package modules.indexing;

import modules.database.PostgresDB;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index{

    public String pathToIndex;
    public String pathToWeights;
    public int corpusSize;
    public String pathToAvgDocLength;
    public String pathToCorpus;

    public DiskPositionalIndex(Path path)
    {
        String pathString = path.toAbsolutePath().toString();
        pathToCorpus = pathString;
        pathToWeights = pathString + "/index/docWeights.bin";
        pathToIndex = pathString + "/index/postings.bin";
        pathToAvgDocLength = pathString + "/index/docLengthAvg.bin";
    }

    public DiskPositionalIndex(Path path, int sizeOfCorpus)
    {
        String pathString = path.toAbsolutePath().toString();
        pathToWeights = pathString + "/index/docWeights.bin";
        pathToIndex = pathString + "/index/postings.bin";
        corpusSize = sizeOfCorpus;
        pathToAvgDocLength = pathString + "/index/docLengthAvg.bin";
    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) throws IOException {
        PostgresDB database = new PostgresDB(pathToCorpus);
        Long bytePosition = database.selectTerm(term);
        RandomAccessFile onDiskIndex = new RandomAccessFile(pathToIndex, "r");
        onDiskIndex.seek(bytePosition);

        int docFrequency = onDiskIndex.readInt();
        List<Posting> postings = new ArrayList<>();
        int docID = 0;
        List<Integer> positions = new ArrayList<>();

        for(int i = 0; i < docFrequency; i++)
        {
            docID = docID + onDiskIndex.readInt();
            int termFrequency = onDiskIndex.readInt();
            Integer position = 0;

            for(int j = 0; j < termFrequency; j++)
            {
                Integer positionGap = onDiskIndex.readInt();
                position = position + positionGap;
                positions.add(position);
            }

            Posting posting = new Posting(docID, positions);
            postings.add(posting);
            positions = new ArrayList<>();

        }

        return postings;
    }

    @Override
    public List<Posting> getPostings(String term) throws IOException{
        PostgresDB database = new PostgresDB(pathToCorpus);
        Long bytePosition = database.selectTerm(term);
        List<Posting> postings = new ArrayList<>();

        if (bytePosition != null)
        {
            RandomAccessFile onDiskIndex = new RandomAccessFile(pathToIndex, "r");
            onDiskIndex.seek(bytePosition);
            int documentFrequency = onDiskIndex.readInt();
            int docID = 0;

            for (int i = 0; i < documentFrequency; i++)
            {
                docID = docID + onDiskIndex.readInt();
                int termFrequency = onDiskIndex.readInt();
                onDiskIndex.skipBytes(termFrequency * 4);
                Posting posting = new Posting(docID, null);
                posting.setTermFrequency(termFrequency);
                postings.add(posting);
            }

        }
        return postings;

    }

    public Integer getCorpusSize() {
        return corpusSize;
    }

    @Override
    public List<String> getVocabulary() {
        PostgresDB database = new PostgresDB(pathToCorpus);
        return database.retrieveVocabulary();
    }
}