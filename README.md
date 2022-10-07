# Database Management System

## Authors: Daniel Jann, Ben Kogan, Jacob Kraizman, Robert Zhao

### Top-Level

The top level class of our code is [Interpreter.java](./src/main/java/com/dbms/Interpreter.java)

### Operators

The logical operators are [here](./src/main/java/com/dbms/operators/logical/), and the physical operators are [here](./src/main/java/com/dbms/operators/physical/). We convert from logical operators to physical operators using the [PhysicalPlanBuilder.java](./src/main/java/com/dbms/visitors/PhysicalPlanBuilder.java).


### Join Condition Extraction

Explanation of how we extract join conditions from the WHERE clause:
1. In the constructor for [QueryPlanBuilder](./src/main/java/com/dbms/utils/QueryPlanBuilder.java), we extract the from table and joins from the query.

2. We create a map in [JoinVisitor](./src/main/java/com/dbms/visitors/JoinVisitor.java) called "expressions" whose set of keys are
    - the names of the JoinItems, plus
    - the (unique) pairings between those names
and whose values are the list of Expressions referencing all tables in the key.

3. We parse the WHERE clause in JoinVisitor and use it to fill out the "expressions" map. Specifically, we take each conjunct within the WHERE clause, find the names of tables that it references, and add it to the correct running expression. For conjuncts that do not reference any table, we evaluate it to a boolean, and if false, we cut the join short and return nothing for the query.

4. In QueryPlanBuilder, we then create a left-deep tree based on the order of the from table and join tables. This left-deep tree is represented by a [JoinOperator](./src/main/java/com/dbms/operators/JoinOperator.java), whose children may also be JoinOperators.
    - We begin with the first two tables in the join. We obtain the expression corresponding to these 2 tables from the map "expressions" in JoinVisitor, and then create a JoinOperator using the names of these 2 JoinItems and that expression.
    - We then continue this process for the remaining joins; for each table, we create a new JoinOperator whose left child is the previous JoinOperator and whose right child is a scan/select operator, depending on the referenced expressions for the next join. The join condition for this join operator is the conjunction of all expressions referencing the next join and any previous join in the left subtree.
