package com.gkgeorgiev.app.synch.files;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by Georgi on 19-Jul-17.
 */
public abstract class  DirectorySynchronizer {

    protected Path sourceDir, targetDir;

    protected ExecutorService executorService;

    WatchService srcWatcher;
    WatchService targetWatcherService;

    public DirectorySynchronizer(Path source, Path target) throws IOException, BrokenBarrierException, InterruptedException {
        if (source == null || target == null ) {
            throw new IllegalArgumentException("Null argument not allowed.");
        }

        executorService = Executors.newFixedThreadPool(10);

        this.sourceDir = source;
        this.targetDir = target;

       initSynchronizer();
    }

    private void initSynchronizer() throws BrokenBarrierException, InterruptedException, IOException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1, ()-> {
            try {
                synchDir();
            } catch (IOException e) {
                e.printStackTrace(); //TODO
            }
        });

        synchDir();

        srcWatcher = sourceDir.getFileSystem().newWatchService();
        targetWatcherService = sourceDir.getFileSystem().newWatchService();

        cyclicBarrier.await(); //wait until the initial synchronization has completed and then register file watchers

        Runnable sourceWatcherRunnable = initSourceWatcher(srcWatcher);
        Runnable targetWatcherRunnable = initTargetWatcher(targetWatcherService);


        initDirectoryWatchService(sourceDir, sourceWatcherRunnable, srcWatcher);
        initDirectoryWatchService(targetDir, targetWatcherRunnable, targetWatcherService);
    }

    private void initDirectoryWatchService(Path watchDir, Runnable watchRunnable, WatchService watchService) throws IOException {
        if (watchRunnable != null) {
            Files.walk(watchDir).parallel().filter((path)->!Files.isRegularFile(path)).forEach((dir) -> {
                try {
                    dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            executorService.execute(watchRunnable);
        }
    }

    protected abstract Runnable initSourceWatcher(WatchService watchService);

    protected abstract Runnable initTargetWatcher(WatchService watchService);

    protected abstract void synchDir() throws IOException;

    @Override
    public void finalize() throws IOException {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        srcWatcher.close();
        targetWatcherService.close();
    }

    protected void copyFile(Path srcDir) throws IOException {
        //check if the same file structure exists at the target dir
        Path targetFile = this.targetDir.resolve(this.sourceDir.relativize(srcDir));

        if (Files.exists(targetFile)) {
            Path backupFile = Paths.get(targetFile.toAbsolutePath().toString() + ".bak");
            Files.deleteIfExists(backupFile);
            Files.move(targetFile, backupFile,StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.createDirectories(targetFile.getParent()); //recreating the directory structure
        }

        Files.copy(srcDir, targetFile, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    }


    protected void deleteFile(Path pathToDelete) throws IOException {
        Path targetFile = this.targetDir.resolve(this.sourceDir.relativize(pathToDelete));
        if (!Files.isDirectory(targetFile)) {
            Files.deleteIfExists(targetFile);
        }
    }
}
