package com.dbms.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/** Type that represents the information provided by the configuration file*/
public class PlanBuilderConfig {

    /** The 3 types of join operations we support: tuple-nested loop join, block-nested loop join, and sort-merge join. */
    public static enum Join {
        TNLJ,
        BNLJ,
        SMJ
    }

    /** The 2 types of sort operations we support: sorting in memory and sorting with external storage. */
    public static enum Sort {
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

    /**
     * Creates a {@code Config} type based on the contents of the configuration file
     * @param br is a {@code BufferedReader} containing the config file to read
     * @throws IOException
     */
    public PlanBuilderConfig(BufferedReader br) throws IOException {
        StringTokenizer joinNums = new StringTokenizer(br.readLine(), " ");
        StringTokenizer sortNums = new StringTokenizer(br.readLine(), " ");
        JOINTYPE = Join.values()[Integer.parseInt(joinNums.nextToken())];
        SORTTYPE = Sort.values()[Integer.parseInt(sortNums.nextToken())];
        if (JOINTYPE == Join.BNLJ) BNLJPages = Integer.parseInt(joinNums.nextToken());
        if (SORTTYPE == Sort.External) EXTPages = Integer.parseInt(sortNums.nextToken());
        br.close();
    }

    /**
     * Constructor for programmatic configurations
     * @param joinType the type of join to use
     * @param sortType the type of sorting to use
     * @param bnljPages the number of pages for BNLJ
     * @param extPages the number pages for external sort
     */
    public PlanBuilderConfig(Join joinType, Sort sortType, int bnljPages, int extPages) {
        JOINTYPE = joinType;
        SORTTYPE = sortType;
        BNLJPages = bnljPages;
        EXTPages = extPages;
    }

    /**
     * Constructor for programmatic configurations
     * @param joinType the type of join to use
     * @param sortType the type of sorting to use
     * @param extPages the number pages for external sort
     */
    public PlanBuilderConfig(Join joinType, Sort sortType, int extPages) {
        JOINTYPE = joinType;
        SORTTYPE = sortType;
        BNLJPages = -1;
        EXTPages = extPages;
    }
}
