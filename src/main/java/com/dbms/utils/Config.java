package com.dbms.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Config {
    public static enum Join {
        TNLJ,
        BNLJ,
        SMJ
    }

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

    public Config(BufferedReader br) throws IOException {
        // br = new BufferedReader(new FileReader(config));
        StringTokenizer joinNums = new StringTokenizer(br.readLine(), " ");
        StringTokenizer sortNums = new StringTokenizer(br.readLine(), " ");
        JOINTYPE = Join.values()[Integer.parseInt(joinNums.nextToken())];
        SORTTYPE = Sort.values()[Integer.parseInt(sortNums.nextToken())];
        if (JOINTYPE == Join.BNLJ) BNLJPages = Integer.parseInt(joinNums.nextToken());
        if (SORTTYPE == Sort.External) EXTPages = Integer.parseInt(sortNums.nextToken());
        br.close();
    }
}
