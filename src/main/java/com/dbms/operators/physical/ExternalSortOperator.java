package com.dbms.operators.physical;

import com.dbms.utils.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;

// algorithm:
// initial pass - load B pages worth of tuples, sort, and write out
// merge pass - load the first tuples from B - 1 runs, write minimum to merge file
// (note that writing to file is still only 1 page, because we buffer the write)
// hard thing to solve first: how many attributes in child tuples?

/** An operator that reads all of its child Tuples, stores and sorts them in ascending order, and
 * returns them one-by-one as requested. */
public class ExternalSortOperator extends SortOperator {

    private PhysicalOperator child;

    /** internal buffer that holds all of child's output in ascending order */
    private ArrayList<Tuple> table = new ArrayList<>();

    /** index of the next tuple to return in table */
    private int index = 0;

    /** Reads all Tuples from child into table, then sorts in the order specified by orderBys.
     *
     * @param child    child operator
     * @param orderBys list of orderBys, null if none */
    public ExternalSortOperator(PhysicalOperator child, List<OrderByElement> orderBys, int pages) {
        this.child = child;
        Tuple rep = child.getNextTuple();
        child.reset();

        populateTable();
        Collections.sort(table, new TupleComparator(orderBys, rep));
    }

    /** @return Tuple at index in table */
    @Override
    public Tuple getNextTuple() {
        if (index == table.size()) return null;
        return table.get(index++);
    }

    /** reads all of the Tuples from the child operator and adds them to table */
    public void populateTable() {
        Tuple currTuple;
        while ((currTuple = child.getNextTuple()) != null) {
            table.add(currTuple);
        }
    }

    /** resets internal buffer index */
    @Override
    public void reset() {
        index = 0;
    }

    @Override
    public void reset(int index) {
        // TODO Auto-generated method stub

    }
}

/** A comparator that compares two Tuples by comparing their equality based on a specified column
 * ordering. */
class TupleComparator implements Comparator<Tuple> {
    private List<String> tableColumnNames;

    /** @param tableColumnNames (aliased) table.column names to sort by */
    public TupleComparator(List<String> tableColumnNames) {
        this.tableColumnNames = tableColumnNames;
    }

    /** compares Tuples column by column as specified by tableColumnNames */
    @Override
    public int compare(Tuple t1, Tuple t2) {
        for (String name : tableColumnNames) {
            String tableName = Tuple.getTableName(name);
            String columnName = Tuple.getColumnName(name);
            int comp = Integer.compare(t1.get(tableName, columnName), t2.get(tableName, columnName));
            if (comp != 0) return comp;
        }
        return 0;
    }
}
