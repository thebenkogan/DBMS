package DBMS;

import java.io.IOException;

import DBMS.utils.Catalog;
import DBMS.utils.QueryPlanBuilder;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class Interpreter {

    public static void run() throws IOException, ParseException {
        CCJSqlParser parser= Catalog.getInstance().getQueryFile();
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

    public static void main(String[] args) throws IOException, ParseException {
        Catalog.init(args[0], args[1]);
        run();
    }
}