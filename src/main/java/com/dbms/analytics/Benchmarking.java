package com.dbms.analytics;

import com.dbms.Interpreter;
import com.dbms.utils.Catalog;
import com.google.common.base.Stopwatch;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Main entry point of benchmarking experiments. */
public class Benchmarking {
    static final String SAILORS = "Sailors";
    static final String BOATS = "Boats";
    static final String RESERVES = "Reserves";

    /**
     * {@code CONFIG_FILEPATH} is the file storing all the configuration information for our benchmarking experiment.
     */
    static final String CONFIG_FILEPATH = "benchmarking/config.txt";

    /**
     * {@code LOG_FILEPATH} is the folder storing files containing the execution times of each join type.
     */
    static final String LOG_FILEPATH = "benchmarking/logs/";

    /**
     * {@code SAILOR_COLUMNS} are the names of the columns of the Sailors sample data.
     */
    private static final Set<String> SAILOR_COLUMNS = new HashSet<>(Arrays.asList("A", "B", "C"));

    /**
     * {@code BOAT_COLUMNS} are the names of the columns of the Boats sample data.
     */
    private static final Set<String> BOAT_COLUMNS = new HashSet<>(Arrays.asList("D", "E", "F"));

    /**
     * {@code RESERVE_COLUMNS} are the names of the columns of the Reserves sample data.
     */
    private static final Set<String> RESERVE_COLUMNS = new HashSet<>(Arrays.asList("G", "H"));

    /**
     * Uses our DBMS to run the query and output the result to {@code OUTPUT} and logs the execution times to {@code LOG_FILEPATH}
     * @param stopwatch is the singleton instance of the stopwatch.
     * @param filepath contains the name of the file to store time logs to.
     * @throws IOException
     */
    static void experiment(Stopwatch stopwatch, String filepath, List<String> queries) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true));
        int size = queries.size();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String query = queries.get(i);
            long startQuery = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            Interpreter.executeQuery(query, i + 1);
            long endQuery = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            long elapsed = endQuery - startQuery;
            System.out.println("Current Query: " + query);
            System.out.println("Time Taken: " + elapsed);
            line.append((i == size - 1) ? elapsed : (elapsed + " "));
        }
        writer.write(line.toString());
        writer.newLine();
        writer.close();
    }

    /**
     * Generates random data for Sailors, Boats, and Reserves tables.
     * @param rows number of tuples in each relation
     * @param maxValue maximum value for RNG
     */
    static void generate(int rows, int maxValue) {
        final String sailorsTable = Catalog.pathToTable(SAILORS);
        final String boatsTable = Catalog.pathToTable(BOATS);
        final String reservesTable = Catalog.pathToTable(RESERVES);
        TupleGenerator.generate(sailorsTable, SAILOR_COLUMNS, maxValue, rows);
        TupleGenerator.generate(boatsTable, BOAT_COLUMNS, maxValue, rows);
        TupleGenerator.generate(reservesTable, RESERVE_COLUMNS, maxValue, rows);
    }
}
