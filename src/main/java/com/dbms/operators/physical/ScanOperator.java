package com.dbms.operators.physical;

import com.dbms.utils.Catalog;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleReader;
import java.io.IOException;
import java.util.List;

/** An operator that reads data from file and builds Tuples. */
public class ScanOperator extends PhysicalOperator {

    /** name (aliased) of underlying table */
    private String name;

    /** reader for the underlying table */
    private TupleReader reader;

    /** @param tableName name (aliased) of underlying table
     * @throws IOException */
    public ScanOperator(String tableName) {
        try {
            name = tableName;
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
            return new Tuple(name, Catalog.getInstance().getTableColumns(Catalog.getRealTableName(name)), next);
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
