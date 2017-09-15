package com.gkgeorgiev.app.synch.files;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Created by Georgi on 19-Jul-17.
 */
public class UniDirectionalSynchronizer extends DirectorySynchronizer {
    private static final Logger LOGGER = Logger.getLogger(UniDirectionalSynchronizer.class.getName());

    public UniDirectionalSynchronizer(Path source, Path target) throws IOException, BrokenBarrierException, InterruptedException {
        super(source, target);
    }

    @Override
    public Runnable initSourceWatcher(WatchService watchService) {
        return () ->{
            //TODO not tested well
               /* while (true) {
                    LOGGER.fine("watch again " );
                    try {
                        WatchKey key = watchService.take();

                        key.pollEvents().forEach(watchEvent -> {
                            WatchEvent.Kind<?> kind = watchEvent.kind();
                            // The filename is the
                            // context of the event.
                            WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
                            Path newFile = ev.context();
                            newFile = ((Path)key.watchable()).resolve(newFile); //build the absolute path since the watch event point only to a relative one

                            LOGGER.log(FINE, "Event: {0}; Path: {1}", new Object[]{ kind, newFile});

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
                                LOGGER.info("Operation completed");
                            } catch (IOException e) {
                                LOGGER.log(SEVERE, "Ups...", e);
                            }
                        });

                        key.reset();
                    } catch (InterruptedException e) {
                        LOGGER.log(SEVERE, "Ups...", e);
                    }
                }*/
        };
    }

    @Override
    public Runnable initTargetWatcher(WatchService watchService) {
        return null; //we don't care about changes on target folder
    }



    @Override
    public void synchDir() throws IOException {
        LOGGER.entering(UniDirectionalSynchronizer.class.getName(), "synchDir", sourceDir);

        Files.walk(sourceDir).parallel().filter((path)->Files.isRegularFile(path)).forEach(path -> {
            try {
                copyFile(path);
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Ups...", e);
            }
        });
        LOGGER.exiting(UniDirectionalSynchronizer.class.getName(), "synchDir");
    }
}
