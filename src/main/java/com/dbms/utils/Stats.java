package com.dbms.utils;

import com.dbms.index.Index;
import com.dbms.index.TreeDeserializer;
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

/** Class that keeps track of the stats of all the tables in the database. */
public class Stats {

    /** Bytes per page */
    private static final int PAGE_SIZE = 4096;

    /** Maps unaliased table name to its stats */
    private Map<String, TableStats> stats = new HashMap<>();

    /** Constructor for a {@code Stats} object
     *
     * @param bw     {@code BufferedWriter} for writing the stats to {@code stats.txt}
     * @param schema schema of our database
     * @throws IOException */
    Stats(BufferedWriter bw, Map<String, List<Attribute>> schema) throws IOException {
        for (String table : schema.keySet()) {
            TupleReader tr = new TupleReader(Catalog.pathToTable(table));
            List<Attribute> columnNames = schema.get(table);
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
                String columnName = columnNames.get(i).COLUMN;
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

    /** @param tableName (unaliased) table name
     * @return number of pages in the relation */
    private double getNumPages(String tableName) {
        TableStats tstats = stats.get(tableName);
        return tstats.ROWS * tstats.NUM_ATTRIBUTES * 4.0 / PAGE_SIZE;
    }

    /** @param tableName (unaliased) table name
     * @return I/O cost of scanning the table (number of pages) */
    public double getTableScanCost(String tableName) {
        return getNumPages(tableName);
    }

    /** @param i  the index for which to calculate the cost
     * @param extent the extent of values being selected
     * @return I/O cost of using the index to select the values
     * @throws IOException */
    public double getTableIndexCost(Index i, int extent) throws IOException {
        TreeDeserializer td = new TreeDeserializer(i);
        int numLeaves = td.numLeaves;
        td.close();
        TableStats tstats = stats.get(i.name.TABLE);
        double reductionFactor = extent * 1.0 / tstats.get(i.name.COLUMN).extent();
        if (i.isClustered) {
            return 3 + getNumPages(i.name.TABLE) * reductionFactor;
        } else {
            return 3 + numLeaves * reductionFactor + extent * reductionFactor;
        }
    }

    /** Gets the range of values for a given table and attribute
     *
     * @param a {@code Attribute} object that stores the unaliased table name and column name
     * @return {@code Range} object that contains the minimum and maximum of the table and column */
    public Range getAttributeRange(Attribute a) {
        return stats.get(a.TABLE).get(a.COLUMN);
    }

    /** Number of rows in a given table
     *
     * @param tableName the unaliased name of table
     * @return number of rows in that table */
    public int numRows(String tableName) {
        return stats.get(tableName).ROWS;
    }

    /** Number of attributes in a given table
     *
     * @param tableName the unaliased name of table
     * @return number of attributes/columns it has */
    public int numAttributes(String tableName) {
        return stats.get(tableName).NUM_ATTRIBUTES;
    }

    /** Generates random tuples with the given {@code stats}
     *
     * @param path destination directory of table with random tuples
     * @throws IOException */
    void generate(String path) throws IOException {
        for (String tableName : stats.keySet()) {
            TupleWriter tw = new TupleWriter(String.join(File.separator, path, tableName));
            TableStats ts = stats.get(tableName);
            Set<Attribute> schema = new HashSet<>();
            for (int i = 0; i < ts.ROWS; i++) {
                List<Integer> rngList = new LinkedList<>();
                for (String column : ts.columns()) {
                    int min = ts.get(column).min;
                    int max = ts.get(column).max;
                    int rng = (int) (Math.random() * (max - min)) + min;
                    rngList.add(rng);
                    schema.add(Attribute.bundle(tableName, column));
                }
                Tuple t = new Tuple(schema, rngList);
                tw.writeTuple(t);
            }
            tw.close();
        }
    }
}

/** Class that keeps track of stats of a table: number of rows, attributes, and stats about its
 * columns */
class TableStats {
    /** number of rows in the table */
    final int ROWS;

    /** number of attributes in a tuple */
    final int NUM_ATTRIBUTES;

    /** range of each table column */
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
