package hudson.plugins.cobertura;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageMetric;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

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
        Map<String, Long> files = new LinkedHashMap<String, Long>();
        files.put("coverage.xml", 100000L);
        files.put("coverage-with-data.xml", 100000L);
        files.put("coverage-with-lots-of-data.xml", 2500000L);
        
        for(Entry<String, Long> e : files.entrySet()) {
            final String fileName = e.getKey();
            long maxMemory = e.getValue();
            Callable<CoverageResult> callable = new Callable<CoverageResult>() {
                public CoverageResult call() throws Exception {
                    InputStream in = getClass().getResourceAsStream(fileName);
                    CoverageResult result = CoberturaCoverageParser.parse(in, null, null);
                    result.setOwner(null);
                    return result;
                }
            };
            assertMaxMemoryUsage(fileName + " results", callable, maxMemory);
        }
    }
    
    /**
     * Tests the memory usage of a specified Callable.  The Callable should
     * return the object that it is generating, so that it is not garbage
     * collected until the proper point in the test.  The callable will be
     * called 6 times.  The results of the first call are be ignored, to avoid
     * pollution of the results by class initialization.  The amount of memory
     * used in the other runs are averaged to produce the average memory usage
     * for the Callable.  This number is then compared with the specified
     * maximum desired memory usage.  If the average memory usage is greater
     * than the specified number, it will be reported as a failed assertion.
     * 
     * @param description a plain-text description of the Callable, to be used
     *          in diagnostic messages
     * @param callable the callable for which to run a memory usage test
     * @param maxMemoryUsage the maximum desired memory usage for the Callable,
     *          in bytes 
     */
    private static void assertMaxMemoryUsage(String description, Callable<? extends Object> callable, long maxMemoryUsage) throws Exception {
      Runtime rt = Runtime.getRuntime();
      final int iterations = 5;
      long sum = 0;
      for(int i=0; i<iterations+1; i++) {
          rt.gc();
          long startMemUsed = rt.totalMemory() - rt.freeMemory();
          @SuppressWarnings("unused")
          Object result = callable.call();
          rt.gc();
          long endMemUsed = rt.totalMemory() - rt.freeMemory();
          long deltaMemUsed = endMemUsed - startMemUsed;
          if(deltaMemUsed < 0) {
              deltaMemUsed = 0;
          }
          if(i != 0) {
              //Ignore the first iteration, due to class initialization
              sum += deltaMemUsed;
          }
      }
      long averageMemoryUsage = sum / iterations;
      String message = description + " consume " + averageMemoryUsage + " bytes of memory on average, " + (averageMemoryUsage - maxMemoryUsage) + " bytes more than the specified limit of " + maxMemoryUsage + " bytes";
      assertTrue(message, averageMemoryUsage < maxMemoryUsage);
      System.out.println(description + " consume " + averageMemoryUsage + "/" + maxMemoryUsage + " bytes of memory");
    }

    public static Test suite() {
        return new TestSuite(CoberturaCoverageParserTest.class);
    }
}
