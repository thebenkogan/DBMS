package com.dbms.operators.physical;

import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;

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
