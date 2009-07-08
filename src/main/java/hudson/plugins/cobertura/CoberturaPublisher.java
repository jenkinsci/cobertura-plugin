package hudson.plugins.cobertura;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.plugins.cobertura.renderers.SourceCodePainter;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.tasks.Publisher;
import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * Cobertura {@link Publisher}.
 *
 * @author Stephen Connolly
 */
public class CoberturaPublisher extends Publisher {

    private final String coberturaReportFile;
    private final boolean onlyStable;
    
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private CoverageTarget failingTarget;
    public static final CoberturaReportFilenameFilter COBERTURA_FILENAME_FILTER = new CoberturaReportFilenameFilter();

    /**
     * @param coberturaReportFile the report directory
     * @stapler-constructor
     */
    @DataBoundConstructor 
    public CoberturaPublisher(String coberturaReportFile, boolean onlyStable) {
        this.coberturaReportFile = coberturaReportFile;
        this.onlyStable = onlyStable;
        this.healthyTarget = new CoverageTarget();
        this.unhealthyTarget = new CoverageTarget();
        this.failingTarget = new CoverageTarget();
    }

    /**
     * Getter for property 'targets'.
     *
     * @return Value for property 'targets'.
     */
    public List<CoberturaPublisherTarget> getTargets() {
        Map<CoverageMetric, CoberturaPublisherTarget> targets = new TreeMap<CoverageMetric, CoberturaPublisherTarget>();
        for (CoverageMetric metric : healthyTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            target.setHealthy(healthyTarget.getTarget(metric));
            targets.put(metric, target);
        }
        for (CoverageMetric metric : unhealthyTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            target.setUnhealthy(unhealthyTarget.getTarget(metric));
            targets.put(metric, target);
        }
        for (CoverageMetric metric : failingTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            target.setUnstable(failingTarget.getTarget(metric));
            targets.put(metric, target);
        }
        List<CoberturaPublisherTarget> result = new ArrayList<CoberturaPublisherTarget>(targets.values());
        return result;
    }

    /**
     * Setter for property 'targets'.
     *
     * @param targets Value to set for property 'targets'.
     */
    private void setTargets(List<CoberturaPublisherTarget> targets) {
        healthyTarget.clear();
        unhealthyTarget.clear();
        failingTarget.clear();
        for (CoberturaPublisherTarget target : targets) {
            if (target.getHealthy() != null) {
                healthyTarget.setTarget(target.getMetric(), target.getHealthy());
            }
            if (target.getUnhealthy() != null) {
                unhealthyTarget.setTarget(target.getMetric(), target.getUnhealthy());
            }
            if (target.getUnstable() != null) {
                failingTarget.setTarget(target.getMetric(), target.getUnstable());
            }
        }
    }

    /**
     * Getter for property 'coberturaReportFile'.
     *
     * @return Value for property 'coberturaReportFile'.
     */
    public String getCoberturaReportFile() {
        return coberturaReportFile;
    }

    /**
     * Which type of build should be considered.
     * @return the onlyStable
     */
    public boolean getOnlyStable() {
        return onlyStable;
    }

    /**
     * Getter for property 'healthyTarget'.
     *
     * @return Value for property 'healthyTarget'.
     */
    public CoverageTarget getHealthyTarget() {
        return healthyTarget;
    }

    /**
     * Setter for property 'healthyTarget'.
     *
     * @param healthyTarget Value to set for property 'healthyTarget'.
     */
    public void setHealthyTarget(CoverageTarget healthyTarget) {
        this.healthyTarget = healthyTarget;
    }

    /**
     * Getter for property 'unhealthyTarget'.
     *
     * @return Value for property 'unhealthyTarget'.
     */
    public CoverageTarget getUnhealthyTarget() {
        return unhealthyTarget;
    }

