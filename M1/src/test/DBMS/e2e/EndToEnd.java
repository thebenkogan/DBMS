package DBMS.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import DBMS.Interpreter;
import DBMS.utils.Catalog;
import net.sf.jsqlparser.parser.ParseException;

class EndToEnd {

    private static final String inputPath= "samples/input";
    private static final String outputPath= "samples/e2e_output";
    private static final String expectedOutputPath= "samples/expected_output";

    @Test
    void endToEndTest() throws IOException, ParseException {
        Catalog.init(inputPath, outputPath);
        Interpreter.run();

        File[] outputFiles= (new File(outputPath)).listFiles();
        File[] expectedFiles= (new File(expectedOutputPath)).listFiles();
        for (int i= 0; i < outputFiles.length; i++ ) {
            String outputText= new String(Files.readAllBytes(Paths.get(outputFiles[i].getPath())));
            String expectedText= new String(
                Files.readAllBytes(Paths.get(expectedFiles[i].getPath())));
            assertEquals(outputText, expectedText);
        }
    }
}