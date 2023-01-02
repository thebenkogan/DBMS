# Database Management System

## Authors: [Daniel Jann](https://www.github.com/stressmaster), [Ben Kogan](https://www.github.com/thebenkogan), [Jacob Kraizman](https://www.github.com/c4pt41n-V0Yag3R), [Robert Zhao](https://www.github.com/robertzhao2002)

### Overview of Features
* Query Parsing & Evaluation
    * This DBMS only supports SELECT queries with an arbitrary number of tables to join, a simple guard expression, sorting, and duplicate elimination. Tables can be joined by specifying a comma-separated list after the FROM clause. The WHERE condition supports conjunctions with simple comparisons (e.g R.A > S.B AND R.C != S.D). Columns can also be projected by adding the associated names before the FROM clause. An example query is `SELECT DISTINCT R.A, S.B FROM R, S WHERE R.A > S.B ORDER BY R.A`.
    * Queries can contain aliased table names. The alias should be specified immediately after the full table name, and there must not be any name conflicts. An example query is `SELECT * FROM Sailors S WHERE S.A = 3`.
    * The DBMS requires a configuration file specifying the input, output, and temporary directories. Input and output files are stored in a compact 4096 KB page binary format. Along with query outputs, the DBMS also produces the logical and physical query plans that were constructed to evaluate each query.
* Operators
    * The DBMS first constructs a logical plan that outlines the high-level logic that will occur during evaluation. The logical plan is then converted to a physical plan, where more fine-grained implementation details are selected based on a series of optimization algorithms. All operators can be found via a folder in the top-level of the source directory.
    * The physical plan will use either a block nested loop join operator or a sort merge join operator to evaluate a join. All sorting is done externally (previous versions have the implementation for in-memory sort). External sorting is done in the specified temporary directory. Both BNLJ and external sort are set to use 5 buffer pages. This can be configured in the [Catalog.java](./src/main/java/com/dbms/utils/Catalog.java).
* B+ Tree Indexes
    * The DBMS supports creating and deserializing B+ tree indexes. Indexes can be clustered or unclustered, with clustered indexes requiring the table be sorted by the associated key. Indexes and their order/clustering are specified in the input file. Before evaluating any queries, the DBMS will first build all indexes on file, then selectively use them to minimize the cost for evaluating a query. All index logic is in the [index](./src/main/java/com/dbms/index/) directory.
* Query Optimization
    * There are four main optimization steps when constructing a query plan: selection pushing, selection implementation, join ordering, and join implementation.
    * Selection pushing is the first optimization. The DBMS reads the WHERE expression and constructs a union find that finds transitive relationships between attributes. The union find is then queried for each specified table, extracting the most fine-grained selection expression such that the resulting data is as small as possible.
    * Selection implementation is the process of choosing whether to read a relation directly via a scan operator or to also use an index on one of the attributes. The DBMS calculates the cost for all possible methods of reading the data and chooses the one with lowest cost.
    * Join Ordering is the most complex part of the optimization process. All joins are constructed as left-deep trees, where an intermediate combination of relations is always joined with a single relation. The DBMS selects the optimal ordering of joining the tables such that the intermediate join sizes are minimized. The details are explained in the info file. At a high-level, it uses a dynamic programming algorithm to iterate through increasingly sized subsets of tables and keep track of the optimal ordering for these subsets.
    * Join Implementation is the last step in the opimization process, and it is the relatively simple decision of whether to execute the join with a BNLJ operator or a SMJ operator. Our simple heuristic is to always use SMJ on equijoins. We observed through benchmarking that SMJ often times significantly out performs BNLJ, thus we use it whenever we can.

### Top-Level

The top level class of our code is [Interpreter.java](./src/main/java/com/dbms/Interpreter.java)

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

#### Zip Command

```
make zip
```

### Acknowledgements

Third-party services used to facilitate the project:

* [Guava String Joiner](https://guava.dev/releases/18.0/api/docs/com/google/common/base/Joiner.html) for clean syntax of joining union-find strings with a new-line character
