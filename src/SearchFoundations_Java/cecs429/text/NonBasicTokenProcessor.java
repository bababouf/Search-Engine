package SearchFoundations_Java.cecs429.text;
import opennlp.tools.stemmer.PorterStemmer;

import java.lang.*;
import java.util.*;


import static java.lang.Character.isLetterOrDigit;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class NonBasicTokenProcessor implements TokenProcessor {
    @Override
    public List<String> processToken(String token) {
        PorterStemmer stemmer = new PorterStemmer();

        List<String> termList = new ArrayList<>();
        List<String> termListLowercase = new ArrayList<>();

        boolean hyphenatedWord = token.contains("-");
        if(hyphenatedWord)
        {
            int startIndex = 0;
            String hyphenatedTerm = "";
            for(int i = 0; i < token.length(); i++)
            {
                char charAtIndex = token.charAt(i);
                if(charAtIndex == ' ' && (i - startIndex > 1))
                {
                    String term = token.substring(startIndex, i);
                    termList.add(token.substring(startIndex, i));
                    startIndex = i + 1;
                    hyphenatedTerm += term;
                }
            }
            termList.add(hyphenatedTerm);

            for(String term : termList)
            {
                String processedTerm = processTokenHelper(term);

                String stemmedTerm = stemmer.stem(processedTerm);

                termListLowercase.add(stemmedTerm.toLowerCase());

            }

        }
        else
        {
            String processedToken = processTokenHelper(token);
            String stemmedTerm = stemmer.stem(processedToken);
            termListLowercase.add(stemmedTerm.toLowerCase());

        }
        return termListLowercase;

    }

    public String processTokenHelper(String token) {
        int index = 0;

        while (index <= token.length() - 1) {

            boolean isAlphanumeric = isLetterOrDigit(token.charAt(index));
            if (isAlphanumeric) {
                index++;
            } else if (index == 0) {
                token = token.substring(1);

            } else if (index == token.length() - 1) {
                token = token.substring(0, index);
                index--;

            } else {
                if (token.charAt(index) == 34 || token.charAt(index) == 39) {
                    token = token.substring(0, index) + token.substring(index + 1);

                } else {
                    index++;
                }
            }

        }
        return token;
    }

}