package SearchEngineFoundation.modules.ranking_schemes;

import SearchEngineFoundation.modules.documents.DirectoryCorpus;
import SearchEngineFoundation.modules.indexing.DiskPositionalIndex;
import SearchEngineFoundation.modules.queries.QueryComponent;
import SearchEngineFoundation.modules.queries.RankedQueryParser;
import SearchEngineFoundation.modules.misc.Entry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static SearchEngineFoundation.drivers.DiskPositionalIndexer.readInQuery;

/**
 * This class doesn't implement any of the algorithms but instead delegates the task to the appropriate strategy.
 * In other words, this class will call the appropriate calculate and calculateAccumulatorValue methods depending on
 * the strategy passed.
 */
public class RankedDispatch {

    private final DiskPositionalIndex onDiskIndex;
    private final DirectoryCorpus directoryCorpus;
    private Map<Integer, Double> ADMap = new HashMap<>();

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

    /**
     * Depending on which ranking scheme is passed (strategy), the appropriate calculate method will be called.
     */
    public void calculate(RankingStrategy strategy){
        List<QueryComponent> literals = promptUser();
        ADMap = strategy.calculate(literals, onDiskIndex, directoryCorpus);
    }

    /**
     * This method operates in the same way as above, calling the appropriate calculateAccumulatorValue method.
     */
    public List<Entry> calculateAccumulatorValue(RankingStrategy strategy) {
        return strategy.calculateAccumulatorValue(ADMap, onDiskIndex);
    }
}
