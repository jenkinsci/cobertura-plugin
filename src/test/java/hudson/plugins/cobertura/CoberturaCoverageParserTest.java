package hudson.plugins.cobertura;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import hudson.plugins.cobertura.results.ProjectCoverage;
import hudson.plugins.cobertura.results.PackageCoverage;

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
            CoberturaCoverageParser.parse(null, "");
        } catch (NullPointerException e) {
            assertTrue("Expected exception thrown", true);
        }
    }

    public void testParse() throws Exception {
        ProjectCoverage result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage.xml"));
        assertNotNull(result);
        assertEquals(ProjectCoverage.class, result.getClass());
        assertEquals("Maven Cloverreport", result.getName());
        assertEquals(10, result.getMethods());
        assertEquals(1, result.getPackageCoverages().size());
        PackageCoverage subResult = result.getPackageCoverages().get(0);
        assertEquals("hudson.plugins.cobertura", subResult.getName());
        assertEquals(70, subResult.getNcloc());
    }

    public void testParseMultiPackage() throws Exception {
        ProjectCoverage result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage-two-packages.xml"));
        result = CoberturaCoverageParser.trimPaths(result, "C:\\local\\maven\\helpers\\hudson\\cobertura\\");
        assertNotNull(result);
        assertEquals(ProjectCoverage.class, result.getClass());
        assertEquals("Maven Cloverreport", result.getName());
        assertEquals(40, result.getMethods());
        assertEquals(2, result.getPackageCoverages().size());
        assertEquals(14, result.findClassCoverage("hudson.plugins.cobertura.results.AbstractCloverMetrics").getCoveredmethods());
    }

    public static Test suite() {
        return new TestSuite(CoberturaCoverageParserTest.class);
    }
}
