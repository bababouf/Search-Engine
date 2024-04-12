package modules.misc;

import java.util.Comparator;

public class EntryComparator implements Comparator<Entry> {

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    public int compare(Entry s1, Entry s2) {
        if (s1.Ad < s2.Ad)
            return 1;
        else if (s1.Ad > s2.Ad)
            return -1;
        return 0;
    }
}
