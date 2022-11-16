package com.dbms.operators.logical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.queryplan.PhysicalPlanBuilder;
import com.dbms.queryplan.UnionFindVisitor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

/** The logical representation of the join operator, which contains the expression, left child
 * operator, and right child operator which we need to construct the physical operator */
public class LogicalJoinOperator extends LogicalOperator {

    public LogicalOperator left;
    public LogicalOperator right;
    public Expression exp;
    public String innerTableName;
    public List<LogicalOperator> children;
    public UnionFindVisitor uv;

    /** @param left       left child operator
     * @param right          right child operator
     * @param innerTableName aliased inner table name
     * @param exp            join condition, null if none */
    public LogicalJoinOperator(LogicalOperator left, LogicalOperator right, String innerTableName, Expression exp) {
        this.left = left;
        this.right = right;
        this.exp = exp;
        this.innerTableName = innerTableName;
    }

    /** Creates a {@code LogicalJoinOperator}
     *
     * @param children list of children after {@code FROM}
     * @param uv       visitor that contains union find and access to join expressions */
    public LogicalJoinOperator(List<LogicalOperator> children, UnionFindVisitor uv) {
        this.children = children;
        this.uv = uv;
    }

    /** @param physicalPlan visitor which converts logical to physical operator */
    @Override
    public void accept(PhysicalPlanBuilder physicalPlan) {
        try {
            physicalPlan.visit(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(PrintWriter pw, int level) {
        String s = "TODO: Join";
        // TODO: Uncomment this when new join operator is integrated
        // Expression joinExp = uv.getAllJoinExps();
        // String s = String.format("Join[%s]\n", joinExp != null ? joinExp.toString() : "") +
        // uv.unionFind.toString();
        // for (LogicalOperator op : children) op.write(pw, level + 1);
        pw.println(writeLevel(s, level));
        left.write(pw, level + 1);
        right.write(pw, level + 1);
    }
}
