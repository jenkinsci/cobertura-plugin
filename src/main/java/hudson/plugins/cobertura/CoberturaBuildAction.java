package hudson.plugins.cobertura;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.util.ChartUtil;
import hudson.util.DescribableList;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.JFreeChart;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 03-Jul-2007 08:43:08
 */
public class CoberturaBuildAction implements HealthReportingAction, StaplerProxy, Chartable, SimpleBuildStep.LastBuildAction, RunAction2 {

    private Run<?, ?> owner;
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private boolean failUnhealthy;
    private boolean failUnstable;
    private boolean autoUpdateHealth;
    private boolean autoUpdateStability;
    private boolean zoomCoverageChart;
    private int maxNumberOfBuilds;
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
        if (healthyTarget == null || unhealthyTarget == null) {
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

    public Run<?, ?> getOwner() {
        return owner;
    }

    private void setOwner(Run<?, ?> owner) {
        this.owner = owner;
        if (report != null) {
            CoverageResult r = report.get();
            if (r != null) {
                r.setOwner(owner);
            }
        }
    }

    public Map<CoverageMetric, Ratio> getResults() {
        return result;
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
    static CoberturaBuildAction getPreviousResult(Run<?, ?> start) {
        Run<?, ?> b = start;
        while (true) {
            b = BuildUtils.getPreviousNotFailedCompletedBuild(b);
            if (b == null) {
                return null;
            }
            assert b.getResult() != Result.FAILURE : "We asked for the previous not failed build";
            CoberturaBuildAction r = b.getAction(CoberturaBuildAction.class);
            if (r != null && r.includeOnlyStable() && b.getResult() != Result.SUCCESS) {
                r = null;
            }
            if (r != null) {
                return r;
            }
        }
    }

    private boolean includeOnlyStable() {
        return onlyStable;
    }

    CoberturaBuildAction(CoverageResult r, CoverageTarget healthyTarget,
            CoverageTarget unhealthyTarget, boolean onlyStable, boolean failUnhealthy,
            boolean failUnstable, boolean autoUpdateHealth, boolean autoUpdateStability,
            boolean zoomCoverageChart, int maxNumberOfBuilds) {
        this.report = new WeakReference<CoverageResult>(r);
        this.healthyTarget = healthyTarget;
        this.unhealthyTarget = unhealthyTarget;
        this.onlyStable = onlyStable;
        this.failUnhealthy = failUnhealthy;
        this.failUnstable = failUnstable;
        this.autoUpdateHealth = autoUpdateHealth;
        this.autoUpdateStability = autoUpdateStability;
        this.zoomCoverageChart = zoomCoverageChart;
        this.maxNumberOfBuilds = maxNumberOfBuilds;
        if (result == null) {
            result = new EnumMap<CoverageMetric, Ratio>(CoverageMetric.class);
            result.putAll(r.getResults());
        }
        getBuildHealth(); // populate the health field so we don't have to parse everything all the time
    }

    /**
     * Obtains the detailed
     * {@link hudson.plugins.cobertura.targets.CoverageResult} instance.
     */
    public synchronized CoverageResult getResult() {
        if (report != null) {
            CoverageResult r = report.get();
            if (r != null) {
                return r;
            }
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

    public static CoberturaBuildAction load(CoverageResult result, CoverageTarget healthyTarget,
            CoverageTarget unhealthyTarget, boolean onlyStable, boolean failUnhealthy, boolean failUnstable,
            boolean autoUpdateHealth, boolean autoUpdateStability, boolean zoomCoverageChart, int maxNumberOfBuilds) {
        return new CoberturaBuildAction(result, healthyTarget, unhealthyTarget, onlyStable,
            failUnhealthy, failUnstable, autoUpdateHealth, autoUpdateStability, zoomCoverageChart, maxNumberOfBuilds);
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

        if (req.checkIfModified(t, rsp)) {
            return; // up to date
        }
        JFreeChart chart = new CoverageChart(this).createChart();
        ChartUtil.generateGraph(req, rsp, chart, 500, 200);
    }

    public boolean getZoomCoverageChart() {
        return zoomCoverageChart;
    }

    public int getMaxNumberOfBuilds() {
        return maxNumberOfBuilds;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new CoberturaProjectAction(owner, onlyStable));
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        setOwner(r);
    }

    @Override
    public void onLoad(Run<?,?> r) {
        setOwner(r);
    }
}
