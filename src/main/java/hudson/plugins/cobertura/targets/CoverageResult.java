package hudson.plugins.cobertura.targets;

import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Run;
import hudson.plugins.cobertura.Chartable;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.CoverageChart;
import hudson.plugins.cobertura.Ratio;
import hudson.util.ChartUtil;
import hudson.util.TextFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Coverage result for a specific programming element. 
 *
 * <p>
 * Instances of {@link CoverageResult} form a tree structure to progressively represent smaller elements.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:47:10
 */
@ExportedBean(defaultVisibility=2)
public class CoverageResult implements Serializable, Chartable {
    /**
	 * Generated
	 */
	private static final long serialVersionUID = -3524882671364156445L;
	
	/**
     * The type of the programming element.
     */
    private final CoverageElement element;
    /**
     * Name of the programming element that this result object represent, such as package name, class name, method name, etc.
     */
    private final String name;

    // these two pointers form a tree structure where edges are names.
    private final CoverageResult parent;
    private final Map<String, CoverageResult> children = new TreeMap<String, CoverageResult>();

    private final Map<CoverageMetric,Ratio> aggregateResults = new EnumMap<CoverageMetric, Ratio>(CoverageMetric.class);
    private final Map<CoverageMetric,Ratio> localResults = new EnumMap<CoverageMetric, Ratio>(CoverageMetric.class);

    /**
     * Line-by-line coverage information. Computed lazily, since it's memory intensive.
     */
    private final CoveragePaint paint;
    private String relativeSourcePath;

    public AbstractBuild<?, ?> owner = null;

    public CoverageResult(CoverageElement elementType, CoverageResult parent, String name) {
        this.element = elementType;
        this.paint = CoveragePaintRule.makePaint(element);
        this.parent = parent;
        this.name = name;
        this.relativeSourcePath = null;
        if (this.parent != null) {
            this.parent.children.put(name, this);
        }
    }

    /**
     * Getter for property 'relativeSourcePath'.
     *
     * @return Value for property 'relativeSourcePath'.
     */
    public String getRelativeSourcePath() {
        return relativeSourcePath;
    }

    /**
     * Setter for property 'relativeSourcePath'.
     *
     * @param relativeSourcePath Value to set for property 'relativeSourcePath'.
     */
    public void setRelativeSourcePath(String relativeSourcePath) {
        this.relativeSourcePath = relativeSourcePath;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name == null || name.trim().length() == 0 ? Messages.CoverageElement_Project() : name;
    }

    /**
     * Getter for property 'parent'.
     *
     * @return Value for property 'parent'.
     */
    public CoverageResult getParent() {
        return parent;
    }

    /**
     * Getter for property 'element'.
     *
     * @return Value for property 'element'.
     */
    public CoverageElement getElement() {
        return element;
    }

    /**
     * Getter for property 'sourceCodeLevel'.
     *
     * @return Value for property 'sourceCodeLevel'.
     */
    public boolean isSourceCodeLevel() {
        return relativeSourcePath != null;
    }

    /**
     * Getter for property 'paint'.
     *
     * @return Value for property 'paint'.
     */
    public CoveragePaint getPaint() {
        return paint;
    }

    public void paint(int line, int hits) {
        if (paint != null) {
            paint.paint(line, hits);
        }
    }

    public void paint(int line, int hits, int branchHits, int branchTotal) {
        if (paint != null) {
            paint.paint(line, hits, branchHits, branchTotal);
        }
    }

    /**
     * gets the file corresponding to the source file.
     *
     * @return The file where the source file should be (if it exists)
     */
    private File getSourceFile() {
        return new File(owner.getProject().getRootDir(), "cobertura/" + relativeSourcePath);
    }

    /**
     * Getter for property 'sourceFileAvailable'.
     *
     * @return Value for property 'sourceFileAvailable'.
     */
    public boolean isSourceFileAvailable() {
        return owner == owner.getProject().getLastSuccessfulBuild() && getSourceFile().exists();
    }

