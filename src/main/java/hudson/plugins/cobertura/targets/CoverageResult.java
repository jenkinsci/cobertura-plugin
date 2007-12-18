package hudson.plugins.cobertura.targets;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.Ratio;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.TextFile;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:47:10
 */
public class CoverageResult implements Serializable {
    private final CoverageElement element;
    private final Map<CoverageMetric, Ratio> aggregateResults = new HashMap<CoverageMetric, Ratio>();
    private final Map<CoverageMetric, Ratio> localResults = new HashMap<CoverageMetric, Ratio>();
    private final CoverageResult parent;
    private final CoveragePaint paint;
    private final Map<String, CoverageResult> children = new HashMap<String, CoverageResult>();
    private final String name;
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
        return name;
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
        return owner == owner.getProject().getLastStableBuild() && getSourceFile().exists();
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
        Set<CoverageElement> result = new HashSet<CoverageElement>();
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
     * Getter for property 'children'.
     *
     * @return Value for property 'children'.
     */
    public Set<String> getChildren() {
        return children.keySet();
    }

    /**
     * Getter for property 'results'.
     *
     * @return Value for property 'results'.
     */
    public Map<CoverageMetric, Ratio> getResults() {
        return Collections.unmodifiableMap(aggregateResults);
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
        return Collections.unmodifiableSet(new TreeSet(aggregateResults.keySet()));
    }

    public void updateMetric(CoverageMetric metric, Ratio additionalResult) {
        if (localResults.containsKey(metric)) {
            Ratio existingResult = localResults.get(metric);
            localResults.put(metric, CoverageAggreagtionRule.combine(metric, existingResult, additionalResult));
        } else {
            localResults.put(metric, additionalResult);
        }
    }

    /**
     * Getter for property 'owner'.
     *
     * @return Value for property 'owner'.
     */
    public AbstractBuild getOwner() {
        return owner;
    }

    /**
     * Setter for property 'owner'.
     *
     * @param owner Value to set for property 'owner'.
     */
    public void setOwner(AbstractBuild owner) {
        this.owner = owner;
        aggregateResults.clear();
        for (CoverageResult child : children.values()) {
            child.setOwner(owner);
            if (paint != null && child.paint != null && CoveragePaintRule.propagatePaintToParent(child.element)) {
                paint.add(child.paint);
            }
            for (Map.Entry<CoverageMetric, Ratio> childResult : child.aggregateResults.entrySet()) {
                aggregateResults.putAll(CoverageAggreagtionRule.aggregate(child.getElement(),
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
            Run prevBuild = owner.getPreviousNotFailedBuild();
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
        if (ChartUtil.awtProblem) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        AbstractBuild build = getOwner();
        Calendar t = build.getTimestamp();

        if (req.checkIfModified(t, rsp))
            return; // up to date

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (CoverageResult a = this; a != null; a = a.getPreviousResult()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.getOwner());
            for (Map.Entry<CoverageMetric, Ratio> value : a.aggregateResults.entrySet()) {
                dsb.add(value.getValue().getPercentageFloat(), value.getKey().getName(), label);
            }
        }

        ChartUtil.generateGraph(req, rsp, createChart(dsb.build()), 500, 200);
    }

    private JFreeChart createChart(CategoryDataset dataset) {

        final JFreeChart chart = ChartFactory.createLineChart(
                null,                   // chart title
                null,                   // unused
                "%",                    // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(100);
        rangeAxis.setLowerBound(0);

        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setStroke(new BasicStroke(2.0f));
        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
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
}
