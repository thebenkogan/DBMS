package com.dbms.utils;

import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;

public class UnionFind {

    /** Maps an attribute to its stats, as well as other attributes that share the same stats */
    private Map<Attribute, UnionFindElement> elements = new HashMap<>();

    /**
     * Get the {@code UnionFindElement} associated with an attribute. If it doesn't exist, it puts a new record into the {@code UnionFind}.
     * @param a given attribute
     * @param isJoin whether or not the relation with {@code a} is a join
     * @return {@code UnionFindElement} containing {@code a}. If one doesn't exist, it creates a new one using its stats.
     */
    private UnionFindElement elementContainingAttribute(Attribute a, boolean isJoin) {
        UnionFindElement result = elements.get(a);
        if (result == null) {
            result = new UnionFindElement(a, isJoin);
            elements.put(a, result);
            return result;
        }
        return result;
    }

    /**
     * Creates a select/join expression containing all conditions with {@code table}
     * @param table aliased table name
     * @param attributes all attributes of the given table
     * @param isJoin whether {@code a} and {@code b} are an equijoin
     * @return an expression with all conditions and-ed together
     */
    public Expression expressionOfTable(String table, List<Attribute> attributes, boolean isJoin) {
        List<String> stringExpressions = new LinkedList<>();
        Set<Attribute> seenSelect = new HashSet<>();
        Set<Attribute> seenJoin = new HashSet<>();

        for (Attribute a : attributes) {
            seenSelect.add(a);
            seenJoin.add(a);
            String attributeString = (isJoin) ? joinExpression(a, seenJoin) : selectExpression(a, seenSelect);
            if (attributeString == null) continue;
            stringExpressions.add(attributeString);
        }
        return (stringExpressions.size() == 0) ? null : Helpers.strExpToExp(String.join(" AND ", stringExpressions));
    }

    /**
     * Creates a join expression with {@code a}
     * @param a given attribute
     * @param seen seen attributes (so no duplicate conditions)
     * @return a join condition containing everything with {@code a}
     */
    private String joinExpression(Attribute a, Set<Attribute> seen) {
        if (elements.get(a) == null) return null;
        UnionFindElement ufe = elements.get(a);
        if (ufe.max == null && ufe.min == null && ufe.equality == null) {
            Set<Attribute> attributes = ufe.joinAttributes;
            List<String> exp = new LinkedList<>();
            for (Attribute attr : attributes) {
                if (attr.equals(a)) continue;
                if (seen.contains(attr)) continue;
                exp.add(String.join(" = ", a.toString(), attr.toString()));
            }
            return (exp.size() == 0) ? null : String.join(" AND ", exp);
        }
        return null;
    }

    /**
     * Creates a select expression with {@code a}
     * @param a given attribute
     * @param seen seen attributes (so no duplicate conditions)
     * @return a select condition containing everything with {@code a}
     */
    private String selectExpression(Attribute a, Set<Attribute> seen) {
        if (elements.get(a) == null) return null;
        UnionFindElement ufe = elements.get(a);
        if (ufe.equality != null) return String.join(" = ", a.toString(), ufe.equality.toString());
        if (ufe.min == null && ufe.max == null) {
            // COL = COL
            Set<Attribute> attributes = ufe.sameTableSelectAttributes;
            System.out.println(a);
            System.out.println(ufe.joinAttributes);
            System.out.println(ufe.sameTableSelectAttributes);
            List<String> exp = new LinkedList<>();
            for (Attribute attr : attributes) {
                if (attr.equals(a) || !attr.TABLE.equals(a.TABLE)) continue;
                if (seen.contains(attr)) continue;
                exp.add(String.join(" = ", a.toString(), attr.toString()));
            }
            return (exp.size() == 0) ? null : String.join(" AND ", exp);
        } else if (ufe.min == null) {
            // COL <= 69
            return String.join(" <= ", a.toString(), ufe.max.toString());
        } else if (ufe.max == null) {
            // COL >= 69
            return String.join(" >= ", a.toString(), ufe.min.toString());
        } else {
            // 69 <= COL <= 100
            String lower = String.join(" >= ", a.toString(), ufe.min.toString());
            String upper = String.join(" <= ", a.toString(), ufe.max.toString());
            return String.join(" AND ", lower, upper);
        }
    }

    /**
     * Minimum value (inclusive) of an attribute
     * @param a given attribute
     * @return minimum value of {@code a}, {@code null} if there isn't one
     */
    public Integer min(Attribute a) {
        if (elements.get(a) == null) return null;
        return elements.get(a).min;
    }

    /**
     * Maximum value (inclusive) of an attribute
     * @param a given attribute
     * @return maximum value of {@code a}, {@code null} if there isn't one
     */
    public Integer max(Attribute a) {
        if (elements.get(a) == null) return null;
        return elements.get(a).max;
    }

    /**
     * Possible equality of an attribute
     * @param a given attribute
     * @return equality value of {@code a}, {@code null} if it doesn't exist
     */
    public Integer equality(Attribute a) {
        if (elements.get(a) == null) return null;
        return elements.get(a).equality;
    }

