package com.dbms.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Tree {
    public static Map<String, Integer> order;
    public static Map<String, Boolean> cluster;

    public Tree(BufferedReader br) throws IOException {
        order = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            String relationInfo[] = line.split(" ");
            String tableName = relationInfo[0];
            order.put(tableName, Integer.parseInt(relationInfo[2]));
            cluster.put(tableName, Integer.parseInt(relationInfo[1]) == 1);
        }
    }
}
