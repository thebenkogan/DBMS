package com.dbms.queryplan;

import static com.dbms.utils.Helpers.str;
import static com.dbms.utils.Helpers.strExpToExp;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Attribute;
import com.dbms.utils.Catalog;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UnionFindVisitorTest {
    private static UnionFindVisitor uv1 = new UnionFindVisitor();
    private static Expression exp1 = strExpToExp(String.join(
            " AND ",
            "Sailors.A <= 169",
            "Sailors.A != Boats.D",
            "Sailors.B = 42",
            "Sailors.C < Reserves.G",
            "3 < Sailors.C",
            "Sailors.C <= 4",
            "Sailors.C != 69",
            "Boats.D > 5",
            "70 > Boats.D",
            "Boats.E > 8",
            "Boats.E = Sailors.A",
            "Boats.F = Reserves.H",
            "Reserves.G != 6",
            "Reserves.H = Sailors.B"));
    private static String SAILORS = "Sailors";
    private static String BOATS = "Boats";
    private static String RESERVES = "Reserves";
    private static Attribute SailorsA = Attribute.bundle(SAILORS, "A");
    private static Attribute SailorsB = Attribute.bundle(SAILORS, "B");
    private static Attribute SailorsC = Attribute.bundle(SAILORS, "C");
    private static Attribute BoatsD = Attribute.bundle(BOATS, "D");
    private static Attribute BoatsE = Attribute.bundle(BOATS, "E");
    private static Attribute BoatsF = Attribute.bundle(BOATS, "F");
    private static Attribute ReservesG = Attribute.bundle(RESERVES, "G");
    private static Attribute ReservesH = Attribute.bundle(RESERVES, "H");

    private static UnionFindVisitor uv2 = new UnionFindVisitor();
    private static Expression exp2 = strExpToExp(String.join(
            " AND ",
            "A1.A = A2.X",
            "A1.A <= A2.Z",
            "A1.B = 5",
            "A1.B < A1.C",
            "A1.B >= A1.F",
            "A1.C > 68",
            "A2.E = 96",
            "A1.D = A2.E",
            "A1.G != A2.Y",
            "A2.Z = A1.N",
            "A2.Z = A2.L",
            "A2.Z = A1.P"));
    private static String A1 = "A1";
    private static String A2 = "A2";
    private static Attribute A1A = Attribute.bundle(A1, "A");
    private static Attribute A1B = Attribute.bundle(A1, "B");
    private static Attribute A1C = Attribute.bundle(A1, "C");
    private static Attribute A1D = Attribute.bundle(A1, "D");
    private static Attribute A1F = Attribute.bundle(A1, "F");
    private static Attribute A1G = Attribute.bundle(A1, "G");
    private static Attribute A1N = Attribute.bundle(A1, "N");
    private static Attribute A1P = Attribute.bundle(A1, "P");
    private static Attribute A2E = Attribute.bundle(A2, "E");
    private static Attribute A2L = Attribute.bundle(A2, "L");
    private static Attribute A2X = Attribute.bundle(A2, "X");
    private static Attribute A2Y = Attribute.bundle(A2, "Y");
    private static Attribute A2Z = Attribute.bundle(A2, "Z");

    private static Map<String, List<Attribute>> schema = Map.ofEntries(
            entry(SAILORS, Arrays.asList(SailorsA, SailorsB, SailorsC)),
            entry(BOATS, Arrays.asList(BoatsD, BoatsE, BoatsF)),
            entry(RESERVES, Arrays.asList(ReservesG, ReservesH)),
            entry(A1, Arrays.asList(A1A, A1B, A1C, A1D, A1F, A1G, A1N, A1P)),
            entry(A2, Arrays.asList(A2E, A2L, A2X, A2Y, A2Z)));

    @BeforeAll
    public static void setup() throws IOException {
        Catalog.setSchema(schema);
        exp1.accept(uv1);
        exp2.accept(uv2);
    }

    @ParameterizedTest(name = "UnionFindVisitor Test {index}: expected {1}; actual {0} ")
    @MethodSource("argumentProvider")
    void testUnionFindVisitor(String actual, String expected) {
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of(
                        str(uv1.getExpression(SAILORS)),
                        String.join(
                                " AND ",
                                "Sailors.C != 69",
                                "Sailors.A <= 169",
                                "Sailors.A >= 9",
                                "Sailors.B = 42",
                                "Sailors.C = 4")),
                Arguments.of(
                        str(uv1.getExpression(BOATS)),
                        String.join(
                                " AND ",
                                "Boats.D <= 69",
                                "Boats.D >= 6",
                                "Boats.E <= 169",
                                "Boats.E >= 9",
                                "Boats.F = 42")),
                Arguments.of(
                        str(uv1.getExpression(RESERVES)), String.join(" AND ", "Reserves.G != 6", "Reserves.H = 42")),
                Arguments.of(
                        str(uv1.getExpression(RESERVES, SAILORS)),
                        String.join(" AND ", "Sailors.C < Reserves.G", "Reserves.H = Sailors.B")),
                Arguments.of(
                        str(uv1.getExpression(BOATS, SAILORS)),
                        String.join(" AND ", "Sailors.A != Boats.D", "Boats.E = Sailors.A")),
                Arguments.of(
                        str(uv2.getExpression(A1)),
                        String.join(" AND ", "A1.B < A1.C", "A1.B >= A1.F", "A1.B = 5", "A1.C >= 69", "A1.D = 96")),
                Arguments.of(str(uv2.getExpression(A2)), String.join(" AND ", "A2.Z = A2.L", "A2.E = 96")),
                Arguments.of(
                        str(uv2.getExpression(A1, A2)),
                        String.join(
                                " AND ",
                                "A1.A = A2.X",
                                "A1.A <= A2.Z",
                                "A1.D = A2.E",
                                "A1.G != A2.Y",
                                "A2.Z = A1.N",
                                "A2.Z = A1.P")));
    }
}
