package DBMS.visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import DBMS.utils.Helpers;
import net.sf.jsqlparser.expression.Expression;

/** Unit tests for the JoinVisitor */
class JoinVisitorTest {

    @Test
    void testJoinVisitor() {
        String name1= "t";
        String name2= "b";
        String name3= "c";

        JoinVisitor jv= new JoinVisitor(Arrays.asList(new String[] { name1, name2, name3 }));
        Expression exp= Helpers
            .strExpToExp("b.A = t.G AND t.H > 3 AND 99 < b.Y AND b.A = c.A AND t.A = c.A");
        exp.accept(jv);

        assertEquals(jv.getExpression(name1).toString(), "t.H > 3");
        assertEquals(jv.getExpression(name2).toString(), "99 < b.Y");
        assertEquals(jv.getExpression(name1, name2).toString(), "b.A = t.G");
        assertEquals(
            jv.getExpression(name3, Arrays.asList(new String[] { name1, name2 })).toString(),
            "t.A = c.A AND b.A = c.A");
    }

    @Test
    void testJoinConditionAlwaysFails() {
        String name1= "t";
        String name2= "b";

        JoinVisitor jv= new JoinVisitor(Arrays.asList(new String[] { name1, name2 }));
        Expression exp= Helpers.strExpToExp("b.A = t.G AND 1 > 2 AND t.H > 3 AND 99 < b.Y");
        assertThrows(ArithmeticException.class, () -> exp.accept(jv));
    }

}