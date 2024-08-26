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
public class RankedDispatch
{
    // Contains the AzureBlobPositionalIndex instance (holding the bytestream representing the index being queried)
    private final AzureBlobPositionalIndex onDiskIndex;

    // Contains the DirectoryCorpus instance (allowing access to the content of the documents)
    private final DirectoryCorpus directoryCorpus;

    // Constructor that will set the index and corpus instances that are passed
    public RankedDispatch(AzureBlobPositionalIndex index, DirectoryCorpus corpus)
    {
        onDiskIndex = index;
        directoryCorpus = corpus;
    }

   // Calls the appropriate concrete class' calculate method
    public List<Entry> calculate(RankingStrategy strategy, String query)
    {
        System.out.println("In the calculate method... ");
        System.out.println("Query: " + query);
        System.out.println("Corpus Size: " + directoryCorpus.getCorpusSize());
        RankedQueryParser rankedParser = new RankedQueryParser();

        // Obtain the literals (terms) in the query
        List<QueryComponent> literals = rankedParser.parseQuery(query);

        // The ADMap is a hashmap mapping documentIDs to accumulator values (where the values represent relevance of that doc to the query)
        Map<Integer, Double> ADMap = strategy.calculate(literals, onDiskIndex, directoryCorpus);

        // The hashmap values are normalized (allowing for varying document lengths to be adjusted)
        return strategy.normalizeAccumulatorValues(ADMap, onDiskIndex);
    }


}
