package com.dbms.operators.physical;

import com.dbms.utils.Catalog;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;

public abstract class PhysicalOperator {

    public abstract Tuple getNextTuple();

    public abstract void reset();

    /** @param writer the FileWriter to use to dump all Tuples of this operator to an output file
     * @throws IOException */
    public void dump(int i) throws IOException {
        TupleWriter tw = new TupleWriter(Catalog.pathToOutputFile(i));
        Tuple next;
        while ((next = getNextTuple()) != null) {
            tw.writeTuple(next);
        }
        tw.close();
    }
}
