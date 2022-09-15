package DBMS.operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import DBMS.utils.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class SortOperator extends Operator {

    private Operator child;
    private ArrayList<Tuple> table= new ArrayList<>();
    private List<OrderByElement> orderBys;
    private int index= 0;

    /** @param child child operator
     * @param orderBys list of orderBys, null if none */
    public SortOperator(Operator child, List<OrderByElement> orderBys) {
        this.child= child;
        this.orderBys= orderBys;
        populateTable();
        Collections.sort(table, new TupleComparator(getTableColumnNames()));
    }

    /** @return table (aliased) & column names in order of sorting; first the columns specified in
     *         the ORDER BY statement, then the columns not previously mentioned from the SELECT
     *         statement */
    private List<String> getTableColumnNames() {
        List<String> tableColumnNames= new LinkedList<>();
        if (orderBys != null) {
            for (OrderByElement orderBy : orderBys) {
                Column col= (Column) orderBy.getExpression();
                String tableName= col.getTable().getName();
                String columnName= col.getColumnName();
                tableColumnNames.add(Tuple.key(tableName, columnName));
            }
        }
        if (table.size() > 0) {
            for (String tableColumnName : table.get(0).getTableColumnNames()) {
                if (!tableColumnNames.contains(tableColumnName))
                    tableColumnNames.add(tableColumnName);
            }
        }
        return tableColumnNames;
    }

    @Override
    public Tuple getNextTuple() {
        if (index == table.size()) return null;
        return table.get(index++ );
    }

    /** {@summary} populate tuples into table */
    public void populateTable() {
        Tuple currTuple;
        while ((currTuple= child.getNextTuple()) != null) {
            table.add(currTuple);
        }
    }

    @Override
    public void reset() {
        index= 0;
    }
}

class TupleComparator implements Comparator<Tuple> {
    private List<String> tableColumnNames;

    public TupleComparator(List<String> tableColumnNames) {
        this.tableColumnNames= tableColumnNames;
    }

    @Override
    public int compare(Tuple t1, Tuple t2) {
        for (String name : tableColumnNames) {
            String tableName= Tuple.getTableName(name);
            String columnName= Tuple.getColumnName(name);
            int comp= Integer.compare(t1.get(tableName, columnName), t2.get(tableName, columnName));
            if (comp != 0) return comp;
        }
        return 0;
    }

}
