package DBMS.operators;

import java.util.List;
import java.util.stream.Collectors;

import DBMS.utils.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectOperator extends Operator {

    private Operator child;
    private List<Column> projectedColumns;

    /** Requires selectItems does not contain AllColumns.
     * 
     * @param child       child operator to project
     * @param selectItems columns to project */
    public ProjectOperator(Operator child, List<SelectItem> selectItems) {
        this.child= child;
        this.projectedColumns= selectItems.stream()
            .map(item -> (Column) ((SelectExpressionItem) item).getExpression())
            .collect(Collectors.toList());
    }

    @Override
    public void reset() {
        child.reset();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple= child.getNextTuple();
        if (nextTuple == null) return null;
        nextTuple.project(projectedColumns);
        return nextTuple;
    }
}
