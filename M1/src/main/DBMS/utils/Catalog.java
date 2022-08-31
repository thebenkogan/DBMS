package DBMS.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Catalog {

    /** path to input directory */
    private static String input;

    /** Map from table name to list of columns */
    private static Map<String, List<String>> schema= new HashMap<>();

    private static Catalog instance= new Catalog();

    private Catalog() {}

    public static Catalog getInstance() {
        return instance;
    }

    /** @param path path to file
     * @return BufferedReader for the file at path
     * @throws FileNotFoundException */
    private static BufferedReader readerFromPath(String... path) throws FileNotFoundException {
        return new BufferedReader(new FileReader(String.join(File.separator, path)));
    }

    /** @param input path to input directory
     * @throws IOException */
    public static void init(String input) throws IOException {
        Catalog.input= input;

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

}
