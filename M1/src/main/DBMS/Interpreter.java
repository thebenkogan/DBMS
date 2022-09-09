package DBMS;

import java.io.FileReader;

import DBMS.utils.Catalog;
import DBMS.utils.QueryPlanBuilder;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;

public class Interpreter {

    private static final String queriesFile= "samples/input/queries_join.sql";
    private static final String inputPath= "samples/input";

    public static void main(String[] args) {
        try {
            Catalog.init(inputPath);
            CCJSqlParser parser= new CCJSqlParser(new FileReader(queriesFile));
            Statement statement;
            while ((statement= parser.Statement()) != null) {
                System.out.println(statement);
                QueryPlanBuilder queryPlan= new QueryPlanBuilder(statement);
                queryPlan.operator.dump();
            }
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }
}