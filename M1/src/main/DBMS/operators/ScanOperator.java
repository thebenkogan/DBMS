package DBMS.operators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import DBMS.utils.Catalog;
import DBMS.utils.Tuple;

public class ScanOperator extends Operator {

    /** name of underlying table */
    private String name;

    /** reader for the underlying table */
    private BufferedReader reader;

    /** @param tableName name of table to get a reader
     * @return Reader to table
     * @throws FileNotFoundException */
    private BufferedReader getReader(String tableName) throws FileNotFoundException {
        return Catalog.getInstance().getTable(tableName);
    }

    public ScanOperator(String tableName) throws FileNotFoundException {
        name= tableName;
        reader= getReader(tableName);
    }

    @Override
    public Tuple getNextTuple() {
        try {
            String next= reader.readLine();
            if (next == null) return null;
            StringTokenizer data= new StringTokenizer(next, ",");
            int size= data.countTokens();
            List<Integer> nums= new ArrayList<>(size);
            for (int i= 0; i < size; i++ ) {
                nums.add(Integer.parseInt(data.nextToken()));
            }
            return new Tuple(Catalog.getInstance().getTableColumns(name), nums);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {
        try {
            reader.close();
            reader= getReader(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
