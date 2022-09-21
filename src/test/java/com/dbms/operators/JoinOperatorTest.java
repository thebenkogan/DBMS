package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import com.dbms.visitors.JoinVisitor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the JoinOperator */
class JoinOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null);
    }

    JoinOperator getOperator() throws FileNotFoundException {
        ScanOperator scanOp1 = new ScanOperator("Sailors");
        ScanOperator scanOp2 = new ScanOperator("Reserves");
        JoinVisitor jv = new JoinVisitor(Arrays.asList(new String[] {"Sailors", "Reserves"}));
        Expression exp = Helpers.strExpToExp("Sailors.A = Reserves.G AND Reserves.H = 23 AND Sailors.A = 106");
        exp.accept(jv);
        return new JoinOperator(scanOp1, scanOp2, exp);
    }

    @Test
    void testGetNextTuple() throws IOException {
        JoinOperator joinOp = getOperator();

        assertEquals("106,39,42,106,23", joinOp.getNextTuple().toString());
        assertEquals("106,102,163,106,23", joinOp.getNextTuple().toString());
        assertEquals("106,71,138,106,23", joinOp.getNextTuple().toString());
        assertEquals("106,99,118,106,23", joinOp.getNextTuple().toString());
        assertEquals("106,59,191,106,23", joinOp.getNextTuple().toString());
        assertEquals("106,142,77,106,23", joinOp.getNextTuple().toString());
        assertNull(joinOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        JoinOperator joinOp = getOperator();

        assertEquals("106,39,42,106,23", joinOp.getNextTuple().toString());
        joinOp.reset();
        assertEquals("106,39,42,106,23", joinOp.getNextTuple().toString());
    }
}
