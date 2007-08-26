package hudson.plugins.cobertura;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

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
//        ProjectCoverage result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage.xml"));
//        assertNotNull(result);
//        assertEquals(ProjectCoverage.class, result.getClass());
//        assertEquals("Cobertura Coverage Report", result.getName());
////        assertEquals(10, result.getMethods());
//        assertEquals(2, result.getPackageCoverages().size());
//        PackageCoverage subResult = result.getPackageCoverages().get(0);
//        assertEquals("", subResult.getName());
//        assertEquals(1, subResult.getClasses());
//        assertEquals(Ratio.create(0, 3), subResult.getMethodCoverage());
//        assertEquals(Ratio.create(0, 11), subResult.getLineCoverage());
//        subResult = result.getPackageCoverages().get(1);
//        assertEquals("search", subResult.getName());
//        assertEquals(3, subResult.getClasses());
//        assertEquals(Ratio.create(0, 19), subResult.getLineCoverage());
//        assertEquals(Ratio.create(0, 12), subResult.getConditionalCoverage());
//        assertEquals(Ratio.create(0, 4), subResult.getMethodCoverage());
    }

    public void testParse2() throws Exception {
//        ProjectCoverage result = CoberturaCoverageParser.parse(getClass().getResourceAsStream("coverage-with-data.xml"));
//        assertNotNull(result);
//        assertEquals(ProjectCoverage.class, result.getClass());
//        assertEquals("Cobertura Coverage Report", result.getName());
////        assertEquals(10, result.getMethods());
//        assertEquals(2, result.getPackageCoverages().size());
//        PackageCoverage subResult = result.getPackageCoverages().get(0);
//        assertEquals("", subResult.getName());
//        assertEquals(1, subResult.getClasses());
//        assertEquals(Ratio.create(3, 3), subResult.getMethodCoverage());
//        assertEquals(Ratio.create(11, 11), subResult.getLineCoverage());
//        subResult = result.getPackageCoverages().get(1);
//        assertEquals("search", subResult.getName());
//        assertEquals(3, subResult.getClasses());
//        assertEquals(Ratio.create(16, 19), subResult.getLineCoverage());
//        assertEquals(Ratio.create(8, 12), subResult.getConditionalCoverage());
//        assertEquals(Ratio.create(4, 4), subResult.getMethodCoverage());
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
