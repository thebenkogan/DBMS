package com.dbms.visitors;

import com.dbms.index.Index;
import com.dbms.operators.logical.LogicalDuplicateEliminationOperator;
import com.dbms.operators.logical.LogicalJoinOperator;
import com.dbms.operators.logical.LogicalProjectOperator;
import com.dbms.operators.logical.LogicalScanOperator;
import com.dbms.operators.logical.LogicalSelectOperator;
import com.dbms.operators.logical.LogicalSortOperator;
import com.dbms.operators.physical.BlockNestedLoopJoinOperator;
import com.dbms.operators.physical.DuplicateEliminationOperator;
import com.dbms.operators.physical.ExternalSortOperator;
import com.dbms.operators.physical.InMemorySortOperator;
import com.dbms.operators.physical.IndexScanOperator;
import com.dbms.operators.physical.PhysicalOperator;
import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.operators.physical.SortMergeJoinOperator;
import com.dbms.operators.physical.TupleNestedLoopJoinOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** Builds a physical plan from a query plan */
public class PhysicalPlanBuilder {

    /** Represents the current physical operator */
    public PhysicalOperator physOp;

    /** Construct physical scan from logical scan
     *
     * @param logicalScan is the scan operator from the logical plan */
    public void visit(LogicalScanOperator logicalScan) {
        physOp = new ScanOperator(logicalScan.tableName);
    }

    /** Constructs an index scan and a select operator if indexes can be used, otherwise creates a
     * physical select.
     *
     * @param logicalSelect is the select operator from the logical plan
     * @throws IOException */
    public void visit(LogicalSelectOperator logicalSelect) throws IOException {
        String tableName = ((LogicalScanOperator) logicalSelect.child).tableName;
        Index i = Catalog.getIndex(Catalog.getRealTableName(tableName));
        if (Catalog.CONFIG.indexSelection && i != null) {
            IndexExpressionVisitor iev = new IndexExpressionVisitor();
            iev.index = i;
            logicalSelect.exp.accept(iev);
            if (iev.booleanResult) {
                // we should use IndexScan
                physOp = new IndexScanOperator(tableName, i, iev.low, iev.high);
                if (!iev.nonIndexedExps.isEmpty()) {
                    // use both IndexScan and normal selection
                    physOp = new SelectOperator(physOp, Helpers.wrapListOfExpressions(iev.nonIndexedExps));
                }
                return;
            }
        }
        logicalSelect.child.accept(this);
        physOp = new SelectOperator(physOp, logicalSelect.exp);
    }

    /** Construct physical project from logical project
     *
     * @param logicalProject is the project operator from the logical plan
     * @throws IOException */
    public void visit(LogicalProjectOperator logicalProject) throws IOException {
        logicalProject.child.accept(this);
        physOp = new ProjectOperator(physOp, logicalProject.selectItems);
    }

    /** Construct physical sort from logical sort
     *
     * @param logicalSort is the sort operator from the logical plan
     * @throws IOException */
    public void visit(LogicalSortOperator logicalSort) throws IOException {
        logicalSort.child.accept(this);
        switch (Catalog.CONFIG.SORTTYPE) {
            case InMemory:
                physOp = new InMemorySortOperator(physOp, logicalSort.orderBys);
                break;

            case External:
                physOp = new ExternalSortOperator(physOp, logicalSort.orderBys, Catalog.CONFIG.EXTPages);
                break;
        }
    }

    /** Construct physical duplicate elimination from logical duplicate elimination
     *
     * @param logicalDupl is the duplicate elimination operator from the physical plan
     * @throws IOException */
    public void visit(LogicalDuplicateEliminationOperator logicalDupl) throws IOException {
        logicalDupl.child.accept(this);
        physOp = new DuplicateEliminationOperator(physOp);
    }

    /** Construct physical join from logical join
     *
     * @param logicalJoin is the join operator from the logical plan
     * @throws IOException */
    public void visit(LogicalJoinOperator logicalJoin) throws IOException {
        logicalJoin.left.accept(this);
        PhysicalOperator localLeft = physOp;
        logicalJoin.right.accept(this);
        PhysicalOperator localRight = physOp;
        switch (Catalog.CONFIG.JOINTYPE) {
            case TNLJ:
                physOp = new TupleNestedLoopJoinOperator(localLeft, localRight, logicalJoin.exp);
                break;

            case BNLJ:
                physOp = new BlockNestedLoopJoinOperator(
                        localLeft, localRight, logicalJoin.exp, Catalog.CONFIG.BNLJPages);
                break;
            case SMJ:
                List<EqualsTo> equalityConditions = Helpers.getEqualityConditions(logicalJoin.exp);
                physOp = createSortMergeJoinOperator(equalityConditions, logicalJoin, localLeft, localRight);
                break;
        }
    }

    /** @param equalityConditions list of EqualTo expressions found in the EquiJoin condition
     * @param joinOperator       is the logical join operator
     * @param localLeft          is the local left physical operator
     * @param localRight         is the local right physical operator
     * @return initialized SortMergeJoin Operator with left and right sorted children
     * @throws IOException */
    private SortMergeJoinOperator createSortMergeJoinOperator(
            List<EqualsTo> equalityConditions,
            LogicalJoinOperator joinOperator,
            PhysicalOperator localLeft,
            PhysicalOperator localRight)
            throws IOException {
        List<OrderByElement> leftOrderByElements = new LinkedList<>();
        List<OrderByElement> rightOrderByElements = new LinkedList<>();
        for (EqualsTo condition : equalityConditions) {
            Column leftCol = (Column) condition.getLeftExpression();
            Column rightCol = (Column) condition.getRightExpression();
            boolean leftIsInner = Helpers.getProperTableName(leftCol.getTable()).equals(joinOperator.innerTableName);

            OrderByElement left = new OrderByElement();
            left.setExpression(leftIsInner ? rightCol : leftCol);
            OrderByElement right = new OrderByElement();
            right.setExpression(leftIsInner ? leftCol : rightCol);

            leftOrderByElements.add(left);
            rightOrderByElements.add(right);
        }

        switch (Catalog.CONFIG.SORTTYPE) {
            case InMemory:
                return new SortMergeJoinOperator(
                        new InMemorySortOperator(localLeft, leftOrderByElements),
                        new InMemorySortOperator(localRight, rightOrderByElements));
            case External:
                return new SortMergeJoinOperator(
                        new ExternalSortOperator(localLeft, leftOrderByElements, Catalog.CONFIG.EXTPages),
                        new ExternalSortOperator(localRight, rightOrderByElements, Catalog.CONFIG.EXTPages));
        }

        return null;
    }
}
