package hudson.plugins.cobertura.renderers;

import hudson.plugins.cobertura.targets.CoveragePaint;
import hudson.FilePath;
import hudson.remoting.Callable;

import java.util.Set;
import java.util.Map;
import java.io.*;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 31-Aug-2007 16:52:25
 */
public class SourceCodePainter implements Callable<Boolean, IOException>, Serializable {
    private final Set<String> sourcePaths;
    private final Map<String, CoveragePaint> paint;
    private final FilePath destination;

    public SourceCodePainter(FilePath destination, Set<String> sourcePaths, Map<String, CoveragePaint> paint) {
        this.destination = destination;
        this.sourcePaths = sourcePaths;
        this.paint = paint;
    }

    public Boolean call() throws IOException {
        for (String sourcePath: sourcePaths) {
            final File source = new File(sourcePath);
            if (paint.containsKey(sourcePath) && source.exists()) {
                try {
                    paintSourceCode(source, paint.get(sourcePath), destination.child(sourcePath));
                } catch (InterruptedException e) {
                    return Boolean.FALSE;
                }
            }
        }

        return Boolean.TRUE;
    }

    public void paintSourceCode(File source, CoveragePaint paint, FilePath canvas) throws IOException, InterruptedException {
        OutputStream os = null;
        Reader reader = null;
        BufferedReader input = null;
        BufferedOutputStream bos = null;
        PrintStream output = null;
        int line = 0;
        try {
            canvas.getParent().mkdirs();
            os = canvas.write();
            reader = new FileReader(source);
            input = new BufferedReader(reader);
            bos = new BufferedOutputStream(os);
            output = new PrintStream(bos);
            String content = input.readLine();
            while (content != null) {
                line++;
                final boolean painted = paint.isPainted(line);
                if (painted) {
                    output.println("<tr style=''>");
                } else {
                    output.println("<tr>");
                }
                output.println("<td>" + line + "</td>");
                if (painted) {
                    output.println("<td/>");
                    output.println("<td/>");
                } else {
                    output.println("<td/>");
                    output.println("<td/>");
                }
                output.println("<td>" + content + "</td>");
                output.println("</tr>");
            }

        } finally {
            if (output != null) {
                output.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (input != null) {
                input.close();
            }
            if (os != null) {
                os.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }
}
