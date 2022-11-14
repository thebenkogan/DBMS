package com.dbms.visitors;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.utils.Attribute;
import com.dbms.utils.Helpers;
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
    private static Expression exp1 = Helpers.strExpToExp(String.join(
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

    private static Map<String, List<Attribute>> schema1 = Map.ofEntries(
            entry(SAILORS, Arrays.asList(SailorsA, SailorsB, SailorsC)),
            entry(BOATS, Arrays.asList(BoatsD, BoatsE, BoatsF)),
            entry(RESERVES, Arrays.asList(ReservesG, ReservesH)));

    private static UnionFindVisitor uv2 = new UnionFindVisitor();
    private static Expression exp2 = Helpers.strExpToExp(String.join(
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

    private static Map<String, List<Attribute>> schema2 = Map.ofEntries(
            entry(A1, Arrays.asList(A1A, A1B, A1C, A1D, A1F, A1G, A1N, A1P)),
            entry(A2, Arrays.asList(A2E, A2L, A2X, A2Y, A2Z)));

    @BeforeAll
    public static void setup() throws IOException {
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
                Arguments.of(str(uv1.unionFind.equality(SailorsA)), "null"),
                Arguments.of(str(uv1.unionFind.min(SailorsA)), "9"),
                Arguments.of(str(uv1.unionFind.max(SailorsA)), "169"),
                Arguments.of(str(uv1.unionFind.min(SailorsB)), "42"),
                Arguments.of(str(uv1.unionFind.max(SailorsB)), "42"),
                Arguments.of(str(uv1.unionFind.equality(SailorsB)), "42"),
                Arguments.of(str(uv1.unionFind.min(SailorsC)), "4"),
                Arguments.of(str(uv1.unionFind.max(SailorsC)), "4"),
                Arguments.of(str(uv1.unionFind.equality(SailorsC)), "4"),
                Arguments.of(str(uv1.unionFind.max(BoatsD)), "69"),
                Arguments.of(str(uv1.unionFind.min(BoatsD)), "6"),
                Arguments.of(str(uv1.unionFind.equality(BoatsD)), "null"),
                Arguments.of(str(uv1.unionFind.equality(BoatsE)), "null"),
                Arguments.of(str(uv1.unionFind.min(BoatsE)), "9"),
                Arguments.of(str(uv1.unionFind.max(BoatsE)), "169"),
                Arguments.of(str(uv1.unionFind.max(BoatsF)), "42"),
                Arguments.of(str(uv1.unionFind.max(BoatsF)), "42"),
                Arguments.of(str(uv1.unionFind.equality(BoatsF)), "42"),
                Arguments.of(str(uv1.unionFind.max(ReservesH)), "42"),
                Arguments.of(str(uv1.unionFind.max(ReservesH)), "42"),
                Arguments.of(str(uv1.unionFind.equality(ReservesH)), "42"),
                Arguments.of(
                        str(uv1.unionFind.expressionOfTable(SAILORS, schema1.get(SAILORS), false)),
                        String.join(" AND ", "Sailors.A >= 9", "Sailors.A <= 169", "Sailors.B = 42", "Sailors.C = 4")),
                Arguments.of(str(uv1.unionFind.expressionOfTable(SAILORS, schema1.get(SAILORS), true)), "null"),
                Arguments.of(uv1.unusableSelects.get(SAILORS).toString(), listWrap("Sailors.C != 69")),
                Arguments.of(uv1.unusableSelects.get(RESERVES).toString(), listWrap("Reserves.G != 6")),
                Arguments.of(
                        uv1.unusableJoins.get(listWrap(BOATS + ", " + SAILORS)).toString(),
                        listWrap("Sailors.A != Boats.D")),
                Arguments.of(
                        uv1.unusableJoins
                                .get(listWrap(RESERVES + ", " + SAILORS))
                                .toString(),
                        listWrap("Sailors.C < Reserves.G")),
                Arguments.of(str(uv2.unionFind.min(A1A)), "null"),
                Arguments.of(str(uv2.unionFind.max(A1A)), "null"),
                Arguments.of(str(uv2.unionFind.equality(A1A)), "null"),
                Arguments.of(str(uv2.unionFind.min(A1B)), "5"),
                Arguments.of(str(uv2.unionFind.max(A1B)), "5"),
                Arguments.of(str(uv2.unionFind.equality(A1B)), "5"),
                Arguments.of(str(uv2.unionFind.min(A1C)), "69"),
                Arguments.of(str(uv2.unionFind.max(A1C)), "null"),
                Arguments.of(str(uv2.unionFind.equality(A1C)), "null"),
                Arguments.of(str(uv2.unionFind.min(A1D)), "96"),
                Arguments.of(str(uv2.unionFind.max(A1D)), "96"),
                Arguments.of(str(uv2.unionFind.equality(A1D)), "96"),
                Arguments.of(str(uv2.unionFind.min(A1P)), "null"),
                Arguments.of(str(uv2.unionFind.max(A1P)), "null"),
                Arguments.of(str(uv2.unionFind.equality(A1P)), "null"),
                Arguments.of(str(uv2.unionFind.min(A2E)), "96"),
                Arguments.of(str(uv2.unionFind.max(A2E)), "96"),
                Arguments.of(str(uv2.unionFind.equality(A2E)), "96"),
                Arguments.of(str(uv2.unionFind.min(A2L)), "null"),
                Arguments.of(str(uv2.unionFind.max(A2L)), "null"),
                Arguments.of(str(uv2.unionFind.equality(A2L)), "null"),
                Arguments.of(str(uv2.unionFind.min(A2Z)), "null"),
                Arguments.of(str(uv2.unionFind.max(A2Z)), "null"),
                Arguments.of(str(uv2.unionFind.equality(A2Z)), "null"),
                Arguments.of(
                        str(uv2.unionFind.expressionOfTable(A1, schema2.get(A1), false)),
                        String.join(" AND ", "A1.B = 5", "A1.C >= 69", "A1.D = 96", "A1.N = A1.P")),
                Arguments.of(
                        str(uv2.unionFind.expressionOfTable(A1, schema2.get(A1), true)),
                        String.join(" AND ", "A1.A = A2.X", "A1.N = A2.Z", "A1.P = A2.L")),
                Arguments.of(
                        uv2.unusableSelects.get(A1).toString(),
                        listWrap(String.join(", ", "A1.B < A1.C", "A1.B >= A1.F"))),
                Arguments.of(
                        uv2.unusableJoins.get(listWrap(A1 + ", " + A2)).toString(),
                        listWrap(String.join(", ", "A1.A <= A2.Z", "A1.G != A2.Y"))));
    }

    /**
     * Just a shorter version of {@code Integer.toString()}
     * @param i given integer (can be null)
     * @return either {@code "null"} or the integer value as a string
     */
    private static String str(Integer i) {
        return (i == null) ? "null" : Integer.toString(i);
    }

    /**
     * Just a shorter version of {@code Expression.toString()}
     * @param e given {@code Expression} (can be null)
     * @return either {@code "null"} or the integer value as a string
     */
    private static String str(Expression e) {
        return (e == null) ? "null" : e.toString();
    }

    /**
     * Places square brackets around the string
     * @param s given {@code String}
     * @return {@code [s]}
     */
    private static String listWrap(String s) {
        return "[" + s + "]";
    }
}
