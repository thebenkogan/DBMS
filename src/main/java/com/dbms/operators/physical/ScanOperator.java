package com.dbms.operators.physical;

import com.dbms.utils.Catalog;
import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleReader;
import java.io.IOException;
import java.util.List;

/** An operator that reads data from file and builds Tuples. */
public class ScanOperator extends PhysicalOperator {

    /** reader for the underlying table */
    private TupleReader reader;

    /** @param tableName name (aliased) of underlying table */
    public ScanOperator(String tableName) {
        super(Schema.from(tableName, Catalog.getTableColumns(Catalog.getRealTableName(tableName))));
        try {
            reader = new TupleReader(Catalog.pathToTable(Catalog.getRealTableName(tableName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** @return next Tuple from underlying DB file */
    @Override
    public Tuple getNextTuple() {
        try {
            List<Integer> next = reader.nextTuple();
            if (next == null) return null;
            return new Tuple(schema, next);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** closes DB file reader and opens a new one starting at the top of the file */
    @Override
    public void reset() {
        try {
            reader.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
