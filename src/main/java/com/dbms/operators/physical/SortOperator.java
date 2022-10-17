package com.dbms.operators.physical;

import com.dbms.utils.ColumnName;
import com.dbms.utils.Tuple;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public abstract class SortOperator extends PhysicalOperator {
    List<OrderByElement> orderBys;

    @Override
    public abstract Tuple getNextTuple();

    @Override
    public abstract void reset();

    public abstract void reset(int index);

    /** @param orderBys list of columns to prioritize for sorting
     * @param rep      representative child tuple
     * @return table (aliased) & column names in order of sorting; first the columns specified in
     *         the ORDER BY statement, then the columns not previously mentioned as they appear in
     *         the child Tuples */
    private List<ColumnName> getTableColumnNames(List<OrderByElement> orderBys, Tuple rep) {
        List<ColumnName> tableColumnNames = new LinkedList<>();
        if (orderBys != null) {
            for (OrderByElement orderBy : orderBys) {
                Column col = (Column) orderBy.getExpression();
                String tableName = col.getTable().getName();
                String columnName = col.getColumnName();
                tableColumnNames.add(ColumnName.bundle(tableName, columnName));
            }
        }
        if (rep != null) {
            for (ColumnName tableColumnName : rep.getSchema()) {
                if (!tableColumnNames.contains(tableColumnName)) tableColumnNames.add(tableColumnName);
            }
        }
        return tableColumnNames;
    }

    /** A comparator that compares two Tuples by comparing their equality based on a specified
     * column ordering. */
    public class TupleComparator implements Comparator<Tuple> {
        private List<ColumnName> tableColumnNames;

        /** @param tableColumnNames (aliased) table.column names to sort by */
        public TupleComparator(List<OrderByElement> orderBys, Tuple rep) {
            tableColumnNames = getTableColumnNames(orderBys, rep);
        }

        /** compares Tuples column by column as specified by tableColumnNames */
        @Override
        public int compare(Tuple t1, Tuple t2) {
            for (ColumnName name : tableColumnNames) {
                String tableName = name.TABLE;
                String columnName = name.COLUMN;
                int comp = Integer.compare(t1.get(tableName, columnName), t2.get(tableName, columnName));
                if (comp != 0) return comp;
            }
            return 0;
        }
    }
}
