package hudson.plugins.cobertura;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import hudson.model.Run;
import hudson.plugins.cobertura.targets.CoverageElement;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageResult;
import junit.framework.TestCase;

/**
 * Unit tests for {@link CoverageResult}.
 * 
 * @author davidmc24
 * @since 28-Apr-2009
 */
public class CoverageResultTest extends TestCase {
    private static final String FILE_COVERAGE_DATA = "coverage-with-data.xml";
    private IMocksControl ctl;
    private Run<?, ?> build;

    /**
     * Set up the mock objects used by the tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        ctl = EasyMock.createControl();
        build = ctl.createMock("build", Run.class);
    }
    
    /**
     * Parses a coverage XML file into a CoverageResult object.
     * 
     * @param fileName the name of the resource to parse
     * @return a CoverageResult object
     */
    private CoverageResult loadResults(String fileName) throws Exception {
        InputStream in = getClass().getResourceAsStream(fileName);
        CoverageResult result = CoberturaCoverageParser.parse(in, null);
        return result;
    }

    /**
     * Tests the behavior of {@link CoverageResult#setOwner(Run)}.
     */
    public void testSetOwner() throws Exception {
        ctl.replay();
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        assertNull(result.getOwner());
        result.setOwner(build);
        assertSame(build, result.getOwner());
        ctl.verify();
    }
    
    /**
     * Tests the behavior of {@link CoverageResult#getResults()}.
     */
    public void testGetResults() throws Exception {
        ctl.replay();
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        assertEquals(Collections.EMPTY_MAP, result.getResults());
        result.setOwner(build);
        Map<CoverageMetric,Ratio> metrics = result.getResults();
        assertEquals(6, result.getResults().size());
        assertEquals(Ratio.create(2, 2), metrics.get(CoverageMetric.PACKAGES));
        assertEquals(Ratio.create(3, 3), metrics.get(CoverageMetric.FILES));
        assertEquals(Ratio.create(3, 3), metrics.get(CoverageMetric.CLASSES));
        assertEquals(Ratio.create(7, 7), metrics.get(CoverageMetric.METHOD));
        assertEquals(Ratio.create(27, 30), metrics.get(CoverageMetric.LINE));
        assertEquals(Ratio.create(9, 12), metrics.get(CoverageMetric.CONDITIONAL));
        ctl.verify();
    }
    
    /**
     * Test behavior of {@link CoverageResult#getMetricsWithEmpty()}.
     */
    public void testGetMetricsWithEmpty() throws Exception {
    	ctl.replay();
    	CoverageResult result = loadResults("coverage-no-data.xml");
    	Set<CoverageMetric> metrics = result.getMetricsWithEmpty();
    	List<CoverageMetric> allMetrics = new LinkedList<CoverageMetric>(Arrays.asList(CoverageMetric.PACKAGES, CoverageMetric.FILES, CoverageMetric.CLASSES, CoverageMetric.METHOD, CoverageMetric.LINE, CoverageMetric.CONDITIONAL));
    	assertEquals(metrics.size(), allMetrics.size());
    	ctl.verify();
    }
    
    /**
     * Tests the behavior of {@link CoverageResult#getParent()}.
     */
    public void testGetParent() throws Exception {
        ctl.replay();
        // Project level
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        result.setOwner(build);
        assertNull(result.getParent());
        // Package level
        CoverageResult expectedParent = result;
        result = result.getChild("search");
        assertSame(expectedParent, result.getParent());
        // File level
        expectedParent = result;
        result = result.getChild("LinearSearch.java");
        assertSame(expectedParent, result.getParent());
        // Class level
        expectedParent = result;
        result = result.getChild("LinearSearch");
        assertSame(expectedParent, result.getParent());
        // Method level
        expectedParent = result;
        result = result.getChild("int find(int,int)");
        assertSame(expectedParent, result.getParent());
        ctl.verify();
    }
    
    /**
     * Tests the behavior of {@link CoverageResult#getParents()}.
     */
    public void testGetParents() throws Exception {
        ctl.replay();
        // Project level
        LinkedList<CoverageResult> expectedParents = new LinkedList<CoverageResult>();
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        result.setOwner(build);
        assertEquals(expectedParents, result.getParents());
        // Package level
        expectedParents.add(result);
        result = result.getChild("search");
        assertEquals(expectedParents, result.getParents());
        // File level
        expectedParents.add(result);
        result = result.getChild("LinearSearch.java");
        assertEquals(expectedParents, result.getParents());
        // Class level
        expectedParents.add(result);
        result = result.getChild("LinearSearch");
        assertEquals(expectedParents, result.getParents());
        // Method level
        expectedParents.add(result);
        result = result.getChild("int find(int,int)");
        assertEquals(expectedParents, result.getParents());
        ctl.verify();
    }
    
