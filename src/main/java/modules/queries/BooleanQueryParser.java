package modules.queries;

import opennlp.tools.stemmer.PorterStemmer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will parse a boolean query into its individual components. A boolean query can consist of one or more AND
 * queries OR'd together; the parser will go through the query and find each of these components, returning a single
 * component that will be used to determine the final list of results.
 */
public class BooleanQueryParser
{

    // Used to identify a part of string (with start index and length)
    private static class StringBounds
    {
        int start;
        int length;

        StringBounds(int start, int length)
        {
            this.start = start;
            this.length = length;
        }
    }

    // Encapsulates a query component
    private static class Literal
    {
        StringBounds bounds;
        QueryComponent literalComponent;

        Literal(StringBounds bounds, QueryComponent literalComponent)
        {
            this.bounds = bounds;
            this.literalComponent = literalComponent;
        }
    }

    /**
     * This method is used to parse a boolean query into its individual components. The routine is as follows: loop through
     * the raw query, putting each literal (single term) into a list. When a "+" is encountered (signifying the previous
     * literals should be OR'd with the next literals after the "+"), build an AND query with each of the literals found.
     * This process is repeated for each segment of the query separated by "+" signs. Finally, build an OR query that consists
     * of all the the built AND subqueries.
     */
    public QueryComponent parseQuery(String query)
    {

        // Start at index 0
        int start = 0;

        // Will contain each of the subqueries (these are the literals AND'd together that represent a segment of the query)
        List<QueryComponent> allSubqueries = new ArrayList<>();

        do
        {
            // Identify the next subquery: a portion of the query up to the next + sign.
            StringBounds nextSubquery = findNextSubquery(query, start);

            // Extract the identified subquery into its own string.
            String subquery = query.substring(nextSubquery.start, nextSubquery.start + nextSubquery.length);
            int subStart = 0;

            // Store all the individual components of this subquery.
            List<QueryComponent> subqueryLiterals = new ArrayList<>(0);

			/*
			At this point, we have found a segment (portion of string up until the "+"). The do while below will look
			within this segment, find each individual component (could be a single literal or phrase component), and add
			it to a list.
			 */
            do
            {

                // Find the next literal in the subquery
                Literal lit = findNextLiteral(subquery, subStart);

                // Add the literal component to the conjunctive list.
                subqueryLiterals.add(lit.literalComponent);

                // Set the next index to start searching for a literal.
                subStart = lit.bounds.start + lit.bounds.length;

            }
            while (subStart < subquery.length());

			/*
			At this point, subqueryLiterals may contain one or more literals. In the event our segment looked like this:
			"dogs cats walrus +" -- we would have three literals in our subqueryLiterals list. These literals need to be
			AND'd together, which is done below.
			 */

            // If there is only one literal in the list, no AND'ing is necessary
            if (subqueryLiterals.size() == 1)
            {
                allSubqueries.add(subqueryLiterals.get(0));
            }
            // If there is more than one literal, we need to AND them together
            else
            {
                allSubqueries.add(new AndQuery(subqueryLiterals));
            }

			/*
			At this point, we have fully processed one segment of the query, creating an AND query for all the literals
			in that segment. The start index is set to the end of this subquery so that we can begin the search for the next
			subquery (starting again at the first do while)
			 */
            start = nextSubquery.start + nextSubquery.length;

        }
        while (start < query.length());
		
		/*
		At this point, all subqueries in the query have been processed. If the original query contained several segments
		(delineated by "+"), we need to OR the components together. Otherwise, we simply return the component.
		 */
        if (allSubqueries.size() == 1)
        {
            return allSubqueries.get(0);
        }
        else
        {
            return new OrQuery(allSubqueries);
        }

    }

    /**
     * Locates and returns the StringBounds (start and length) of the next subquery. The "startIndex" passed to this
     * method is where the method will begin the search.
     */
    private StringBounds findNextSubquery(String query, int startIndex)
    {

        int length;
        char test = query.charAt(startIndex);

        // Spaces are skipped, as well as "+" signs to find the start of the next subquery
        while (test == ' ' || test == '+')
        {
            test = query.charAt(++startIndex);
        }

        // The end of the next subquery is found when the next "+" is encountered
        int nextPlus = query.indexOf('+', startIndex + 1);

        // If nextPlus is < 0, this is the final subquery (no other "+" was found)
        if (nextPlus < 0)
        {
            length = query.length() - startIndex;
        }
        else
        {
            // Find the next "+" sign
            test = query.charAt(nextPlus);
            while (test == ' ' || test == '+')
            {
                test = query.charAt(--nextPlus);
            }

            length = 1 + nextPlus - startIndex;
        }

        // Return the StringBounds of the next subquery
        return new StringBounds(startIndex, length);
    }


