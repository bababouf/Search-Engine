package SearchFoundations_Java.cecs429.algorithms;

import SearchFoundations_Java.cecs429.documents.DirectoryCorpus;
import SearchFoundations_Java.cecs429.indexing.DiskPositionalIndex;
import SearchFoundations_Java.cecs429.queries.QueryComponent;
import SearchFoundations_Java.edu.csulb.Entry;

import java.util.List;

public interface RankingStrategy {



    List<Entry> calculate(List<QueryComponent> literals, DiskPositionalIndex onDiskIndex, DirectoryCorpus corpus);
}
