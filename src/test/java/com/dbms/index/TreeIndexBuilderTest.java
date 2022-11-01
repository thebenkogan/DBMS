package com.dbms.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Catalog;
import com.dbms.utils.ColumnName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for testing the serializing functionality of our {@code TreeIndexBuilder}.
 */
public class TreeIndexBuilderTest {
    private static final String expectedPath = "expected/indexes";
    private static final String delimiter = ".";
    private static List<Arguments> testCases;

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("input/index/config.txt");
        TreeIndexTestSet t = new TreeIndexTestSet(Catalog.INDEXES, expectedPath, delimiter);
        testCases = t.indexInfo();
    }

    @ParameterizedTest(name = "Serializing Index Tree Test {index}: {2}")
    @MethodSource("argumentProvider")
    void serializeTest(String expected, String actual, String name) throws IOException {
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(testCases.toArray(new Arguments[testCases.size()]));
    }
}

class TreeIndexTestSet {
    private List<Arguments> arguments = new LinkedList<>();

    TreeIndexTestSet(Map<ColumnName, Index> indexInfo, String expectedPath, String delimiter) throws IOException {
        for (ColumnName c : indexInfo.keySet()) {
            TreeIndexBuilder.serialize(indexInfo.get(c));
            String actual = new String(Files.readAllBytes(Paths.get(Catalog.pathToIndexFile(c))));
            String expected = new String(Files.readAllBytes(Paths.get(expectedPath, c.TABLE + delimiter + c.COLUMN)));
            arguments.add(Arguments.of(expected, actual, c.TABLE + delimiter + c.COLUMN));
        }
    }

    public List<Arguments> indexInfo() {
        return arguments;
    }
}
