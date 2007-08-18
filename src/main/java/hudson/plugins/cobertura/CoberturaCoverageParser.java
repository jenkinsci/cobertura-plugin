package hudson.plugins.cobertura;

import hudson.plugins.cobertura.results.ClassCoverage;
import hudson.plugins.cobertura.results.FileCoverage;
import hudson.plugins.cobertura.results.PackageCoverage;
import hudson.plugins.cobertura.results.ProjectCoverage;
import hudson.Util;
import hudson.model.Result;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 03-Jul-2007 09:03:30
 */
public class CoberturaCoverageParser {

    /** Do not instantiate CoberturaCoverageParser. */
    private CoberturaCoverageParser() {
    }

    public static ProjectCoverage trimPaths(ProjectCoverage result, String pathPrefix) {
        if (result == null) throw new NullPointerException();
        if (pathPrefix == null) return result;
        for (PackageCoverage p: result.getPackageCoverages()) {
            for (FileCoverage f: p.getFileCoverages()) {
                if (f.getName().startsWith(pathPrefix)) {
                    f.setName(f.getName().substring(pathPrefix.length()));
                }
                f.setName(f.getName().replace('\\', '/'));
                for (ClassCoverage c: f.getClassCoverages()) {
                    c.setName(p.getName() + "." + c.getName());
                }
            }
        }
        return result;
    }

    public static ProjectCoverage parse(File inFile, String pathPrefix) throws IOException {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            fileInputStream = new FileInputStream(inFile);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            CoberturaCoverageParser parser = new CoberturaCoverageParser();
            return trimPaths(parse(bufferedInputStream), pathPrefix);
        } finally {
            try {
                if (bufferedInputStream != null)
                    bufferedInputStream.close();
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static ProjectCoverage parse(InputStream in) throws IOException {
        if (in == null) throw new NullPointerException();
        Digester digester = new Digester();
        digester.setClassLoader(CoberturaCoverageParser.class.getClassLoader());
        digester.addObjectCreate("coverage/project", ProjectCoverage.class);
        digester.addSetProperties("coverage/project");
        digester.addSetProperties("coverage/project/metrics");

        digester.addObjectCreate("coverage/project/package", PackageCoverage.class);
        digester.addSetProperties("coverage/project/package");
        digester.addSetProperties("coverage/project/package/metrics");
        digester.addSetNext("coverage/project/package", "addPackageCoverage", PackageCoverage.class.getName());

        digester.addObjectCreate("coverage/project/package/file", FileCoverage.class);
        digester.addSetProperties("coverage/project/package/file");
        digester.addSetProperties("coverage/project/package/file/metrics");
        digester.addSetNext("coverage/project/package/file", "addFileCoverage", FileCoverage.class.getName());

        digester.addObjectCreate("coverage/project/package/file/class", ClassCoverage.class);
        digester.addSetProperties("coverage/project/package/file/class");
        digester.addSetProperties("coverage/project/package/file/class/metrics");
        digester.addSetNext("coverage/project/package/file/class", "addClassCoverage", ClassCoverage.class.getName());

        try {
            return (ProjectCoverage) digester.parse(in);
        } catch (SAXException e) {
            throw new IOException("Cannot parse coverage results", e);
        }
    }
}
