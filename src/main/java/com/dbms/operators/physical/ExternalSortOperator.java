package com.dbms.operators.physical;

import com.dbms.utils.Catalog;
import com.dbms.utils.Tuple;
import com.dbms.utils.TupleReader;
import com.dbms.utils.TupleWriter;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.UUID;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** An operator that performs an external sort algorithm on the output of the child operator and
 * opens a reader to the sorted scratch file.
 *
 * The algorithm first performs an initial pass over the data, creating sorted runs of B pages.
 *
 * It then performs merge passes until there is one scratch file remaining.
 *
 * For each merge pass: opens readers on B - 1 previous runs performs a merge sort between them by
 * buffering one tuple from each reader writes smallest of these tuples to an output file using the
 * remaining buffer */
public class ExternalSortOperator extends SortOperator {

    /** {@code child} is the child operator for external sort */
    private PhysicalOperator child;

    /** Unique identifier for this sort. Used to distinguish this sort in temp directory. */
    private String id = UUID.randomUUID().toString();

    /** Number of buffer pages */
    private int pages;

    /** Number of attributes per child tuple */
    private int numAttributes;

    /** Number of tuples that can fit on all buffer pages */
    private int tuplesPerRun;

    /** Tuple comparator for child tuples */
    private TupleComparator tc;

    /** The index of the current merge pass */
    private int mergePass;

    /** Number of merges/runs created in the previous pass */
    private int mergeLen;

    /** Reader for the final sorted merge */
    private TupleReader sortedReader;

    /** Reads all Tuples from child into table, then sorts in the order specified by orderBys.
     *
     * @param child    child operator
     * @param orderBys list of orderBys, null if none
     * @param pages    is the number of pages used for external sorting
     * @throws IOException */
    public ExternalSortOperator(PhysicalOperator child, List<OrderByElement> orderBys, int pages) throws IOException {
        super(child.schema);
        this.orderBys = orderBys;
        this.child = child;
        this.pages = pages;
        numAttributes = schema.size();
        tuplesPerRun = pages * 4096 / numAttributes * 4;
        tc = new TupleComparator(orderBys);
        Catalog.createTempSubDir(id);
        initialPass();
        mergePasses();
    }

    /** @return Tuple at index in table */
    @Override
    public Tuple getNextTuple() {
        try {
            if (sortedReader == null) return null;
            List<Integer> nextVal = sortedReader.nextTuple();
            if (nextVal == null) return null;
            return new Tuple(schema, nextVal);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** resets internal buffer index */
    @Override
    public void reset() {
        try {
            if (sortedReader != null) sortedReader.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** @param index is the block in memory to reset back to */
    @Override
    public void reset(int index) {
        try {
            if (sortedReader != null) sortedReader.reset(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** @param pass the index of the current pass
     * @param num  the index of the current run/merge in current pass
     * @return path to unique temp subdirectory with specified filename */
    private String path(int pass, int num) {
        return Catalog.pathToTempFile(id + File.separator + pass + "_" + num);
    }

    /** Executes all merge passes until single file remaining, and opens a reader on this file
     *
     * @throws IOException */
    private void mergePasses() throws IOException {
        while (mergeLen > 1) {
            int count = 0;
            for (int i = 0; i < mergeLen; i += pages - 1) {
                executeMerge(count, i);
                count++;
            }
            mergePass++;
            mergeLen = count;
        }
        sortedReader = mergeLen > 0 ? new TupleReader(path(mergePass - 1, 0)) : null;
    }

    /** @param mergeNum the number of merge in the current pass
     * @param prevStart the number of merge in the previous pass from which to start the merge
     * @throws IOException */
    private void executeMerge(int mergeNum, int prevStart) throws IOException {
        TupleWriter tw = new TupleWriter(path(mergePass, mergeNum));
        PriorityQueue<Map.Entry<TupleReader, Tuple>> queue = new PriorityQueue<>(Map.Entry.comparingByValue(tc));
        int stop = Math.min(prevStart + pages - 1, mergeLen);
        for (int j = prevStart; j < stop; j++) {
            TupleReader tr = new TupleReader(path(mergePass - 1, j));
            Tuple tp = new Tuple(schema, tr.nextTuple());
            queue.offer(new AbstractMap.SimpleEntry<>(tr, tp));
        }
        while (queue.size() > 0) {
            Entry<TupleReader, Tuple> minEntry = queue.poll();
            TupleReader minTr = minEntry.getKey();
            Tuple minTp = minEntry.getValue();
            tw.writeTuple(minTp);
            List<Integer> nextVal = minTr.nextTuple();
            if (nextVal != null) {
                Tuple next = new Tuple(schema, nextVal);
                queue.offer(new AbstractMap.SimpleEntry<>(minTr, next));
            }
        }
        tw.close();
    }

    /** Reads all of child tuples and creates sorted runs
     *
     * @throws IOException */
    private void initialPass() throws IOException {
        int run = 0;
        boolean isNextRun;
        do {
            Boolean result = executeRun(run);
            isNextRun = result != null && result;
            run++;
        } while (isNextRun);
        mergePass = 1;
        mergeLen = run;
    }

    /** Executes a run by getting up to the available number buffer pages amount of tuples from the
     * child, sorting them, and writing to a run file
     *
     * @param i run number
     * @return true if there are more child tuples for a next run, null if this did not write
     *         anything
     * @throws IOException */
    private Boolean executeRun(int i) throws IOException {
        boolean tuplesRemaining = true;
        List<Tuple> runTuples = new LinkedList<>();
        for (int j = 0; j < tuplesPerRun; j++) {
            Tuple next = child.getNextTuple();
            if (next == null) {
                tuplesRemaining = false;
                break;
            }
            runTuples.add(next);
        }
        if (runTuples.size() == 0) return null;
        TupleWriter tw = new TupleWriter(path(0, i));
        Collections.sort(runTuples, tc);
        for (Tuple t : runTuples) {
            tw.writeTuple(t);
        }
        tw.close();
        return tuplesRemaining;
    }
}
