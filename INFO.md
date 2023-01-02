### Selection Pushing

The selection pushing is initiated in the [LogicalPlanBuilder.java](./src/main/java/com/dbms/queryplan/LogicalPlanBuilder.java), which creates a [UnionFindVisitor.java](./src/main/java/com/dbms/queryplan/UnionFindVisitor.java) to parse the WHERE expression and use the [UnionFind.java](./src/main/java/com/dbms/queryplan/UnionFind.java) to keep track of transitive equalities and the bounds.

We follow the expression visiting algorithm introduced in the handout. For each conjunct, we update the union find and keep track of any unusable expressions. We deviated slightly from the handout by also adding equalities between attributes to the unusable expressions data structure. This is because it is hard to extract this information directly from the union find, so by persisting the expression, we can easily add it back when creating the select/join condition. We will also note that we separate unusable join expressions from unusable select expressions. The difference lies in the number of tables referenced in the conjunct, and this distinction helps us easily extract one or the other without having to check the number of referenced tables later.

### Selection Implementation

The selection implementation is decided in the [PhysicalPlanBuilder.java](./src/main/java/com/dbms/queryplan/PhysicalPlanBuilder.java) when we visit a `LogicalSelectOperator`.

We followed the cost formulas in the handout, and we utilize the [Stats.java](./src/main/java/com/dbms/utils/Stats.java) class to perform most of the calculations. We start by getting all the indexes that are for the table being selected. If no indexes are available, then we cannot perform any optimizations and just make a scan and select operator. If there are indexes, then we start by getting the cost to scan the table directly. Then, for each index, we create an [IndexExpressionVisitor.java](./src/main/java/com/dbms/index/IndexExpressionVisitor.java) to parse the select condition and get the bounds for the index attribute. We compute the reduction factor and cost for using the index based on these bounds, and save the index with lowest cost. Finally, if the best index cost is lower than the scan cost, then we use the best index and create an [IndexScanOperator.java](./src/main/java/com/dbms/operators/physical/IndexScanOperator.java), potentially adding a select operator if some conjuncts are not covered by the index. Otherwise, we create a scan and select operator.

### Join Ordering

The join ordering is decided in the [JoinOrderOptimizer.java](./src/main/java/com/dbms/queryplan/JoinOrderOptimizer.java).

We followed the dynamic programming algorithm introduced in the handout. We iterate through increasing sizes of subsets. For each subset size, we generate all the combinations of table names, then compute their entry in the DP table. An entry contains the best join order, its associated cost, the size of the join (including the root), and the V-Values for each attribute in the join. Before creating this entry, we have to find the ordering of tables that minimizes the cost. To do this, we iterate through all possible tables to exclude from the current subset, and we obtain the cost by looking up the one-smaller subset in the DP table and adding it's cost and output size. Once we have the ordering with lowest cost, we compute the new output size and corresponding V-Values. These functions generally have three cases: single table scan, single table select, and a join between two tables. In each case, we follow the formulas in the handout. Instead of parsing the join expressions again to obtain the equalities, we make use of the union find to extract sets of equal attributes.

### Join Implementation

The join implementation is decided in the [PhysicalPlanBuilder.java](./src/main/java/com/dbms/queryplan/PhysicalPlanBuilder.java) via the `selectJoinImplementation` function.

We followed the simple strategy of using SMJ whenever possible. More specifically, if the join expression represents an equijoin, then we create an SMJ operator, otherwise, we create a BNLJ operator. We hardcoded 5 buffer pages for the BNLJ operator.

### Index Scan Operator Logic

Our index scan operator takes an index along with a lowkey and a highkey, and uses the [TreeDeserializer.java](./src/main/java/com/dbms/index/TreeDeserializer.java) to scan the index for keys in the inclusive range [lowkey, highkey]. Both of the bounds could be null, which means we will perform a scan to the end of the key range.

##### Setting the Lowkey and Highkey
The lowkey and highkey are set in the [PhysicalPlanBuilder.java](./src/main/java/com/dbms/visitors/PhysicalPlanBuilder.java) when we visit a logical select operator and visit the corresponding WHERE expression with the [IndexExpressionVisitor.java](./src/main/java/com/dbms/visitors/IndexExpressionVisitor.java). This visitor handles each conjunct in the expression and extracts the column name and long value, if they exist. The visitor then updates its lowkey and highkey depending on the type of binary operator. Once we have visited the whole expression, the visitor will have the final lowkey and highkey exposed and ready to use in the physical plan builder.

##### Clustered/Unclustered Index Scanning
The [TreeDeserializer.java](./src/main/java/com/dbms/index/TreeDeserializer.java) exposes a `getNextTuple()` method that behaves differently for clustered and unclustered indexes. For an unclustered index, it reads the next RID in the data entry, then looks up the associated page ID and tuple ID from file. If the index is clustered, it instead just calls getNextTuple on the [TupleReader.java](./src/main/java/com/dbms/utils/TupleReader.java). This is because the underlying data file is sorted by key, so once we find the first tuple in the key range, all following tuples are already ordered in file.

