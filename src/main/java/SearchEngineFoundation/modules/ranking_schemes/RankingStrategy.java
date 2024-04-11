package SearchEngineFoundation.modules.ranking_schemes;

import SearchEngineFoundation.modules.documents.DirectoryCorpus;
import SearchEngineFoundation.modules.indexing.DiskPositionalIndex;
import SearchEngineFoundation.modules.queries.QueryComponent;
import SearchEngineFoundation.modules.misc.Entry;

import java.util.List;
import java.util.Map;

/**
 * All four of the different ranking schemes will implement this RankingStrategy class and it's two methods calculate and
 * calculateAccumulatorValue. The strategy design pattern is used to implement the ranking schemes, and it works as follows:
 * 1. At program runtime, the user chooses which ranking scheme to use
 * 2. Based on this choice, RankedDispatch will call the appropriate calculate method (from one of the four schemes)
 * 3. Once this is done, RankedDispatch will call the appropriate calculateAccumulatorValue method
 */

/**
 * This is the strategy interface. Each concrete strategy class (default, tfidf, okapi, wacky) will implement this interface.
 */
public interface RankingStrategy {

    Map<Integer, Double> calculate(List<QueryComponent> literals, DiskPositionalIndex onDiskIndex, DirectoryCorpus corpus);


    List<Entry> calculateAccumulatorValue(Map<Integer, Double> ADMap, DiskPositionalIndex onDiskIndex);
}
