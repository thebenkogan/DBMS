package DBMS.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;

public class Tuple {
    /** Maps table name to another map mapping column name to field value. */
    private Map<String, Map<String, Integer>> items;

    /** Specifies the order of the columns, where columnOrder[i] is [tableName, columnName] */
    private String[][] columnOrder;

    /** Creates a new tuple for the items in columns. Requires columns and items are of the same
     * size.
     *
     * @param tableName the name of the table associated with each column
     * @param columns   the name of each column in the table
     * @param data      the row of data in the table */
    public Tuple(String tableName, List<String> columns, List<Integer> data) {
        String[][] columnOrder= new String[columns.size()][2];
        Map<String, Map<String, Integer>> items= new HashMap<>();
        items.put(tableName, new HashMap<>());
        for (int i= 0; i < data.size(); i++ ) {
            items.get(tableName).put(columns.get(i), data.get(i));
            columnOrder[i][0]= tableName;
            columnOrder[i][1]= columns.get(i);
        }
        this.items= items;
        this.columnOrder= columnOrder;
    }

    private Tuple(Map<String, Map<String, Integer>> items, String[][] columnOrder) {
        this.items= items;
        this.columnOrder= columnOrder;
    }

    /** @param columnName name of table column
     * @return value in the column */
    public int get(String tableName, String columnName) {
        return items.get(tableName).get(columnName);
    }

    /** Projects this tuple to those in columns. Requires: columns is a subset of the columns in
     * this tuple.
     *
     * @param columns projected columns */
    public void project(List<Column> columns) {
        Integer[] data= columns.stream()
            .map(c -> items.get(c.getTable().getName()).get(c.getColumnName()))
            .toArray(Integer[]::new);
        items.clear();
        columnOrder= new String[columns.size()][2];
        for (int i= 0; i < data.length; i++ ) {
            String tableName= columns.get(i).getTable().getName();
            items.putIfAbsent(tableName, new HashMap<>());
            items.get(tableName).put(columns.get(i).getColumnName(), data[i]);
            columnOrder[i][0]= tableName;
            columnOrder[i][1]= columns.get(i).getColumnName();
        }
    }

    @Override
    public String toString() {
        int[] data= new int[columnOrder.length];
        for (int i= 0; i < columnOrder.length; i++ ) {
            data[i]= this.get(columnOrder[i][0], columnOrder[i][1]);
        }
        return Arrays.toString(data).replaceAll("\\s|\\[|\\]", "");
    }

    /** Merges left and right into a new Tuple. Merged columnOrder is the concatenation of
     * left.columnOrder and right.columnOrder.
     * 
     * @param left  left tuple
     * @param right right tuple */
    public static Tuple mergeTuples(Tuple left, Tuple right) {
        int mergedLength= left.columnOrder.length + right.columnOrder.length;
        String[][] mergedOrder= new String[mergedLength][2];
        int[] mergedData= new int[mergedLength];
        Map<String, Map<String, Integer>> mergedItems= new HashMap<>();

        for (int i= 0; i < mergedLength; i++ ) {
            Tuple refTuple= i < left.columnOrder.length ? left : right;
            String[] entry= i < left.columnOrder.length ? left.columnOrder[i] :
                right.columnOrder[i - left.columnOrder.length];
            mergedOrder[i]= entry;
            mergedData[i]= refTuple.get(entry[0], entry[1]);
            mergedItems.putIfAbsent(entry[0], new HashMap<>());
            mergedItems.get(entry[0]).put(entry[1], mergedData[i]);
        }

        return new Tuple(mergedItems, mergedOrder);
    }
}
