package com.dbms.operators.physical;

import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class PhysicalOperator {
    /** schema for this operator, representing the order and names of columns */
    public Schema schema;

    /** @param schema schema for this operator */
    PhysicalOperator(Schema schema) {
        this.schema = schema;
    }

    /** @return next tuple of the result of the relation */
    public abstract Tuple getNextTuple();

    /** Resets the operator to the first tuple of the result of the relation */
    public abstract void reset();

    /** Writes this operator at the corresponding level and writes each child operator on the next
     * level.
     *
     * @param pw    the writer that is accumulating the printed plan
     * @param level 0-based level in the tree */
    public abstract void write(PrintWriter pw, int level);

    /** @param path is the destination file location for tuple writing
     * @throws IOException */
    public void dump(String path) throws IOException {
        TupleWriter tw = new TupleWriter(path);
        Tuple next;
        while ((next = getNextTuple()) != null) {
            tw.writeTuple(next);
        }
        tw.close();
    }
}
