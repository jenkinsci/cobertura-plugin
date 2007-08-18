package hudson.plugins.cobertura;

import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.plugins.cobertura.results.*;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageTarget;
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
public class CoberturaBuildAction extends AbstractPackageAggregatedMetrics implements HealthReportingAction, StaplerProxy {
    public final Build owner;
    private String buildBaseDir;
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;

    private transient WeakReference<ProjectCoverage> report;

    public HealthReport getBuildHealth() {
        if (healthyTarget == null || unhealthyTarget == null) return null;
        ProjectCoverage projectCoverage = getResult();
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
        switch (minKey) {
            case METHOD:
                description.append("Methods ");
                description.append(projectCoverage.getMethodCoverage().getPercentage());
                description.append("% (");
                description.append(projectCoverage.getMethodCoverage().toString());
                description.append(")");
                break;
            case CONDITIONAL:
                description.append("Conditionals ");
                description.append(projectCoverage.getConditionalCoverage().getPercentage());
                description.append("% (");
                description.append(projectCoverage.getConditionalCoverage().toString());
                description.append(")");
                break;
            case LINE:
                description.append("Lines ");
                description.append(projectCoverage.getLineCoverage().getPercentage());
                description.append("% (");
                description.append(projectCoverage.getLineCoverage().toString());
                description.append(")");
                break;
            default:
                return null;
        }
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

    /** Gets the previous {@link CoberturaBuildAction} of the given build. */
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

    CoberturaBuildAction(Build owner, String workspacePath, ProjectCoverage r, CoverageTarget healthyTarget,
                      CoverageTarget unhealthyTarget) {
        this.owner = owner;
        this.report = new WeakReference<ProjectCoverage>(r);
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


    /** Obtains the detailed {@link CoverageReport} instance. */
    public synchronized ProjectCoverage getResult() {
        if (report != null) {
            ProjectCoverage r = report.get();
            if (r != null) return r;
        }

        File reportFile = CoberturaPublisher.getCoberturaReport(owner);
        try {

            ProjectCoverage r = CoberturaCoverageParser.parse(reportFile, buildBaseDir);
            ;
            r.setOwner(owner);

            report = new WeakReference<ProjectCoverage>(r);
            return r;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load " + reportFile, e);
            return null;
        }
    }

    // the following is ugly but I might need it

    /** {@inheritDoc} */
    public PackageCoverage findPackageCoverage(String name) {
        return getResult().findPackageCoverage(name);
    }

    /** {@inheritDoc} */
    public FileCoverage findFileCoverage(String name) {
        return getResult().findFileCoverage(name);
    }

    /** {@inheritDoc} */
    public ClassCoverage findClassCoverage(String name) {
        return getResult().findClassCoverage(name);
    }

    /** {@inheritDoc} */
    public int getPackages() {
        return getResult().getPackages();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getFiles() {
        return getResult().getFiles();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getClasses() {
        return getResult().getClasses();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getLoc() {
        return getResult().getLoc();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getNcloc() {
        return getResult().getNcloc();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public Ratio getMethodCoverage() {
        return getResult().getMethodCoverage();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public Ratio getLineCoverage() {
        return getResult().getLineCoverage();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public Ratio getConditionalCoverage() {
        return getResult().getConditionalCoverage();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public Ratio getElementCoverage() {
        return getResult().getElementCoverage();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getConditionals() {
        return getResult().getConditionals();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getMethods() {
        return getResult().getMethods();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getCoveredlines() {
        return getResult().getCoveredlines();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getCoveredmethods() {
        return getResult().getCoveredmethods();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getCoveredconditionals() {
        return getResult().getCoveredconditionals();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getLines() {
        return getResult().getLines();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getCoveredelements() {
        return getResult().getCoveredelements();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /** {@inheritDoc} */
    public int getElements() {
        return getResult().getElements();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private static final Logger logger = Logger.getLogger(CoberturaBuildAction.class.getName());

    public static CoberturaBuildAction load(Build<?, ?> build, String workspacePath, ProjectCoverage result,
                                         CoverageTarget healthyTarget, CoverageTarget unhealthyTarget) {
        return new CoberturaBuildAction(build, workspacePath, result, healthyTarget, unhealthyTarget);
    }
}
