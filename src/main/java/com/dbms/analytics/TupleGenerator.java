package com.dbms.analytics;

import com.dbms.utils.Tuple;
import com.dbms.utils.TupleWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TupleGenerator {

    /**
     * {@code ROWS} is how many rows the tables will contain
     */
    private static final int ROWS = 5000;

    /**
     * Generates a random dataset to the input path for our DBMS
     * @param tableName is the name of the table, which is also the name of the file
     * @param columns is a list containing all the column names
     */
    public static void generate(String tableName, Set<String> columns) {
        try {
            TupleWriter tw = new TupleWriter(tableName);
            for (int i = 0; i < ROWS; i++) {
                List<Integer> rngList = new ArrayList<>();
                for (int j = 0; j < columns.size(); j++) {
                    int rng = (int) (Math.random() * 100);
                    rngList.add(j, rng);
                }
                Tuple t = new Tuple(columns, rngList);
                tw.writeTuple(t);
            }
            tw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
