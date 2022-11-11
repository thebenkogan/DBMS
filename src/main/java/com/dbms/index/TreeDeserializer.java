package com.dbms.index;

import com.dbms.utils.Catalog;
import com.dbms.utils.TupleReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/** A class that deserializes an index by first traversing to a leaf node, then walking across the
 * leaf layer to extract tuples. If the index is clustered, this reads directly from file after the
 * first traversal. */
public class TreeDeserializer {

    /** Bytes per page */
    private static final int PAGE_SIZE = 4096;

    /** flag telling whether the buffer can be used */
    private boolean memUsable;

    /** The index to deserialize */
    private Index i;

    /** Read buffer */
    private ByteBuffer buffer;

    /** The index at which to read the next integer in the buffer */
    private int bufferIndex;

    /** Input stream for the file */
    private FileInputStream fin;

    /** Channel to read the stream into the buffer */
    private FileChannel fc;

    /** Reader for the relation file */
    private TupleReader tr;

    /** Page number of the root node */
    private int rootAddress;

    /** Number of leaves in the index */
    private int numLeaves;

    /** Order of the index */
    // private int order;

    /** The current leaf node page to read next tuple. */
    private int currLeafPage;

    /** The current number of entries on leaf page. */
    private int currNumEntries;

    /** The current number of RIDs in current data entry */
    private int currNumRids;

    /** Number of data entries read in the current leaf page */
    private int currNumEntriesRead;

    /** Number of RIDs read in the current data entry */
    private int currNumRidsRead;

    public TreeDeserializer(Index i) throws IOException {
        this.i = i;
        buffer = ByteBuffer.allocate(PAGE_SIZE);
        open();

        // read header page
        readNode(0);
        rootAddress = buffer.getInt(0);
        numLeaves = buffer.getInt(4);
        // order = buffer.getInt(8);
    }

    /** Reads the index to a leaf node and extracts the first matching tuple.
     *
     * @param key the attribute key of the tuple to look up; may not be present, null for lowest key
     * @return the first tuple in the relation with the key
     * @throws IOException */
    public List<Integer> getFirstTupleAtKey(Integer key) throws IOException {
        open();
        readNode(rootAddress);

        // find leaf node
        int leafPage = 0;
        while (true) {
            int nodeType = readInt();
            if (nodeType == 0) break;
            int numKeys = readInt();
            int childIndex = numKeys;
            for (int i = 0; i < numKeys; i++) {
                int indexKey = readInt();
                if ((key == null || key < indexKey) && childIndex == numKeys) childIndex = i;
            }
            leafPage = buffer.getInt(bufferIndex + 4 * childIndex);
            readNode(leafPage);
        }

        // find data entry with matching key, go to next entry if key not found
        int numEntries = readInt();
        int entryNum = 0;
        int numRids = 0;
        for (int i = 0; i < numEntries; i++) {
            int entryKey = readInt();
            numRids = readInt();
            if (key == null || entryKey >= key) break;
            bufferIndex += 8 * numRids;
            entryNum++;
        }

        currLeafPage = leafPage;
        currNumEntries = numEntries;
        currNumEntriesRead = entryNum;
        currNumRids = numRids;
        currNumRidsRead = 1;

        // check if key not found on node, step to next node
        if (entryNum == numEntries) {
            currNumRidsRead = numRids;
            currNumEntriesRead -= 1; // hack to make stepping work
            if (!stepLeafLayer()) return null;
        }

        // read first tuple RID and return tuple
        return readTuple();
    }

    /** Reads the next tuple in the leaves. Requires getFirstTupleAtKey was called to set the
     * deserializer to the beginning of a data entry.
     *
     * @return the next tuple as ordered by the leaves
     * @throws IOException */
    public List<Integer> getNextTuple() throws IOException {
        if (!memUsable) return null;
        if (i.isClustered) {
            List<Integer> next = tr.nextTuple();
            if (next == null) close();
            return next;
        }
        if (!stepLeafLayer()) return null;

        return readTuple();
    }

    /** Reads the next two integers in the buffer and looks up the associated RID in the file.
     *
     * @return tuple at next RID
     * @throws IOException */
    private List<Integer> readTuple() throws IOException {
        int pageId = readInt();
        int tupleId = readInt();
        return tr.readTuple(new RID(pageId, tupleId));
    }

    /** Steps the leaf layer by incrementing currNumRidsRead and checking for data entry and node
     * overflow. Finishes with the buffer index at the next RID to read.
     *
     * @return true if successfully stepped to new RID, false if none left
     * @throws IOException */
    private boolean stepLeafLayer() throws IOException {
        if (currNumRidsRead == currNumRids) {
            currNumEntriesRead++;
            if (currNumEntriesRead == currNumEntries) {
                if (currLeafPage == numLeaves) {
                    close();
                    return false;
                }
                readNode(++currLeafPage);
                currNumEntriesRead = 0;
                readInt(); // discard first number indicating a leaf node
                currNumEntries = readInt();
            }
            currNumRidsRead = 0;
            readInt(); // discard entry key
            currNumRids = readInt();
        }
        currNumRidsRead++;
        return true;
    }

    /** Reads buffer at bufferIndex and increments bufferIndex by 4.
     *
     * @return integer at bufferIndex */
    private int readInt() {
        int num = buffer.getInt(bufferIndex);
        bufferIndex += 4;
        return num;
    }

    /** Clears the buffer by filling it with zeros and resetting the position to the front. */
    private void clearBuffer() {
        buffer.clear();
        buffer.put(new byte[PAGE_SIZE]); // hack to reset with zeros
        buffer.clear();
    }

    /** @param pageNumber the page number of the node to read
     * @throws IOException */
    private void readNode(int pageNumber) throws IOException {
        clearBuffer();
        fc.position(pageNumber * PAGE_SIZE);
        fc.read(buffer);
        bufferIndex = 0;
    }

    /** Opens the input stream, channel, and TupleReader if not already open
     *
     * @throws IOException */
    public void open() throws IOException {
        if (!memUsable) {
            fin = new FileInputStream(Catalog.pathToIndexFile(i.name));
            fc = fin.getChannel();
            tr = new TupleReader(Catalog.pathToTable(i.name.TABLE));
            memUsable = true;
        }
    }

    /** Closes the input stream, channel, and TupleReader if not already closed
     *
     * @throws IOException */
    public void close() throws IOException {
        if (memUsable) {
            fin.close();
            fc.close();
            tr.close();
            memUsable = false;
        }
    }
}
