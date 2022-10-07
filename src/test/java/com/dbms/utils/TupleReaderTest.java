package com.dbms.utils;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Unit tests for the TupleReader */
class TupleReaderTest {
    @Test
    void testTupleReader() throws IOException {
        Catalog.init("samples2/input", null, null);
        TupleReader tr = new TupleReader("Boats");

        assertEquals("[12, 143, 196]", tr.nextTuple().toString());
        assertEquals("[30, 63, 101]", tr.nextTuple().toString());
        assertEquals("[57, 24, 130]", tr.nextTuple().toString());
        assertEquals("[172, 68, 43]", tr.nextTuple().toString());

        tr.reset();

        assertEquals("[12, 143, 196]", tr.nextTuple().toString());
        assertEquals("[30, 63, 101]", tr.nextTuple().toString());
        assertEquals("[57, 24, 130]", tr.nextTuple().toString());
        assertEquals("[172, 68, 43]", tr.nextTuple().toString());

        tr.reset(2);

        assertEquals("[57, 24, 130]", tr.nextTuple().toString());
        assertEquals("[172, 68, 43]", tr.nextTuple().toString());
        assertEquals("[61, 58, 36]", tr.nextTuple().toString());
        assertEquals("[199, 47, 127]", tr.nextTuple().toString());

        tr.reset(995);

        assertEquals("[105, 166, 52]", tr.nextTuple().toString());
        assertEquals("[199, 162, 162]", tr.nextTuple().toString());
        assertEquals("[36, 128, 28]", tr.nextTuple().toString());
        assertEquals("[181, 83, 135]", tr.nextTuple().toString());
        assertEquals("[44, 39, 136]", tr.nextTuple().toString());

        assertNull(tr.nextTuple());
    }
}
