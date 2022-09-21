package com.dbms.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TupleReader {
    /** Bytes per page */
    private static final int PAGE_SIZE = 4096;

    /** Name of underlying table (unaliased) */
    private String tableName;

    /** Number of attribute per tuple in current page */
    private int numAttributes;

    /** Number of tuples on current page */
    private int numTuples;

    /** Read buffer */
    private ByteBuffer buffer;

    /** The index at which to read the next integer in the buffer */
    private int bufferIndex;

    /** The number of tuples read for the current page */
    private int tuplesRead;

    /** Input stream for the file */
    private FileInputStream fin;

    /** Channel to read the stream into the buffer */
    private FileChannel fc;

    /** @param tableName (unaliased) table name
     * @throws IOException */
    public TupleReader(String tableName) throws IOException {
        this.tableName = tableName;
        buffer = ByteBuffer.allocate(PAGE_SIZE);
        reset();
    }

    /** Resets the reader to the first tuple in the file
     *
     * @throws IOException */
    public void reset() throws IOException {
        fin = Catalog.getInstance().getTableStream(tableName);
        fc = fin.getChannel();
        readNextPage();
    }

    /** Reads the next page of data in the file. First clears the buffer, then reads the next page
     * and the metadata values. Places bufferIndex at first integer to read in file and resets
     * tuplesRead.
     *
     * @return true if new page read, false if no more pages to read
     * @throws IOException */
    private boolean readNextPage() throws IOException {
        clearBuffer();
        int bytesRead = fc.read(buffer);
        if (bytesRead == -1) return false;
        numAttributes = buffer.getInt(0);
        numTuples = buffer.getInt(4);
        bufferIndex = 8;
        tuplesRead = 0;
        return true;
    }

    /** Clears the buffer by filling it with zeros and resetting the position to the front. */
    private void clearBuffer() {
        buffer.clear();
        buffer.put(new byte[PAGE_SIZE]); // hack to reset with zeros
        buffer.clear();
    }

    /** @return Integer list of data in the tuple, null if no tuples left
     * @throws IOException */
    public List<Integer> nextTuple() throws IOException {
        if (tuplesRead == numTuples) {
            if (!readNextPage()) return null;
        }
        List<Integer> data = new ArrayList<>(numAttributes);
        for (int i = 0; i < numAttributes; i++) {
            data.add(buffer.getInt(bufferIndex));
            bufferIndex += 4;
        }
        tuplesRead++;
        return data;
    }

    /** Closes the reader. Call reset to restart.
     *
     * @throws IOException */
    public void close() throws IOException {
        fin.close();
        fc.close();
    }
}
