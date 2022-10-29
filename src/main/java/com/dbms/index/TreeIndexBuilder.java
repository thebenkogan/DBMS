package com.dbms.index;

import com.dbms.operators.physical.ExternalSortOperator;
import com.dbms.operators.physical.ScanOperator;
import com.dbms.operators.physical.SortOperator;
import com.dbms.utils.Catalog;
import com.dbms.utils.TupleReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class TreeIndexBuilder {

    /** Output writer for serializing nodes */
    private static NodeWriter nw;

    /** Order of the index */
    private static int order;

    /** The leftmost leaf keys of the previous layer */
    private static List<Integer> previousKeys;

    /** The addresses of the nodes in the previous layer */
    private static List<Integer> previousAddresses;

    /** The list of entries from the scan operation */
    private static List<DataEntry> tableEntries;

    /** @param i information about indexing from {@code index_info.txt} wrapped in {@code Index}
     */
    public static void serialize(Index i) {
        try {
            TreeIndexBuilder.order = i.order;
            nw = new NodeWriter(i.table, i.column);
            if (i.cluster) createClusters(i.table, i.column);
            tableEntries = getDataEntries(i.table, i.column);
            serializeLeaves();
            serializeIndexAndHeader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sorts the scanned table by {@code tableName.attributeName} and replaces the input file with it
     * @param tableName unaliased name of the table
     * @param attributeName name of the column
     * @throws IOException
     */
    private static void createClusters(String tableName, String attributeName) throws IOException {
        Table t = new Table(tableName);
        Column c = new Column(t, attributeName);
        OrderByElement o = new OrderByElement();
        o.setExpression(c);
        List<OrderByElement> sortCondition = Arrays.asList(o);
        ScanOperator scanOp = new ScanOperator(tableName);
        SortOperator sortOp = new ExternalSortOperator(scanOp, sortCondition, 5);
        sortOp.dump(Catalog.pathToTable(tableName));
    }

    /** @param tableName (unaliased) name of table for index
     * @param attribute attribute in table schema used as index key
     * @return list of all data entries in the table, sorted by key and with RIDs sorted by pageId
     *         and tupleId
     * @throws IOException */
    private static List<DataEntry> getDataEntries(String tableName, String attribute) throws IOException {
        int attributeIndex = Catalog.getColumnIndex(tableName, attribute);
        TupleReader tr = new TupleReader(Catalog.pathToTable(tableName));
        Map<Integer, List<RID>> entries = new HashMap<>();

        List<Integer> next;
        while ((next = tr.nextTuple()) != null) {
            int key = next.get(attributeIndex);
            if (!entries.containsKey(key)) entries.put(key, new LinkedList<>());
            entries.get(key).add(new RID(tr.pageId, tr.tupleId));
        }

        List<DataEntry> out = new ArrayList<>(entries.size());
        entries.forEach((key, rids) -> {
            Collections.sort(rids);
            out.add(new DataEntry(key, rids));
        });
        Collections.sort(out);

        return out;
    }

    /** Serializes the leaf layer by filling it with entries
     *
     * @param entries data entries of this relation
     * @throws IOException */
    private static void serializeLeaves() throws IOException {
        List<DataEntry> entries = tableEntries;
        previousKeys = new ArrayList<>();
        previousAddresses = new ArrayList<>();
        int maxCapacity = 2 * order;
        int pageNumber = 1;

        int step = maxCapacity;
        for (int i = 0; i < entries.size(); i += step) {
            step = updateStep(entries.size(), i, maxCapacity);
            List<DataEntry> leafEntries = entries.subList(i, i + step);
            nw.writeLeafNode(pageNumber, leafEntries);
            previousKeys.add(leafEntries.get(0).key);
            previousAddresses.add(pageNumber);
            pageNumber++;
        }
    }

    /** Serializes all index nodes and the header page. Assumes that the leaf layer was already
     * processed.
     *
     * @throws IOException */
    private static void serializeIndexAndHeader() throws IOException {
        int maxChildren = 2 * order + 1;
        int numLeaves = previousKeys.size();
        int pageNumber = previousAddresses.size() + 1;

        while (previousKeys.size() > 1) {
            List<Integer> nextKeys = new ArrayList<>();
            List<Integer> nextAddresses = new ArrayList<>();

            int step = maxChildren;
            for (int i = 0; i < previousKeys.size(); i += step) {
                step = updateStep(previousKeys.size(), i, maxChildren);

                List<Integer> indexKeys = previousKeys.subList(i + 1, i + step);
                List<Integer> indexAddresses = previousAddresses.subList(i, i + step);
                nw.writeIndexNode(pageNumber, indexKeys, indexAddresses);
                nextKeys.add(previousKeys.get(i));
                nextAddresses.add(pageNumber);
                pageNumber++;
            }

            previousKeys = nextKeys;
            previousAddresses = nextAddresses;
        }

        nw.writeHeaderNode(pageNumber - 1, numLeaves, order);
    }

    /** @param total total number of keys in layer
     * @param index    index of current key to consider
     * @param capacity max number of keys in a node
     * @return number of keys in next node, cut in half if second to last node */
    private static int updateStep(int total, int index, int capacity) {
        double remaining = total - index;
        int step = Math.min(capacity, (int) remaining);
        if (remaining > capacity && remaining < 3 * capacity / 2) {
            step = (int) Math.ceil(remaining / 2);
        }
        return step;
    }
}
