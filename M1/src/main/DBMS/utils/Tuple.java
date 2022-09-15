package DBMS.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Tuple {
    private static final String delimiter= "/";
    /** Maps (aliased) table.column key to value in row. Insertion order represents column
     * ordering. */
    private LinkedHashMap<String, Integer> row;

    /** Constructs a key to look up the corresponding value in the row */
    public static String key(String tableName, String columnName) {
        return tableName + delimiter + columnName;
    }

    /** Creates a new tuple for the items in columns. Requires columns and items are of the same
     * size.
     *
     * @param tableName the name (alias) of the table associated with each column
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

    /** @param name key
     * @return table name (aliased) */
    public static String getTableName(String name) {
        return name.split(delimiter)[0];
    }

    /** @param name key
     * @return column name */
    public static String getColumnName(String name) {
        return name.split(delimiter)[1];
    }

    /** @return set of table/column keys */
    public Set<String> getTableColumnNames() {
        return row.keySet();
    }

    /** Projects this tuple to those in columns. Requires: columns is a subset of the columns in
     * this tuple.
     * 
     * @param tableNames  projected (aliased) table names
     * @param columnNames projected column names */
    public void project(List<String> tableNames, List<String> columnNames) {
        Integer[] data= new Integer[columnNames.size()];
        for (int i= 0; i < columnNames.size(); i++ ) {
            data[i]= row.get(key(tableNames.get(i), columnNames.get(i)));
        }

        row.clear();
        for (int i= 0; i < data.length; i++ ) {
            row.put(key(tableNames.get(i), columnNames.get(i)), data[i]);
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
