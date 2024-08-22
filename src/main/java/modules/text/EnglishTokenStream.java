package modules.text;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Scanner;

/**
 * An EnglishTokenStream is created during the indexing process, and is used to obtain tokens (individual terms split by
 * whitespace) from a file.
 */
public class EnglishTokenStream implements TokenStream
{
    private final Reader mReader;

    private class EnglishTokenIterator implements Iterator<String>
    {
        private final Scanner mScanner;

        /**
         * A Scanner automatically tokenizes text by splitting on whitespace. By composing a Scanner we don't have to
         * duplicate that behavior.
         */
        private EnglishTokenIterator()
        {
            mScanner = new Scanner(mReader);
        }

        @Override
        public boolean hasNext()
        {
            return mScanner.hasNext();
        }

        @Override
        public String next()
        {
            return mScanner.next();
        }
    }

    /**
     * Constructor for the EnglishTokenStream that takes an input stream as a parameter.
     */
    public EnglishTokenStream(Reader inputStream)
    {
        mReader = inputStream;
    }

    /**
     * Converts an Iterator to an Iterable. Once an EnglishTokenStream has been created, this method can be called, which
     * will return am Iterable<String> tokens. This can then be used (with for-each loop) to loop through each token.
     */
    @Override
    public Iterable<String> getTokens()
    {
        return () -> new EnglishTokenIterator();
    }

    /**
     * Closes the reader
     */
    @Override
    public void close() throws IOException
    {
        if (mReader != null)
        {
            mReader.close();
        }
    }
}
