package com.dbms.queryplan;

import static com.dbms.utils.Helpers.getColumnNamesFromSelectItems;
import static com.dbms.utils.Helpers.getEqualityConditions;
import static com.dbms.utils.Helpers.getProperTableName;
import static com.dbms.utils.Helpers.isEquiJoin;
import static com.dbms.utils.Helpers.wrapListOfExpressions;

import com.dbms.index.Index;
import com.dbms.index.IndexExpressionVisitor;
import com.dbms.operators.logical.LogicalDuplicateEliminationOperator;
import com.dbms.operators.logical.LogicalJoinOperator;
import com.dbms.operators.logical.LogicalOperator;
import com.dbms.operators.logical.LogicalProjectOperator;
import com.dbms.operators.logical.LogicalScanOperator;
import com.dbms.operators.logical.LogicalSelectOperator;
import com.dbms.operators.logical.LogicalSortOperator;
import com.dbms.operators.physical.BlockNestedLoopJoinOperator;
import com.dbms.operators.physical.DuplicateEliminationOperator;
import com.dbms.operators.physical.ExternalSortOperator;
import com.dbms.operators.physical.IndexScanOperator;
import com.dbms.operators.physical.PhysicalOperator;
import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.operators.physical.SortMergeJoinOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Schema;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;
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
        String unaliasedName = Catalog.getRealTableName(tableName);
        List<Index> indexes = Catalog.getIndexes(unaliasedName);

        // if no indexes available, create a scan and select
        if (indexes == null) {
            logicalSelect.child.accept(this);
            physOp = new SelectOperator(physOp, logicalSelect.exp);
            return;
        }

        // get the scan cost and search for the index with lowest cost
        double scanCost = Catalog.STATS.getTableScanCost(unaliasedName);
        IndexExpressionVisitor bestIev = null;
        double bestCost = Integer.MAX_VALUE;
        for (Index i : indexes) {
            IndexExpressionVisitor iev = new IndexExpressionVisitor(i);
            logicalSelect.exp.accept(iev);
            if (iev.isIndexable) {
                double cost = Catalog.STATS.getTableIndexCost(i, iev.extent());
                if (cost < bestCost) {
                    bestIev = iev;
                    bestCost = cost;
                }
            }
        }

        // if scanning is cheaper, use a scan and select, otherwise use the best index
        if (scanCost < bestCost) {
            logicalSelect.child.accept(this);
            physOp = new SelectOperator(physOp, logicalSelect.exp);
        } else {
            physOp = new IndexScanOperator(tableName, bestIev.index, bestIev.low, bestIev.high);
            if (!bestIev.nonIndexedExps.isEmpty()) {
                // use both IndexScan and normal selection
                physOp = new SelectOperator(physOp, wrapListOfExpressions(bestIev.nonIndexedExps));
            }
        }
    }

    /** Construct physical project from logical project
     *
     * @param logicalProject is the project operator from the logical plan
     * @throws IOException */
    public void visit(LogicalProjectOperator logicalProject) throws IOException {
        logicalProject.child.accept(this);
        Schema s = new Schema(getColumnNamesFromSelectItems(logicalProject.selectItems));
        physOp = new ProjectOperator(physOp, s, true);
    }

    /** Construct physical sort from logical sort
     *
     * @param logicalSort is the sort operator from the logical plan
     * @throws IOException */
    public void visit(LogicalSortOperator logicalSort) throws IOException {
        logicalSort.child.accept(this);
        physOp = new ExternalSortOperator(physOp, logicalSort.orderBys, Catalog.EXTPages);
    }

    /** Construct physical duplicate elimination from logical duplicate elimination
     *
     * @param logicalDupl is the duplicate elimination operator from the physical plan
     * @throws IOException */
    public void visit(LogicalDuplicateEliminationOperator logicalDupl) throws IOException {
        logicalDupl.child.accept(this);
        physOp = new DuplicateEliminationOperator(physOp);
    }

    /** Constructs the left deep join tree with optimal order. Inserts a project operator at the
     * root of this tree if the optimal join order is different than the join order in the query.
     *
     * @param logicalJoin is the join operator from the logical plan
     * @throws IOException */
    public void visit(LogicalJoinOperator logicalJoin) throws IOException {
        // we use DP to calculate the best ordering for the children of logicalJoin
        JoinOrderOptimizer opt = new JoinOrderOptimizer(logicalJoin);
        // we then create a left deep tree of physical operators using the best join order
        List<String> optOrder = opt.getBestOrder();
        physOp = createLeftDeepTree(optOrder, logicalJoin, logicalJoin.children);
        if (!logicalJoin.tableNames.equals(optOrder)) {
            physOp = new ProjectOperator(physOp, Schema.from(logicalJoin.tableNames), false);
        }
    }

    /** @param tables table names (aliaed), removes first
     * @param children the Logical Scan/Select operators corresponding to tables
     * @return
     * @throws IOException */
    private PhysicalOperator getNextOperator(List<String> tables, Map<String, LogicalOperator> children)
            throws IOException {
        String tableName = tables.remove(0);
        LogicalOperator logicalChild = children.get(tableName);
        logicalChild.accept(this);

        // physOp is the physical operator for the logical operator that tableName maps to
        return physOp;
    }

    /** @param left       outer child
     * @param right          inner child
     * @param joinExp        join expression
     * @param innerTableName inner child's table name
     * @return SMJ operator if joinExp is an equijoin, otherwise a BNLJ operator
     * @throws IOException */
    private PhysicalOperator selectJoinImplementation(
            PhysicalOperator left, PhysicalOperator right, Expression joinExp, String innerTableName)
            throws IOException {
        if (isEquiJoin(joinExp)) {
            List<EqualsTo> equalityConditions = getEqualityConditions(joinExp);
            return createSortMergeJoinOperator(equalityConditions, left, right, innerTableName);
        } else {
            return new BlockNestedLoopJoinOperator(left, right, joinExp, Catalog.BNLJPages);
        }
    }

    /** Creates children for join condition parsing
     *
     * @param tables   tables to place in tree. If aliases exist, then tables consists solely of
     *                 aliases. Otherwise, tables contains actual table names; tables.length > 1
     * @param uv       the {@code UnionFindVisitor} for obtaining the join conditions
     * @param children the list of logical scan/select operators corresponding to tables
     * @return a {@code LogicalJoinOperator} containing the left-deep tree
     * @throws IOException, FileNotFoundException */
    private PhysicalOperator createLeftDeepTree(
            List<String> tables, LogicalJoinOperator logicalJoin, Map<String, LogicalOperator> children)
            throws IOException, FileNotFoundException {
        UnionFindVisitor uv = logicalJoin.uv;

        String leftName = tables.get(0);
        PhysicalOperator leftOp = getNextOperator(tables, children);

        String rightName = tables.get(0);
        PhysicalOperator rightOp = getNextOperator(tables, children);

        Expression joinExp = uv.getExpression(leftName, rightName);
        PhysicalOperator joinOp = selectJoinImplementation(leftOp, rightOp, joinExp, rightName);

        List<String> seenNames = new ArrayList<>();
        seenNames.add(leftName);
        seenNames.add(rightName);

        while (tables.size() > 0) {
            String nextName = tables.get(0);
            PhysicalOperator nextOp = getNextOperator(tables, children);
            joinExp = uv.getExpression(nextName, seenNames);
            joinOp = selectJoinImplementation(joinOp, nextOp, joinExp, nextName);
            seenNames.add(nextName);
        }
        return joinOp;
    }

    /** @param equalityConditions list of EqualTo expressions found in the EquiJoin condition
     * @param joinOperator       is the logical join operator
     * @param localLeft          is the local left physical operator
     * @param localRight         is the local right physical operator
     * @return initialized SortMergeJoin Operator with left and right sorted children
     * @throws IOException */
    private SortMergeJoinOperator createSortMergeJoinOperator(
            List<EqualsTo> equalityConditions,
            PhysicalOperator localLeft,
            PhysicalOperator localRight,
            String innerTableName)
            throws IOException {
        List<OrderByElement> leftOrderByElements = new LinkedList<>();
        List<OrderByElement> rightOrderByElements = new LinkedList<>();
        for (EqualsTo condition : equalityConditions) {
            Column leftCol = (Column) condition.getLeftExpression();
            Column rightCol = (Column) condition.getRightExpression();
            boolean leftIsInner = getProperTableName(leftCol.getTable()).equals(innerTableName);

            OrderByElement left = new OrderByElement();
            left.setExpression(leftIsInner ? rightCol : leftCol);
            OrderByElement right = new OrderByElement();
            right.setExpression(leftIsInner ? leftCol : rightCol);

            leftOrderByElements.add(left);
            rightOrderByElements.add(right);
        }

        return new SortMergeJoinOperator(
                new ExternalSortOperator(localLeft, leftOrderByElements, Catalog.EXTPages),
                new ExternalSortOperator(localRight, rightOrderByElements, Catalog.EXTPages));
    }

    /** Writes this plan. Assumes a logical plan was already visited and physOp is not null.
     *
     * @param i query number
     * @throws FileNotFoundException */
    public void writePlan(int i) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(Catalog.pathToOutputPhysicalPlan(i));
        physOp.write(pw, 0);
        pw.close();
    }
}
