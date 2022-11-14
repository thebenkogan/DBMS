package com.dbms.utils;

import java.util.Objects;
import net.sf.jsqlparser.schema.Column;

/**
 * Class to represent a column name of a tuple, along with the aliased table name that the tuple belongs to
 */
public final class Attribute {
    /**
     * {@code TABLE} is the aliased name of the table as a {@code String}.
     */
    public final String TABLE;

    /**
     * {@code COLUMN} is the name of the column as a {@code String}.
     */
    public final String COLUMN;

    /**
     * A private constructor used for initializing a {@code Attribute} type.
     * @param table is the aliased name of the table as a {@code String}
     * @param column is the name of the column as a {@code String}
     */
    private Attribute(String table, String column) {
        TABLE = table;
        COLUMN = column;
    }

    /**
     * A function used to call the constructor, so the clients do not need to use the {@code new} keyword.
     * @param table is the aliased name of the table as a {@code String}
     * @param column is the name of the column as a {@code String}
     * @return an initialized {@code Column}
     */
    public static Attribute bundle(String table, String column) {
        return new Attribute(table, column);
    }

    /**
     * Aliases the attribute
     * @param alias aliased table name
     * @return a copy of {@code Attribute} with aliased name
     */
    public Attribute alias(String alias) {
        return new Attribute(alias, COLUMN);
    }

    /**
     * Create attribute from column
     * @param c is the {@code Column} object containing (possibly aliased) table and column name
     * @return {@code Attribute} object containing information of {@code c}
     */
    public static Attribute fromColumn(Column c) {
        return Attribute.bundle(Helpers.getProperTableName(c.getTable()), c.getColumnName());
    }

    /** Two {@code Schema} objects are equal if they have identical table and column names. */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Attribute) {
            Attribute otherName = (Attribute) other;
            return TABLE.equals(otherName.TABLE) && COLUMN.equals(otherName.COLUMN);
        }
        return false;
    }

    @Override
    public String toString() {
        return TABLE + "." + COLUMN;
    }

    /** {@code Schema} types are hashed using their table and column {@code String} values. */
    @Override
    public int hashCode() {
        return Objects.hash(TABLE, COLUMN);
    }
}
