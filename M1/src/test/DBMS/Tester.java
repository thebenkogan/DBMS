package DBMS;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Tester {

    @Test
    void test() {
        assertEquals(4, 2 + 2);
    }

    @ParameterizedTest
    @ValueSource(ints= { 1, 2, 3, 4, 5 })
    void isPositive(int number) {
        assertTrue(number > 0);
    }

}
