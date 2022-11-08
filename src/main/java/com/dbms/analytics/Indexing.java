package com.dbms.analytics;

import static com.dbms.analytics.Benchmarking.BOATS;
import static com.dbms.analytics.Benchmarking.SAILORS;
import static com.dbms.analytics.Benchmarking.experiment;
import static com.dbms.analytics.Benchmarking.generate;
import static java.util.Map.entry;

import com.dbms.index.Index;
import com.dbms.index.TreeIndexBuilder;
import com.dbms.utils.Attribute;
import com.dbms.utils.Catalog;
import com.dbms.utils.PlanBuilderConfig;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Indexing {
    private static final int ORDER = 15;

    private static final Attribute SailorsA = Attribute.bundle(SAILORS, "A");

    private static final Attribute BoatsD = Attribute.bundle(BOATS, "D");

    private static List<String> QUERIES = Arrays.asList(
            "SELECT * FROM Sailors WHERE Sailors.A < 50069",
            "SELECT * FROM Boats WHERE Boats.D < 69",
            "SELECT * FROM Sailors, Boats WHERE Sailors.B = Boats.E AND Sailors.A < 50000 AND Boats.D > 50000");

    private static final Map<Integer, String> LOG_FILES =
            Map.ofEntries(entry(0, "normal-scan"), entry(1, "index-select"), entry(2, "clustered"));

    private static final Map<String, Index> UNCLUSTERED_CONFIG = Map.ofEntries(
            entry(SAILORS, new Index(SailorsA, ORDER, false)), entry(BOATS, new Index(BoatsD, ORDER, false)));

    private static final Map<String, Index> CLUSTERING_CONFIG = Map.ofEntries(
            entry(SAILORS, new Index(SailorsA, ORDER, true)), entry(BOATS, new Index(BoatsD, ORDER, true)));

    private static void trial(Stopwatch stopwatch, int i) throws IOException {
        PlanBuilderConfig p = Catalog.CONFIG;
        Catalog.CONFIG = new PlanBuilderConfig(p.JOINTYPE, p.SORTTYPE, p.BNLJPages, p.EXTPages, (i >= 1));
        if (i == 2) {
            Catalog.INDEXES = CLUSTERING_CONFIG;
            CLUSTERING_CONFIG.values().forEach(index -> TreeIndexBuilder.serialize(index));
        }
        String filename = LOG_FILES.get(i);
        experiment(stopwatch, Benchmarking.LOG_FILEPATH + filename, QUERIES);
    }

    /**
     * Runs benchmarking task for all 3 join types on randomly generated data. Each trial generates a new random data set and runs the 3 queries using each join type. If no argument is provided, it runs 1 trial by default.
     * @param args the command line argument for the number of trials.
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int TRIALS = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        for (int j = 0; j < TRIALS; j++) {
            System.out.println("Generating new data...");
            Catalog.init(Benchmarking.CONFIG_FILEPATH);
            generate(100_000, 100_000);
            UNCLUSTERED_CONFIG.values().forEach(index -> TreeIndexBuilder.serialize(index));
            for (int i = 0; i < 3; i++) trial(stopwatch, i);
        }
        stopwatch.stop();
        System.out.println("Total Time: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }
}
