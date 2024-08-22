package modules.queries;

import modules.text.NonBasicTokenProcessor;

import java.util.ArrayList;
import java.util.List;

public class RankedQueryParser
{

    /**
     * This class is used for parsing ranked queries. The query is split by spaces, and further processed using
     * the NonBasicTokenProcessor. Each literal found is added to a list of query components and returned.
     */
    public List<QueryComponent> parseQuery(String query)
    {

        List<QueryComponent> subqueryLiterals = new ArrayList<>();
        String[] terms = query.toLowerCase().split(" ");
        List<String> processedTokens;
        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();

        for (String term : terms)
        {
            processedTokens = processor.processToken(term); // May contain multiple terms (if the term passed is hyphenated)

            for (String token : processedTokens)
            {
                TermLiteral literal = new TermLiteral(token);
                subqueryLiterals.add(literal);
            }

        }
        return subqueryLiterals;

    }
}