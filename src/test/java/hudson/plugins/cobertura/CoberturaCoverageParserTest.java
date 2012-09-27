package hudson.plugins.cobertura;

import junit.framework.TestCase;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageMetric;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.netbeans.insane.scanner.CountingVisitor;
import org.netbeans.insane.scanner.ScannerUtils;

/**
 * Unit tests for {@link CoberturaCoverageParser}.
 *
 * @author Stephen Connolly
 * @version 1.0
 * @author davidmc24
 */
public class CoberturaCoverageParserTest extends TestCase {
    public CoberturaCoverageParserTest(String name) {
        super(name);
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
        assertEquals(Messages.CoberturaCoverageParser_name(), result.getName());
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
        assertEquals(Messages.CoberturaCoverageParser_name(), result.getName());
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
    
    /**
     * Tests the memory usage of
     * {@link CoberturaCoverageParser#parse(InputStream, CoverageResult, Set)}.
     * 
     * @since 28-Apr-2009
     */
    public void testParseMemoryUsage() throws Exception {
        Map<String,Integer> files = new LinkedHashMap<String,Integer>();
        files.put("coverage.xml", 16152);
        files.put("coverage-with-data.xml", 16232);
        files.put("coverage-with-lots-of-data.xml", 298960);
        
        for (Map.Entry<String,Integer> e : files.entrySet()) {
            final String fileName = e.getKey();
            InputStream in = getClass().getResourceAsStream(fileName);
            CoverageResult result = CoberturaCoverageParser.parse(in, null, null);
            result.setOwner(null);
            assertMaxMemoryUsage(fileName + " results", result, e.getValue());
        }
    }
    
    /**
     * Tests the memory usage of a specified object.
     * The memory usage is then compared with the specified
     * maximum desired memory usage.  If the average memory usage is greater
     * than the specified number, it will be reported as a failed assertion.
     * 
     * @param description a plain-text description, to be used
     *          in diagnostic messages
     * @param o the object to measure
     * @param maxMemoryUsage the maximum desired memory usage for the Callable,
     *          in bytes 
     */
    private static void assertMaxMemoryUsage(String description, Object o, int maxMemoryUsage) throws Exception {
        CountingVisitor v = new CountingVisitor();
        ScannerUtils.scan(null, v, Collections.singleton(o), false);
        long memoryUsage = v.getTotalSize();
        String message = description + " consume " + memoryUsage + " bytes of memory on average, " + (memoryUsage - maxMemoryUsage) + " bytes more than the specified limit of " + maxMemoryUsage + " bytes";
        assertTrue(message, memoryUsage <= maxMemoryUsage);
        System.out.println(description + " consume " + memoryUsage + "/" + maxMemoryUsage + " bytes of memory");
    }

}
