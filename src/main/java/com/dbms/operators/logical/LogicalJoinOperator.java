package com.dbms.operators.logical;

import static com.dbms.utils.Helpers.writeLevel;

import com.dbms.queryplan.PhysicalPlanBuilder;
import com.dbms.queryplan.UnionFindVisitor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.expression.Expression;

/** The logical representation of the join operator, which contains the children, the union find
 * visitor which contains organized join expressions, and the aliased list of table names as orderd
 * in the query. */
public class LogicalJoinOperator extends LogicalOperator {

    /** maps (aliased) table name to child operator (scan or select) */
    public Map<String, LogicalOperator> children;

    /** the UF visitor that parsed this query's expression */
    public UnionFindVisitor uv;

    /** The ordered (aliased) names of joined tables in the query */
    public List<String> tableNames;

    /** Creates a {@code LogicalJoinOperator}
     *
     * @param children list of children after {@code FROM}
     * @param uv       visitor that contains union find and access to join expressions */
    public LogicalJoinOperator(Map<String, LogicalOperator> children, UnionFindVisitor uv, List<String> tableNames) {
        this.children = children;
        this.uv = uv;
        this.tableNames = tableNames;
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
        Expression joinExp = uv.getAllJoinExps();
        String s = String.format("Join[%s]", joinExp != null ? joinExp.toString() : "");
        String uf = uv.unionFind.toString();
        if (!uf.equals("")) s += "\n" + uf;
        pw.println(writeLevel(s, level));
        for (LogicalOperator op : children.values()) op.write(pw, level + 1);
    }
}
