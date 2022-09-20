package com.dbms.operators;

import com.dbms.utils.Catalog;
import com.dbms.utils.Tuple;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** An operator that reads data from file and builds Tuples. */
public class ScanOperator extends Operator {

    /** name (aliased) of underlying table */
    private String name;

    /** reader for the underlying table */
    private BufferedReader reader;

    /**
     * @param tableName name of table to get a reader
     * @return Reader to table
     * @throws FileNotFoundException
     */
    private BufferedReader getReader(String tableName) throws FileNotFoundException {
        return Catalog.getInstance().getTable(tableName);
    }

    /**
     * @param tableName name (aliased) of underlying table
     * @throws FileNotFoundException
     */
    public ScanOperator(String tableName) throws FileNotFoundException {
        name = tableName;
        reader = getReader(Catalog.getRealTableName(tableName));
    }

    /**
     * @return next Tuple from underlying DB file
     */
    @Override
    public Tuple getNextTuple() {
        try {
            String next = reader.readLine();
            if (next == null) return null;
            StringTokenizer data = new StringTokenizer(next, ",");
            int size = data.countTokens();
            List<Integer> nums = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                nums.add(Integer.parseInt(data.nextToken()));
            }
            return new Tuple(name, Catalog.getInstance().getTableColumns(Catalog.getRealTableName(name)), nums);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** closes DB file reader and opens a new one starting at the top of the file */
    @Override
    public void reset() {
        try {
            reader.close();
            reader = getReader(Catalog.getRealTableName(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
