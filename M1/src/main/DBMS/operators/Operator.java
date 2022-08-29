package DBMS.operators;

import DBMS.utils.Tuple;

public abstract class Operator {
	public abstract Tuple getNextTuple();

	public abstract void reset();

	public void dump() {
		Tuple next;
		while ((next = getNextTuple()) != null) {
			System.out.println(next.toString());
		}
	}
}
