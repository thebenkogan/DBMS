package DBMS.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import DBMS.utils.Catalog;
import DBMS.utils.QueryPlanBuilder;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;

class EndToEnd {

    private static final String queriesFile= "samples/input/queries_supported.sql";
    private static final String inputPath= "samples/input";
    private static final String outputPath= "samples/e2e_output";
    private static final String expectedOutputPath= "samples/expected_output";

    @Test
    void endToEndTest() throws IOException {
        try {
            Catalog.init(inputPath);
            CCJSqlParser parser= new CCJSqlParser(new FileReader(queriesFile));
            int i= 1;
            Statement statement;
            while ((statement= parser.Statement()) != null) {
                QueryPlanBuilder queryPlan= new QueryPlanBuilder(statement);
                File file= new File(outputPath + File.separator + "query" + i);
                file.getParentFile().mkdirs();
                FileWriter writer= new FileWriter(file);
                queryPlan.operator.dump(writer);
                i++ ;
            }
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }

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