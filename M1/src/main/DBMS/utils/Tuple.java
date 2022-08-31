package DBMS.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tuple {
    private Map<String, Integer> items;

    /** Creates a new tuple for the items in columns. Requires columns and items are of the same
     * size.
     * 
     * @param columns the name of each column in the table
     * @param data    the row of data in the table */
    public Tuple(List<String> columns, int... data) {
        Map<String, Integer> items= new HashMap<>();
        for (int i= 0; i < data.length; i++ ) {
            items.put(columns.get(i), data[i]);
        }
        this.items= items;
    }

    public int get(String columnName) {
        return items.get(columnName);
    }

    @Override
    public String toString() {
        return items.values().toString();
    }
}
