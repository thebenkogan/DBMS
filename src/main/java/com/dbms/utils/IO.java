package com.dbms.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@SuppressWarnings("all")
public class IO {

    /** Bytes per page */
    protected static final int PAGE_SIZE = 4096;

    /** IO buffer */
    protected ByteBuffer buffer;

    /** Input stream for the file */
    protected FileInputStream fin;

    /** Output stream for the query */
    protected FileOutputStream fout;

    /** Channel to write the buffer to the IO stream */
    protected FileChannel fc;

    /** The index at which to IO the next integer in the buffer */
    protected int bufferIndex;

    /** Clears the buffer by filling it with zeros and resetting the position to the front. */
    protected void clearBuffer() {
        buffer.clear();
        buffer.put(new byte[PAGE_SIZE]); // hack to reset with zeros
        buffer.clear();
    }
}
