package hudson.plugins.cobertura;

import java.io.Closeable;

import com.google.common.base.Charsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;

/**
 * General IO stream manipulation utilities.
 */
public class IOUtils {
    /**
     * Closes a Closeable unconditionally.
     * Equivalent to Closeable.close(), except any exceptions will be ignored. This is typically used in finally blocks.
     *
     * @param closeable the Closeable to close
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
              closeable.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Sanitizes filename to prevent directory trasversals or other security threats
     * Transforms every non alphanumeric character in its ascii number in the _XX format
     *
     * @param inputName the name to sanitize
     * @return the sanitized string
     */
    public static String sanitizeFilename(String inputName) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9-]");
        Matcher m = p.matcher(inputName);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String match = m.group();
            m.appendReplacement(sb, "_" + Hex.encodeHexString(match.getBytes(Charsets.UTF_8)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
