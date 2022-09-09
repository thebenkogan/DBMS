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

class JoinOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input");
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
        JoinOperator projectOp= getOperator();

        assertEquals(Arrays.toString(new int[] { 1, 200, 50, 1, 101 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 1, 200, 50, 1, 102 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 1, 200, 50, 1, 103 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 2, 200, 200, 2, 101 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 3, 100, 105, 3, 102 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 4, 100, 50, 4, 104 }),
            projectOp.getNextTuple().toString());
        assertNull(projectOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        JoinOperator projectOp= getOperator();

        assertEquals(Arrays.toString(new int[] { 1, 200, 50, 1, 101 }),
            projectOp.getNextTuple().toString());

        projectOp.reset();

        assertEquals(Arrays.toString(new int[] { 1, 200, 50, 1, 101 }),
            projectOp.getNextTuple().toString());
    }
}