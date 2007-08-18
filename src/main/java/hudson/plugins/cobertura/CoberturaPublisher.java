package hudson.plugins.cobertura;

import hudson.Launcher;
import hudson.FilePath;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import hudson.plugins.cobertura.results.ProjectCoverage;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.model.*;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.StaplerRequest;

import java.io.*;
import java.util.Set;

/**
 * Cobertura {@link Publisher}.
 *
 * @author Stephen Connolly
 */
public class CoberturaPublisher extends Publisher {

    private final String coberturaReportDir;

    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private CoverageTarget failingTarget;

    /**
     *
     * @param coberturaReportDir the report directory
     * @stapler-constructor
     */
    public CoberturaPublisher(String coberturaReportDir) {
        this.coberturaReportDir = coberturaReportDir;
        this.healthyTarget = new CoverageTarget();
        this.unhealthyTarget = new CoverageTarget();
        this.failingTarget = new CoverageTarget();
    }

    public String getCoberturaReportDir() {
        return coberturaReportDir;
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

    /** Gets the directory where the Cobertura Report is stored for the given project. */
    /*package*/ static File getCoberturaReportDir(AbstractItem project) {
        return new File(project.getRootDir(), "cobertura");
    }

    /** Gets the directory where the Cobertura Report is stored for the given project. */
    /*package*/
    static File getCoberturaReport(Build build) {
        return new File(build.getRootDir(), "coverage.xml");
    }


    public boolean perform(Build<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        listener.getLogger().println("Publishing Cobertura coverage report...");
        FilePath coverageReport = build.getParent().getWorkspace().child(coberturaReportDir);

        FilePath target = new FilePath(getCoberturaReportDir(build.getParent()));
        final File buildCoberturaDir = build.getRootDir();
        FilePath buildTarget = new FilePath(buildCoberturaDir);

        try {
            // if the build has failed, then there's not
            // much point in reporting an error
            if (build.getResult().isWorseOrEqualTo(Result.FAILURE) && !coverageReport.exists())
                return true;

            // Copy the code
            coverageReport.copyRecursiveTo("**/*", target);
            // Copy the xml report

            coverageReport.copyRecursiveTo("coverage.xml", buildTarget);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to copy coverage from " + coverageReport + " to " + target));
            build.setResult(Result.FAILURE);
        }

        String workspacePath = "";
        try {
            workspacePath = build.getParent().getWorkspace().act(new FilePath.FileCallable<String>() {
                public String invoke(File file, VirtualChannel virtualChannel) throws IOException {
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException e) {
                        return file.getAbsolutePath();
                    }
                }
            });
        } catch (IOException e) {
        }
        if (!workspacePath.endsWith(File.separator)) {
            workspacePath += File.separator;
        }

        File coberturaXmlReport = getCoberturaReport(build);
        if (coberturaXmlReport.exists()) {
            listener.getLogger().println("Publishing Cobertura coverage results...");
            ProjectCoverage result = null;
            try {
                result = CoberturaCoverageParser.parse(coberturaXmlReport, workspacePath);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to copy coverage from " + coverageReport + " to " + target));
                build.setResult(Result.FAILURE);
            }
            final CoberturaBuildAction action = CoberturaBuildAction.load(build, workspacePath, result, healthyTarget, unhealthyTarget);

            build.getActions().add(action);
            Set<CoverageMetric> failingMetrics = failingTarget.getFailingMetrics(result);
            if (!failingMetrics.isEmpty()) {
                listener.getLogger().println("Code coverage enforcement failed for the following metrics:");
                for (CoverageMetric metric : failingMetrics) {
                    listener.getLogger().println("    " + metric);
                }
                listener.getLogger().println("Setting Build to unstable.");
                build.setResult(Result.UNSTABLE);
            }

        } else {
            flagMissingCoberturaXml(listener, build);
        }

        return true;
    }

    private void flagMissingCoberturaXml(BuildListener listener, Build<?, ?> build) {
        listener.getLogger().println("Could not find '" + coberturaReportDir + "/coverage.xml'.  Did you generate " +
                "the XML report for Cobertura?");
        build.setResult(Result.FAILURE);
    }


    public Action getProjectAction(Project project) {
        return new CoberturaProjectAction(project);
    }

    public Descriptor<Publisher> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    /** Descriptor should be singleton. */
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
        DescriptorImpl() {
            super(CoberturaPublisher.class);
        }

        /** This human readable name is used in the configuration screen. */
        public String getDisplayName() {
            return "Publish Cobertura Coverage Report";
        }


        public boolean configure(StaplerRequest req) throws FormException {
            req.bindParameters(this, "cobertura.");
            save();
            return super.configure(req);    //To change body of overridden methods use File | Settings | File Templates.
        }

        /** Creates a new instance of {@link CoberturaPublisher} from a submitted form. */
        public CoberturaPublisher newInstance(StaplerRequest req) throws FormException {
            CoberturaPublisher instance = req.bindParameters(CoberturaPublisher.class, "cobertura.");
            req.bindParameters(instance.failingTarget, "coberturaFailingTarget.");
            req.bindParameters(instance.healthyTarget, "coberturaHealthyTarget.");
            req.bindParameters(instance.unhealthyTarget, "coberturaUnhealthyTarget.");
            // start ugly hack
            if (instance.healthyTarget.isEmpty()) {
                instance.healthyTarget = new CoverageTarget(70, 80, 80);
            }
            // end ugly hack
            return instance;
        }
    }
}
