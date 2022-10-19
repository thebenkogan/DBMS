package com.dbms.utils;

import java.util.Objects;

/**
 * Class to represent a column name of a tuple, along with the aliased table name that the tuple belongs to
 */
public final class ColumnName {
    /**
     * {@code TABLE} is the aliased name of the table as a {@code String}.
     */
    public final String TABLE;

    /**
     * {@code COLUMN} is the name of the column as a {@code String}.
     */
    public final String COLUMN;

    /**
     * A private constructor used for initializing a {@code ColumnName} type.
     * @param table is the aliased name of the table as a {@code String}
     * @param column is the name of the column as a {@code String}
     */
    private ColumnName(String table, String column) {
        TABLE = table;
        COLUMN = column;
    }

    /**
     * A function used to call the constructor, so the clients do not need to use the {@code new} keyword.
     * @param table is the aliased name of the table as a {@code String}
     * @param column is the name of the column as a {@code String}
     * @return an initialized {@code Column}
     */
    public static ColumnName bundle(String table, String column) {
        return new ColumnName(table, column);
    }

    /** Two {@code Schema} objects are equal if they have identical table and column names. */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ColumnName) {
            ColumnName otherName = (ColumnName) other;
            return TABLE.equals(otherName.TABLE) && COLUMN.equals(otherName.COLUMN);
        }
        return false;
    }

    /** {@code Schema} types are hashed using their table and column {@code String} values. */
    @Override
    public int hashCode() {
        return Objects.hash(TABLE, COLUMN);
    }
}
