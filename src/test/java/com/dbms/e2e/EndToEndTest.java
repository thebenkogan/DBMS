package com.dbms.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dbms.Interpreter;
import com.dbms.utils.Catalog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.sf.jsqlparser.parser.ParseException;
import org.junit.jupiter.api.Test;

/** Runs end-to-end tests. */
class EndToEndTest {
    private static final String inputPath = "samples2/input";
    private static final String outputPath = "samples2/e2e_output";
    private static final String expectedOutputPath = "samples2/expected";

    /** Initializes catalog, runs the input queries file, and asserts interpreter output with
     * expected output.
     *
     * @throws IOException
     * @throws ParseException */
    @Test
    void endToEndTest() throws IOException, ParseException {
        Catalog.init(inputPath, outputPath, null);
        Interpreter.run();

        File[] outputFiles = new File(outputPath).listFiles();
        File[] expectedFiles = new File(expectedOutputPath).listFiles();
        for (int i = 0; i < outputFiles.length; i++) {
            String outputText = new String(Files.readAllBytes(Paths.get(outputFiles[i].getPath())));
            String expectedText = new String(Files.readAllBytes(Paths.get(expectedFiles[i].getPath())));
            assertEquals(outputText, expectedText);
        }
    }
}
