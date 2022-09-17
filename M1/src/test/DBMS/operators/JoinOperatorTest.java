package DBMS.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import DBMS.utils.Catalog;
import DBMS.utils.Helpers;
import DBMS.visitors.JoinVisitor;
import net.sf.jsqlparser.expression.Expression;

/** Unit tests for the JoinOperator */
class JoinOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input", null);
    }

    JoinOperator getOperator() throws FileNotFoundException {
        ScanOperator scanOp1= new ScanOperator("Sailors");
        ScanOperator scanOp2= new ScanOperator("Reserves");
        JoinVisitor jv= new JoinVisitor(Arrays.asList(new String[] { "Sailors", "Reserves" }));
        Expression exp= Helpers.strExpToExp("Sailors.A = Reserves.G");
        exp.accept(jv);
        return new JoinOperator(scanOp1, scanOp2, exp);
    }

    @Test
    void testGetNextTuple() throws IOException {
        JoinOperator joinOp= getOperator();

        assertEquals("1,200,50,1,101", joinOp.getNextTuple().toString());
        assertEquals("1,200,50,1,102", joinOp.getNextTuple().toString());
        assertEquals("1,200,50,1,103", joinOp.getNextTuple().toString());
        assertEquals("2,200,200,2,101", joinOp.getNextTuple().toString());
        assertEquals("3,100,105,3,102", joinOp.getNextTuple().toString());
        assertEquals("4,100,50,4,104", joinOp.getNextTuple().toString());
        assertNull(joinOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        JoinOperator joinOp= getOperator();

        assertEquals("1,200,50,1,101", joinOp.getNextTuple().toString());
        joinOp.reset();
        assertEquals("1,200,50,1,101", joinOp.getNextTuple().toString());

    }
}