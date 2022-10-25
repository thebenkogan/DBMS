package com.dbms.index;

import com.dbms.utils.Catalog;
import com.dbms.utils.TupleReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeBuilder {
    /** @param tableName (unaliased) name of table for index
     * @param attribute attribute in table schema used as index key
     * @return list of all data entries in the table, sorted by key and with RIDs sorted by pageId
     *         and tupleId
     * @throws IOException */
    public List<DataEntry> getDataEntries(String tableName, String attribute) throws IOException {
        int attributeIndex = Catalog.getColumnIndex(tableName, attribute);
        TupleReader tr = new TupleReader(Catalog.pathToTable(tableName));
        Map<Integer, List<RID>> entries = new HashMap<>();

        List<Integer> next;
        while ((next = tr.nextTuple()) != null) {
            int key = next.get(attributeIndex);
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
}
