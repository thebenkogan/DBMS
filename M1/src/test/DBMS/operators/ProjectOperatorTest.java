package DBMS.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import DBMS.utils.Catalog;
import DBMS.utils.Helpers;
import net.sf.jsqlparser.statement.select.SelectItem;

class ProjectOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input");
    }

    ProjectOperator getOperator() throws FileNotFoundException {
        List<SelectItem> items= Helpers
            .selectItemsFromQuery("select Boats.F, Boats.D from Boats");
        ScanOperator scanOp= new ScanOperator("Boats");
        return new ProjectOperator(scanOp, items);
    }

    @Test
    void testGetNextTuple() throws IOException {
        ProjectOperator projectOp= getOperator();

        assertEquals(Arrays.toString(new int[] { 3, 101 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 4, 102 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 2, 104 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 1, 103 }),
            projectOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 8, 107 }),
            projectOp.getNextTuple().toString());
        assertNull(projectOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        ProjectOperator projectOp= getOperator();

        assertEquals(Arrays.toString(new int[] { 3, 101 }),
            projectOp.getNextTuple().toString());

        projectOp.reset();

        assertEquals(Arrays.toString(new int[] { 3, 101 }),
            projectOp.getNextTuple().toString());
    }
}