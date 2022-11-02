package com.dbms.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dbms.index.Index;
import com.dbms.operators.physical.IndexScanOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.ColumnName;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for the TreeDeserializer */
class IndexScanOperatorTest {
    private static Index boatsIndex;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/index/config.txt");
        boatsIndex = Catalog.INDEXES.get(ColumnName.bundle("Boats", "E"));
    }

    @ParameterizedTest(name = "Next Tuple Test {index}: expected {0}; actual {1} ")
    @MethodSource("nextTupleProvider")
    void testGetNextTuple(String expected, String actual) throws IOException {
        if (expected.equals("null") && actual == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Stream<Arguments> nextTupleProvider() throws IOException {
        List<Arguments> args = new LinkedList<>();

        IndexScanOperator lowAndHigh = new IndexScanOperator(boatsIndex, 7774, 7777);
        args.add(Arguments.of("2181,7774,2664", lowAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("2488,7774,771", lowAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("1038,7774,5865", lowAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("6306,7777,7314", lowAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("null", lowAndHigh.getNextTuple()));

        IndexScanOperator lowAndUnbound = new IndexScanOperator(boatsIndex, 9998, null);
        args.add(Arguments.of("6437,9998,2317", lowAndUnbound.getNextTuple().toString()));
        args.add(Arguments.of("8439,9998,6378", lowAndUnbound.getNextTuple().toString()));
        args.add(Arguments.of("4461,9999,4000", lowAndUnbound.getNextTuple().toString()));
        args.add(Arguments.of("5317,9999,266", lowAndUnbound.getNextTuple().toString()));
        args.add(Arguments.of("null", lowAndUnbound.getNextTuple()));

        IndexScanOperator unboundAndHigh = new IndexScanOperator(boatsIndex, null, 5);
        args.add(Arguments.of("9206,4,5488", unboundAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("7775,4,6175", unboundAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("9076,4,8209", unboundAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("1803,5,8850", unboundAndHigh.getNextTuple().toString()));
        args.add(Arguments.of("1109,5,9486", unboundAndHigh.getNextTuple().toString()));

        IndexScanOperator bothNotFound = new IndexScanOperator(boatsIndex, 1, 7);
        args.add(Arguments.of("9206,4,5488", bothNotFound.getNextTuple().toString()));
        args.add(Arguments.of("7775,4,6175", bothNotFound.getNextTuple().toString()));
        args.add(Arguments.of("9076,4,8209", bothNotFound.getNextTuple().toString()));
        args.add(Arguments.of("1803,5,8850", bothNotFound.getNextTuple().toString()));
        args.add(Arguments.of("1109,5,9486", bothNotFound.getNextTuple().toString()));
        args.add(Arguments.of("4225,6,9275", bothNotFound.getNextTuple().toString()));

        return args.stream();
    }
}
