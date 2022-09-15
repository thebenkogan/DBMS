package DBMS.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import DBMS.utils.Catalog;
import DBMS.utils.Helpers;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;

class SortOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input", null);
    }

    SortOperator getOperator(String selectColumns, String whereCondition, String orderBy)
        throws FileNotFoundException {
        List<OrderByElement> orderByElements= Helpers.strOrderBysToOrderBys(orderBy);

        ScanOperator scanOperator= new ScanOperator("Boats");
        if (whereCondition.isBlank()) {
            return new SortOperator(scanOperator, orderByElements);
        } else {
            Expression exp= Helpers.strExpToExp(whereCondition);
            SelectOperator selectOperator= new SelectOperator(scanOperator, exp);
            return new SortOperator(selectOperator, orderByElements);
        }
    }

    @Test
    void testGetNextTuple() throws IOException {
        SortOperator sortOperation1= getOperator("*", "Boats.D > 102", "Boats.E");
        SortOperator sortOperation2= getOperator("*", "", "Boats.D");

        assertEquals("103,1,1", sortOperation1.getNextTuple().toString());
        assertEquals("107,2,8", sortOperation1.getNextTuple().toString());
        assertEquals("104,104,2", sortOperation1.getNextTuple().toString());
        assertEquals("101,2,3", sortOperation2.getNextTuple().toString());
        assertEquals("102,3,4", sortOperation2.getNextTuple().toString());
        assertEquals("103,1,1", sortOperation2.getNextTuple().toString());
        assertEquals("104,104,2", sortOperation2.getNextTuple().toString());
        assertEquals("107,2,8", sortOperation2.getNextTuple().toString());
    }

    @Test
    void testReset() throws IOException {
        SortOperator sortOperator= getOperator("*", "Boats.D > 102", "Boats.E");

        assertEquals("103,1,1", sortOperator.getNextTuple().toString());
        sortOperator.reset();
        assertEquals("103,1,1", sortOperator.getNextTuple().toString());
    }

}
