package SearchFoundations_Java.cecs429.queries;

import SearchFoundations_Java.cecs429.text.NonBasicTokenProcessor;
import opennlp.tools.stemmer.PorterStemmer;

import java.util.ArrayList;
import java.util.List;

public class RankedQueryParser {

    public List<QueryComponent> parseQuery(String query) {

        List<QueryComponent> subqueryLiterals = new ArrayList<>();
        NonBasicTokenProcessor processor = new NonBasicTokenProcessor();
        query = query.toLowerCase();
        String[] arrayOfUnstemmedTerms = query.split(" ");
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