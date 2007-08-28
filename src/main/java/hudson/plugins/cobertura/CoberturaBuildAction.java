package hudson.plugins.cobertura;

import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.ColorPalette;
import org.kohsuke.stapler.StaplerProxy;
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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 03-Jul-2007 08:43:08
 */
public class CoberturaBuildAction implements HealthReportingAction, StaplerProxy {
    public final Build owner;
    private String buildBaseDir;
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private Map<CoverageMetric, Ratio> result;

    private transient WeakReference<CoverageResult> report;

    public HealthReport getBuildHealth() {
        if (healthyTarget == null || unhealthyTarget == null) return null;
        if (result == null) {
            CoverageResult projectCoverage = getResult();
            result = new HashMap<CoverageMetric, Ratio>();
            result.putAll(projectCoverage.getResults());
        }
        Map<CoverageMetric, Integer> scores = healthyTarget.getRangeScores(unhealthyTarget, result);
        int minValue = 100;
        CoverageMetric minKey = null;
        for (Map.Entry<CoverageMetric, Integer> e : scores.entrySet()) {
            if (e.getValue() < minValue) {
                minKey = e.getKey();
                minValue = e.getValue();
            }
        }
        if (minKey == null) return null;

        StringBuilder description = new StringBuilder("Cobertura Coverage: ");
        description.append(minKey.getName());
        description.append(" ");
        description.append(result.get(minKey).getPercentage());
        description.append("% (");
        description.append(result.get(minKey).toString());
        description.append(")");
        return new HealthReport(minValue, description.toString());
    }

    public String getIconFileName() {
        return "graph.gif";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDisplayName() {
        return "Coverage Report";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUrlName() {
        return "cobertura";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getTarget() {
        return getResult();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CoberturaBuildAction getPreviousResult() {
        return getPreviousResult(owner);
    }

    /**
     * Gets the previous {@link CoberturaBuildAction} of the given build.
     */
    /*package*/
    static CoberturaBuildAction getPreviousResult(Build start) {
        Build<?, ?> b = start;
        while (true) {
            b = b.getPreviousBuild();
            if (b == null)
                return null;
            if (b.getResult() == Result.FAILURE)
                continue;
            CoberturaBuildAction r = b.getAction(CoberturaBuildAction.class);
            if (r != null)
                return r;
        }
    }

    CoberturaBuildAction(Build owner, String workspacePath, CoverageResult r, CoverageTarget healthyTarget,
                         CoverageTarget unhealthyTarget) {
        this.owner = owner;
        this.report = new WeakReference<CoverageResult>(r);
        this.buildBaseDir = workspacePath;
        if (this.buildBaseDir == null) {
            this.buildBaseDir = File.separator;
        } else if (!this.buildBaseDir.endsWith(File.separator)) {
            this.buildBaseDir += File.separator;
        }
        this.healthyTarget = healthyTarget;
        this.unhealthyTarget = unhealthyTarget;
        r.setOwner(owner);
        if (result == null) {
            result = new HashMap<CoverageMetric, Ratio>();
            result.putAll(r.getResults());
        }
    }


    /**
     * Obtains the detailed {@link hudson.plugins.cobertura.targets.CoverageResult} instance.
     */
    public synchronized CoverageResult getResult() {
        if (report != null) {
            CoverageResult r = report.get();
            if (r != null) return r;
        }

        File reportFile = CoberturaPublisher.getCoberturaReport(owner);
        try {
            CoverageResult r = CoberturaCoverageParser.parse(reportFile, buildBaseDir);
            r.setOwner(owner);
            report = new WeakReference<CoverageResult>(r);
            return r;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load " + reportFile, e);
            return null;
        }
    }

    private static final Logger logger = Logger.getLogger(CoberturaBuildAction.class.getName());

    public static CoberturaBuildAction load(Build<?, ?> build, String workspacePath, CoverageResult result,
                                            CoverageTarget healthyTarget, CoverageTarget unhealthyTarget) {
        return new CoberturaBuildAction(build, workspacePath, result, healthyTarget, unhealthyTarget);
    }
    /** Generates the graph that shows the coverage trend up to this report. */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblem) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Calendar t = owner.getTimestamp();

        if (req.checkIfModified(t, rsp))
            return; // up to date

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (CoberturaBuildAction a = this; a != null; a = a.getPreviousResult()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.owner);
            for (Map.Entry<CoverageMetric, Ratio> value: a.result.entrySet()) {
                dsb.add(value.getValue().getPercentageFloat(), value.getKey().getName(), label);
            }
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
