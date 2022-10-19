package com.dbms.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/** Class for reading byte-code files containing relational data */
public class TupleReader {

    /** Bytes per page */
    private static final int PAGE_SIZE = 4096;

    /** Path to file */
    private String path;

    /** flag telling whether the buffer can be used */
    private boolean memUsable;

    /** Number of attribute per tuple in current page */
    private int numAttributes;

    /** Number of tuples on current page */
    private int numTuples;

    /** Maximum number of tuples on a page for this relation */
    private int maxTuples;

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

    /**
     * @param path (unaliased) table name (to represent file path)
     * @throws IOException
     */
    public TupleReader(String path) throws IOException {
        this.path = path;
        buffer = ByteBuffer.allocate(PAGE_SIZE);
        reset();
    }

    /**
     * Resets the reader to the first tuple in the file
     * @throws IOException
     */
    public void reset() throws IOException {
        fin = new FileInputStream(path);
        fc = fin.getChannel();
        readNextPage();
        maxTuples = numTuples;
        memUsable = true;
    }

    /**
     * @param index index of tuple to start reading from; requires the index is a valid index to a
     *              tuple that exists in the relation
     * @throws IOException
     */
    public void reset(int index) throws IOException {
        fin = new FileInputStream(path);
        fc = fin.getChannel();
        fc.position(index / maxTuples * PAGE_SIZE);
        readNextPage();
        tuplesRead = index % maxTuples;
        bufferIndex += tuplesRead * numAttributes * 4;
        memUsable = true;
    }

    /**
     * Reads the next page of data in the file. First clears the buffer, then reads the next page
     * and the metadata values. Places bufferIndex at first integer to read in file and resets
     * tuplesRead.
     * @return true if new page read, false if no more pages to read
     * @throws IOException
     */
    private boolean readNextPage() throws IOException {
        clearBuffer();
        int bytesRead = fc.read(buffer);
        if (bytesRead == -1) {
            close();
            return false;
        }
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

    /**
     * @return Integer list of data in the tuple, null if no tuples left or if file channel is
     *         closed
     * @throws IOException
     */
    public List<Integer> nextTuple() throws IOException {
        if (tuplesRead == numTuples) {
            if (!memUsable || !readNextPage()) return null;
        }
        List<Integer> data = new ArrayList<>(numAttributes);
        for (int i = 0; i < numAttributes; i++) {
            data.add(buffer.getInt(bufferIndex));
            bufferIndex += 4;
        }
        tuplesRead++;
        return data;
    }

    /**
     * Closes the reader. Call reset to restart.
     * @throws IOException
     */
    public void close() throws IOException {
        fin.close();
        fc.close();
        memUsable = false;
    }
}
