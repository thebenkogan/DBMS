package DBMS.utils;

import java.io.FileNotFoundException;
import java.util.List;

import DBMS.operators.Operator;
import DBMS.operators.ProjectOperator;
import DBMS.operators.ScanOperator;
import DBMS.operators.SelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class QueryPlanBuilder {
    public Operator operator;

    @SuppressWarnings("unchecked")
    public QueryPlanBuilder(Statement statement) throws FileNotFoundException {
        Select select= (Select) statement;
        PlainSelect body= (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems= body.getSelectItems();

        boolean isAllColumns= selectItems.get(0) instanceof AllColumns;
        Expression expression= body.getWhere();

        Operator scanOp= new ScanOperator(body.getFromItem().toString());
        Operator selectOp= expression != null ? new SelectOperator(scanOp, expression) : scanOp;
        Operator rootOp= !isAllColumns ? new ProjectOperator(selectOp, selectItems) : selectOp;
        operator= rootOp;
    }
}
