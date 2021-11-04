package hudson.plugins.cobertura.renderers;

import static hudson.plugins.cobertura.IOUtils.closeQuietly;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.cobertura.IOUtils;
import hudson.plugins.cobertura.targets.CoveragePaint;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 31-Aug-2007 16:52:25
 */
public class SourceCodePainter extends MasterToSlaveFileCallable<Map<String, String>> implements Serializable {

    private final Set<String> sourcePaths;

    private final Map<String, CoveragePaint> paint;

    private final TaskListener listener;

    private final SourceEncoding sourceEncoding;

    public SourceCodePainter(Set<String> sourcePaths, Map<String, CoveragePaint> paint, TaskListener listener,
            SourceEncoding sourceEncoding) {
        this.sourcePaths = sourcePaths;
        this.paint = paint;
        this.listener = listener;
        this.sourceEncoding = sourceEncoding;
    }

    public void paint(FilePath workspace, FilePath paintedSourcesPath) throws IOException, InterruptedException {
        Map<String, String> result = workspace.act(this);
        for (String key : paint.keySet()) {
            String content = result.get(key);
            if (content != null) {
                FilePath canvas = paintedSourcesPath.child(IOUtils.sanitizeFilename(key));
                canvas.getParent().mkdirs();
                canvas.write(content, "UTF-8");
            }
        }
    }

    private String paintSourceCode(File source, CoveragePaint paint) throws IOException {
        FileInputStream is = null;
        InputStreamReader reader = null;
        BufferedReader input = null;
        StringWriter output = new StringWriter();
        int line = 0;
        try {
            is = new FileInputStream(source);
            reader = new InputStreamReader(is, getSourceEncoding().getEncodingName());
            input = new BufferedReader(reader);
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
                            output.write("<tr class=\"coverFull\">\n");
                        } else {
                            output.write("<tr class=\"coverPart\" title=\"Line " + line + ": Conditional coverage " + coveragePercent + "% ("
                                    + branchCoverage + "/" + branchTotal + ")\">\n");
                        }
                    } else {
                        output.write("<tr class=\"coverNone\">\n");
                    }
                    output.write("<td class=\"line\"><a name='" + line + "'/>" + line + "</td>\n");
                    output.write("<td class=\"hits\">" + hits + "</td>\n");
                } else {
                    output.write("<tr class=\"noCover\">\n");
                    output.write("<td class=\"line\"><a name='" + line + "'/>" + line + "</td>\n");
                    output.write("<td class=\"hits\"/>\n");
                }
                output.write("<td class=\"code\">"
                        + content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "").replace("\r", "").replace(" ",
                        "&nbsp;").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;") + "</td>\n");
                output.write("</tr>\n");
            }

            paint.setTotalLines(line);
        } finally {
            closeQuietly(output);
            closeQuietly(input);
            closeQuietly(is);
            closeQuietly(reader);
        }
        return output.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> invoke(File workspaceDir, VirtualChannel channel) throws IOException {
        Map<String, String> r = new HashMap<>();
        final List<File> trialPaths = new ArrayList<>(sourcePaths.size());
        for (String sourcePath : sourcePaths) {
            final File trialPath = new File(sourcePath);
            if (trialPath.exists()) {
                trialPaths.add(trialPath);
            }
            final File trialPath2 = new File(workspaceDir, sourcePath);
            if (trialPath2.exists() && !trialPath2.equals(trialPath)) {
                trialPaths.add(trialPath2);
            }
        }
        for (Map.Entry<String, CoveragePaint> entry : paint.entrySet()) {
            // first see if we can find the file
            File source = new File(workspaceDir, entry.getKey());
            final Iterator<File> possiblePath = trialPaths.iterator();
            while (!source.exists() && possiblePath.hasNext()) {
                source = new File(possiblePath.next(), entry.getKey());
            }
            if (source.isFile()) {
                try {
                    r.put(entry.getKey(), paintSourceCode(source, entry.getValue()));
                } catch (IOException e) {
                    // We made our best shot at generating painted source code,
                    // but alas, we failed. Log the error and continue. We
                    // should not fail the build just because we cannot paint
                    // one file.
                    e.printStackTrace(listener.error("ERROR: Failure to paint " + source));
                }
            } else {
                listener.getLogger().println("Source file mentioned in coverage report not found: " + source);
            }
        }
        listener.getLogger().flush();
        return r;
    }

    public SourceEncoding getSourceEncoding() {
        if (sourceEncoding == null) {
            return SourceEncoding.UTF_8;
        }
        return sourceEncoding;
    }
}
