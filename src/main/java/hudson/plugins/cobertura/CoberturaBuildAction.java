package hudson.plugins.cobertura;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.DescribableList;
import hudson.util.ShiftedCategoryAxis;
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
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 03-Jul-2007 08:43:08
 */
public class CoberturaBuildAction implements HealthReportingAction, StaplerProxy {
    private final AbstractBuild<?, ?> owner;
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private boolean failUnhealthy;
    private boolean failUnstable;
    private boolean autoUpdateHealth;
    private boolean autoUpdateStability;
    /**
     * Overall coverage result.
     */
    private Map<CoverageMetric, Ratio> result;
    private HealthReport health = null;

    private transient WeakReference<CoverageResult> report;
    private boolean onlyStable;


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public HealthReport getBuildHealth() {
        if (health != null) {
            return health;
        }
        //try to get targets from root project (for maven modules targets are null)
        DescribableList rootpublishers = owner.getProject().getRootProject().getPublishersList();

        if (rootpublishers != null) {
            CoberturaPublisher publisher = (CoberturaPublisher) rootpublishers.get(CoberturaPublisher.class);
            if (publisher != null) {
                healthyTarget = publisher.getHealthyTarget();
                unhealthyTarget = publisher.getUnhealthyTarget();
            }
        }

        if(healthyTarget == null || unhealthyTarget == null){
            return null;
        }
            
        if (result == null) {
            CoverageResult projectCoverage = getResult();
            result = new EnumMap<CoverageMetric, Ratio>(CoverageMetric.class);
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
        if (minKey == null) {
            if (result == null || result.size() == 0) {
                return null;
            } else {
                for (Map.Entry<CoverageMetric, Integer> e : scores.entrySet()) {
                    minKey = e.getKey();
                }
                if (minKey != null) {
                    Localizable localizedDescription = Messages._CoberturaBuildAction_description(result.get(minKey).getPercentage(), result.get(minKey).toString(), minKey.getName());
                    health = new HealthReport(minValue, localizedDescription);
                    return health;
                }
                return null;
            }

        } else {
            Localizable localizedDescription = Messages._CoberturaBuildAction_description(result.get(minKey).getPercentage(), result.get(minKey).toString(), minKey.getName());
            health = new HealthReport(minValue, localizedDescription);
            return health;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        return "graph.gif";  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return Messages.CoberturaBuildAction_displayName();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName() {
        return "cobertura";  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    public Object getTarget() {
        return getResult();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for property 'previousResult'.
     *
     * @return Value for property 'previousResult'.
     */
    public CoberturaBuildAction getPreviousResult() {
        return getPreviousResult(owner);
    }

    /**
     * Gets the previous {@link CoberturaBuildAction} of the given build.
     */
    /*package*/
    static CoberturaBuildAction getPreviousResult(AbstractBuild<?,?> start) {
        AbstractBuild<?, ?> b = start;
        while (true) {
            b = b.getPreviousNotFailedBuild();
            if (b == null)
                return null;
            assert b.getResult() != Result.FAILURE : "We asked for the previous not failed build";
            CoberturaBuildAction r = b.getAction(CoberturaBuildAction.class);
            if(r != null && r.includeOnlyStable() && b.getResult() != Result.SUCCESS){
                r = null;
            }
            if (r != null)
                return r;
        }
    }

    private boolean includeOnlyStable() {
        return onlyStable;
    }

    CoberturaBuildAction(AbstractBuild<?, ?> owner, CoverageResult r, CoverageTarget healthyTarget,
            CoverageTarget unhealthyTarget, boolean onlyStable, boolean failUnhealthy, boolean failUnstable, boolean autoUpdateHealth, boolean autoUpdateStability) {
        this.owner = owner;
        this.report = new WeakReference<CoverageResult>(r);
        this.healthyTarget = healthyTarget;
        this.unhealthyTarget = unhealthyTarget;
        this.onlyStable = onlyStable;
        this.failUnhealthy = failUnhealthy;
        this.failUnstable = failUnstable;
        this.autoUpdateHealth = autoUpdateHealth;
        this.autoUpdateStability = autoUpdateStability;
        r.setOwner(owner);
        if (result == null) {
            result = new EnumMap<CoverageMetric,Ratio>(CoverageMetric.class);
            result.putAll(r.getResults());
        }
        getBuildHealth(); // populate the health field so we don't have to parse everything all the time
    }


    /**
     * Obtains the detailed {@link hudson.plugins.cobertura.targets.CoverageResult} instance.
     */
    public synchronized CoverageResult getResult() {
        if (report != null) {
            CoverageResult r = report.get();
            if (r != null) return r;
        }

        CoverageResult r = null;
        for (File reportFile : CoberturaPublisher.getCoberturaReports(owner)) {
            try {
                r = CoberturaCoverageParser.parse(reportFile, r);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to load " + reportFile, e);
            }
        }
        if (r != null) {
            r.setOwner(owner);
            report = new WeakReference<CoverageResult>(r);
            return r;
        } else {
            return null;
        }
    }

    private static final Logger logger = Logger.getLogger(CoberturaBuildAction.class.getName());

    public static CoberturaBuildAction load(AbstractBuild<?, ?> build, CoverageResult result, CoverageTarget healthyTarget,
            CoverageTarget unhealthyTarget, boolean onlyStable, boolean failUnhealthy, boolean failUnstable, boolean autoUpdateHealth, boolean autoUpdateStability) {
        return new CoberturaBuildAction(build, result, healthyTarget, unhealthyTarget, onlyStable, failUnhealthy, failUnstable, autoUpdateHealth, autoUpdateStability);
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

        Calendar t = owner.getTimestamp();

        if (req.checkIfModified(t, rsp))
            return; // up to date

        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        for (CoberturaBuildAction a = this; a != null; a = a.getPreviousResult()) {
            ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(a.owner);
            for (Map.Entry<CoverageMetric, Ratio> value : a.result.entrySet()) {
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
        renderer.setBaseStroke(new BasicStroke(2.0f));
        ColorPalette.apply(renderer);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }
}
