package com.dbms.queryplan;

import static com.dbms.utils.Helpers.strExpToExp;

import com.dbms.utils.Attribute;
import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;

/**
 * This class reads a selection condition and uses a union-find data structure to keep track of the possible bounds of an attribute.
 */
public class UnionFind {

    /** Maps an attribute to its stats, as well as other attributes that share the same stats */
    private Map<Attribute, UnionFindElement> elements = new HashMap<>();

    /** Get the {@code UnionFindElement} associated with an attribute. If it doesn't exist, it puts
     * a new record into the {@code UnionFind}.
     *
     * @param a given attribute
     * @return {@code UnionFindElement} containing {@code a}. If one doesn't exist, it creates a new
     *         one using its stats. */
    private UnionFindElement find(Attribute a) {
        UnionFindElement result = elements.get(a);
        if (result == null) {
            result = new UnionFindElement(a);
            elements.put(a, result);
            return result;
        }
        return result;
    }

    /** Creates a select expression containing all conditions with {@code attributes}
     *
     * @param attributes all attributes of the given table
     * @return an expression with all conditions and-ed together */
    public Expression expressionOfAttributes(List<Attribute> attributes) {
        List<String> stringExpressions = new LinkedList<>();
        for (Attribute a : attributes) {
            String attributeString = selectExpression(a);
            if (attributeString == null) continue;
            stringExpressions.add(attributeString);
        }
        return stringExpressions.size() == 0 ? null : strExpToExp(String.join(" AND ", stringExpressions));
    }

    /** Creates a select expression with {@code a}
     *
     * @param a    given attribute
     * @param seen seen attributes (so no duplicate conditions)
     * @return a select condition containing everything with {@code a} */
    private String selectExpression(Attribute a) {
        if (elements.get(a) == null) return null;
        UnionFindElement ufe = elements.get(a);
        if (ufe.equality == null && ufe.min == null && ufe.max == null) return null;
        if (ufe.equality != null) return String.join(" = ", a.toString(), ufe.equality.toString());

        List<String> conditions = new LinkedList<>();
        if (ufe.max != null) conditions.add(String.join(" <= ", a.toString(), ufe.max.toString()));
        if (ufe.min != null) conditions.add(String.join(" >= ", a.toString(), ufe.min.toString()));
        return String.join(" AND ", conditions);
    }

    /** Updates lower bound of union find element of a given attribute. It sets the minimum to the
     * larger of the given and existing min value.
     *
     * @param a   attribute
     * @param min potential new minimum value */
    public void updateLower(Attribute a, int min) {
        UnionFindElement ufe = find(a);
        ufe.min = max(ufe.min, min); // largest lower bound
        if (ufe.min == ufe.max) ufe.equality = ufe.min;
    }

    /** Updates upper bound of union find element of a given attribute. It sets the maximum to the
     * smaller of the given and existing max value.
     *
     * @param a   attribute
     * @param max potential new maximum value */
    public void updateUpper(Attribute a, int max) {
        UnionFindElement ufe = find(a);
        ufe.max = min(ufe.max, max); // smallest upper bound
        if (ufe.max == ufe.min) ufe.equality = ufe.min;
    }

    /** Updates equality relation of an attribute and a number
     *
     * @param a  given attribute
     * @param eq value of new equality condition */
    public void updateEquality(Attribute a, int eq) {
        UnionFindElement ufe = find(a);
        ufe.equality = eq;
        ufe.max = eq;
        ufe.min = eq;
    }

    /** Merges the properties of 2 {@code Attribute} objects on an equality relation. It will update
     * the map containing the {@code UnionFind} data.
     *
     * @param a
     * @param b */
    public void union(Attribute a, Attribute b) {
        UnionFindElement ufa = find(a);
        UnionFindElement ufb = find(b);
        ufa.attributes.addAll(ufb.attributes);

        Integer newEq = ufa.equality != null ? ufa.equality : ufb.equality;
        Integer newMin = newEq == null ? max(ufa.min, ufb.min) : newEq;
        Integer newMax = newEq == null ? min(ufa.max, ufb.max) : newEq;

        UnionFindElement ufe = new UnionFindElement(ufa.attributes, newMax, newMin, newEq);
        ufe.attributes.forEach(attr -> elements.put(attr, ufe));
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(elements.values());
    }

    /** Maximum value with {@code null} support
     *
     * @param a
     * @param b
     * @return {@code max(a,b)}; if either is {@code null}, return the non-{@code null} one;
     *         otherwise, return {@code null} */
    private Integer max(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }

    /** Minimum value with {@code null} support
     *
     * @param a
     * @param b
     * @return {@code max(a,b)}; if either is {@code null}, return the non-{@code null} one;
     *         otherwise, return {@code null} */
    private Integer min(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.min(a, b);
    }
}

/** Keeps track of multiple attributes that all fall within a range with their data. */
class UnionFindElement {

    /** The set of attributes represented by this element. */
    Set<Attribute> attributes = new HashSet<>();

    /** Maximum possible element (inclusive) of all attributes in {@code attributes} */
    Integer max = null;

    /** Minimum possible element (inclusive) of all attributes in {@code attributes} */
    Integer min = null;

    /** All attributes are equal to {@code equality} if it is not null. {@code min} and {@code max}
     * will be updated accordingly */
    Integer equality = null;

    /** Creates a {@code UnionFindElement} with a single attribute
     *
     * @param attribute set of attributes */
    UnionFindElement(Attribute a) {
        attributes.add(a);
    }

    /** @param sa   set of attributes
     * @param max      upper bound
     * @param min      lower bound
     * @param equality equality */
    UnionFindElement(Set<Attribute> sa, Integer max, Integer min, Integer equality) {
        attributes = sa;
        this.max = max;
        this.min = min;
        this.equality = max == min ? min : equality;
    }

    @Override
    public String toString() {
        return Arrays.asList(attributes.toString(), "equals " + equality, "min " + min, "max " + max)
                .toString();
    }
}
