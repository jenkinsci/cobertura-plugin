package hudson.plugins.cobertura;

import java.io.Closeable;

/**
 * General IO stream manipulation utilities.
 */
public class IOUtils {
   /**
    * Closes a Closeable unconditionally.
    * Equivalent to Closeable.close(), except any exceptions will be ignored. This is typically used in finally blocks.
    */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
              closeable.close();
            } catch (Throwable t) {
            }
        }
   }
}
