package com.dbms.operators;

import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;

public abstract class Operator {

    public abstract Tuple getNextTuple();

    public abstract void reset();

    /** @param writer the FileWriter to use to dump all Tuples of this operator to an output file
     * @throws IOException */
    public void dump(int i) throws IOException {
        TupleWriter tw = new TupleWriter(i);
        Tuple next;
        while ((next = getNextTuple()) != null) {
            tw.writeTuple(next);
        }
        tw.close();
    }
}
