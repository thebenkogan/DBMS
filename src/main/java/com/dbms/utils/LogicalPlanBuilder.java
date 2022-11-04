package com.dbms.utils;

import com.dbms.operators.logical.LogicalDuplicateEliminationOperator;
import com.dbms.operators.logical.LogicalJoinOperator;
import com.dbms.operators.logical.LogicalOperator;
import com.dbms.operators.logical.LogicalProjectOperator;
import com.dbms.operators.logical.LogicalScanOperator;
import com.dbms.operators.logical.LogicalSelectOperator;
import com.dbms.operators.logical.LogicalSortOperator;
import com.dbms.visitors.JoinVisitor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
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

/** Builds a query plan from a Statement and stores the root operator. */
public class LogicalPlanBuilder {
    public LogicalOperator root;

    /** @param tableName (aliased) table name for the scan
     * @param exp       select expression, null if not filtered
     * @return scan operator if expression is null, otherwise a select operator */
    private LogicalOperator createScanAndSelect(String tableName, Expression exp) {
        LogicalOperator op = new LogicalScanOperator(tableName);
        if (exp != null) op = new LogicalSelectOperator(op, exp);
        return op;
    }

    /** @param tables table names (aliased), removes first
     * @param jv     join visitor
     * @return scan/select operator from first table name in tables
     * @throws FileNotFoundException */
    private LogicalOperator getNextOperator(List<String> tables, JoinVisitor jv) throws FileNotFoundException {
        String tableName = tables.remove(0);
        Expression exp = jv.getExpression(tableName);
        return createScanAndSelect(tableName, exp);
    }

    /** Creates left-deep tree for join condition parsing
     *
     * @param tables tables to place in tree. If aliases exist, then tables consists solely of
     *               aliases. Otherwise, tables contains actual table names; tables.length > 1
     * @param jv     the {@code JoinVisitor} for obtaining the join conditions
     * @return a {@code LogicalJoinOperator} containing the left-deep tree
     * @throws FileNotFoundException */
    private LogicalJoinOperator createLeftDeepTree(List<String> tables, JoinVisitor jv) throws FileNotFoundException {
        String leftName = tables.get(0);
        LogicalOperator leftOp = getNextOperator(tables, jv);

        String rightName = tables.get(0);
        LogicalOperator rightOp = getNextOperator(tables, jv);

        LogicalJoinOperator joinOp =
                new LogicalJoinOperator(leftOp, rightOp, rightName, jv.getExpression(leftName, rightName));

        List<String> seenNames = new ArrayList<>();
        seenNames.add(leftName);
        seenNames.add(rightName);

        while (tables.size() > 0) {
            String nextName = tables.get(0);
            LogicalOperator nextScan = getNextOperator(tables, jv);

            LogicalJoinOperator nextOp =
                    new LogicalJoinOperator(joinOp, nextScan, nextName, jv.getExpression(nextName, seenNames));
            seenNames.add(nextName);
            joinOp = nextOp;
        }

        return joinOp;
    }

    /** Populates Catalog alias map if tables use aliases.
     *
     * @param from  from table
     * @param joins join tables, null if not a join
     * @return list of (aliased) table names in the order of tables provided */
    private List<String> extractNames(FromItem from, List<Join> joins) {
        LinkedList<FromItem> tables = joins != null
                ? joins.stream().map(j -> j.getRightItem()).collect(Collectors.toCollection(LinkedList::new))
                : new LinkedList<>();
        tables.addFirst(from);
        return Catalog.populateAliasMap(tables);
    }

    /** @param statement Statement for which to build a query plan and create a root operator
     * @throws FileNotFoundException */
    public LogicalPlanBuilder(Statement statement) throws FileNotFoundException {
        // extract relevant items from statement
        Select select = (Select) statement;
        PlainSelect body = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems = body.getSelectItems();
        boolean isAllColumns = selectItems.get(0) instanceof AllColumns;
        Expression exp = body.getWhere();
        FromItem mainFromItem = body.getFromItem();
        List<Join> joins = body.getJoins();
        List<String> tableNames = extractNames(mainFromItem, joins);
        List<OrderByElement> orderByElements = body.getOrderByElements();
        Distinct distinct = body.getDistinct();

        LogicalOperator subRoot;
        if (joins != null) {
            // build left deep tree of expressions and operators
            JoinVisitor jv = new JoinVisitor(tableNames);
            Helpers.wrapExpressionWithAnd(exp).accept(jv);
            subRoot = createLeftDeepTree(tableNames, jv);
        } else {
            // if no joins, create a scan/select operator for the from table
            subRoot = createScanAndSelect(tableNames.get(0), exp);
        }

        // add if necessary: projection, sorting, duplicate elimination
        if (!isAllColumns) subRoot = new LogicalProjectOperator(subRoot, selectItems);
        subRoot = orderByElements != null || distinct != null
                ? new LogicalSortOperator(subRoot, orderByElements)
                : subRoot;
        root = distinct != null ? new LogicalDuplicateEliminationOperator(subRoot) : subRoot;
    }
}