##### Root-to-leaf Tree Descent
The [TreeDeserializer.java](./src/main/java/com/dbms/index/TreeDeserializer.java) also handles the root-to-leaf tree descent of the index. This process behaves the same for both clustered and unclustered indexes. Note that the key we are searching for may not be contained in the index, in which case we look for the first data entry with a key that is greater than the search key. 

It starts off by going to the root address specificed on the header page. Next, it follows an algorithm for reading index nodes and finding the next index/leaf address as follows. It first reads the number of keys in the node, then iterates through them and finds the smallest key that is greater than the search key. If the search key is null, we use the first key, and if no key is greater than the search key, we use the last key. Let the index of this key be i. Then, the next node's address is located at index i + 1. We read this address from the index node, then repeat the algorithm for the next node. We stop when the first number on the node page is a 0, which indicates that we have found a leaf node and are finished with the descent. 

The next step is to search the leaf node for the data entry with the smallest key that is greater than or equal to the search key. This data entry may not exist (an example is looking up 6 in the index from the handout), in which case we move to the next leaf node and use its first data entry. Once we find the data entry, we are ready to read its RIDs and return tuples.

### Separating the Selection Condition

The [IndexExpressionVisitor.java](./src/main/java/com/dbms/visitors/IndexExpressionVisitor.java) needs to determine which conjuncts can be handled with the index. To solve this problem, we require each conjunct to satisfy three conditions: the first is that it contains a column with the same attribute as the index, the second is that it contains a long value, and the third is that the binary operator must be one of >, <, >=, <=, or =. If all of these conditions are met, then we can use an index and we can update our lowkey and highkey to include the long value. All conjuncts that do not meet these conditions are placed in a list. If the physical plan builder sees that this list is nonempty, it will first join them together in an AND expression, then construct a seperate select operator with the index scan operator as a child.

### Operators

The logical operators are [here](./src/main/java/com/dbms/operators/logical/), and the physical operators are [here](./src/main/java/com/dbms/operators/physical/). We convert from logical operators to physical operators using the [PhysicalPlanBuilder.java](./src/main/java/com/dbms/visitors/PhysicalPlanBuilder.java).


### Join Condition Extraction

Explanation of how we extract join conditions from the WHERE clause:
1. In the constructor for [LogicalPlanBuilder](./src/main/java/com/dbms/utils/LogicalPlanBuilder.java), we extract the from table and joins from the query.

2. We create a map in [JoinVisitor](./src/main/java/com/dbms/visitors/JoinVisitor.java) called "expressions" whose set of keys are
    - the names of the JoinItems, plus
    - the (unique) pairings between those names
and whose values are the list of Expressions referencing all tables in the key.

3. We parse the WHERE clause in JoinVisitor and use it to fill out the "expressions" map. Specifically, we take each conjunct within the WHERE clause, find the names of tables that it references, and add it to the correct running expression. For conjuncts that do not reference any table, we evaluate it to a boolean, and if false, we cut the join short and return nothing for the query.

4. In QueryPlanBuilder, we then create a left-deep tree based on the order of the from table and join tables. This left-deep tree is represented by a [JoinOperator](./src/main/java/com/dbms/operators/JoinOperator.java), whose children may also be JoinOperators.
    - We begin with the first two tables in the join. We obtain the expression corresponding to these 2 tables from the map "expressions" in JoinVisitor, and then create a JoinOperator using the names of these 2 JoinItems and that expression.
    - We then continue this process for the remaining joins; for each table, we create a new JoinOperator whose left child is the previous JoinOperator and whose right child is a scan/select operator, depending on the referenced expressions for the next join. The join condition for this join operator is the conjunction of all expressions referencing the next join and any previous join in the left subtree.

### Distinct

We implement `DISTINCT` using the sorted approach, so we never buffer tuples in memory. We just call `getNextTuple()` until the first new `Tuple` appears.

### Sort Merge Join

Below is the pseudocode for Sort Merge Join. Essentially, we keep iterating the inner and outer until either they are equal or the outer relation has no more tuples to check. We set the mark (which we call `lastEqual` in our code) whenever the inner relation is no longer equal to the outer relation. We reset the outer relation to the last position that inner and outer are equal. Our `getNextTuple()` in Sort Merge Join will always terminate because either the outer value will reach the end, or we will return a merged tuple that satisfies the equality property. We keep it in a bounded state too because we only store up to 2 tuples in memory: the left and right tuples from the sort operators. The code for Sort Merge Join can be found [here](./src/main/java/com/dbms/operators/physical/SortMergeJoinOperator.java).

```
while (outer hasn't reached the end) {
    if (lastEqual is -1) {
        while (outer value < inner value) { go to next value in outer }
        while (outer value > inner value) { go to next value in inner }
        lastEqual = position of inner
    }
    if (inner value = outer value) {
        result = merge(inner value, outer value)
        go to next value in inner
        return result
    } else {
        reset inner position to lastEqual
        go to next value in outer
        lastEqual = -1
    }
}
```
