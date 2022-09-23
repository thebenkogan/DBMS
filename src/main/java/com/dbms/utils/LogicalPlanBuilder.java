package com.dbms.utils;

import com.dbms.operators.logical.LogicalDuplicateEliminationOperator;
import com.dbms.operators.logical.LogicalJoinOperator;
import com.dbms.operators.logical.LogicalOperator;
import com.dbms.operators.logical.LogicalProjectOperator;
import com.dbms.operators.logical.LogicalScanOperator;
import com.dbms.operators.logical.LogicalSelectOperator;
import com.dbms.operators.logical.LogicalSortOperator;
import com.dbms.visitors.JoinVisitor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** Builds a query plan from a Statement and stores the root operator. */
public class LogicalPlanBuilder {
    public LogicalOperator logicalOperator;

    /** @param tables table names (aliased), removes first
     * @param jv     join visitor
     * @return scan/select operator from first table name in tables
     * @throws FileNotFoundException */
    private LogicalOperator getNextOperator(List<String> tables, JoinVisitor jv) throws FileNotFoundException {
        String tableName = tables.remove(0);
        Expression exp = jv.getExpression(tableName);

        LogicalOperator op = new LogicalScanOperator(tableName);
        if (exp != null) op = new LogicalSelectOperator(op, exp);
        return op;
    }

    /** @param tables tables to place in tree. If aliases exist, then tables consists solely of
     *               aliases. Otherwise, tables contains actual table names; tables.length > 1
     * @throws FileNotFoundException */
    private LogicalJoinOperator createLeftDeepTree(List<String> tables, JoinVisitor jv) throws FileNotFoundException {

        String leftName = tables.get(0);
        LogicalOperator leftOp = getNextOperator(tables, jv);

        String rightName = tables.get(0);
        LogicalOperator rightOp = getNextOperator(tables, jv);

        LogicalJoinOperator joinOp = new LogicalJoinOperator(leftOp, rightOp, jv.getExpression(leftName, rightName));

        List<String> seenNames = new ArrayList<>();
        seenNames.add(leftName);
        seenNames.add(rightName);

        while (tables.size() > 0) {
            String nextName = tables.get(0);
            LogicalOperator nextScan = getNextOperator(tables, jv);

            LogicalJoinOperator nextOp =
                    new LogicalJoinOperator(joinOp, nextScan, jv.getExpression(nextName, seenNames));
            seenNames.add(nextName);
            joinOp = nextOp;
        }

        return joinOp;
    }

    /** @param statement Statement for which to build a query plan and create a root operator
     * @throws FileNotFoundException */
    @SuppressWarnings("unchecked")
    public LogicalPlanBuilder(Statement statement) throws FileNotFoundException {
        // extract relevant items from statement
        Select select = (Select) statement;
        PlainSelect body = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems = body.getSelectItems();
        boolean isAllColumns = selectItems.get(0) instanceof AllColumns;
        Expression exp = body.getWhere();
        FromItem mainFromItem = body.getFromItem();
        List<Join> joins = body.getJoins();
        List<OrderByElement> orderByElements = body.getOrderByElements();
        boolean usingAliases = mainFromItem.getAlias() != null;
        Distinct distinct = body.getDistinct();

        // get aliased (or not) from table and store in alias map
        String fromTable;
        if (usingAliases) {
            Catalog.populateAliasMap(mainFromItem);
            fromTable = mainFromItem.getAlias().getName();
        } else {
            fromTable = mainFromItem.toString();
        }

        LogicalOperator subRoot;
        if (joins != null) {
            // store the join tables in the alias map if aliased
            if (usingAliases) {
                Catalog.populateAliasMap((LinkedList<FromItem>)
                        joins.stream().map(j -> j.getRightItem()).collect(Collectors.toCollection(LinkedList::new)));
            }

            // get full list of (aliased) table names to join
            LinkedList<String> joinNames = joins.stream()
                    .map(j -> usingAliases ? j.getRightItem().getAlias().getName() : j.toString())
                    .collect(Collectors.toCollection(LinkedList::new));
            joinNames.addFirst(fromTable);

            // build left deep tree of expressions and operators
            JoinVisitor jv = new JoinVisitor(joinNames);
            Helpers.wrapExpressionWithAnd(exp).accept(jv);
            subRoot = createLeftDeepTree(joinNames, jv);
        } else {
            // if no joins, create a scan/select operator for the from table
            LogicalOperator scanOp = new LogicalScanOperator(fromTable);
            subRoot = exp != null ? new LogicalSelectOperator(scanOp, exp) : scanOp;
        }

        // add if necessary: projection, sorting, duplicate elimination
        if (!isAllColumns) subRoot = new LogicalProjectOperator(subRoot, selectItems);
        subRoot = orderByElements != null || distinct != null
                ? new LogicalSortOperator(subRoot, orderByElements)
                : subRoot;
        logicalOperator = distinct != null ? new LogicalDuplicateEliminationOperator(subRoot) : subRoot;
    }
}
