package DBMS;

import java.io.IOException;

import DBMS.utils.Catalog;
import DBMS.utils.QueryPlanBuilder;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class Interpreter {

    /** Executes all queries in the catalog queries file and writes the output to the catalog output
     * directory with ascending query number. If a query fails during the process, this prints the
     * failed query and error message to the console, then continues to the next query.
     * 
     * @throws IOException
     * @throws ParseException */
    public static void run() throws IOException, ParseException {
        CCJSqlParser parser= Catalog.getInstance().getQueriesFile();
        int i= 1;
        Statement statement;
        while ((statement= parser.Statement()) != null) {
            try {
                QueryPlanBuilder queryPlan= new QueryPlanBuilder(statement);
                queryPlan.operator.dump(Catalog.getInstance().getOutputWriter(i));
            } catch (Exception e) {
                System.out.println("Failure: " + statement);
                e.printStackTrace();
            }
            i++ ;
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