    /**
     * Setter for property 'unhealthyTarget'.
     *
     * @param unhealthyTarget Value to set for property 'unhealthyTarget'.
     */
    public void setUnhealthyTarget(CoverageTarget unhealthyTarget) {
        this.unhealthyTarget = unhealthyTarget;
    }

    /**
     * Getter for property 'failingTarget'.
     *
     * @return Value for property 'failingTarget'.
     */
    public CoverageTarget getFailingTarget() {
        return failingTarget;
    }

    /**
     * Setter for property 'failingTarget'.
     *
     * @param failingTarget Value to set for property 'failingTarget'.
     */
    public void setFailingTarget(CoverageTarget failingTarget) {
        this.failingTarget = failingTarget;
    }

    /**
     * Gets the directory where the Cobertura Report is stored for the given project.
     */
    /*package*/
    static File getCoberturaReportDir(AbstractItem project) {
        return new File(project.getRootDir(), "cobertura");
    }

    /**
     * Gets the directory where the Cobertura Report is stored for the given project.
     */
    /*package*/
    static File[] getCoberturaReports(AbstractBuild build) {
        return build.getRootDir().listFiles(COBERTURA_FILENAME_FILTER);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Result threshold = onlyStable ? Result.SUCCESS : Result.UNSTABLE;
        if(build.getResult().isWorseThan(threshold)) {
            listener.getLogger().println("Skipping Cobertura coverage report as build was not " + threshold.toString() + " or better ...");
            return true;
        }
        listener.getLogger().println("Publishing Cobertura coverage report...");
        final AbstractProject<?, ?> project = build.getProject();
        final FilePath[] moduleRoots = project.getModuleRoots();
        final boolean multipleModuleRoots =
            moduleRoots != null && moduleRoots.length > 1;
        final FilePath moduleRoot = multipleModuleRoots ? project.getWorkspace() : project.getModuleRoot();
        final File buildCoberturaDir = build.getRootDir();
        FilePath buildTarget = new FilePath(buildCoberturaDir);

        FilePath[] reports = new FilePath[0];
        try {
            reports = moduleRoot.list(coberturaReportFile);

            // if the build has failed, then there's not
            // much point in reporting an error
            if (build.getResult().isWorseOrEqualTo(Result.FAILURE) && reports.length == 0)
                return true;

        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to find coverage results"));
            build.setResult(Result.FAILURE);
        }

        if (reports.length == 0) {
            String msg = "No coverage results were found using the pattern '"
            + coberturaReportFile + "' relative to '"
            + moduleRoot.getRemote() + "'."
            + "  Did you enter a pattern relative to the correct directory?"
            + "  Did you generate the XML report(s) for Cobertura?";
            listener.getLogger().println(msg);
            build.setResult(Result.FAILURE);
            return true;
        }

        for (int i = 0; i < reports.length; i++) {
            final FilePath targetPath = new FilePath(buildTarget, "coverage" + (i == 0 ? "" : i) + ".xml");
            try {
                reports[i].copyTo(targetPath);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to copy coverage from " + reports[i] + " to " + buildTarget));
                build.setResult(Result.FAILURE);
            }
        }

        listener.getLogger().println("Publishing Cobertura coverage results...");
        Set<String> sourcePaths = new HashSet<String>();
        CoverageResult result = null;
        for (File coberturaXmlReport : getCoberturaReports(build)) {
            try {
                result = CoberturaCoverageParser.parse(coberturaXmlReport, result, sourcePaths);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to parse " + coberturaXmlReport));
                build.setResult(Result.FAILURE);
            }
        }
        if (result != null) {
            result.setOwner(build);
            final FilePath paintedSourcesPath = new FilePath(new File(build.getProject().getRootDir(), "cobertura"));
            paintedSourcesPath.mkdirs();
            SourceCodePainter painter = new SourceCodePainter(paintedSourcesPath, sourcePaths,
                    result.getPaintedSources());

            moduleRoot.act(painter);

            final CoberturaBuildAction action = CoberturaBuildAction.load(build, result, healthyTarget,
                    unhealthyTarget, getOnlyStable());

            build.getActions().add(action);
            Set<CoverageMetric> failingMetrics = failingTarget.getFailingMetrics(result);
            if (!failingMetrics.isEmpty()) {
                listener.getLogger().println("Code coverage enforcement failed for the following metrics:");
                for (CoverageMetric metric : failingMetrics) {
                    listener.getLogger().println("    " + metric.getName());
                }
                listener.getLogger().println("Setting Build to unstable.");
                build.setResult(Result.UNSTABLE);
            }
        } else {
            listener.getLogger().println("No coverage results were successfully parsed.  Did you generate " +
                    "the XML report(s) for Cobertura?");
            build.setResult(Result.FAILURE);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Action getProjectAction(Project project) {
        return new CoberturaProjectAction(project, getOnlyStable());
    }

    /**
     * {@inheritDoc}
     */
    public Descriptor<Publisher> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link CoberturaPublisher}. Used as a singleton. The class is marked as public so that it can be
     * accessed from views.
     * <p/>
     * <p/>
     * See <tt>views/hudson/plugins/cobertura/CoberturaPublisher/*.jelly</tt> for the actual HTML fragment for the
     * configuration screen.
     */
    public static final class DescriptorImpl extends Descriptor<Publisher> {
        CoverageMetric[] metrics = {
                CoverageMetric.PACKAGES,
                CoverageMetric.FILES,
                CoverageMetric.CLASSES,
                CoverageMetric.METHOD,
                CoverageMetric.LINE,
                CoverageMetric.CONDITIONAL,
        };

        /**
         * Constructs a new DescriptorImpl.
         */
        DescriptorImpl() {
            super(CoberturaPublisher.class);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish Cobertura Coverage Report";
        }

        /**
         * Getter for property 'metrics'.
         *
         * @return Value for property 'metrics'.
         */
        public List<CoverageMetric> getMetrics() {
            return Arrays.asList(metrics);
        }

        /**
         * Getter for property 'defaultTargets'.
         *
         * @return Value for property 'defaultTargets'.
         */
        public List<CoberturaPublisherTarget> getDefaultTargets() {
            List<CoberturaPublisherTarget> result = new ArrayList<CoberturaPublisherTarget>();
            result.add(new CoberturaPublisherTarget(CoverageMetric.METHOD, 80, null, null));
            result.add(new CoberturaPublisherTarget(CoverageMetric.LINE, 80, null, null));
            result.add(new CoberturaPublisherTarget(CoverageMetric.CONDITIONAL, 70, null, null));
            return result;
        }

        public List<CoberturaPublisherTarget> getTargets(CoberturaPublisher instance) {
            if (instance == null) {
                return getDefaultTargets();
            }
            return instance.getTargets();
        }

        /**
         * {@inheritDoc}
         */
        public boolean configure(StaplerRequest req) throws FormException {
            req.bindParameters(this, "cobertura.");
            save();
            return super.configure(req);
        }

        /**
         * Creates a new instance of {@link CoberturaPublisher} from a submitted form.
         */
        public CoberturaPublisher newInstance(StaplerRequest req) throws FormException {
            CoberturaPublisher instance = req.bindParameters(CoberturaPublisher.class, "cobertura.");
            ConvertUtils.register(CoberturaPublisherTarget.CONVERTER, CoverageMetric.class);
            List<CoberturaPublisherTarget> targets = req
                    .bindParametersToList(CoberturaPublisherTarget.class, "cobertura.target.");
            instance.setTargets(targets);
            return instance;
        }
    }

    private static class CoberturaReportFilenameFilter implements FilenameFilter {
        /**
         * {@inheritDoc}
         */
        public boolean accept(File dir, String name) {
            // TODO take this out of an anonymous inner class, create a singleton and use a Regex to match the name
            return name.startsWith("coverage") && name.endsWith(".xml");
        }
    }
}