    /**
     * Tests the behavior of {@link CoverageResult#getChildElements()}.
     */
    public void testGetChildElements() throws Exception {
        ctl.replay();
        // Project level
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        result.setOwner(build);
        assertEquals(Collections.singleton(CoverageElement.JAVA_PACKAGE), result.getChildElements());
        // Package level
        result = result.getChild("search");
        assertEquals(Collections.singleton(CoverageElement.JAVA_FILE), result.getChildElements());
        // File level
        result = result.getChild("LinearSearch.java");
        assertEquals(Collections.singleton(CoverageElement.JAVA_CLASS), result.getChildElements());
        // Class level
        result = result.getChild("LinearSearch");
        assertEquals(Collections.singleton(CoverageElement.JAVA_METHOD), result.getChildElements());
        // Method level
        result = result.getChild("int find(int,int)");
        assertEquals(Collections.emptySet(), result.getChildElements());
        ctl.verify();
    }
    
    /**
     * Tests the behavior of {@link CoverageResult#getChildren()}.
     */
    public void testGetChildren() throws Exception {
        ctl.replay();
        // Project level
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        result.setOwner(build);
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"search", "<default>"})), result.getChildren());
        // Package level
        result = result.getChild("search");
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"LinearSearch.java", "ISortedArraySearch.java", "BinarySearch.java"})), result.getChildren());
        // File level
        result = result.getChild("LinearSearch.java");
        assertEquals(Collections.singleton("LinearSearch"), result.getChildren());
        // Class level
        result = result.getChild("LinearSearch");
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"void <init>()", "int find(int,int)"})), result.getChildren());
        // Method level
        result = result.getChild("int find(int,int)");
        assertEquals(Collections.emptySet(), result.getChildren());
        ctl.verify();
    }
    
    /**
     * Tests the behavior of {@link CoverageResult#getChildren(CoverageElement)}.
     */
    public void testGetChildrenCoverageElement() throws Exception {
        ctl.replay();
        // Project level
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        result.setOwner(build);
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"search", "<default>"})), result.getChildren(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.PROJECT));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_FILE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_METHOD));
        // Package level
        result = result.getChild("search");
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"LinearSearch.java", "ISortedArraySearch.java", "BinarySearch.java"})), result.getChildren(CoverageElement.JAVA_FILE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.PROJECT));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_METHOD));
        // File level
        result = result.getChild("LinearSearch.java");
        assertEquals(Collections.singleton("LinearSearch"), result.getChildren(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.PROJECT));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_FILE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_METHOD));
        // Class level
        result = result.getChild("LinearSearch");
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"void <init>()", "int find(int,int)"})), result.getChildren(CoverageElement.JAVA_METHOD));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.PROJECT));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_FILE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_CLASS));
        // Method level
        result = result.getChild("int find(int,int)");
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.PROJECT));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_FILE));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.emptySet(), result.getChildren(CoverageElement.JAVA_METHOD));
        ctl.verify();
    }

    /**
     * Tests the behavior of {@link CoverageResult#getChildMetrics(CoverageElement)}.
     */
    public void testGetChildMetricsCoverageElement() throws Exception {
        ctl.replay();
        // Project level
        CoverageResult result = loadResults(FILE_COVERAGE_DATA);
        result.setOwner(build);
        assertEquals(new HashSet<CoverageMetric>(Arrays.asList(new CoverageMetric[] {CoverageMetric.FILES, CoverageMetric.CLASSES, CoverageMetric.METHOD, CoverageMetric.LINE, CoverageMetric.CONDITIONAL})), result.getChildMetrics(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.PROJECT));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_FILE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_METHOD));
        // Package level
        result = result.getChild("search");
        assertEquals(new HashSet<CoverageMetric>(Arrays.asList(new CoverageMetric[] {CoverageMetric.CLASSES, CoverageMetric.METHOD, CoverageMetric.LINE, CoverageMetric.CONDITIONAL})), result.getChildMetrics(CoverageElement.JAVA_FILE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.PROJECT));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_METHOD));
        // File level
        result = result.getChild("LinearSearch.java");
        assertEquals(new HashSet<CoverageMetric>(Arrays.asList(new CoverageMetric[] {CoverageMetric.METHOD, CoverageMetric.LINE, CoverageMetric.CONDITIONAL})), result.getChildMetrics(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.PROJECT));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_FILE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_METHOD));
        // Class level
        result = result.getChild("LinearSearch");
        assertEquals(new HashSet<CoverageMetric>(Arrays.asList(new CoverageMetric[] {CoverageMetric.LINE, CoverageMetric.CONDITIONAL})), result.getChildMetrics(CoverageElement.JAVA_METHOD));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.PROJECT));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_FILE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_CLASS));
        // Method level
        result = result.getChild("int find(int,int)");
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.PROJECT));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_PACKAGE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_FILE));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_CLASS));
        assertEquals(Collections.EMPTY_SET, result.getChildMetrics(CoverageElement.JAVA_METHOD));
        ctl.verify();
    }
}
