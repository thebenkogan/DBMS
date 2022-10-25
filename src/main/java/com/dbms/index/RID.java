package com.dbms.index;

public class RID implements Comparable<RID> {
    public int pageId;
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
