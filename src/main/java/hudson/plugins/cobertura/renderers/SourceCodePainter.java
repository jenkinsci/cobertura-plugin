package hudson.plugins.cobertura.renderers;

import hudson.FilePath;
import hudson.plugins.cobertura.targets.CoveragePaint;
import hudson.remoting.VirtualChannel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 31-Aug-2007 16:52:25
 */
public class SourceCodePainter implements FilePath.FileCallable<Boolean>, Serializable {
    private final Set<String> sourcePaths;
    private final Map<String, CoveragePaint> paint;
    private final FilePath destination;

    public SourceCodePainter(FilePath destination, Set<String> sourcePaths, Map<String, CoveragePaint> paint) {
        this.destination = destination;
        this.sourcePaths = sourcePaths;
        this.paint = paint;
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
            String content;
            while ((content = input.readLine()) != null) {
                line++;

                if (paint.isPainted(line)) {
                    final int hits = paint.getHits(line);
                    final int branchCoverage = paint.getBranchCoverage(line);
                    final int branchTotal = paint.getBranchTotal(line);
                    final int coveragePercent = (hits == 0) ? 0 : (int) (branchCoverage * 100.0 / branchTotal);
                    if (paint.getHits(line) > 0) {
                        if (branchTotal == branchCoverage) {
                            output.println("<tr class=\"coverFull\">");
                        } else {
                            output.println("<tr class=\"coverPart\" title=\"Line "
                                    + line + ": Conditional coverage " + coveragePercent + "% ("
                                    + branchCoverage + "/" + branchTotal + ")\">");
                        }
                    } else {
                        output.println("<tr class=\"coverNone\">");
                    }
                    output.println("<td class=\"line\">" + line + "</td>");
                    output.println("<td class=\"hits\">" + hits + "</td>");
                } else {
                    output.println("<tr class=\"noCover\">");
                    output.println("<td class=\"line\">" + line + "</td>");
                    output.println("<td class=\"hits\"/>");
                }
                output.println("<td class=\"code\">" + content.replaceAll("\\&", "&amp;").replaceAll("\\<", "&lt;")
                        .replaceAll("\\>", "&gt;").replaceAll("\\\\[nr]", "").replace(" ", "&nbsp;")
                        .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
                        + "</td>");
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

    public Boolean invoke(File workspaceDir, VirtualChannel channel) throws IOException {
        for (Map.Entry<String, CoveragePaint> entry : paint.entrySet()) {
            // first see if we can find the file
            File source = new File(workspaceDir, entry.getKey());
            Iterator<String> possiblePath = sourcePaths.iterator();
            while (!source.exists() && possiblePath.hasNext()) {
                source = new File(possiblePath.next(), entry.getKey());
            }
            if (source.exists()) {
                try {
                    paintSourceCode(source, entry.getValue(), destination.child(entry.getKey()));
                } catch (InterruptedException e) {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }
}
