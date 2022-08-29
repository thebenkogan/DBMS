package DBMS;

import java.io.FileReader;

import DBMS.operators.ScanOperator;
import DBMS.utils.Catalog;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class Interpreter {

	private static final String queriesFile = "samples/input/queries_scan.sql";

	public static void main(String[] args) {
		try {
			Catalog.init("samples/input");
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			while ((statement = parser.Statement()) != null) {
				Select select = (Select) statement;
				PlainSelect body = (PlainSelect) select.getSelectBody();
				ScanOperator table = new ScanOperator(body.getFromItem().toString());
				table.dump();
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}