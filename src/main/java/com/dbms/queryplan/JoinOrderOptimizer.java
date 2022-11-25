package com.dbms.queryplan;

import com.dbms.operators.logical.LogicalJoinOperator;
import com.dbms.operators.logical.LogicalOperator;
import com.dbms.operators.logical.LogicalScanOperator;
import com.dbms.operators.logical.LogicalSelectOperator;
import com.dbms.utils.Attribute;
import com.dbms.utils.Catalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Optimizes the join order by using the dynamic programming to build up the best join ordering
 * with lowest intermediate sizes. */
public final class JoinOrderOptimizer {

    /** List of all (aliased) table names as they appear in the query */
    List<String> allNames = new ArrayList<>();

    /** maps (aliased) table name to child operator */
    Map<String, LogicalOperator> children;

    /** Table to map subsets of table names to their optimal order and the cost of that ordering */
    Map<String, DPTuple> dpTable = new HashMap<>();

    /** list of transitively equal attribute sets as determined by the union find */
    List<Set<Attribute>> equalitySets;

    /** the UF visitor that parsed this query's expression */
    UnionFindVisitor uv;

    public JoinOrderOptimizer(LogicalJoinOperator joinOp) {
        allNames = new ArrayList<>(joinOp.tableNames);
        uv = joinOp.uv;
        equalitySets = uv.unionFind.getAllAttributeSets();
        children = joinOp.children;
        calculateDPTable();
    }

    /** Constructs a key from a list of table names to lookup their corresponding values in the
     * DPTable.
     *
     * @param names list of (aliased) table names
     * @return DPTable key */
    public String namesToKey(List<String> names) {
        Collections.sort(names);
        return names.toString();
    }

    /** Fills the DP Table, where the keys are the power set of the children of the logical join,
     * and the values are the tuples containing the optimal join ordering, associated cost and
     * output size, and a V-Value map for each attribute. */
    public void calculateDPTable() {
        // for every possible subset size i
        for (int i = 1; i <= allNames.size(); i++) {
            // fill subsetsOfSizeI with all subsets of size i
            List<List<String>> subsetsOfSizeI = new ArrayList<>();
            generateCombinationOfSize(subsetsOfSizeI, i);

            for (List<String> subset : subsetsOfSizeI) {
                // fill the DP table for every subset of size i
                dpTable.put(namesToKey(subset), valuesForSubset(subset));
            }
        }
    }

    /** Gets the best ordering for the tables
     *
     * @return List of (aliased) table names representing the best ordering */
    public List<String> getBestOrder() {
        return dpTable.get(namesToKey(allNames)).joins;
    }

    /** @param order the optimal order of tables
     * @return DP Tuple for this ordering */
    private DPTuple createDPTuple(List<String> order) {
        String innerTableName = order.get(order.size() - 1);
        DPTuple outerTuple = dpTable.get(namesToKey(order.subList(0, order.size() - 1)));
        double size = computeSize(outerTuple, innerTableName);
        return new DPTuple(order, 0, createVSet(outerTuple, innerTableName, size), size);
    }

