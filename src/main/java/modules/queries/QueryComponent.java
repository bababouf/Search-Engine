package modules.queries;

import modules.indexing.Index;
import modules.indexing.Posting;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * A QueryComponent is one piece of a larger query, whether that piece is a literal string or represents a merging of
 * other components. All nodes in a query parse tree are QueryComponent objects.
 */
public interface QueryComponent
{
    /**
     * Retrieves a list of postings for the query component, using an Index as the source.
     */
    List<Posting> getPostings(Index index) throws IOException;

    default List<Posting> getPostingsWithPositions(Index index) throws IOException
    {
        return Collections.emptyList();
    }
}



