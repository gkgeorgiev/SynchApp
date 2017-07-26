import com.gkgeorgiev.app.synch.files.DirectorySynchronizer;
import com.gkgeorgiev.app.synch.files.UniDirectionalSynchronizer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by Georgi on 19-Jul-17.
 */
public class StartAPP {

    public static Collection<DirectorySynchronizer> synchronizers;

    public static void main(String... args) {
        synchronizers = new ArrayList<>(args.length);

        Stream.of(args).forEach(arg->{
            int splitter = arg.indexOf("->");
            if ( splitter > 0) {
                Path source = Paths.get(arg.substring(0, splitter));
                Path target = Paths.get(arg.substring(splitter+2));
                try {
                    synchronizers.add(new UniDirectionalSynchronizer(source.toAbsolutePath(), target.toAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        while(true) {
            System.out.print(".");
              try {
                  Thread.sleep(10000);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
      }

    }
}
