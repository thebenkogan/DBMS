package com.dbms.index;

import com.dbms.utils.Attribute;

/** Class that contains information from {@code index_info.txt} */
public class Index {
    /** object containing unaliased table and column name */
    public final Attribute name;

    /** order used for indexing leaves and nodes */
    public final int order;

    /** whether or not to sort the table by {@code table.column} before indexing */
    public final boolean isClustered;

    /** Constructs an instance of an {@code Index} object
     *
     * @param name object containing unaliased table and column name
     * @param order       order to serialize nodes by
     * @param isClustered whether or not to sort the relation */
    public Index(Attribute name, int order, boolean isClustered) {
        this.name = name;
        this.order = order;
        this.isClustered = isClustered;
    }
}
