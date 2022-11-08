package com.dbms.analytics;

import com.dbms.utils.Attribute;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Handles randomly generating binary data files for benchmarking. */
public class TupleGenerator {

    /**
     * Generates a random dataset to the input path for our DBMS
     * @param tableName is the name of the table, which is also the name of the file
     * @param columns is a list containing all the column names
     * @param maxValue is the maximum value for the RNG
     * @param rows is how many tuples will be in the relation
     */
    static void generate(String tableName, Set<String> columns, int maxValue, int rows) {
        try {
            TupleWriter tw = new TupleWriter(tableName);
            for (int i = 0; i < rows; i++) {
                List<Integer> rngList = new ArrayList<>();
                for (int j = 0; j < columns.size(); j++) {
                    int rng = (int) (Math.random() * maxValue);
                    rngList.add(j, rng);
                }
                Tuple t = new Tuple(mapStringNameToColumnName(tableName, columns), rngList);
                tw.writeTuple(t);
            }
            tw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a {@code Set} of column names only to a {@code Set} of {@code ColumnName} type.
     * @param tableName is the name of the data file
     * @param columns is a set containing the name of the columns
     * @return {@code Set} of {@code ColumnName} type for inputting into {@code Tuple} constructor
     */
    private static Set<Attribute> mapStringNameToColumnName(String tableName, Set<String> columns) {
        Set<Attribute> result = new HashSet<>();
        for (String column : columns) {
            result.add(Attribute.bundle(tableName, column));
        }
        return result;
    }
}
