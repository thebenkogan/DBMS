package com.dbms.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

    /** path to temp directory */
    private static String temp;

    /** Map from (unaliased) table name to list of column names */
    private static Map<String, List<String>> schema = new HashMap<>();

    /** Map of aliases to real table names */
    private static Map<String, String> aliasMap = new HashMap<>();

    /** The physical operator configuration */
    public static Config CONFIG;

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
    public static void init(String input, String output, String temp) throws IOException {
        Catalog.input = input;
        Catalog.output = output;
        Catalog.temp = temp;

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

        CONFIG = new Config(readerFromPath(input, "plan_builder_config.txt"));
    }

    /** @param name (unaliased) name of the table to lookup
     * @return BufferedReader for the table
     * @throws FileNotFoundException */
    public BufferedReader getTable(String name) throws FileNotFoundException {
        return readerFromPath(input, "db", "data", name);
    }

    /** @param tableName (aliased) table name
     * @return path to table file in input directory */
    public static String pathToTable(String tableName) {
        return join(input, "db", "data", tableName);
    }

    /** @param path path to temp file within temp directory
     * @return updated path with temp directory prepended */
    public static String pathToTempFile(String path) {
        return join(temp, path);
    }

    /** @param i query number
     * @return path to output file */
    public static String pathToOutputFile(int i) {
        return join(output, "query" + i);
    }

    /** @param name (unaliased) name of the table to extract columns
     * @return list of column names */
    public static List<String> getTableColumns(String name) {
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
    public static BufferedReader getQueriesFile() throws FileNotFoundException {
        return new BufferedReader(new FileReader(String.join(File.separator, input, "queries.sql")));
    }

    public static void createTempSubDir(String id) throws IOException {
        new File(join(temp, id)).mkdir();
    }

    public static void cleanTempDir() throws IOException {
        Files.walkFileTree(Paths.get(temp), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                } catch (Exception e) {
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                } catch (Exception e) {
                }
                return FileVisitResult.CONTINUE;
            }
        });
        new File(temp).mkdir();
    }
}
