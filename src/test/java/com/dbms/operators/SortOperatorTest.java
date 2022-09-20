package com.dbms.operators;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Catalog;
import com.dbms.utils.Helpers;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Unit tests for the SortOperator */
class SortOperatorTest {
    @BeforeAll
    public static void setup() throws IOException {
        Catalog.init("samples/input", null);
    }

    SortOperator getOperator(String selectColumns, String whereCondition, String orderBy) throws FileNotFoundException {
        List<OrderByElement> orderByElements = Helpers.strOrderBysToOrderBys(orderBy);

        ScanOperator scanOperator = new ScanOperator("Boats");
        if (whereCondition.isBlank()) {
            return new SortOperator(scanOperator, orderByElements);
        } else {
            Expression exp = Helpers.strExpToExp(whereCondition);
            SelectOperator selectOperator = new SelectOperator(scanOperator, exp);
            return new SortOperator(selectOperator, orderByElements);
        }
    }

    @Test
    void testGetNextTuple() throws IOException {
        SortOperator sortOperation1 = getOperator("*", "Boats.D > 102", "Boats.E");
        SortOperator sortOperation2 = getOperator("*", "", "Boats.D");

        assertEquals("103,1,1", sortOperation1.getNextTuple().toString());
        assertEquals("107,2,8", sortOperation1.getNextTuple().toString());
        assertEquals("104,104,2", sortOperation1.getNextTuple().toString());
        assertNull(sortOperation1.getNextTuple());
        assertEquals("101,2,3", sortOperation2.getNextTuple().toString());
        assertEquals("102,3,4", sortOperation2.getNextTuple().toString());
        assertEquals("103,1,1", sortOperation2.getNextTuple().toString());
        assertEquals("104,104,2", sortOperation2.getNextTuple().toString());
        assertEquals("107,2,8", sortOperation2.getNextTuple().toString());
        assertNull(sortOperation2.getNextTuple());
    }

    @Test
    void testReset() throws IOException {
        SortOperator sortOperator = getOperator("*", "Boats.D > 102", "Boats.E");

        assertEquals("103,1,1", sortOperator.getNextTuple().toString());
        sortOperator.reset();
        assertEquals("103,1,1", sortOperator.getNextTuple().toString());
    }
}