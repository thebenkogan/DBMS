# Database Management System

## Authors: Daniel Jann, Ben Kogan, Jacob Kraizman, Robert Zhao

### Top-Level

The top level class of our code is [Interpreter.java](./src/main/java/com/dbms/Interpreter.java)

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

### Running the Application

To run the top-level application, use the command below.

```
./gradlew run --args="[config-file-path]"
```

### Formatting Code

To have unified formatting, we used a 3rd party formatting Gradle plugin called Spotless by Diffplug. We used the Palantir Java Formatting style. We have the formatter as a pre-commit hook, so it runs every time we commit. 

#### Applying the Formatting to All Java Files

To manually run the formatter, run the command below. NOTE: this will potentially change many files, if they aren't following the formatting guidelines properly.

```
./gradlew spotlessJavaApply
```

#### Verifying that the All Java Files are Formatted Properly

To check if your sourcecode follows the formatting guidelines, run the command below.

```
./gradlew spotlessJavaCheck
```

#### Committing without Formatting

```
git commit -am [message] --no-verify
```

### Unit Tests

#### Run all Unit Tests

```
./gradlew test
```

#### Run a Specific Set of Unit Tests

```
./gradlew test --tests [test-class-name]
```

### Acknowledgements

Third-party services used to facilitate the project:

* [Guava Stopwatch](https://guava.dev/releases/18.0/api/docs/com/google/common/base/Stopwatch.html) for Benchmarking purposes
