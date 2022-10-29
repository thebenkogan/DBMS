package com.dbms.index;

/**
 * Class that contains information from {@code index_info.txt}
 */
public class Index {
    /** Unaliased name of the table */
    public final String table;

    /** name of the column */
    public final String column;

    /** order used for indexing leaves and nodes */
    public final int order;

    /** whether or not to sort the table by {@code table.column} before indexing */
    public final boolean cluster;

    /**
     * Constructs an instance of an {@code Index} object
     * @param table unaliased name of table
     * @param column name of column
     * @param order order to serialize nodes by
     * @param cluster whether or not to sort the relation
     */
    public Index(String table, String column, int order, boolean cluster) {
        this.table = table;
        this.column = column;
        this.order = order;
        this.cluster = cluster;
    }
}
