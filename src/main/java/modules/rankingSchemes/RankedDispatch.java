package modules.rankingSchemes;

import modules.documents.DirectoryCorpus;
import modules.indexing.AzureBlobPositionalIndex;
import modules.misc.Entry;
import modules.queries.QueryComponent;
import modules.queries.RankedQueryParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class doesn't implement any of the algorithms but instead delegates the task to the appropriate strategy.
 * In other words, this class will call the appropriate calculate and calculateAccumulatorValue methods depending on
 * the strategy passed.
 */
public class RankedDispatch {

    private final AzureBlobPositionalIndex onDiskIndex;
    private final DirectoryCorpus directoryCorpus;
    private Map<Integer, Double> ADMap = new HashMap<>();

    public RankedDispatch(AzureBlobPositionalIndex index, DirectoryCorpus corpus){
        onDiskIndex = index;
        directoryCorpus = corpus;
    }


    /**
     * Depending on which ranking scheme is passed (strategy), the appropriate calculate method will be called.
     */
    public void calculate(RankingStrategy strategy, String query){
        //List<QueryComponent> literals = promptUser();

        RankedQueryParser rankedParser = new RankedQueryParser();
        List<QueryComponent> literals = rankedParser.parseQuery(query);
        ADMap = strategy.calculate(literals, onDiskIndex, directoryCorpus);
    }

    /**
     * This method operates in the same way as above, calling the appropriate calculateAccumulatorValue method.
     */
    public List<Entry> calculateAccumulatorValue(RankingStrategy strategy) {
        return strategy.calculateAccumulatorValue(ADMap, onDiskIndex);
    }
}