    /** Calculates the best ordering and cost for a subset, using a recurrence based on smaller
     * subsets
     *
     * @param subset the subset which we calculate the optimal ordering and cost of
     * @return a DPTuple with the best ordering and cost of the subset */
    private DPTuple valuesForSubset(List<String> subset) {
        if (subset.size() == 1) {
            // base case: cost is 0, there is only one order
            return createDPTuple(subset);
        } else if (subset.size() == 2) {
            // secondary base case: cost is 0, order: smaller relation goes on the outside
            if (Catalog.STATS.numRows(subset.get(0)) > Catalog.STATS.numRows(subset.get(1))) {
                Collections.reverse(subset);
            }
            return createDPTuple(subset);
        } else {
            // recursive case:
            // compare all the subsets which are 1 smaller than the current
            // eg. if current subset is ABCD, then take the minimum cost out of
            // BCD, ACD, ABD, and ABC
            int indexOfBestExclusion = -1;
            double minimumCost = Double.MAX_VALUE;
            DPTuple bestPrevTuple = null;
            for (int exclude = 0; exclude < subset.size(); exclude++) {
                List<String> smallerSubset = new ArrayList<>(subset);
                smallerSubset.remove(exclude);
                DPTuple prevTuple = dpTable.get(namesToKey(smallerSubset));
                if (prevTuple.cost + prevTuple.size < minimumCost) {
                    minimumCost = prevTuple.cost + prevTuple.size;
                    indexOfBestExclusion = exclude;
                    bestPrevTuple = prevTuple;
                }
            }

            // given the best predecessor subset, return the DPTuple for this subset
            List<String> bestOrder = new ArrayList<>(bestPrevTuple.joins);
            bestOrder.add(subset.get(indexOfBestExclusion));
            return createDPTuple(bestOrder);
        }
    }

    /** @param val the value to clamp
     * @param size the size of the join (upper bound), null if unbound
     * @return clamped value in the range [1, size] */
    private double clamp(double val, Double size) {
        double upper = size != null ? size : Double.MAX_VALUE;
        return Math.max(Math.min(val, upper), 1);
    }

    /** @param prevTuple previous tuple with best cost, null if single table
     * @param innerTable new inner table to add to join
     * @return size of this join with the inner table */
    private double computeSize(DPTuple prevTuple, String innerTable) {
        if (prevTuple == null) {
            // find the table which corresponds with onlyTable
            LogicalOperator logicalChild = children.get(innerTable);
            if (logicalChild instanceof LogicalScanOperator) {
                // case 1: scan operator
                return Catalog.STATS.numRows(innerTable);
            }
            // case 2: select operator
            double baseSize = Catalog.STATS.numRows(innerTable);
            for (Attribute a : Catalog.getAliasedAttributes(innerTable)) {
                int extent = uv.unionFind.getAttributeExtent(a);
                double rfactor = Catalog.STATS.getReductionFactor(a, extent);
                baseSize *= rfactor;
            }
            return clamp(baseSize, null);
        } else {
            // case 3: join
            DPTuple innerTuple = dpTable.get(namesToKey(Arrays.asList(innerTable)));
            List<Set<Attribute>> joinEqualitySets = uv.getJoinEqualities(innerTable, prevTuple.joins);
            double denominator = 1;
            // iterate through all equality sets, getting max V-Value and multiplying denominator
            for (Set<Attribute> eqSet : joinEqualitySets) {
                double bestV = Double.MIN_VALUE;
                for (Attribute a : eqSet) {
                    double v = innerTuple.VSet.containsKey(a) ? innerTuple.VSet.get(a) : prevTuple.VSet.get(a);
                    bestV = Math.max(v, bestV);
                }
                denominator *= bestV;
            }
            return clamp(innerTuple.size * prevTuple.size / denominator, null);
        }
    }

