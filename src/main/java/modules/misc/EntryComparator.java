package modules.misc;

import java.util.Comparator;

/**
 * Comparator for comparing two Entry objects based on their accumulator values in descending order.
 */
public class EntryComparator implements Comparator<Entry>
{

    /**
     * Compares two Entry objects for order based on their accumulator values.
     * Returns a negative integer, zero, or a positive integer as the first argument is greater than,
     * equal to, or less than the second.
     *
     * @param s1 the first Entry to be compared.
     * @param s2 the second Entry to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is greater than,
     * equal to, or less than the second.
     */
    @Override
    public int compare(Entry s1, Entry s2)
    {
        return Double.compare(s2.getAccumulatorValue(), s1.getAccumulatorValue());
    }
}
