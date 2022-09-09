package DBMS.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import DBMS.utils.Helpers;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

public class JoinVisitor extends ExpressionVisitorBase {

    /** Map from table key to list of expressions referencing those tables */
    private Map<String, List<Expression>> expressions= new HashMap<>();

    /** @param exps list of conjuncts
     * @return expressions joined with ANDs */
    private Expression joinExpressions(List<Expression> exps) {
        if (exps.size() == 0) return null;
        List<String> stringExps= exps.stream().map(e -> e.toString()).collect(Collectors.toList());
        return Helpers.strExpToExp(String.join(" AND ", stringExps));
    }

    /** Constructs a key from two table names to lookup their corresponding expressions.
     * 
     * @param name1 first name
     * @param name2 second name
     * @return expressions key */
    private String namesToKey(String name1, String name2) {
        List<String> tables= Arrays.asList(new String[] { name1, name2 });
        Collections.sort(tables);
        return tables.toString();
    }

    public JoinVisitor(List<String> tableNames) {
        for (int i= 0; i < tableNames.size(); i++ ) {
            expressions.put(tableNames.get(i), new LinkedList<>());
            for (int j= i + 1; j < tableNames.size(); j++ ) {
                expressions.put(namesToKey(tableNames.get(i), tableNames.get(j)),
                    new LinkedList<>());
            }
        }
    }

    /** @param tableName name of table
     * @return expression referencing only tableName */
    public Expression getExpression(String tableName) {
        return joinExpressions(expressions.get(tableName));
    }

    /** @param tableName1 first table name
     * @param tableName2 second table name
     * @return expression referencing tableName1 and tableName2 */
    public Expression getExpression(String tableName1, String tableName2) {
        return joinExpressions(expressions.get(namesToKey(tableName1, tableName2)));
    }

    /** @param tableName name of table to join with all tables in tableNames
     * @param tableNames list of table names to join with tableName
     * @return expression referencing the cross product tableName x tableNames */
    public Expression getExpression(String tableName, List<String> tableNames) {
        List<Expression> exps= new LinkedList<>();
        for (int i= 0; i < tableNames.size(); i++ ) {
            exps.addAll(expressions.get(namesToKey(tableName, tableNames.get(i))));
        }
        return joinExpressions(exps);
    }

    private ExpressionParseVisitor epv= new ExpressionParseVisitor();

    /** Table names referenced in current conjunct. */
    private Set<String> refTables= new HashSet<>();

    @Override
    public void visit(AndExpression exp) {
        refTables.clear();
        Expression right= exp.getRightExpression();
        if (right == null) return;
        right.accept(this);

        if (refTables.isEmpty()) {
            right.accept(epv);
            if (!epv.getBooleanResult()) {
                throw new ArithmeticException("Join condition always fails");
            }
        } else {
            String[] tables= refTables.toArray(String[]::new);
            String key= refTables.size() == 1 ? tables[0] : namesToKey(tables[0], tables[1]);
            expressions.get(key).add(right);
        }

        Expression left= Helpers.wrapExpressionWithAnd(exp.getLeftExpression());
        left.accept(this);
    }

    @Override
    public void visit(EqualsTo exp) {
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
    }

    @Override
    public void visit(GreaterThan exp) {
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
    }

    @Override
    public void visit(GreaterThanEquals exp) {
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
    }

    @Override
    public void visit(MinorThan exp) {
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
    }

    @Override
    public void visit(MinorThanEquals exp) {
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
    }

    @Override
    public void visit(NotEqualsTo exp) {
        exp.getLeftExpression().accept(this);
        exp.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {
        // noop
    }

    @Override
    public void visit(Column col) {
        refTables.add(col.getTable().getName());
    }
}
