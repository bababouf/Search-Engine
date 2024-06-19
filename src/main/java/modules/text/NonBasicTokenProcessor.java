package modules.text;

import opennlp.tools.stemmer.PorterStemmer;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isLetterOrDigit;

/**
 * This class offers further processing to tokens that are passed to its processToken function.
 */
public class NonBasicTokenProcessor implements TokenProcessor {

    /**
     * This function will first check the token passed to it is hyphenated. If so, it will split on each hyphen (if more than one)
     * and will create separate tokens for each. Each of these will then be passed to the processTokenHelper function.
     */
    private PorterStemmer stemmer;

    @Override
    public List<String> processToken(String token) {
        if (token == null || token.isEmpty())
        {
            return new ArrayList<>();
        }

        List<String> termList = splitToken(token);
        return processTerms(termList);
    }

    /**
     * This method will split a token that contains hyphens by the hyphens, adding each of the individual terms (as well
     * as the full hyphenated term without the hyphens) to the term list
     */
    private List<String> splitToken(String token) {
        List<String> termList = new ArrayList<>();
        if (token.contains("-"))
        {
            // Split the token by hyphens and add to term list
            String[] terms = token.split("-");
            for (String term : terms)
            {
                termList.add(term);
            }
            // Add the full hyphenated term, removing the hyphens
            termList.add(token.replace("-", ""));
        }
        // If the token does not contain a hyphen, simply add the token to the term list
        else
        {
            termList.add(token);
        }

        return termList;
    }

    /**
     * This method will stem and lowercase each of the terms passed to it
     */
    private List<String> processTerms(List<String> termList) {
        List<String> termListLowercase = new ArrayList<>();

        for (String term : termList)
        {
            // Remove specific non-alphanumeric characters from token
            String processedTerm = processTokenHelper(term);

            // Retrieves the stemmed term
            String stemmedTerm = stemmer.stem(processedTerm);

            // Lowercases the term
            termListLowercase.add(stemmedTerm.toLowerCase());
        }

        return termListLowercase;
    }

    /**
     * This method will loop through the token passed, removing certain non-alphanumeric characters based
     * on their position within the string.
     */
    private String processTokenHelper(String token)
    {
        int index = 0;

        // Loop through the entire token from start to end
        while (index <= token.length() - 1)
        {

            // Check if the character at index is alphanumeric
            boolean isAlphanumeric = isLetterOrDigit(token.charAt(index));

            // If the character is alphanumeric, increment the index and move to the next character
            if (isAlphanumeric)
            {
                index++;
            }
            // The character is NOT alphanumeric AND is at the beginning of the token
            else if (index == 0)
            {
                // Remove the character by creating a substring from index 1 to the end of string
                token = token.substring(1);
            }
            // The character is NOT alphanumeric AND is at the end of the token
            else if (index == token.length() - 1)
            {
                // Remove the character by creating a substring from index 0 to end of string - 1 (last char removed)
                token = token.substring(0, index);

                // Adjust index since token is now shortened
                index--;

            }

            // Character is NOT alphanumeric AND is in middle of token
            else
            {
                // Check if the character is a double quote (ASCII 34) or single quote (ASCII 39)
                if (token.charAt(index) == 34 || token.charAt(index) == 39)
                {
                    // Create a new substring with that index removed
                    token = token.substring(0, index) + token.substring(index + 1);
                }
                // If the character is not a single or double quote, simply move to the next character
                else
                {
                    index++;
                }
            }

        }
        return token;
    }
}


