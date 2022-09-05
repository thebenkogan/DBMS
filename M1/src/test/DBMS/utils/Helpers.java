package DBMS.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class Helpers {

    private static PlainSelect convertQuery(String query) {
        try {
            InputStream is= new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
            CCJSqlParser parser= new CCJSqlParser(is);
            return (PlainSelect) ((Select) parser.Statement()).getSelectBody();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Expression expressionFromQuery(String query) {
        return convertQuery(query).getWhere();
    }

    @SuppressWarnings("unchecked")
    public static List<SelectItem> selectItemsFromQuery(String query) {
        return convertQuery(query).getSelectItems();
    }

}
