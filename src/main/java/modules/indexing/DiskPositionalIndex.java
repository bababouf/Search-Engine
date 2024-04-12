package modules.indexing;

import modules.database.SQLiteDB;

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


    public DiskPositionalIndex(Path path, int sizeOfCorpus)
    {
        String pathString = path.toAbsolutePath().toString();
        pathToWeights = pathString + "/index/docWeights.bin";
        pathToIndex = pathString + "/index/postings.bin";
        corpusSize = sizeOfCorpus;
        pathToAvgDocLength = pathString + "/index/docLengthAvg.bin";

    }
    public double returnLD(int index) throws IOException {
        RandomAccessFile docWeights = new RandomAccessFile(pathToWeights, "r");
        docWeights.skipBytes(4 * index);
        double LD = docWeights.readDouble();
        return LD;

    }


    @Override
    public List<Posting> getPostingsWithPositions(String term) throws IOException {
        SQLiteDB database = new SQLiteDB();
        Long byte_position = database.selectTerm(term);
        RandomAccessFile onDiskIndex = new RandomAccessFile(pathToIndex, "r");
        onDiskIndex.seek(byte_position);


        Integer dft = onDiskIndex.readInt();
        List<Posting> postingList = new ArrayList<>();

        Integer docID = 0;
        List<Integer> positionList = new ArrayList<>();
        for(int i = 0; i < dft; i++)
        {


            docID = docID + onDiskIndex.readInt();
            Integer tftd = onDiskIndex.readInt();
            Integer position = 0;
            for(int j = 0; j < tftd; j++)
            {

                Integer positionGap = onDiskIndex.readInt();
                position = position + positionGap;
                positionList.add(position);

            }
                Posting p = new Posting(docID, positionList);
                postingList.add(p);
                positionList = new ArrayList<>();


        }

        return postingList;
    }

    @Override
    public List<Posting> getPostings(String term) throws IOException {
        SQLiteDB database = new SQLiteDB();
        Long byte_position = database.selectTerm(term);
        List<Posting> postingList = new ArrayList<>();

        if (byte_position == null) {
            return postingList;
        } else {


            RandomAccessFile onDiskIndex = new RandomAccessFile(pathToIndex, "r");
            onDiskIndex.seek(byte_position);
            Integer dft = onDiskIndex.readInt();
            Integer docID = 0;

            for (int i = 0; i < dft; i++) {
                docID = docID + onDiskIndex.readInt();
                Integer tftd = onDiskIndex.readInt();
                onDiskIndex.skipBytes(tftd * 4);
                Posting p = new Posting(docID, null);
                p.setTFTD(tftd);
                postingList.add(p);

            }

            return postingList;
        }

    }

    @Override

    public Integer getCorpusSize() {
       return corpusSize;
    }

    @Override
    public Integer getVocabSize() throws IOException {
        return null;
    }
    @Override
    public List<String> getVocabulary() {
        SQLiteDB database = new SQLiteDB();
        return database.retrieveVocabulary();
    }
}
