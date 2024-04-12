package modules.queries;

import modules.text.NonBasicTokenProcessor;

import java.util.ArrayList;
import java.util.List;

public class RankedQueryParser {

    /**
     * This class is used for parsing ranked queries. The query is split by spaces, and further processed using
     * the NonBasicTokenProcessor. Each literal found is added to a list of query components and returned.
     */
    public List<QueryComponent> parseQuery(String query) {

        List<QueryComponent> subqueryLiterals = new ArrayList<>();
        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
        String lowerCaseQuery = query.toLowerCase();
        String[] arrayOfUnstemmedTerms = lowerCaseQuery.split(" ");
        List<String> processedTokens;

        for (String term : arrayOfUnstemmedTerms) {
            processedTokens = processor.processToken(term);
            for(String token: processedTokens){
                TermLiteral literal = new TermLiteral(token);
                subqueryLiterals.add(literal);
            }

        }
        return subqueryLiterals;

    }
}