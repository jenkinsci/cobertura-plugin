package hudson.plugins.cobertura;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageMetric;

import java.io.InputStream;
import java.util.Set;
import java.util.HashSet;

/**
 * CloverCoverageParser Tester.
 *
 * @author Stephen Connolly
 * @version 1.0
 */
public class CoberturaCoverageParserTest extends TestCase {
    public CoberturaCoverageParserTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFailureMode1() throws Exception {
        try {
            CoberturaCoverageParser.parse((InputStream)null, null);
        } catch (NullPointerException e) {
            assertTrue("Expected exception thrown", true);
        }
    }

    public void print(CoverageResult r, int d) {
        System.out.print("                    ".substring(0, d*2));
        System.out.print(r.getElement() + "[" + r.getName() + "]");
        for (CoverageMetric m : r.getMetrics()) {
            System.out.print(" " + m + "=" + r.getCoverage(m));
        }
        System.out.println();
        for (String child: r.getChildren()) {
            print(r.getChild(child), d + 1);
        }
    }

    public void testParse() throws Exception {
        Set<String> paths = new HashSet<String>();
        CoverageResult result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage.xml"), null, paths);
        result.setOwner(null);
        print(result, 0);
        assertNotNull(result);
        assertEquals(CoverageResult.class, result.getClass());
        assertEquals("Cobertura Coverage Report", result.getName());
//        assertEquals(10, result.getMethods());
        assertEquals(2, result.getChildren().size());
        CoverageResult subResult = result.getChild("<default>");
        assertEquals("<default>", subResult.getName());
        assertEquals(1, subResult.getChildren().size());
        assertEquals(Ratio.create(0, 3), subResult.getCoverage(CoverageMetric.METHOD));
        assertEquals(Ratio.create(0, 11), subResult.getCoverage(CoverageMetric.LINE));
        subResult = result.getChild("search");
        assertEquals("search", subResult.getName());
        assertEquals(3, subResult.getChildren().size());
        assertEquals(Ratio.create(0, 19), subResult.getCoverage(CoverageMetric.LINE));
        assertEquals(Ratio.create(0, 12), subResult.getCoverage(CoverageMetric.CONDITIONAL));
        assertEquals(Ratio.create(0, 4), subResult.getCoverage(CoverageMetric.METHOD));
        assertEquals(1, paths.size());
    }

    public void testParse2() throws Exception {
        CoverageResult result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage-with-data.xml"), null);
        result.setOwner(null);
        print(result, 0);
        assertNotNull(result);
        assertEquals(CoverageResult.class, result.getClass());
        assertEquals("Cobertura Coverage Report", result.getName());
//        assertEquals(10, result.getMethods());
        assertEquals(2, result.getChildren().size());
        CoverageResult subResult = result.getChild("<default>");
        assertEquals("<default>", subResult.getName());
        assertEquals(1, subResult.getChildren().size());
        assertEquals(Ratio.create(3, 3), subResult.getCoverage(CoverageMetric.METHOD));
        assertEquals(Ratio.create(11, 11), subResult.getCoverage(CoverageMetric.LINE));
        subResult = result.getChild("search");
        assertEquals("search", subResult.getName());
        assertEquals(3, subResult.getChildren().size());
        assertEquals(Ratio.create(16, 19), subResult.getCoverage(CoverageMetric.LINE));
        assertEquals(Ratio.create(9, 12), subResult.getCoverage(CoverageMetric.CONDITIONAL));
        assertEquals(Ratio.create(4, 4), subResult.getCoverage(CoverageMetric.METHOD));
    }

    public void testParseMultiPackage() throws Exception {
//        ProjectCoverage result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage-two-packages.xml"));
//        result = CoberturaCoverageParser.trimPaths(result, "C:\\local\\maven\\helpers\\hudson\\cobertura\\");
//        assertNotNull(result);
//        assertEquals(ProjectCoverage.class, result.getClass());
//        assertEquals("Maven Cloverreport", result.getName());
////        assertEquals(40, result.getMethods());
//        assertEquals(2, result.getPackageCoverages().size());
////        assertEquals(14, result.findClassCoverage("hudson.plugins.cobertura.results.AbstractCloverMetrics").getCoveredmethods());
    }

    public static Test suite() {
        return new TestSuite(CoberturaCoverageParserTest.class);
    }
}
