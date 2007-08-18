package hudson.plugins.cobertura.results;

import hudson.model.Build;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.ColorPalette;
import hudson.plugins.cobertura.Ratio;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import java.io.IOException;
import java.util.Calendar;
import java.awt.*;

/**
 * Abstract Cobertura Coverage results.
 * @author Stephen Connolly
 */
abstract public class AbstractCoberturaMetrics {


    private String name;

    private int methods;
    private int coveredmethods;

    private int conditionals;
    private int coveredconditionals;

    private int lines;
    private int coveredlines;

    private int elements;
    private int coveredelements;
    public Build owner = null;

    public Ratio getMethodCoverage() {
        return Ratio.create(coveredmethods, methods);
    }

    public Ratio getConditionalCoverage() {
        return Ratio.create(coveredconditionals, conditionals);
    }

    public Ratio getLineCoverage() {
        return Ratio.create(coveredlines, lines);
    }

    public Ratio getElementCoverage() {
        return Ratio.create(coveredelements, elements);
    }

    /**
     * Getter for property 'conditionals'.
     *
     * @return Value for property 'conditionals'.
     */
    public int getConditionals() {
        return conditionals;
    }

    /**
     * Setter for property 'conditionals'.
     *
     * @param conditionals Value to set for property 'conditionals'.
     */
    public void setConditionals(int conditionals) {
        this.conditionals = conditionals;
    }

    /**
     * Getter for property 'methods'.
     *
     * @return Value for property 'methods'.
     */
    public int getMethods() {
        return methods;
    }

    /**
     * Setter for property 'methods'.
     *
     * @param methods Value to set for property 'methods'.
     */
    public void setMethods(int methods) {
        this.methods = methods;
    }

    /**
     * Getter for property 'coveredlines'.
     *
     * @return Value for property 'coveredlines'.
     */
    public int getCoveredlines() {
        return coveredlines;
    }

    /**
     * Setter for property 'coveredlines'.
     *
     * @param coveredlines Value to set for property 'coveredlines'.
     */
    public void setCoveredlines(int coveredlines) {
        this.coveredlines = coveredlines;
    }

    /**
     * Getter for property 'coveredmethods'.
     *
     * @return Value for property 'coveredmethods'.
     */
    public int getCoveredmethods() {
        return coveredmethods;
    }

    /**
     * Setter for property 'coveredmethods'.
     *
     * @param coveredmethods Value to set for property 'coveredmethods'.
     */
    public void setCoveredmethods(int coveredmethods) {
        this.coveredmethods = coveredmethods;
    }

    /**
     * Getter for property 'coveredconditionals'.
     *
     * @return Value for property 'coveredconditionals'.
     */
    public int getCoveredconditionals() {
        return coveredconditionals;
    }

    /**
     * Setter for property 'coveredconditionals'.
     *
     * @param coveredconditionals Value to set for property 'coveredconditionals'.
     */
    public void setCoveredconditionals(int coveredconditionals) {
        this.coveredconditionals = coveredconditionals;
    }

    /**
     * Getter for property 'lines'.
     *
     * @return Value for property 'lines'.
     */
    public int getLines() {
        return lines;
    }

    /**
     * Setter for property 'lines'.
     *
     * @param lines Value to set for property 'lines'.
     */
    public void setLines(int lines) {
        this.lines = lines;
    }

    /**
     * Getter for property 'coveredelements'.
     *
     * @return Value for property 'coveredelements'.
     */
    public int getCoveredelements() {
        return coveredelements;
    }

    /**
     * Setter for property 'coveredelements'.
     *
     * @param coveredelements Value to set for property 'coveredelements'.
     */
    public void setCoveredelements(int coveredelements) {
        this.coveredelements = coveredelements;
    }

    /**
     * Getter for property 'elements'.
     *
     * @return Value for property 'elements'.
     */
    public int getElements() {
        return elements;
    }

    /**
     * Setter for property 'elements'.
     *
     * @param elements Value to set for property 'elements'.
     */
    public void setElements(int elements) {
        this.elements = elements;
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
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    public Build getOwner() {
        return owner;
    }

    public void setOwner(Build owner) {
        this.owner = owner;
    }

    abstract public AbstractCoberturaMetrics getPreviousResult();

    /** Generates the graph that shows the coverage trend up to this report. */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblem) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Build build = getOwner();
        Calendar t = build.getTimestamp();

        if (req.checkIfModified(t, rsp))
            return; // up to date

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (AbstractCoberturaMetrics a = this; a != null; a = a.getPreviousResult()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.getOwner());
            dsb.add(a.getMethodCoverage().getPercentageFloat(), "method", label);
            dsb.add(a.getConditionalCoverage().getPercentageFloat(), "conditional", label);
            dsb.add(a.getLineCoverage().getPercentageFloat(), "line", label);
        }

        ChartUtil.generateGraph(req, rsp, createChart(dsb.build()), 400, 200);
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
}
