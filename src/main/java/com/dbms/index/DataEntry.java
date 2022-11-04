package com.dbms.index;

import java.util.List;

/** A class for representing data entries in leaf nodes. Stores the key and associated RIDs, and
 * compares by key. */
public class DataEntry implements Comparable<DataEntry> {
    /** The key of this data entry */
    int key;

    /** The associated RIDs */
    List<RID> rids;

    public DataEntry(int key, List<RID> rids) {
        this.key = key;
        this.rids = rids;
    }

    @Override
    public int compareTo(DataEntry o) {
        return Integer.compare(key, o.key);
    }
}
