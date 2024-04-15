package modules.indexing;

import drivers.DiskPositionalIndexer;
import modules.database.MySQLDB;

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

    public DiskPositionalIndex(Path path)
    {
        String pathString = path.toAbsolutePath().toString();
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
    public double returnLD(int index) throws IOException {
        RandomAccessFile docWeights = new RandomAccessFile(pathToWeights, "r");
        docWeights.skipBytes(4 * index);
        double LD = docWeights.readDouble();
        return LD;

    }


    @Override
    public List<Posting> getPostingsWithPositions(String term) throws IOException {
        MySQLDB database = new MySQLDB();
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
    public List<Posting> getPostings(String term) throws IOException{
        MySQLDB database = new MySQLDB();
        Long byte_position = database.selectTerm(term);
        System.out.println(byte_position);
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
                System.out.println("Docid: " + docID);
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
        MySQLDB database = new MySQLDB();
        return database.retrieveVocabulary();
    }
}
