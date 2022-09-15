package DBMS.operators;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import DBMS.utils.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectOperator extends Operator {

    private Operator child;

    /** Name of projected columns */
    private List<String> columnNames;
    /** name (aliased) of projected tables */
    private List<String> tableNames;
    // invariant: tableNames[i] must be the real table name (not alias) of columnNames[i]

    /** Requires selectItems does not contain AllColumns.
     *
     * @param child       child operator to project
     * @param selectItems columns to project */
    public ProjectOperator(Operator child, List<SelectItem> selectItems) {
        this.child= child;
        columnNames= selectItems.stream()
            .map(item -> ((Column) ((SelectExpressionItem) item).getExpression()).getColumnName())
            .collect(Collectors.toList());
        tableNames= new LinkedList<>();
        for (SelectItem item : selectItems) {
            Column col= ((Column) ((SelectExpressionItem) item).getExpression());
            tableNames.add(col.getTable().getAlias() != null ? col.getTable().getAlias() :
                col.getTable().getWholeTableName());
        }
    }

    @Override
    public void reset() {
        child.reset();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple= child.getNextTuple();
        if (nextTuple == null) return null;
        nextTuple.project(tableNames, columnNames);
        return nextTuple;
    }
}
