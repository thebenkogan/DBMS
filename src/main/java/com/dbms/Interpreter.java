package com.dbms;

import com.dbms.utils.Catalog;
import com.dbms.utils.LogicalPlanBuilder;
import com.dbms.visitors.PhysicalPlanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/** Main entry point of the DBMS and the corresponding query runner */
public class Interpreter {

    /**
     * Executes a query and writes the output to the output file
     * @param queryString is the query as a {@code String}
     * @param queryNumber is the {@code queryNumber}th query in the queries.sql file. It helps with naming the outputfiles to their corresponding line number in the queries.sql file.
     */
    public static void executeQuery(String queryString, int queryNumber) {
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(queryString);
            LogicalPlanBuilder logicalPlan = new LogicalPlanBuilder(statement);
            /** convert logical plan to physical plan by using PhysicalPlanBuilder visitor on
             * root logical node */
            PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder();
            logicalPlan.root.accept(physicalPlanBuilder);
            physicalPlanBuilder.physOp.dump(queryNumber);
            Catalog.cleanTempDir();
        } catch (Exception e) {
            System.out.println("Failure: " + statement);
            e.printStackTrace();
        }
    }

    /**
     * Executes all queries in the catalog queries file and writes the output to the catalog output
     * directory with ascending query number. If a query fails during the process, this prints the
     * failed query and error message to the console, then continues to the next query.
     * @throws IOException
     */
    public static void run() throws IOException {
        BufferedReader fileReader = Catalog.getQueriesFile();
        int i = 1;
        String currentQuery;
        while ((currentQuery = fileReader.readLine()) != null) {
            executeQuery(currentQuery, i);
            i++;
        }
    }

    /**
     * Initializes the catalog with the provided input and output paths, then runs the interpreter.
     * @param args args[0] path to interpreter configuration file
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Catalog.init(args[0]);
        if (Catalog.buildIndexes) {
            // TODO build B+ tree indexes
        }
        if (Catalog.evaluateQueries) {
            run();
        }
    }
}
