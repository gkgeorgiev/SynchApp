import com.gkgeorgiev.app.synch.files.DirectorySynchronizer;
import com.gkgeorgiev.app.synch.files.UniDirectionalSynchronizer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by Georgi on 19-Jul-17.
 */
public class StartAPP {
    private static final Logger LOGGER = Logger.getLogger(UniDirectionalSynchronizer.class.getName());
    private static long timeCounter;

    public static Collection<DirectorySynchronizer> synchronizers;

    public static void main(String... args) {
        LOGGER.entering("StartAPP", "main", args);
        synchronizers = new ArrayList<>(args.length);

        Stream.of(args).map(ARG_TO_SYNCHRONIZER).forEach(newSynch -> synchronizers.add(newSynch));

        while(true) {
              try {
                  Thread.sleep(1000);
              } catch (InterruptedException e) {
                  LOGGER.log(Level.SEVERE, "Ups...", e);
              }
            timeCounter++;
        }
    }

    @Override
    public void finalize() {
        LOGGER.exiting("StartAPP", "main", "after "+timeCounter + " seconds");
    }


    private static final Function<String, DirectorySynchronizer> ARG_TO_SYNCHRONIZER = (arg)-> {
        DirectorySynchronizer directorySynchronizer = null;
        int splitter = arg.indexOf("->");
        if (splitter > 0) {
            Path source = Paths.get(arg.substring(0, splitter).trim());
            Path target = Paths.get(arg.substring(splitter + 2).trim());

            try {
                directorySynchronizer = new UniDirectionalSynchronizer(source.toAbsolutePath(), target.toAbsolutePath());
                synchronizers.add(directorySynchronizer);
            } catch (IOException | BrokenBarrierException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Ups...", e);
            }
        }
        return directorySynchronizer;
    };
}
