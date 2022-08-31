package DBMS.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ScanOperatorTest {
	ScanOperator scop_boats = new ScanOperator("M1/samples/input/db/data/Boats");

	List<String> cols = Arrays.asList(new String[] { "D", "E", "F" });
	List<Integer> data = Arrays.asList(new Integer[] { 101, 2, 3 });

	@Test
	void test() {
		assertEquals(4, 2 + 2);
	}

	// Try to scan file that does not exist
	@Test
	void testGetNextTupleFail() {
		assertThrows(FileNotFoundException, new ScanOperator("Failure"));
	}

	// Get first tuple from existing DB
	@Test
	void testGetNextTuple() {
		assertEquals(new Tuple(cols, data).toString(),
				scop.getNextTuple().toString());
	}

	// reset scan to top of table
	@Test
	void testReset() {
		scop_boats.reset();
		assertEquals(new Tuple(cols, data).toString(),
				scop.getNextTuple().toString());
	}

	// read up to last tuple in db
	@Test
	void testGetNextTupleLast() {
		scop_boats.getNextTuple(); // 102,3,4
		scop_boats.getNextTuple(); // 104,104,2
		scop_boats.getNextTuple(); // 103,1,1
		scop_boats.getNextTuple(); // 107,2,8
		assertNull(scop_boats.getNextTuple());
	}

}
