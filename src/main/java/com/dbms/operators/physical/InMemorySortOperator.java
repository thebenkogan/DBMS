package com.dbms.operators.physical;

import com.dbms.utils.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** An operator that reads all of its child Tuples, stores and sorts them in ascending order, and
 * returns them one-by-one as requested. */
public class InMemorySortOperator extends SortOperator {

    private PhysicalOperator child;

    /** internal buffer that holds all of child's output in ascending order */
    private ArrayList<Tuple> table = new ArrayList<>();

    /** index of the next tuple to return in table */
    private int index = 0;

    /** Reads all Tuples from child into table, then sorts in the order specified by orderBys.
     *
     * @param child    child operator
     * @param orderBys list of orderBys, null if none */
    public InMemorySortOperator(PhysicalOperator child, List<OrderByElement> orderBys) {
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
