package com.gkgeorgiev.app.synch.files;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BrokenBarrierException;

import static junit.framework.TestCase.assertFalse;


/**
 * Created by Georgi on 19-Jul-17.
 */
public class UniDirectionalSynchronizerTest {

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
    public void synchDir() throws IOException, BrokenBarrierException, InterruptedException {
        DirectorySynchronizer synchronizer = new UniDirectionalSynchronizer(srcDir, targetDir);
        assertFalse(Files.notExists(targetDir.resolve(Paths.get("subDir/subTemp.file"))));
    }

}