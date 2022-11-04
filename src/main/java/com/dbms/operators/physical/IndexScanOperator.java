package com.dbms.operators.physical;

import com.dbms.index.Index;
import com.dbms.index.TreeDeserializer;
import com.dbms.utils.Catalog;
import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import java.io.IOException;
import java.util.List;

/** A class that scans an index for keys in between a lower and upper bound. */
public class IndexScanOperator extends PhysicalOperator {

    /** Deserializer for index */
    private TreeDeserializer td;

    /** True if this has never received a getNextTuple call */
    private boolean isFirstCall;

    /** Inclusive lower bound on keys, null if unbound */
    private Integer lowkey;

    /** Inclusive upper bound on keys, null if unbound */
    private Integer highkey;

    /** Index of key attribute in tuples */
    int attributeIndex;

    public IndexScanOperator(Index i, Integer lowkey, Integer highkey) throws IOException {
        super(Schema.from(i.name.TABLE, Catalog.getTableColumns(i.name.TABLE)));

        this.lowkey = lowkey;
        this.highkey = highkey;
        td = new TreeDeserializer(i);
        isFirstCall = true;
        attributeIndex = Catalog.getColumnIndex(i.name);
    }

    @Override
    public Tuple getNextTuple() {
        try {
            if (isFirstCall) {
                isFirstCall = false;
                return new Tuple(schema, td.getFirstTupleAtKey(lowkey));
            }
            List<Integer> next = td.getNextTuple();
            if (next == null || highkey != null && next.get(attributeIndex) > highkey) return null;
            return new Tuple(schema, next);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {
        isFirstCall = true;
    }
}
