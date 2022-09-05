package DBMS.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import DBMS.utils.Catalog;

class ScanOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input");
    }

    @Test
    void testFail() throws IOException {
        assertThrows(FileNotFoundException.class, () -> new ScanOperator("Failure"));
    }

    @Test
    void testGetNextTuple() throws IOException {
        ScanOperator scanOp= new ScanOperator("Boats");

        assertEquals(Arrays.toString(new int[] { 101, 2, 3 }),
            scanOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 102, 3, 4 }),
            scanOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 104, 104, 2 }),
            scanOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 103, 1, 1 }),
            scanOp.getNextTuple().toString());
        assertEquals(Arrays.toString(new int[] { 107, 2, 8 }),
            scanOp.getNextTuple().toString());
        assertNull(scanOp.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        ScanOperator scanOp= new ScanOperator("Boats");

        assertEquals(Arrays.toString(new int[] { 101, 2, 3 }),
            scanOp.getNextTuple().toString());

        scanOp.reset();

        assertEquals(Arrays.toString(new int[] { 101, 2, 3 }),
            scanOp.getNextTuple().toString());
    }
}