package com.dbms.operators.physical;

import com.dbms.utils.Helpers;
import com.dbms.utils.Schema;
import com.dbms.utils.Tuple;
import java.util.List;
import net.sf.jsqlparser.statement.select.SelectItem;

/** An operator that projects the Tuples from its child to a specified list of columns. */
public class ProjectOperator extends PhysicalOperator {

    /** {@code child} is the child operator for projection */
    private PhysicalOperator child;

    /** @param child   child operator to project
     * @param selectItems columns to project; does not contain AllColumns */
    public ProjectOperator(PhysicalOperator child, List<SelectItem> selectItems) {
        super(new Schema(Helpers.getColumnNamesFromSelectItems(selectItems)));
        this.child = child;
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
