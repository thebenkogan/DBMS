package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.operators.physical.ProjectOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SelectOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the ProjectOperator */
class ProjectOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null);
    }

    ProjectOperator getOperator() throws FileNotFoundException {
        List<SelectItem> items = Helpers.strSelectItemsToSelectItems("Boats.F", "Boats.D");
        Expression exp = Helpers.strExpToExp("Boats.D = 32 AND Boats.E != 100");
        ScanOperator scanOp = new ScanOperator("Boats");
        SelectOperator selectOP = new SelectOperator(scanOp, exp);
        return new ProjectOperator(selectOP, items);
    }

    @Test
    void testGetNextTuple() throws IOException {
        ProjectOperator projectOp = getOperator();

        assertEquals("191,32", projectOp.getNextTuple().toString());
        assertEquals("178,32", projectOp.getNextTuple().toString());
        assertEquals("66,32", projectOp.getNextTuple().toString());
        assertEquals("84,32", projectOp.getNextTuple().toString());
        assertEquals("129,32", projectOp.getNextTuple().toString());
        assertEquals("161,32", projectOp.getNextTuple().toString());
        assertEquals("122,32", projectOp.getNextTuple().toString());
        assertEquals("138,32", projectOp.getNextTuple().toString());
        assertNull(projectOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        ProjectOperator projectOp = getOperator();

        assertEquals("191,32", projectOp.getNextTuple().toString());
        projectOp.reset();
        assertEquals("191,32", projectOp.getNextTuple().toString());
    }
}
