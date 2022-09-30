package com.dbms;

import com.dbms.utils.Catalog;
import com.dbms.utils.LogicalPlanBuilder;
import com.dbms.visitors.PhysicalPlanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

/** Main entry point of the DBMS and the corresponding query runner */
public class Interpreter {

    /** Executes all queries in the catalog queries file and writes the output to the catalog output
     * directory with ascending query number. If a query fails during the process, this prints the
     * failed query and error message to the console, then continues to the next query.
     *
     * @throws IOException */
    public static void run() throws IOException {
        BufferedReader fileReader = Catalog.getInstance().getQueriesFile();
        int i = 1;
        String currentQuery;
        Statement statement = null;
        while ((currentQuery = fileReader.readLine()) != null) {
            try {
                statement = CCJSqlParserUtil.parse(currentQuery);
                LogicalPlanBuilder logicalPlan = new LogicalPlanBuilder(statement);
                /** convert logical plan to physical plan by using PhysicalPlanBuilder visitor on
                 * root logical node */
                PhysicalPlanBuilder physicalPlanBuilder = new PhysicalPlanBuilder();
                logicalPlan.root.accept(physicalPlanBuilder);
                physicalPlanBuilder.physOp.dump(i);
            } catch (Exception e) {
                System.out.println("Failure: " + statement);
                e.printStackTrace();
            }
            i++;
        }
    }

    /** Initializes the catalog with the provided input and output paths, then runs the interpreter.
     *
     * @param args args[0] = input path, args[1] = output path
     * @throws IOException
     * @throws ParseException */
    public static void main(String[] args) throws IOException, ParseException {
        Catalog.init(args[0], args[1]);
        run();
    }
}
