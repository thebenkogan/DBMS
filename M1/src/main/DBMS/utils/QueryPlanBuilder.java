package DBMS.utils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import DBMS.operators.JoinOperator;
import DBMS.operators.Operator;
import DBMS.operators.ProjectOperator;
import DBMS.operators.ScanOperator;
import DBMS.operators.SelectOperator;
import DBMS.operators.SortOperator;
import DBMS.visitors.JoinVisitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class QueryPlanBuilder {
    public Operator operator;

    /** @param tables table names (aliased), removes first
     * @param jv     join visitor
     * @return scan/select operator from first table name in tables
     * @throws FileNotFoundException */
    private Operator getNextOperator(List<String> tables, JoinVisitor jv)
        throws FileNotFoundException {
        String tableName= tables.remove(0);
        Expression exp= jv.getExpression(tableName);

        // Scan Operator must use the real table name, since it directly
        // accesses the table
        Operator op= new ScanOperator(tableName);
        if (exp != null)
            op= new SelectOperator(op, exp);
        return op;
    }

    /** Requires: tables.length > 1
     *
     * @param tables tables to place in tree. If aliases exist, then tables consists solely of
     *               aliases. Otherwise, tables contains actual table names
     * @throws FileNotFoundException */
    private JoinOperator createLeftDeepTree(List<String> tables, JoinVisitor jv)
        throws FileNotFoundException {

        String leftName= tables.get(0);
        Operator leftOp= getNextOperator(tables, jv);

        String rightName= tables.get(0);
        Operator rightOp= getNextOperator(tables, jv);

        JoinOperator joinOp= new JoinOperator(leftOp, rightOp,
            jv.getExpression(leftName, rightName));

        List<String> seenNames= new ArrayList<>();
        seenNames.add(leftName);
        seenNames.add(rightName);

        while (tables.size() > 0) {
            String nextName= tables.get(0);
            Operator nextScan= getNextOperator(tables, jv);

            JoinOperator nextOp= new JoinOperator(joinOp, nextScan,
                jv.getExpression(nextName, seenNames));
            seenNames.add(nextName);
            joinOp= nextOp;
        }

        return joinOp;
    }

    @SuppressWarnings("unchecked")
    public QueryPlanBuilder(Statement statement)
        throws FileNotFoundException {
        Select select= (Select) statement;
        PlainSelect body= (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems= body.getSelectItems();
        boolean isAllColumns= selectItems.get(0) instanceof AllColumns;
        Expression exp= body.getWhere();
        FromItem mainFromItem= body.getFromItem();
        List<Join> joins= body.getJoins();
        List<OrderByElement> orderByElements= body.getOrderByElements();
        boolean usingAliases= mainFromItem.getAlias() != null;

        String fromTable;
        // set fromTable to be alias if it exists, and table name otherwise
        if (usingAliases) {
            // add first alias to alias map
            Catalog.populateAliasMap(mainFromItem);
            fromTable= mainFromItem.getAlias();
        } else {
            fromTable= mainFromItem.toString();
        }

        Operator subRoot;
        if (joins != null) {
            if (usingAliases) {
                // populate alias map with the rest of the aliases
                Catalog.populateAliasMap(joins.stream()
                    .map(j -> j.getRightItem())
                    .collect(Collectors.toCollection(LinkedList::new)));
            }

            // if there are aliases, joinNames consists of the aliases
            // of joins. otherwise, joinNames consists of the table names
            LinkedList<String> joinNames= joins.stream()
                .map(j -> usingAliases ? ((Table) j.getRightItem()).getAlias() : j.toString())
                .collect(Collectors.toCollection(LinkedList::new));
            joinNames.addFirst(fromTable);

            JoinVisitor jv= new JoinVisitor(joinNames);
            Helpers.wrapExpressionWithAnd(exp).accept(jv);
            subRoot= createLeftDeepTree(joinNames, jv);
        } else {
            Operator scanOp= new ScanOperator(fromTable);
            subRoot= exp != null ? new SelectOperator(scanOp, exp) : scanOp;
        }
        if (!isAllColumns) subRoot= new ProjectOperator(subRoot, selectItems);
        operator= orderByElements != null ? new SortOperator(subRoot, orderByElements) : subRoot;
    }
}
