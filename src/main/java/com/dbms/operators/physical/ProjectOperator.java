package com.dbms.operators.physical;

import com.dbms.utils.Helpers;
import com.dbms.utils.Tuple;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/** An operator that projects the Tuples from its child to a specified list of columns. */
public class ProjectOperator extends PhysicalOperator {

    private PhysicalOperator child;

    /** Name of projected columns */
    private List<String> columnNames;
    /** name (aliased) of projected tables */
    private List<String> tableNames;
    // invariant: tableNames[i] must be the real table name of columnNames[i]

    /**
     * @param child child operator to project
     * @param selectItems columns to project; does not contain AllColumns
     */
    public ProjectOperator(PhysicalOperator child, List<SelectItem> selectItems) {
        this.child = child;
        columnNames = selectItems.stream()
                .map(item -> ((Column) ((SelectExpressionItem) item).getExpression()).getColumnName())
                .collect(Collectors.toList());
        tableNames = new LinkedList<>();
        for (SelectItem item : selectItems) {
            Column col = ((Column) ((SelectExpressionItem) item).getExpression());
            tableNames.add(Helpers.getProperTableName(col.getTable()));
        }
    }

    /** resets child operator */
    @Override
    public void reset() {
        child.reset();
    }

    /**
     * @return next projected Tuple
     */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = child.getNextTuple();
        if (nextTuple == null) return null;
        nextTuple.project(tableNames, columnNames);
        return nextTuple;
    }
}
