package com.dbms.operators.physical;

import com.dbms.utils.Catalog;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;

public abstract class PhysicalOperator {

    /**
     * @return next tuple of the result of the relation
     */
    public abstract Tuple getNextTuple();

    /**
     * Resets the operator to the first tuple of the result of the relation
     */
    public abstract void reset();

    /**
     * @param i is the {@code i}th query, and will be used to name the output file to its corresponding line number in {@code queries.sql}
     * @throws IOException
     */
    public void dump(int i) throws IOException {
        TupleWriter tw = new TupleWriter(Catalog.pathToOutputFile(i));
        Tuple next;
        while ((next = getNextTuple()) != null) {
            tw.writeTuple(next);
        }
        tw.close();
    }
}
