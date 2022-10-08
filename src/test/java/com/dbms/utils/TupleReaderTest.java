package com.dbms.utils;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the TupleReader */
class TupleReaderTest {
    private static TupleReader tr;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples2/input", null, null);
        tr = new TupleReader(Catalog.pathToTable("Boats"));
    }

    @ParameterizedTest(name = "Test {index}: expected {0}; actual {1} ")
    @MethodSource("argumentProvider")
    void testGetNextTuple(String expected, String actual) throws IOException {
        if (expected.equals("null") && actual == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static String resetTuple(int amount) throws IOException {
        if (amount < 0) {
            tr.reset();
        } else {
            tr.reset(amount);
        }
        return tr.nextTuple().toString();
    }

    private static Stream<Arguments> argumentProvider() throws IOException {
        return Stream.of(
                Arguments.of("[12, 143, 196]", tr.nextTuple().toString()),
                Arguments.of("[30, 63, 101]", tr.nextTuple().toString()),
                Arguments.of("[57, 24, 130]", tr.nextTuple().toString()),
                Arguments.of("[172, 68, 43]", tr.nextTuple().toString()),
                Arguments.of("[12, 143, 196]", resetTuple(-1)),
                Arguments.of("[30, 63, 101]", tr.nextTuple().toString()),
                Arguments.of("[57, 24, 130]", tr.nextTuple().toString()),
                Arguments.of("[172, 68, 43]", tr.nextTuple().toString()),
                Arguments.of("[57, 24, 130]", resetTuple(2)),
                Arguments.of("[172, 68, 43]", tr.nextTuple().toString()),
                Arguments.of("[61, 58, 36]", tr.nextTuple().toString()),
                Arguments.of("[199, 47, 127]", tr.nextTuple().toString()),
                Arguments.of("[105, 166, 52]", resetTuple(995)),
                Arguments.of("[199, 162, 162]", tr.nextTuple().toString()),
                Arguments.of("[36, 128, 28]", tr.nextTuple().toString()),
                Arguments.of("[181, 83, 135]", tr.nextTuple().toString()),
                Arguments.of("[44, 39, 136]", tr.nextTuple().toString()),
                Arguments.of("null", tr.nextTuple()));
    }
}
