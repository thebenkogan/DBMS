package com.dbms.operators;

import com.dbms.utils.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * An operator that reads all of its child Tuples, stores and sorts them in ascending order, and
 * returns them one-by-one as requested.
 */
public class SortOperator extends Operator {

    private Operator child;

    /** internal buffer that holds all of child's output in ascending order */
    private ArrayList<Tuple> table = new ArrayList<>();

    /** index of the next tuple to return in table */
    private int index = 0;

    /**
     * Reads all Tuples from child into table, then sorts in the order specified by orderBys.
     *
     * @param child child operator
     * @param orderBys list of orderBys, null if none
     */
    public SortOperator(Operator child, List<OrderByElement> orderBys) {
        this.child = child;
        populateTable();
        Collections.sort(table, new TupleComparator(getTableColumnNames(orderBys)));
    }

    /**
     * @param orderBys list of columns to prioritize for sorting
     * @return table (aliased) & column names in order of sorting; first the columns specified in the
     *     ORDER BY statement, then the columns not previously mentioned as they appear in the child
     *     Tuples
     */
    private List<String> getTableColumnNames(List<OrderByElement> orderBys) {
        List<String> tableColumnNames = new LinkedList<>();
        if (orderBys != null) {
            for (OrderByElement orderBy : orderBys) {
                Column col = (Column) orderBy.getExpression();
                String tableName = col.getTable().getName();
                String columnName = col.getColumnName();
                tableColumnNames.add(Tuple.key(tableName, columnName));
            }
        }
        if (table.size() > 0) {
            for (String tableColumnName : table.get(0).getTableColumnNames()) {
                if (!tableColumnNames.contains(tableColumnName)) tableColumnNames.add(tableColumnName);
            }
        }
        return tableColumnNames;
    }

    /**
     * @return Tuple at index in table
     */
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
}

/**
 * A comparator that compares two Tuples by comparing their equality based on a specified column
 * ordering.
 */
class TupleComparator implements Comparator<Tuple> {
    private List<String> tableColumnNames;

    /**
     * @param tableColumnNames (aliased) table.column names to sort by
     */
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
