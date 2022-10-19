package com.dbms.operators.physical;

import com.dbms.utils.ColumnName;
import com.dbms.utils.Helpers;
import com.dbms.utils.Tuple;
import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/** An operator that projects the Tuples from its child to a specified list of columns. */
public class ProjectOperator extends PhysicalOperator {

    /** {@code child} is the child operator for projection */
    private PhysicalOperator child;

    /** {@code schema} is the list of aliased table names and column names */
    private List<ColumnName> schema = new LinkedList<>();

    /**
     * @param child child operator to project
     * @param selectItems columns to project; does not contain AllColumns
     */
    public ProjectOperator(PhysicalOperator child, List<SelectItem> selectItems) {
        this.child = child;
        for (SelectItem item : selectItems) {
            Column col = ((Column) ((SelectExpressionItem) item).getExpression());
            String tableName = Helpers.getProperTableName(col.getTable());
            schema.add(ColumnName.bundle(tableName, col.getColumnName()));
        }
    }

    /** resets child operator */
    @Override
    public void reset() {
        child.reset();
    }

    /** @return next projected Tuple */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = child.getNextTuple();
        if (nextTuple == null) return null;
        nextTuple.project(schema);
        return nextTuple;
    }
}