    private Literal findNextLiteral(String subquery, int startIndex)
    {
        PorterStemmer stemmer = new PorterStemmer();

        // Skip past white space.
        while (startIndex < subquery.length() && subquery.charAt(startIndex) == ' ')
        {
            startIndex++;
        }

        if (startIndex >= subquery.length())
        {
            return null; // or throw an exception if startIndex is out of bounds
        }

        // Once all white space is skipped, get the next character
        char nextCharacter = subquery.charAt(startIndex);

        // If the next character is a double quote, this signifies we are dealing with a phrase query
        if (nextCharacter == '\"')
        {
            return processPhraseLiteral(subquery, startIndex, stemmer);
        }
        // If not, we are dealing with a literal
        else
        {
            return processTermLiteral(subquery, startIndex, stemmer);
        }
    }

    private Literal processPhraseLiteral(String subquery, int startIndex, PorterStemmer stemmer)
    {

        // To find the end of the phrase, we look for the next double quote
        int nextQuotation = subquery.indexOf('\"', startIndex + 1);

        // Determine the length between the start index and the last double quote
        int lengthOut = nextQuotation >= 0 ? nextQuotation - startIndex + 1 : subquery.length() - startIndex;

        // Get the substring that contains the text within the quotes
        String quotedText = subquery.substring(startIndex + 1, nextQuotation);

        // Call a method (passing terms split by white space) to be stemmed and lowercased
        List<String> phraseLiteralTerms = stemAndLowercaseTerms(quotedText.split(" "), stemmer);

        // Return the query component
        return new Literal(
                new StringBounds(startIndex, lengthOut),
                new PhraseLiteral(phraseLiteralTerms));
    }

    private Literal processTermLiteral(String subquery, int startIndex, PorterStemmer stemmer)
    {

        // Find the next space, signifying the end of a literal
        int nextSpace = subquery.indexOf(' ', startIndex);

        // Determine the length of the literal (from start index to the next space found)
        int lengthOut = nextSpace >= 0 ? nextSpace - startIndex : subquery.length() - startIndex;

        // Get the substring containing the literal found
        String term = subquery.substring(startIndex, startIndex + lengthOut);

        // Stem and lowercase the term
        String stemmedTerm = stemAndLowercaseTerm(term, stemmer);

        // Return the query component
        return new Literal(
                new StringBounds(startIndex, lengthOut),
                new TermLiteral(stemmedTerm));
    }

    // This method takes an array of strings, calling a method to stem and lowercase each one
    private List<String> stemAndLowercaseTerms(String[] terms, PorterStemmer stemmer)
    {
        List<String> stemmedTerms = new ArrayList<>();

        // Loop through each string in the array
        for (String term : terms)
        {
            // Add each stemmed/lowercased term to the list of stemmed terms
            stemmedTerms.add(stemAndLowercaseTerm(term, stemmer));
        }
        return stemmedTerms;
    }

    // Stems and lowercases a term and returns it
    private String stemAndLowercaseTerm(String term, PorterStemmer stemmer)
    {
        return stemmer.stem(term).toLowerCase();
    }

	/*
	private Literal findNextLiteral(String subquery, int startIndex) {
		PorterStemmer stemmer = new PorterStemmer();

		// Obtain length of the subquery
		int subLength = subquery.length();

		List<String> phraseLiteralTerms = new ArrayList<>();
		int lengthOut;


		// Skip past white space.
		while (startIndex < subquery.length() && subquery.charAt(startIndex) == ' ')
		{
			startIndex++;
		}


		// Skip past white space.
		char nextCharacter = ' ';
		while (subquery.charAt(startIndex) == ' ')
		{
			++startIndex;
		}

		nextCharacter = subquery.charAt(startIndex);
		//startIndex++;
		if(nextCharacter == 34)
		{
			// First character of ith term in literal
			++nextCharacter;
			//startIndex++;
			int nextQuotation = subquery.indexOf('\"', startIndex + 1 );

			if (nextQuotation < 0) {

				// No more literals in this subquery.
				lengthOut = subLength - startIndex;
			}
			else {
				String removeQuotes = subquery.substring(startIndex + 1, nextQuotation);

				String terms[] = removeQuotes.split(" ");

				for(String term: terms)
				{
					String stemmedTerm = stemmer.stem(term);
					stemmedTerm = stemmedTerm.toLowerCase();
					phraseLiteralTerms.add(stemmedTerm);
				}
				//lengthOut =  (nextQuotation - startIndex);
				lengthOut = subLength - startIndex;
			}


			return new Literal(
					new StringBounds(startIndex, lengthOut),
					new PhraseLiteral(phraseLiteralTerms));
		}
		else
		{
			int nextSpace = subquery.indexOf(' ', startIndex);
			if (nextSpace < 0)
			{
				// No more literals in this subquery.
				lengthOut = subLength - startIndex;
			}
			else
			{
				lengthOut = nextSpace - startIndex;
			}

			String term = subquery.substring(startIndex, startIndex + lengthOut);
			String stemmedTerm = stemmer.stem(term);
			stemmedTerm = stemmedTerm.toLowerCase();

			return new Literal(
					new StringBounds(startIndex, lengthOut),
					new TermLiteral(stemmedTerm));
		}



	}
	*/

}
