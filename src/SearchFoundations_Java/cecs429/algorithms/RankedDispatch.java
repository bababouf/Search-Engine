package SearchFoundations_Java.cecs429.algorithms;

import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.indexing.DiskPositionalIndex;
import SearchFoundations_Java.cecs429.queries.QueryComponent;
import SearchFoundations_Java.cecs429.queries.RankedQueryParser;
import SearchFoundations_Java.edu.csulb.Entry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static SearchFoundations_Java.edu.csulb.DiskPositionalIndexer.readInQuery;

public class RankedDispatch {

    private final DiskPositionalIndex onDiskIndex;
    private final DirectoryCorpus directoryCorpus;

    public RankedDispatch(DiskPositionalIndex index, DirectoryCorpus corpus){
        onDiskIndex = index;
        directoryCorpus = corpus;
    }

    public List<QueryComponent> promptUser(){
        String query = "";
        do {
            query = readInQuery();
            RankedQueryParser rankedParser = new RankedQueryParser();
            List<QueryComponent> literals = rankedParser.parseQuery(query);
            return literals;

        }while(query != "exit");
    }

    public List<Entry> calculate(RankingStrategy strategy){
        List<QueryComponent> literals = promptUser();
        return strategy.calculate(literals, onDiskIndex, directoryCorpus);
    }
}
