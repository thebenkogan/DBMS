package com.dbms.operators.logical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.queryplan.PhysicalPlanBuilder;
import com.dbms.utils.Catalog;
import java.io.PrintWriter;

/** The logical representation of the scan operator, which contains only the table name which we
 * need to construct the physical operator */
public class LogicalScanOperator extends LogicalOperator {

    /** {@code tableName} is the (aliased) name of the table to scan from */
    public String tableName;

    /** @param tableName name (aliased) of underlying table */
    public LogicalScanOperator(String tableName) {
        this.tableName = tableName;
    }

    /** @param physicalPlan visitor which converts logical to physical operator */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) {
        physicalPlan.visit(this);
    }

    @Override
    public void write(PrintWriter pw, int level) {
        String s = String.format("Leaf[%s]", Catalog.getRealTableName(tableName));
        pw.println(writeLevel(s, level));
    }
}
