package com.dbms.index;

/** A class that represent RIDs in data entries. Stores the page ID and tuple ID of the underlying
 * tuple. First compares by page ID, then by tuple ID. */
public class RID implements Comparable<RID> {
    /** The 0-based page of the tuple in file */
    public int pageId;

    /** The 0-based index of the tuple on the page */
    public int tupleId;

    public RID(int pageId, int tupleId) {
        this.pageId = pageId;
        this.tupleId = tupleId;
    }

    @Override
    public int compareTo(RID o) {
        int pageOrder = Integer.compare(pageId, o.pageId);
        if (pageOrder != 0) return pageOrder;
        return Integer.compare(tupleId, o.tupleId);
    }
}