    /**
     * Getter for property 'sourceFileContent'.
     *
     * @return Value for property 'sourceFileContent'.
     */
    public String getSourceFileContent() {
        try {
            return new TextFile(getSourceFile()).read();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Getter for property 'parents'.
     *
     * @return Value for property 'parents'.
     */
    public List<CoverageResult> getParents() {
        List<CoverageResult> result = new ArrayList<CoverageResult>();
        CoverageResult p = getParent();
        while (p != null) {
            result.add(p);
            p = p.getParent();
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * Getter for property 'childElements'.
     *
     * @return Value for property 'childElements'.
     */
    public Set<CoverageElement> getChildElements() {
        Set<CoverageElement> result = EnumSet.noneOf(CoverageElement.class);
        for (CoverageResult child : children.values()) {
            result.add(child.element);
        }
        return result;
    }

    public Set<String> getChildren(CoverageElement element) {
        Set<String> result = new TreeSet<String>();
        for (CoverageResult child : children.values()) {
            if (child.element.equals(element)) {
                result.add(child.name);
            }
        }
        return result;
    }

    public Set<CoverageMetric> getChildMetrics(CoverageElement element) {
        Set<CoverageMetric> result = new TreeSet<CoverageMetric>();
        for (CoverageResult child : children.values()) {
            if (child.element.equals(element)) {
                result.addAll(child.getMetrics());
            }
        }
        return result;
    }

    /**
     * Getter for keys of property 'children'.
     *
     * @return Value for keys of property 'children'.
     */
    public Set<String> getChildren() {
        return children.keySet();
    }

    /**
     * Getter for property 'children'.
     *
     * @return Value for property 'children'.
     */
    public Map<String, CoverageResult> getChildrenReal() {
        return children;
    }

    /**
     * Getter for property 'results'.
     *
     * @return Value for property 'results'.
     */
     public Map<CoverageMetric, Ratio> getResults() {
        return Collections.unmodifiableMap(aggregateResults);
    }

    /**
     * Getter for property 'results'.
     *
     * @return Value for property 'results'.
     */
    @Exported(name="results")
    public CoverageTree getResultsAPI() {
    	return new CoverageTree(name, aggregateResults, children);
    }

    public String urlTransform(String name) {
        StringBuilder buf = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (('0' <= c && '9' >= c)
                    || ('A' <= c && 'Z' >= c)
                    || ('a' <= c && 'z' >= c)) {
                buf.append(c);
            } else {
                buf.append('_');
            }
        }
        return buf.toString();
    }

    public String xmlTransform(String name) {
        return name.replaceAll("\\&", "&amp;").replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
    }

    public String relativeUrl(CoverageResult parent) {
        StringBuffer url = new StringBuffer("..");
        CoverageResult p = getParent();
        while (p != null && p != parent) {
            url.append("/..");
            p = p.getParent();
        }
        return url.toString();
    }

    public CoverageResult getChild(String name) {
        return children.get(name);
    }

    public Ratio getCoverage(CoverageMetric metric) {
        return aggregateResults.get(metric);
    }

    /**
     * Getter for property 'metrics'.
     *
     * @return Value for property 'metrics'.
     */
    public Set<CoverageMetric> getMetrics() {
        return Collections.unmodifiableSet(EnumSet.copyOf(aggregateResults.keySet()));
    }

    public void updateMetric(CoverageMetric metric, Ratio additionalResult) {
        if (localResults.containsKey(metric)) {
            Ratio existingResult = localResults.get(metric);
            localResults.put(metric, CoverageAggregationRule.combine(metric, existingResult, additionalResult));
        } else {
            localResults.put(metric, additionalResult);
        }
    }

    /**
     * Getter for property 'owner'.
     *
     * @return Value for property 'owner'.
     */
    public AbstractBuild<?,?> getOwner() {
        return owner;
    }

    /**
     * Setter for property 'owner'.
     *
     * @param owner Value to set for property 'owner'.
     */
    public void setOwner(AbstractBuild<?,?> owner) {
        this.owner = owner;
        aggregateResults.clear();
        for (CoverageResult child : children.values()) {
            child.setOwner(owner);
            if (paint != null && child.paint != null && CoveragePaintRule.propagatePaintToParent(child.element)) {
                paint.add(child.paint);
            }
            for (Map.Entry<CoverageMetric, Ratio> childResult : child.aggregateResults.entrySet()) {
                aggregateResults.putAll(CoverageAggregationRule.aggregate(child.getElement(),
                        childResult.getKey(), childResult.getValue(), aggregateResults));
            }
        }
        // override any local results (as they should be more accurate than the aggregated ones)
        aggregateResults.putAll(localResults);
        // now inject any results from CoveragePaint as they should be most accurate.
        if (paint != null) {
            aggregateResults.putAll(paint.getResults());
        }
    }

    /**
     * Getter for property 'previousResult'.
     *
     * @return Value for property 'previousResult'.
     */
    public CoverageResult getPreviousResult() {
        if (parent == null) {
            if (owner == null) {
                return null;
            }
            Run<?,?> prevBuild = owner.getPreviousNotFailedBuild();
            if (prevBuild == null) {
                return null;
            }
            CoberturaBuildAction action = null;
            while ((prevBuild != null) && (null == (action = prevBuild.getAction(CoberturaBuildAction.class)))) {
                prevBuild = prevBuild.getPreviousNotFailedBuild();
            }
            if (action == null) {
                return null;
            }
            return action.getResult();
        } else {
            CoverageResult prevParent = parent.getPreviousResult();
            return prevParent == null ? null : prevParent.getChild(name);
        }
    }

    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException {
        token = token.toLowerCase();
        for (String name : children.keySet()) {
            if (urlTransform(name).toLowerCase().equals(token)) {
                return getChild(name);
            }
        }
        return null;
    }

    public void doCoverageHighlightedSource(StaplerRequest req, StaplerResponse rsp) throws IOException {
        // TODO
    }

    /**
     * Generates the graph that shows the coverage trend up to this report.
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        AbstractBuild<?,?> build = getOwner();
        Calendar t = build.getTimestamp();

        if (req.checkIfModified(t, rsp))
            return; // up to date

        JFreeChart chart = new CoverageChart( this ).createChart();
        ChartUtil.generateGraph(req, rsp, chart, 500, 200);
    }

    /**
     * Getter for property 'paintedSources'.
     *
     * @return Value for property 'paintedSources'.
     */
    public Map<String, CoveragePaint> getPaintedSources() {
        Map<String, CoveragePaint> result = new HashMap<String, CoveragePaint>();
        // check the children
        for (CoverageResult child : children.values()) {
            result.putAll(child.getPaintedSources());
        }
        if (relativeSourcePath != null && paint != null) {
            result.put(relativeSourcePath, paint);
        }
        return result;
    }
    
    public Api getApi(){
    	return new Api(this);
    }
}
