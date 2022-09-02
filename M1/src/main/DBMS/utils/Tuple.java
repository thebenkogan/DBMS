package DBMS.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.jsqlparser.schema.Column;

public class Tuple {
    /** Maps column name to field. Uses a LinkedHashMap, which maintains insertion order. */
    private LinkedHashMap<String, Integer> items;

    /** Creates a new tuple for the items in columns. Requires columns and items are of the same
     * size.
     * 
     * @param columns the name of each column in the table
     * @param data    the row of data in the table */
    public Tuple(List<String> columns, List<Integer> data) {
        LinkedHashMap<String, Integer> items= new LinkedHashMap<>();
        for (int i= 0; i < data.size(); i++ ) {
            items.put(columns.get(i), data.get(i));
        }
        this.items= items;
    }

    /** @param columnName name of table column
     * @return value in the column */
    public int get(String columnName) {
        return items.get(columnName);
    }

    /** Projects this tuple to those in columns. Requires: columns is a subset of the columns in
     * this tuple.
     * 
     * @param columns projected columns */
    public void project(List<Column> columns) {
        List<String> columnNames= columns.stream().map(c -> c.getColumnName())
            .collect(Collectors.toList());
        Integer[] data= columnNames.stream().map(cn -> items.get(cn)).toArray(Integer[]::new);
        items.clear();
        for (int i= 0; i < data.length; i++ ) {
            items.put(columnNames.get(i), data[i]);
        }
    }

    @Override
    public String toString() {
        return items.values().toString();
    }
}
