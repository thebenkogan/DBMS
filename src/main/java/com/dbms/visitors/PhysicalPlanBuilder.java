package com.dbms.visitors;

import com.dbms.operators.logical.LogicalDuplicateEliminationOperator;
import com.dbms.operators.logical.LogicalJoinOperator;
import com.dbms.operators.logical.LogicalProjectOperator;
import com.dbms.operators.logical.LogicalScanOperator;
import com.dbms.operators.logical.LogicalSelectOperator;
import com.dbms.operators.logical.LogicalSortOperator;
import com.dbms.operators.physical.DuplicateEliminationOperator;
import com.dbms.operators.physical.JoinOperator;
import com.dbms.operators.physical.PhysicalOperator;
import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.operators.physical.SortOperator;

/** Builds a physical plan from a query plan */
public class PhysicalPlanBuilder {

    /** Represents the current physical operator */
    public PhysicalOperator physOp;

    /** Construct physical scan from logical scan */
    public void visit(LogicalScanOperator logicalScan) {
        physOp = new ScanOperator(logicalScan.tableName);
    }

    /** Construct physical select from logical select */
    public void visit(LogicalSelectOperator logicalSelect) {
        logicalSelect.child.accept(this);
        physOp = new SelectOperator(physOp, logicalSelect.exp);
    }

    /** Construct physical project from logical project */
    public void visit(LogicalProjectOperator logicalProject) {
        logicalProject.child.accept(this);
        physOp = new ProjectOperator(physOp, logicalProject.selectItems);
    }

    /** Construct physical sort from logical sort */
    public void visit(LogicalSortOperator logicalSort) {
        logicalSort.child.accept(this);
        physOp = new SortOperator(physOp, logicalSort.orderBys);
    }

    /** Construct physical duplicate elimination from logical duplicate elimination */
    public void visit(LogicalDuplicateEliminationOperator logicalDupl) {
        logicalDupl.child.accept(this);
        physOp = new DuplicateEliminationOperator(physOp);
    }

    /** Construct physical join from logical join */
    public void visit(LogicalJoinOperator logicalJoin) {
        logicalJoin.left.accept(this);
        PhysicalOperator localLeft = physOp;
        logicalJoin.right.accept(this);
        PhysicalOperator localRight = physOp;

        physOp = new JoinOperator(localLeft, localRight, logicalJoin.exp);

        // todo: replace join operator with specific join operators
    }
}
