package modules.rankingSchemes;

import modules.documents.DirectoryCorpus;
import modules.indexing.AzureBlobPositionalIndex;
import modules.misc.Entry;
import modules.queries.QueryComponent;
import java.util.List;
import java.util.Map;

/**
 * This is the strategy interface. Each concrete strategy class (default, tfidf, okapi, wacky) will implement this interface.
 */
public interface RankingStrategy
{
    Map<Integer, Double> calculate(List<QueryComponent> literals, AzureBlobPositionalIndex onDiskIndex, DirectoryCorpus corpus);
    List<Entry> normalizeAccumulatorValues(Map<Integer, Double> ADMap, AzureBlobPositionalIndex onDiskIndex);
}
