package com.dbms.queryplan;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/** Type that represents the information provided by the configuration file */
public class PlanBuilderConfig {

    /** The 2 types of join operations we support: block-nested loop join and sort-merge join. */
    public enum Join {
        BNLJ,
        SMJ
    }

    /** Join method for query plan */
    public final Join JOINTYPE;

    /** Number of buffer pages for BNLJ */
    public final int BNLJPages = 5;

    /** Number of buffer pages for external sort */
    public final int EXTPages = 5;

    /** Whether or not to use indexes for selection */
    public final boolean indexSelection;

    /** Creates a {@code Config} type based on the contents of the configuration file
     *
     * @param br reader for the config file
     * @throws IOException */
    public PlanBuilderConfig(BufferedReader br) throws IOException {
        StringTokenizer joinNums = new StringTokenizer(br.readLine(), " ");
        JOINTYPE = Join.values()[Integer.parseInt(joinNums.nextToken())];
        indexSelection = Integer.parseInt(br.readLine()) == 1;
        br.close();
    }
}
