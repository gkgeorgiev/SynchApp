import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Created by Georgi on 23-Jul-17.
 */
public class StartAPPTest {

    Path srcDir, targetDir;

    @Before
    public void setUp() throws IOException {
        srcDir = Files.createTempDirectory("temp");
        Files.createTempFile(srcDir, "temp", "file");
        Path subDir = Files.createTempDirectory(srcDir, "subDir");
        Files.createTempFile(subDir, "subTemp", "file");
        targetDir = Files.createTempDirectory("tmp");
    }

    @Test
    public void main() throws Exception {
        StartAPP.main(srcDir.toAbsolutePath() + "->" + targetDir.toAbsolutePath());
        assertEquals(1, StartAPP.synchronizers.size());
    }

}