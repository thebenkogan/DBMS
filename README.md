# Database Management System

## Authors: Daniel Jann, Ben Kogan, Jacob Kraizman, Robert Zhao

### Top-Level

The top level class of our code is [Interpreter.java](./src/main/java/com/dbms/Interpreter.java)

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

### Benchmarking

To benchmark, we first developed a [class](./src/main/java/com/dbms/analytics/TupleGenerator.java) to randomly generate tuples. We leveraged our [TupleWriter.java](./src/main/java/com/dbms/utils/TupleWriter.java) class and used it to write randomly generated tuples to files. To keep track of execution time, we used a 3rd party library stopwatch by Google called Guava Stopwatch instead of the Java library one because it uses device native time, instead of a time from a server. The stopwatch recorded the elapsed time for each query, and we wrote the time for each query to a log file. To make benchmarking easier, we also added a function to programmatically apply the configuration, so we don't need to manually update the given config file. Finally, we defined a separate `main` function to run our benchmarking experiments a certain number of times (a command line argument). All the code can be found in the [analytics](./src/main/java/com/dbms/analytics) package.

#### Benchmarking File Structure

In order to reduce the number of command line inputs, we defined constant file paths to keep our benchmarking-related files. Set up the benchmarking file path below to avoid runtime errors.

```
.
└── project/
    ├── benchmarking/
    │   ├── input/
    │   │   └── db/
    │   │       ├── schema.txt
    │   │       └── data/
    │   │           └── [rng-data-here]
    │   ├── logs/
    │   │   └── [generated-execution-time-log-files-here]
    │   └── output/
    │       └── [generated-query-outputs-here]
```

#### Benchmarking Different Join Types

To run the benchmarking task in [JoinType.java](./src/main/java/com/dbms/analytics/JoinType.java) only once, run the command below.

```
./gradlew benchmarkJoin
```

To run the benchmarking task in [JoinType.java](./src/main/java/com/dbms/analytics/JoinType.java) a certain number of times, run the command below.

```
./gradlew benchmarkJoin --args="[number-of-trials]"
```

#### Benchmarking Data Indexing

To run the benchmarking task in [Indexing.java](./src/main/java/com/dbms/analytics/Indexing.java) only once, run the command below.

```
./gradlew benchmarkIndexing
```

To run the benchmarking task in [Indexing.java](./src/main/java/com/dbms/analytics/Indexing.java) a certain number of times, run the command below.

```
./gradlew benchmarkIndexing --args="[number-of-trials]"
```

### Running the Application

Because we have multiple executable `main` functions, we defined 2 new Gradle tasks in our [build.gradle.kts](build.gradle.kts) file to run either one. To run the `main` function in [Interpreter.java](./src/main/java/com/dbms/Interpreter.java), you can run either of the commands below (the default `main` function is still set to the one in [Interpreter.java](./src/main/java/com/dbms/Interpreter.java)).

#### Interpreter as its own Gradle task

```
./gradlew interpreter --args="[input-directory] [output-directory] [temp-directory]"
```

#### Interpreter as the default Gradle run task

```
./gradlew run --args="[input-directory] [output-directory] [temp-directory]"
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
