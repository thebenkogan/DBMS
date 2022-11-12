package com.dbms.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.utils.Catalog;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the TreeDeserializer */
class TreeDeserializerTest {
    private static TreeDeserializer boatsTd;
    private static TreeDeserializer sailorsTd;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/index/config.txt");
        Index boatsIndex = Catalog.INDEXES.get("Boats").get(0);
        Index sailorsIndex = Catalog.INDEXES.get("Sailors").get(0);
        boatsTd = new TreeDeserializer(boatsIndex);
        sailorsTd = new TreeDeserializer(sailorsIndex);
    }

    @ParameterizedTest(name = "Next Tuple Test {index}: expected {0}; actual {1} ")
    @MethodSource({"boatsNextTupleProvider", "sailorsNextTupleProvider"})
    void testGetNextTuple(String expected, String actual) throws IOException {
        if (expected.equals("null") && actual == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Stream<Arguments> boatsNextTupleProvider() throws IOException {
        List<Integer> first = boatsTd.getFirstTupleAtKey(7774);
        return Stream.of(
                Arguments.of("[2181, 7774, 2664]", first.toString()),
                Arguments.of("[2488, 7774, 771]", boatsTd.getNextTuple().toString()),
                Arguments.of("[1038, 7774, 5865]", boatsTd.getNextTuple().toString()),
                Arguments.of("[6306, 7777, 7314]", boatsTd.getNextTuple().toString()));
    }

    private static Stream<Arguments> sailorsNextTupleProvider() throws IOException {
        List<Integer> first = sailorsTd.getFirstTupleAtKey(2080);
        return Stream.of(
                Arguments.of("[2080, 268, 5458]", first.toString()),
                Arguments.of("[2080, 4006, 2166]", sailorsTd.getNextTuple().toString()),
                Arguments.of("[2080, 9730, 9417]", sailorsTd.getNextTuple().toString()),
                Arguments.of("[2080, 9746, 8361]", sailorsTd.getNextTuple().toString()),
                Arguments.of("[2082, 832, 4626]", sailorsTd.getNextTuple().toString()));
    }
}
