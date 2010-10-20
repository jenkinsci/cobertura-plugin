package hudson.plugins.cobertura.renderers;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.plugins.cobertura.targets.CoveragePaint;
import hudson.remoting.VirtualChannel;
import hudson.util.TextFile;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
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
public class SourceCodePainter implements FilePath.FileCallable<Boolean>, Serializable {
	private final Set<String> sourcePaths;
	private final Map<String, CoveragePaint> paint;
	private final FilePath destination;
	private final BuildListener listener;
	private final SourceEncoding sourceEncoding;

	public SourceCodePainter(FilePath destination, Set<String> sourcePaths, Map<String, CoveragePaint> paint, BuildListener listener,
			SourceEncoding sourceEncoding) {
		this.destination = destination;
		this.sourcePaths = sourcePaths;
		this.paint = paint;
		this.listener = listener;
		this.sourceEncoding = sourceEncoding;
	}

	public void paintSourceCode(File source, CoveragePaint paint, FilePath canvas) throws IOException, InterruptedException {
		OutputStream os = null;

		FileInputStream is = null;
		InputStreamReader reader = null;
		BufferedReader input = null;
		OutputStreamWriter bos = null;

		BufferedWriter output = null;
		int line = 0;
		try {
			canvas.getParent().mkdirs();
			os = canvas.write();
			is = new FileInputStream(source);
			reader = new InputStreamReader(is, getSourceEncoding().getEncodingName());
			input = new BufferedReader(reader);
			bos = new OutputStreamWriter(os, "UTF-8");
			output = new BufferedWriter(bos);
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
			if (is != null) {
				is.close();
			}

			
			if (os != null) {
				os.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean invoke(File workspaceDir, VirtualChannel channel) throws IOException {
		final List<File> trialPaths = new ArrayList<File>(sourcePaths.size());
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
					paintSourceCode(source, entry.getValue(), destination.child(entry.getKey()));
				} catch (IOException e) {
					// We made our best shot at generating painted source code,
					// but alas, we failed. Log the error and continue. We
					// should not fail the build just because we cannot paint
					// one file.
					e.printStackTrace(listener.error("ERROR: Failure to paint " + source + " to " + destination));
				} catch (InterruptedException e) {
					return Boolean.FALSE;
				}
			}
		}
		return Boolean.TRUE;
	}

	public SourceEncoding getSourceEncoding() {
		if (sourceEncoding == null) {
			return SourceEncoding.UTF_8;
		}
		return sourceEncoding;
	}
}
