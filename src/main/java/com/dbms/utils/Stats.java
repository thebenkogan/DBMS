package com.dbms.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that keeps track of the stats of all the tables in the database.
 */
public class Stats {

    /** Maps unaliased table name to its stats */
    private Map<String, TableStats> stats = new HashMap<>();

    /**
     * Constructor for a {@code Stats} object
     * @param bw {@code BufferedWriter} for writing the stats to {@code stats.txt}
     * @param schema schema of our database
     * @throws IOException
     */
    Stats(BufferedWriter bw, Map<String, List<String>> schema) throws IOException {
        for (String table : schema.keySet()) {
            TupleReader tr = new TupleReader(Catalog.pathToTable(table));
            List<String> columnNames = schema.get(table);
            String result = table;
            Map<String, Range> columnInfo = new HashMap<>();
            Integer max[] = new Integer[columnNames.size()];
            Integer min[] = new Integer[columnNames.size()];
            Arrays.fill(max, Integer.MIN_VALUE);
            Arrays.fill(min, Integer.MAX_VALUE);
            List<Integer> row;
            Integer numRows = 0;
            while ((row = tr.nextTuple()) != null) {
                for (int i = 0; i < row.size(); i++) {
                    int element = row.get(i);
                    if (element > max[i]) max[i] = element;
                    if (element < min[i]) min[i] = element;
                }
                numRows++;
            }
            result += " " + numRows + " ";
            for (int i = 0; i < columnNames.size(); i++) {
                String columnName = columnNames.get(i);
                result += String.join(",", columnName, min[i].toString(), max[i].toString());
                if (i < columnNames.size() - 1) result += " ";
                columnInfo.put(columnName, new Range(min[i], max[i]));
            }
            stats.put(table, new TableStats(numRows, columnInfo));
            bw.write(result);
            bw.newLine();
        }
        bw.close();
    }

    /**
     * Generates random tuples with the given {@code stats}
     * @param path destination directory of table with random tuples
     * @throws IOException
     */
    void generate(String path) throws IOException {
        for (String tableName : stats.keySet()) {
            TupleWriter tw = new TupleWriter(String.join(File.separator, path, tableName));
            TableStats ts = stats.get(tableName);
            Set<ColumnName> schema = new HashSet<>();
            for (int i = 0; i < ts.ROWS; i++) {
                List<Integer> rngList = new LinkedList<>();
                for (String column : ts.columns()) {
                    int min = ts.get(column).min;
                    int max = ts.get(column).max;
                    int rng = (int) (Math.random() * (max - min)) + min;
                    rngList.add(rng);
                    schema.add(ColumnName.bundle(tableName, column));
                }
                Tuple t = new Tuple(schema, rngList);
                tw.writeTuple(t);
            }
            tw.close();
        }
    }

    /**
     * Gets the minimum and maximum value of a given table and attribute
     * @param c {@code ColumnName} object that stores the unaliased table name and column name
     * @return {@code Range} object that contains the minimum and maximum of the table and column
     */
    public Range get(ColumnName c) {
        return stats.get(c.TABLE).get(c.COLUMN);
    }

    /**
     * Number of rows in a given table
     * @param tableName the unaliased name of table
     * @return number of rows in that table
     */
    public int numRows(String tableName) {
        return stats.get(tableName).ROWS;
    }

    /**
     * Number of attributes in a given table
     * @param tableName the unaliased name of table
     * @return number of attributes/columns it has
     */
    public int numAttributes(String tableName) {
        return stats.get(tableName).NUM_ATTRIBUTES;
    }
}

/**
 * Class that keeps track of stats of a table: number of rows, attributes, and stats about its columns
 */
class TableStats {
    final int ROWS;
    final int NUM_ATTRIBUTES;
    private final Map<String, Range> COLUMN_STATS;

    TableStats(int rows, Map<String, Range> columnStats) {
        ROWS = rows;
        NUM_ATTRIBUTES = columnStats.size();
        COLUMN_STATS = columnStats;
    }

    Range get(String columnName) {
        return COLUMN_STATS.get(columnName);
    }

    Set<String> columns() {
        return COLUMN_STATS.keySet();
    }
}

/**
 * Wrapper object for ranges.
 */
class Range {
    public final int min;
    public final int max;

    Range(int min, int max) {
        this.min = min;
        this.max = max;
    }
}
