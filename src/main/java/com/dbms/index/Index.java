package com.dbms.index;

public class Index {
    public final String table;
    public final String column;
    public final int order;
    public final boolean cluster;

    public Index(String table, String column, int order, boolean cluster) {
        this.table = table;
        this.column = column;
        this.order = order;
        this.cluster = cluster;
    }
}
