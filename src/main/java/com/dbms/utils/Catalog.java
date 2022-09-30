package com.dbms.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;

/** A singleton responsible for storing the information of the input queries file, schema file, and
 * DB files. Also creates and maintains an alias map so that all parts of the DBMS can easily
 * retrieve the full table name for a given alias. */
public class Catalog {

    /** path to input directory */
    private static String input;

    /** path to output directory */
    private static String output;

    /** Map from (unaliased) table name to list of column names */
    private static Map<String, List<String>> schema = new HashMap<>();

    /** Map of aliases to real table names */
    private static Map<String, String> aliasMap = new HashMap<>();

    /** single instance */
    private static Catalog instance = new Catalog();

    private Catalog() {}

    /** @return singleton instance */
    public static Catalog getInstance() {
        return instance;
    }

    /** @param segments file path to join
     * @return segments joined with File.seperator */
    private static String join(String... segments) {
        return String.join(File.separator, segments);
    }

    /** @param path path to file
     * @return BufferedReader for the file at path
     * @throws FileNotFoundException */
    private static BufferedReader readerFromPath(String... path) throws FileNotFoundException {
        return new BufferedReader(new FileReader(join(path)));
    }

    /** initializes the input and output paths and reads the corresponding schema file
     *
     * @param input  path to input directory
     * @param output path to output directory
     * @throws IOException */
    public static void init(String input, String output) throws IOException {
        Catalog.input = input;
        Catalog.output = output;

        BufferedReader schemaBr = readerFromPath(input, "db", "schema.txt");
        String line;
        while ((line = schemaBr.readLine()) != null) {
            StringTokenizer table = new StringTokenizer(line, " ");
            String tableName = table.nextToken();
            List<String> columns = new LinkedList<>();
            while (table.hasMoreTokens()) {
                columns.add(table.nextToken());
            }
            schema.put(tableName, columns);
        }
    }

    /** @param name (unaliased) name of the table to lookup
     * @return BufferedReader for the table
     * @throws FileNotFoundException */
    public BufferedReader getTable(String name) throws FileNotFoundException {
        return readerFromPath(input, "db", "data", name);
    }

    /** @param name (unaliased) name of the table to lookup
     * @return FileInputStream for the table
     * @throws FileNotFoundException */
    public FileInputStream getTableStream(String name) throws FileNotFoundException {
        return new FileInputStream(join(input, "db", "data", name));
    }

    /** @param i query number
     * @return FileOutputStream for the output
     * @throws FileNotFoundException */
    public FileOutputStream getOutputStream(int i) throws FileNotFoundException {
        return new FileOutputStream(join(output, "query" + i));
    }

    /** @param name (unaliased) name of the table to extract columns
     * @return list of column names */
    public List<String> getTableColumns(String name) {
        return schema.get(name);
    }

    /** If fromItems use aliases, this populates the aliasMap and returns the aliased names.
     * Otherwise, this returns the real table names.
     *
     * @param fromItems tables that may or may not use aliases
     * @return list of (aliased) table names in fromItems */
    public static List<String> populateAliasMap(List<FromItem> fromItems) {
        boolean usingAliases = fromItems.get(0).getAlias() != null;
        LinkedList<String> tableNames = new LinkedList<>();
        for (FromItem fromItem : fromItems) {
            Table table = (Table) fromItem;
            String tableName = usingAliases ? table.getAlias().getName() : table.getName();
            if (usingAliases) aliasMap.put(tableName, table.getName());
            tableNames.add(tableName);
        }
        return tableNames;
    }

    /** @param name table name (aliased)
     * @return the actual table name corresponding to the input */
    public static String getRealTableName(String name) {
        if (aliasMap.containsKey(name)) return aliasMap.get(name);
        return name;
    }

    /** @return BufferedReader for the query file corresponding to the Catalog input. Each line will
     *         be the query as a string.
     * @throws FileNotFoundException */
    public BufferedReader getQueriesFile() throws FileNotFoundException {
        return new BufferedReader(new FileReader(String.join(File.separator, input, "queries.sql")));
    }

    /** @param i query number
     * @return FileWriter for dumping the query results to write file in the output path
     * @throws IOException */
    public FileWriter getOutputWriter(int i) throws IOException {
        File file = new File(join(output, "query" + i));
        file.getParentFile().mkdirs();
        return new FileWriter(file);
    }
}
