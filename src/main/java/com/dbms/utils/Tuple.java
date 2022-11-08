package com.dbms.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/** The representation of a row in a table. */
public class Tuple {
    /** Maps (aliased) table.column key to value in row. Insertion order represents column
     * ordering. */
    private LinkedHashMap<Attribute, Integer> row;

    /** Creates a new Tuple for the table with columns and data.
     *
     * @param tableName the name (alias) of the table associated with each column
     * @param columns   the name of each column in the table
     * @param data      the row of data in the table; same size as columns */
    public Tuple(Schema s, List<Integer> data) {
        row = new LinkedHashMap<>();
        List<Attribute> columns = s.get();
        for (int i = 0; i < data.size(); i++) {
            row.put(columns.get(i), data.get(i));
        }
    }

    /** @param schema row keys; assumes is in the same order as the input data
     * @param data   associated data \ */
    public Tuple(Set<Attribute> schema, List<Integer> data) {
        row = new LinkedHashMap<>();
        Iterator<Attribute> names = schema.iterator();
        for (int i = 0; i < data.size(); i++) {
            row.put(names.next(), data.get(i));
        }
    }

    /** Creates a new Tuple from a derived row of an old Tuple
     *
     * @param row row map */
    private Tuple(LinkedHashMap<Attribute, Integer> row) {
        this.row = row;
    }

    /** @return number of attributes in this Tuple */
    public int size() {
        return row.size();
    }

    /** @param tableName name of table name
     * @param columnName name of table column
     * @return value in the column */
    public int get(String tableName, String columnName) {
        return row.get(Attribute.bundle(tableName, columnName));
    }

    /** @return set of {@code ColumnName} type containing table and column name */
    public Set<Attribute> getSchema() {
        return row.keySet();
    }

    /** @return collection of all Tuple values */
    public Collection<Integer> getValues() {
        return row.values();
    }

    /** Projects this tuple to those in columns.
     *
     * @param schema list of {@code ColumnName} objects containing aliased table names and column
     *               names */
    public void project(Schema s) {
        List<Attribute> columns = s.get();
        Integer[] data = new Integer[s.size()];
        for (int i = 0; i < columns.size(); i++) {
            data[i] = row.get(columns.get(i));
        }

        row.clear();
        for (int i = 0; i < data.length; i++) {
            row.put(columns.get(i), data[i]);
        }
    }

    /** @return Tuple data separated by commas without white space */
    @Override
    public String toString() {
        return row.values().toString().replaceAll("\\s|\\[|\\]", "");
    }

    /** @param o other object to compare
     * @return true if o is a Tuple with the same data and order as this Tuple */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tuple other = (Tuple) o;
        return Arrays.equals(row.values().toArray(), other.row.values().toArray());
    }

    /** @param left left tuple
     * @param right right tuple
     * @return merged Tuple with order specified by the concatenation of left and right */
    public static Tuple mergeTuples(Tuple left, Tuple right) {
        LinkedHashMap<Attribute, Integer> row = new LinkedHashMap<>();
        left.row.forEach((key, value) -> row.put(key, value));
        right.row.forEach((key, value) -> row.put(key, value));
        return new Tuple(row);
    }
}
