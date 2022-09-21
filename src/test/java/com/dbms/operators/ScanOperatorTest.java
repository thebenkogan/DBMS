package com.dbms.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Catalog;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the ScanOperator */
class ScanOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null);
    }

    @Test
    void testGetNextTuple() throws IOException {
        ScanOperator scanOp = new ScanOperator("Boats");

        assertEquals("12,143,196", scanOp.getNextTuple().toString());
        assertEquals("30,63,101", scanOp.getNextTuple().toString());
        assertEquals("57,24,130", scanOp.getNextTuple().toString());
        assertEquals("172,68,43", scanOp.getNextTuple().toString());
        assertEquals("61,58,36", scanOp.getNextTuple().toString());
    }

    @Test
    void testReset() throws IOException {
        ScanOperator scanOp = new ScanOperator("Boats");

        assertEquals("12,143,196", scanOp.getNextTuple().toString());
        scanOp.reset();
        assertEquals("12,143,196", scanOp.getNextTuple().toString());
    }
}
