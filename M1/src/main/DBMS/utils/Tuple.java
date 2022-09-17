package DBMS.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/** The representation of a row in a table. */
public class Tuple {
    /** Maps (aliased) table.column key to value in row. Insertion order represents column
     * ordering. */
    private LinkedHashMap<String, Integer> row;

    /** string that separates the table name from the column name in the row map */
    private static final String SEPARATOR= "/";

    /** @param tableName table name
     * @param columnName column name
     * @return the key to look up the corresponding value in the row */
    public static String key(String tableName, String columnName) {
        return tableName + SEPARATOR + columnName;
    }

    /** Creates a new Tuple for the table with columns and data.
     *
     * @param tableName the name (alias) of the table associated with each column
     * @param columns   the name of each column in the table
     * @param data      the row of data in the table; same size as columns */
    public Tuple(String tableName, List<String> columns, List<Integer> data) {
        row= new LinkedHashMap<>();
        for (int i= 0; i < data.size(); i++ ) {
            row.put(key(tableName, columns.get(i)), data.get(i));
        }
    }

    /** Creates a new Tuple from a derived row of an old Tuple
     * 
     * @param row row map */
    private Tuple(LinkedHashMap<String, Integer> row) {
        this.row= row;
    }

    /** @param tableName name of table name
     * @param columnName name of table column
     * @return value in the column */
    public int get(String tableName, String columnName) {
        return row.get(key(tableName, columnName));
    }

    /** @param name key separated with SEPARATOR
     * @return table name (aliased) */
    public static String getTableName(String name) {
        return name.split(SEPARATOR)[0];
    }

    /** @param name key separated with SEPARATOR
     * @return column name */
    public static String getColumnName(String name) {
        return name.split(SEPARATOR)[1];
    }

    /** @return set of table.column row keys */
    public Set<String> getTableColumnNames() {
        return row.keySet();
    }

    /** Projects this tuple to those in columns.
     * 
     * @param tableNames  projected (aliased) table names
     * @param columnNames projected column names; must be a subset of the columns in this tuple. */
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

    /** @return Tuple data separated by commas without white space */
    @Override
    public String toString() {
        return row.values().toString().replaceAll("\\s|\\[|\\]", "");
    }

    /** @param o other object to compare
     * @return true if o is a Tuple with the same data and order as this Tuple **/
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Tuple other= (Tuple) o;
        return Arrays.equals(row.values().toArray(), other.row.values().toArray());
    }

    /** @param left left tuple
     * @param right right tuple
     * @return merged Tuple with order specified by the concatenation of left and right */
    public static Tuple mergeTuples(Tuple left, Tuple right) {
        LinkedHashMap<String, Integer> row= new LinkedHashMap<>();
        left.row.forEach((key, value) -> row.put(key, value));
        right.row.forEach((key, value) -> row.put(key, value));
        return new Tuple(row);
    }
}
