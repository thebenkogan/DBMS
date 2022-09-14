package DBMS.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.jsqlparser.parser.CCJSqlParser;

public class Catalog {

    /** path to input directory */
    private static String input;

    /** path to output directory */
    private static String output;

    /** Map from table name to list of column names */
    private static Map<String, List<String>> schema= new HashMap<>();

    private static Catalog instance= new Catalog();

    private Catalog() {}

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

    /** @param input path to input directory
     * @param output path to output directory
     * @throws IOException */
    public static void init(String input, String output) throws IOException {
        Catalog.input= input;
        Catalog.output= output;

        BufferedReader schemaBr= readerFromPath(input, "db", "schema.txt");
        String line;
        while ((line= schemaBr.readLine()) != null) {
            StringTokenizer table= new StringTokenizer(line, " ");
            String tableName= table.nextToken();
            List<String> columns= new LinkedList<>();
            while (table.hasMoreTokens()) {
                columns.add(table.nextToken());
            }
            schema.put(tableName, columns);
        }
    }

    /** @param name name of the table to lookup
     * @return BufferedReader for the table
     * @throws FileNotFoundException */
    public BufferedReader getTable(String name) throws FileNotFoundException {
        return readerFromPath(input, "db", "data", name);
    }

    /** @param name name of the table to extract columns
     * @return list of column names */
    public List<String> getTableColumns(String name) {
        return schema.get(name);
    }

    /** @return Parser for the query file corresponding to the Catalog input.
     * @throws FileNotFoundException */
    public CCJSqlParser getQueryFile() throws FileNotFoundException {
        return new CCJSqlParser(new FileReader(join(input, "queries.sql")));
    }

    /** @param i query number
     * @return FileWriter for dumping the query results
     * @throws IOException */
    public FileWriter getOutputWriter(int i) throws IOException {
        File file= new File(join(output, "query" + i));
        file.getParentFile().mkdirs();
        return new FileWriter(file);
    }

}
