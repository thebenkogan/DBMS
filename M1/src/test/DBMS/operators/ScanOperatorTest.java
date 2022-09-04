package DBMS.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import DBMS.utils.Catalog;

class ScanOperatorTest {
    // Try to init Scan Op with nonexistent table
    @Test
    void testScanOpFail() throws FileNotFoundException {
        assertThrows(FileNotFoundException.class, () -> new ScanOperator("Failure"));
    }

    // reset scan to top of table
    // read up to last tuple in db
    @Test
    void testGetNextTuple() throws IOException {
        Catalog.init("samples/input");
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

}