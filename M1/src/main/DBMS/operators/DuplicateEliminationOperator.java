package DBMS.operators;

import DBMS.utils.Tuple;

public class DuplicateEliminationOperator extends Operator {
    Operator child;

    /** previous Tuple returned */
    Tuple prev= null;

    public DuplicateEliminationOperator(Operator child) {
        this.child= child;
    }

    /** @return next unique Tuple */
    @Override
    public Tuple getNextTuple() {
        Tuple next;
        while ((next= child.getNextTuple()) != null && next.equals(prev)) {}
        prev= next;
        return next;
    }

    @Override
    public void reset() {
        child.reset();
        prev= null;
    }
}
