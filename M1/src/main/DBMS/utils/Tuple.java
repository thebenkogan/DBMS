package DBMS.utils;

import java.util.LinkedHashMap;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

public class Tuple {
    /** Maps table.column key to value in row. Insertion order represents column ordering. */
    private LinkedHashMap<String, Integer> row;

    /** Constructs a key to look up the corresponding value in the row */
    private String key(String tableName, String columnName) {
        return tableName + "/" + columnName;
    }

    /** Creates a new tuple for the items in columns. Requires columns and items are of the same
     * size.
     *
     * @param tableName the name of the table associated with each column
     * @param columns   the name of each column in the table
     * @param data      the row of data in the table */
    public Tuple(String tableName, List<String> columns, List<Integer> data) {
        row= new LinkedHashMap<>();
        for (int i= 0; i < data.size(); i++ ) {
            row.put(key(tableName, columns.get(i)), data.get(i));
        }
    }

    private Tuple(LinkedHashMap<String, Integer> row) {
        this.row= row;
    }

    /** @param columnName name of table column
     * @return value in the column */
    public int get(String tableName, String columnName) {
        return row.get(key(tableName, columnName));
    }

    /** Projects this tuple to those in columns. Requires: columns is a subset of the columns in
     * this tuple.
     *
     * @param columns projected columns */
    public void project(List<Column> columns) {
        Integer[] data= columns.stream()
            .map(c -> row.get(key(c.getTable().getName(), c.getColumnName())))
            .toArray(Integer[]::new);
        row.clear();
        for (int i= 0; i < data.length; i++ ) {
            Column col= columns.get(i);
            row.put(key(col.getTable().getName(), col.getColumnName()), data[i]);
        }
    }

    @Override
    public String toString() {
        return row.values().toString().replaceAll("\\s|\\[|\\]", "");
    }

    /** Merges left and right into a new Tuple. Merged columnOrder is the concatenation of
     * left.columnOrder and right.columnOrder.
     * 
     * @param left  left tuple
     * @param right right tuple */
    public static Tuple mergeTuples(Tuple left, Tuple right) {
        LinkedHashMap<String, Integer> row= new LinkedHashMap<>();
        left.row.forEach((key, value) -> row.put(key, value));
        right.row.forEach((key, value) -> row.put(key, value));
        return new Tuple(row);
    }
}
