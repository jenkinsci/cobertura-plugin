package hudson.plugins.cobertura;

import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageResult;
import org.kohsuke.stapler.StaplerProxy;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
    public final Build owner;
    private String buildBaseDir;
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;

    private transient WeakReference<CoverageResult> report;

    public HealthReport getBuildHealth() {
        if (healthyTarget == null || unhealthyTarget == null) return null;
        CoverageResult projectCoverage = getResult();
        Map<CoverageMetric, Integer> scores = healthyTarget.getRangeScores(unhealthyTarget, projectCoverage);
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
        description.append(minKey.toString());
        description.append(" ");
        description.append(projectCoverage.getCoverage(minKey).getPercentage());
        description.append("% (");
        description.append(projectCoverage.getCoverage(minKey).toString());
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
}