    /** @param prevTuple previous tuple representing outer tables
     * @param innerTable inner table to add to the join
     * @param size       the size of this join
     * @return V-Value set for each attribute in the join */
    private Map<Attribute, Double> createVSet(DPTuple prevTuple, String innerTable, double size) {
        Map<Attribute, Double> vSet;
        if (prevTuple == null) {
            vSet = new HashMap<>();
            // find the table which corresponds with onlyTable
            LogicalOperator logicalChild = children.get(innerTable);
            if (logicalChild instanceof LogicalScanOperator) {
                // case 1: scan operator
                for (Attribute a : Catalog.getAliasedAttributes(innerTable)) {
                    vSet.put(a, clamp(Catalog.STATS.baseTableV(a), size));
                }
            } else if (logicalChild instanceof LogicalSelectOperator) {
                // case 2: select operator
                for (Attribute a : Catalog.getAliasedAttributes(innerTable)) {
                    double baseCost = Catalog.STATS.baseTableV(a);
                    int extent = uv.unionFind.getAttributeExtent(a);
                    double rfactor = Catalog.STATS.getReductionFactor(a, extent);
                    // clamp to estimated join size
                    vSet.put(a, clamp(baseCost * rfactor, size));
                }
            }
        } else {
            // case 3: join
            DPTuple innerTuple = dpTable.get(namesToKey(Arrays.asList(innerTable)));
            Set<String> outerTables = new HashSet<>(prevTuple.joins);
            List<Set<Attribute>> joinEqualitySets = getJoinEqualitySets(innerTable, outerTables);
            vSet = prevTuple.VSet;
            vSet.putAll(innerTuple.VSet);
            // iterate through all equality sets, setting all attribute V-Values to lowest in set
            for (Set<Attribute> eqSet : joinEqualitySets) {
                double minV = Double.MAX_VALUE;
                for (Attribute a : eqSet) minV = Math.min(vSet.get(a), minV);
                for (Attribute a : eqSet) vSet.put(a, minV);
            }
            // clamp down new join V-values
            vSet.forEach((a, v) -> vSet.put(a, clamp(v, size)));
        }
        return vSet;
    }

    /** @param name inner table name
     * @param seen set of table names in outer table
     * @return Sets of attributes joined by equality this join */
    public List<Set<Attribute>> getJoinEqualitySets(String name, Set<String> seen) {
        List<Set<Attribute>> joinEqualitySets = new LinkedList<>();
        for (Set<Attribute> s : equalitySets) {
            boolean hasInner = false;
            boolean hasOuter = false;
            for (Attribute a : s) {
                hasInner |= a.TABLE.equals(name);
                hasOuter |= seen.contains(a.TABLE);
                if (hasInner && hasOuter) break;
            }
            if (hasInner && hasOuter) {
                Set<Attribute> filtered = new HashSet<>(s); // dereference from original set
                filtered.removeIf(a -> !a.TABLE.equals(name) && !seen.contains(a.TABLE));
                joinEqualitySets.add(filtered);
            }
        }
        return joinEqualitySets;
    }

    /** Helper for generating combinations. We store these combinations in the parameter
     * subsetsOfSizeI
     *
     * @param subsetsOfSizeI list in which we store all subsets of size i that we find
     * @param n              the size of the list
     * @param r              the size of the combinations to find
     * @param index          current index in data
     * @param data           list for temporarily storing subsets
     * @param i              the current start */
    private void combinationUtil(List<List<String>> subsetsOfSizeI, int r, int index, List<String> data, int i) {
        // Current combination is ready, store to subsets
        if (index == r) {
            subsetsOfSizeI.add(new ArrayList<>(data.subList(0, index)));
            return;
        }

        if (i >= allNames.size()) return;

        data.add(index, allNames.get(i));
        combinationUtil(subsetsOfSizeI, r, index + 1, data, i + 1);
        combinationUtil(subsetsOfSizeI, r, index, data, i + 1);
    }

    /** Generates all subsets of a certain size and stores them in the input list
     *
     * @param subsetsOfSizeI list in which we store all subsets of size r
     * @param r              the size of the subsets */
    private void generateCombinationOfSize(List<List<String>> subsetsOfSizeI, int r) {
        // A temporary array to store combinations
        List<String> data = new ArrayList<>(r);

        combinationUtil(subsetsOfSizeI, r, 0, data, 0);
    }
}

/** Represents an entry in the DP table, storing the optimal order, output size, cost, and
 * V-Values. */
class DPTuple {

    /** Best ordering of (aliased) tables for the join */
    List<String> joins;

    /** Cost of this ordering */
    double cost;

    /** V-Values for each attribute in final join output */
    Map<Attribute, Double> VSet;

    /** Size of final join output */
    double size;

    public DPTuple(List<String> argJoins, double argCost, Map<Attribute, Double> argVSet, double size) {
        joins = argJoins;
        cost = argCost;
        VSet = argVSet;
        this.size = size;
    }
}
