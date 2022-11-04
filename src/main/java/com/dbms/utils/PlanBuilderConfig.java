package com.dbms.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/** Type that represents the information provided by the configuration file */
public class PlanBuilderConfig {

    /** The 3 types of join operations we support: tuple-nested loop join, block-nested loop join,
     * and sort-merge join. */
    public enum Join {
        TNLJ,
        BNLJ,
        SMJ
    }

    /** The 2 types of sort operations we support: sorting in memory and sorting with external
     * storage. */
    public enum Sort {
        InMemory,
        External
    }

    /** Join method for query plan */
    public final Join JOINTYPE;

    /** Sort method for query plan */
    public final Sort SORTTYPE;

    /** Number of buffer pages for BNLJ */
    public int BNLJPages;

    /** Number of buffer pages for external sort */
    public int EXTPages;

    /** Whether or not to use indexes for selection */
    public final boolean indexSelection;

    /** Creates a {@code Config} type based on the contents of the configuration file
     *
     * @param br reader for the config file
     * @throws IOException */
    public PlanBuilderConfig(BufferedReader br) throws IOException {
        StringTokenizer joinNums = new StringTokenizer(br.readLine(), " ");
        StringTokenizer sortNums = new StringTokenizer(br.readLine(), " ");
        JOINTYPE = Join.values()[Integer.parseInt(joinNums.nextToken())];
        SORTTYPE = Sort.values()[Integer.parseInt(sortNums.nextToken())];
        if (JOINTYPE == Join.BNLJ) BNLJPages = Integer.parseInt(joinNums.nextToken());
        if (SORTTYPE == Sort.External) EXTPages = Integer.parseInt(sortNums.nextToken());
        indexSelection = Integer.parseInt(br.readLine()) == 1;
        br.close();
    }

    /** Constructor for programmatic configurations
     *
     * @param joinType  the type of join to use
     * @param sortType  the type of sorting to use
     * @param bnljPages the number of pages for BNLJ
     * @param extPages  the number pages for external sort
     * @param indexing  whether or not to use indexed selection */
    public PlanBuilderConfig(Join joinType, Sort sortType, int bnljPages, int extPages, boolean indexing) {
        JOINTYPE = joinType;
        SORTTYPE = sortType;
        BNLJPages = bnljPages;
        EXTPages = extPages;
        indexSelection = indexing;
    }
}
