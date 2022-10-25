package com.dbms.index;

import java.util.List;

public class DataEntry implements Comparable<DataEntry> {
    int key;
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
