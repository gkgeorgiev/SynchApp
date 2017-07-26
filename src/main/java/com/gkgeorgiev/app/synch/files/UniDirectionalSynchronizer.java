package com.gkgeorgiev.app.synch.files;


import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.BrokenBarrierException;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by Georgi on 19-Jul-17.
 */
public class UniDirectionalSynchronizer extends DirectorySynchronizer {

    public UniDirectionalSynchronizer(Path source, Path target) throws IOException, BrokenBarrierException, InterruptedException {
        super(source, target);
    }

    @Override
    public Runnable initSourceWatcher(WatchService watchService) {
        return () ->{
                while (true) {
                    System.out.println("watch again " );
                    try {
                        WatchKey key = watchService.take();

                        key.pollEvents().forEach(watchEvent -> {
                            WatchEvent.Kind<?> kind = watchEvent.kind();
                            // The filename is the
                            // context of the event.
                            WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
                            Path newFile = ev.context();
                            newFile = ((Path)key.watchable()).resolve(newFile); //build the absolute path since the watch event point only to a relative one
                            try {
                                if (ENTRY_CREATE.equals(kind)) {
                                    if (Files.isDirectory(newFile)) {
                                        newFile.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                                    } else if (Files.isRegularFile(newFile)) {
                                        copyFile(newFile);
                                    }
                                } else if (ENTRY_DELETE.equals(kind)) {
                                    deleteFile(newFile);
                                } else if (ENTRY_MODIFY.equals(kind)) {
                                    if (Files.isRegularFile(newFile)) {
                                        copyFile(newFile);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        key.reset();
                    } catch (InterruptedException e) {
                        e.printStackTrace();//todo logging
                    }
                }
        };
    }

    @Override
    public Runnable initTargetWatcher(WatchService watchService) {
        return null; //we don't care about changes on target folder
    }



    @Override
    public void synchDir() throws IOException {
//        synchDir(sourceDir, targetDir);
        Files.walk(sourceDir).parallel().filter((path)->Files.isRegularFile(path)).forEach(path -> {
            try {
                copyFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
