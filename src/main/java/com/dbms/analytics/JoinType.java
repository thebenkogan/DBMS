package com.dbms.analytics;

import static com.dbms.analytics.Benchmarking.CONFIG_FILEPATH;
import static com.dbms.analytics.Benchmarking.LOG_FILEPATH;
import static com.dbms.analytics.Benchmarking.experiment;
import static com.dbms.analytics.Benchmarking.generate;

import com.dbms.utils.Catalog;
import com.dbms.utils.PlanBuilderConfig;
import com.dbms.utils.PlanBuilderConfig.Join;
import com.dbms.utils.PlanBuilderConfig.Sort;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JoinType {
    /**
     * Each query (if there needs any sorting to be done), will be done with External Sorting with {@code EXTERNAL_PAGES} pages.
     */
    private static final int EXTERNAL_PAGES = 4;

    /**
     * {@code JOIN_TYPES} is a list containing the codes for each join type. We have it to customize the order by which we use the join types.
     */
    private static final int[] JOIN_TYPES = {2, 1, 0};

    /**
     * {@code QUERIES} are the sample queries we will use to benchmark our new join types.
     */
    private static final List<String> QUERIES = Arrays.asList(
            "SELECT Sailors.A, Reserves.G FROM Sailors, Reserves WHERE Sailors.A = Reserves.G",
            "SELECT S1.A, S2.B, B.E, R.G FROM Sailors S1, Sailors S2, Reserves R, Boats B WHERE S1.A > 69 AND R.H < 44 AND S2.B < 5 AND B.E > 10 AND S1.A = R.G AND R.H = B.D AND S2.B = S1.C AND B.E = R.G",
            "SELECT S.C, R.H, B.D, B.F FROM Sailors S, Reserves R, Boats B WHERE S.C > 69 AND B.F < 44 AND B.D < 5 AND B.F > 10 AND S.C = R.G AND R.H = B.D  AND B.F = S.A ORDER BY R.H");

    /**
     * @param i is the code for the join type we use (follows same convention as the config file)
     * @param page5 is whether or not to do BNLJ with block size of 5
     * @return a string representing the log file name
     */
    private static String mapJoinCodeToFileName(int i, boolean page5) {
        switch (i) {
            case 0:
                return "tnlj";
            case 1:
                return (page5) ? "bnlj-5" : "bnlj-1";
            default:
                return "smj";
        }
    }

    /**
     * Performs 1 trial of benchmarking task
     * @param stopwatch is the singleton instance of {@code Stopwatch} class, which keeps track of execution time
     * @param joinInt is the code for the join type to use (follows same convention as config file)
     * @param page5 is whether or not to do BNLJ with block size of 5
     * @throws IOException
     */
    private static void trial(Stopwatch stopwatch, int joinInt, boolean page5) throws IOException {
        Catalog.CONFIG =
                new PlanBuilderConfig(Join.values()[joinInt], Sort.External, (page5) ? 5 : 1, EXTERNAL_PAGES, false);
        experiment(stopwatch, LOG_FILEPATH + mapJoinCodeToFileName(joinInt, page5), QUERIES);
    }

    /**
     * Runs benchmarking task for all 3 join types on randomly generated data. Each trial generates a new random data set and runs the 3 queries using each join type. If no argument is provided, it runs 1 trial by default.
     * @param args the command line argument for the number of trials.
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        Catalog.init(CONFIG_FILEPATH);
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int TRIALS = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        for (int j = 0; j < TRIALS; j++) {
            System.out.println("Generating new data...");
            generate(5000, 100);
            for (int i = 0; i < JOIN_TYPES.length; i++) {
                int joinType = JOIN_TYPES[i];
                trial(stopwatch, joinType, false);
                if (joinType == 1) {
                    trial(stopwatch, joinType, true);
                }
            }
        }
        stopwatch.stop();
        System.out.println("Total Time: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }
}