    /**
     * Updates lower bound of union find element of a given attribute. It sets the minimum to the larger of the given and existing min value.
     * @param a attribute
     * @param min potential new minimum value
     */
    public void updateLower(Attribute a, int min) {
        UnionFindElement ufe = elementContainingAttribute(a, false);
        ufe.min = max(ufe.min, min); // largest lower bound
        updateRest(ufe, false);
    }

    /**
     * Updates upper bound of union find element of a given attribute. It sets the maximum to the smaller of the given and existing max value.
     * @param a attribute
     * @param max potential new maximum value
     */
    public void updateUpper(Attribute a, int max) {
        UnionFindElement ufe = elementContainingAttribute(a, false);
        ufe.max = min(ufe.max, max); // smallest upper bound
        updateRest(ufe, false);
    }

    /**
     * Updates equality relation of an attribute and a number
     * @param a given attribute
     * @param eq value of new equality condition
     */
    public void updateEquality(Attribute a, int eq) {
        UnionFindElement ufe = elementContainingAttribute(a, false);
        ufe.equality = eq;
        ufe.max = eq;
        ufe.min = eq;
        updateRest(ufe, false);
    }

    /**
     * Merges the properties of 2 {@code Attribute} objects on an equality relation.
     * It will update the map containing the {@code UnionFind} data.
     * @param a
     * @param b
     * @param isJoin whether {@code a} and {@code b} are an equijoin
     */
    public void union(Attribute a, Attribute b, boolean isJoin) {
        UnionFindElement ufa = elementContainingAttribute(a, isJoin);
        UnionFindElement ufb = elementContainingAttribute(b, isJoin);

        //        System.out.println(a + " " + b);

        Set<Attribute> newAttributes = (isJoin) ? ufa.joinAttributes : ufa.sameTableSelectAttributes;
        if (isJoin) newAttributes.addAll(ufb.joinAttributes);
        else newAttributes.addAll(ufb.sameTableSelectAttributes);

        Integer newEq = ufa.equality != null ? ufa.equality : ufb.equality;
        Integer newMin = (newEq == null) ? max(ufa.min, ufb.min) : newEq;
        Integer newMax = (newEq == null) ? min(ufa.max, ufb.max) : newEq;

        boolean join = newEq == null && newMin == null && newMax == null;

        UnionFindElement ufe = new UnionFindElement(newAttributes, newMin, newMax, newEq, join);
        elements.put(a, ufe);
        elements.put(b, ufe);
        updateRest(ufe, join);
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(elements.values());
    }

    /**
     * Updates all attributes of a union find element that share the same stats
     * @param ufe given {@code UnionFindElement}
     * @param isJoin whether {@code a} and {@code b} are an equijoin
     */
    private void updateRest(UnionFindElement ufe, boolean isJoin) {
        if (ufe.max == ufe.min) {
            ufe.equality = ufe.max;
        }
        if (isJoin && ufe.joinAttributes != null) ufe.joinAttributes.forEach(attr -> elements.put(attr, ufe));
        if (!isJoin && ufe.sameTableSelectAttributes != null)
            ufe.sameTableSelectAttributes.forEach(attr -> elements.put(attr, ufe));
    }

    /**
     * Maximum value with {@code null} support
     * @param a
     * @param b
     * @return {@code max(a,b)}; if either is {@code null}, return the non-{@code null} one; otherwise, return {@code null}
     */
    private Integer max(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }

    /**
     * Minimum value with {@code null} support
     * @param a
     * @param b
     * @return {@code max(a,b)}; if either is {@code null}, return the non-{@code null} one; otherwise, return {@code null}
     */
    private Integer min(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.min(a, b);
    }
}

/**
 * Keeps track of multiple attributes that all fall within a range with their data.
 */
class UnionFindElement {

    /** Equijoin attributes */
    Set<Attribute> joinAttributes = new HashSet<>();

    /** Same table selection attributes */
    Set<Attribute> sameTableSelectAttributes = new HashSet<>();

    /** Maximum possible element (inclusive) of all attributes in {@code attributes} */
    Integer max;

    /** Minimum possible element (inclusive) of all attributes in {@code attributes} */
    Integer min;

    /** All attributes are equal to {@code equality} if it is not null. {@code min} and {@code max} will be updated accordingly */
    Integer equality;

    /**
     * Creates a {@code UnionFindElement} using an attribute with unknown minimum, maximum, and equality values
     * @param a
     */
    UnionFindElement(Attribute a, boolean equijoin) {
        ((equijoin) ? joinAttributes : sameTableSelectAttributes).add(a);
    }

    /**
     * Creates a {@code UnionFindElement} using a set of attributes and stats
     * @param attributes set of attributes
     * @param min minimum value
     * @param max maximum value
     * @param eq possible equality value; {@code null} if none
     * @param isJoin whether or not the relation is a join
     */
    UnionFindElement(Set<Attribute> attributes, Integer min, Integer max, Integer eq, boolean isJoin) {
        if (isJoin) this.joinAttributes = attributes;
        else this.sameTableSelectAttributes = attributes;
        this.min = min;
        this.max = max;
        this.equality = eq;
    }

    @Override
    public String toString() {
        return Arrays.asList(joinAttributes.toString(), "equals " + equality, "min " + min, "max " + max)
                .toString();
    }
}
