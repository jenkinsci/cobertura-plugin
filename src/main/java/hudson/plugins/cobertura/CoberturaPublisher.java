package hudson.plugins.cobertura;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.cobertura.adapter.CoberturaReportAdapter;
import hudson.plugins.cobertura.renderers.SourceCodePainter;
import hudson.plugins.cobertura.renderers.SourceEncoding;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.jenkinsci.Symbol;

/**
 * Cobertura {@link Publisher}.
 *
 * @author Stephen Connolly
 */
public class CoberturaPublisher extends Recorder implements SimpleBuildStep {

    private String coberturaReportFile;

    private boolean onlyStable;

    private boolean failUnhealthy;

    private boolean failUnstable;

    private boolean autoUpdateHealth;

    private boolean autoUpdateStability;

    private boolean zoomCoverageChart;

    private int maxNumberOfBuilds = 0;

    private boolean failNoReports = true;

    private String lineCoverageTargets = null;

    private String packageCoverageTargets = null;

    private String fileCoverageTargets = null;

    private String classCoverageTargets = null;

    private String methodCoverageTargets = null;

    private String conditionalCoverageTargets = null;

    private CoverageTarget healthyTarget = new CoverageTarget();

    private CoverageTarget unhealthyTarget = new CoverageTarget();

    private CoverageTarget failingTarget = new CoverageTarget();

    public static final CoberturaReportFilenameFilter COBERTURA_FILENAME_FILTER = new CoberturaReportFilenameFilter();

    private SourceEncoding sourceEncoding = SourceEncoding.UTF_8;

    private boolean enableNewApi;

    @Deprecated
    public CoberturaPublisher(String coberturaReportFile, boolean onlyStable, boolean failUnhealthy, boolean failUnstable,
             boolean autoUpdateHealth, boolean autoUpdateStability, boolean zoomCoverageChart, boolean failNoReports, SourceEncoding sourceEncoding,
             int maxNumberOfBuilds) {
         this.coberturaReportFile = coberturaReportFile;
         this.onlyStable = onlyStable;
         this.failUnhealthy = failUnhealthy;
         this.failUnstable = failUnstable;
         this.autoUpdateHealth = autoUpdateHealth;
         this.autoUpdateStability = autoUpdateStability;
         this.zoomCoverageChart = zoomCoverageChart;
         this.failNoReports = failNoReports;
         this.sourceEncoding = sourceEncoding;
         this.maxNumberOfBuilds = maxNumberOfBuilds;
         this.healthyTarget = new CoverageTarget();
         this.unhealthyTarget = new CoverageTarget();
         this.failingTarget = new CoverageTarget();
    }

    @DataBoundConstructor
    public CoberturaPublisher() {
        this("", true, true, true, true, true, true, true, SourceEncoding.UTF_8, 42);
    }

    /**
     * Getter for property 'targets'.
     *
     * @return Value for property 'targets'.
     */
    public List<CoberturaPublisherTarget> getTargets() {
        Map<CoverageMetric, CoberturaPublisherTarget> targets = new TreeMap<CoverageMetric, CoberturaPublisherTarget>();
        float checker;
        for (CoverageMetric metric : healthyTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            checker = (float) healthyTarget.getTarget(metric) / 100000f;
            if (checker <= 0.001f) {
                checker = (float) (Math.round(checker * 100000f));
            }
            target.setHealthy(checker);
            targets.put(metric, target);
        }
        for (CoverageMetric metric : unhealthyTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            checker = (float) unhealthyTarget.getTarget(metric) / 100000f;
            if (checker <= 0.001f) {
                checker = (float) (Math.round(checker * 100000f));
            }
            target.setUnhealthy(checker);
            targets.put(metric, target);
        }
        for (CoverageMetric metric : failingTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            checker = (float) failingTarget.getTarget(metric) / 100000f;
            if (checker <= 0.001f) {
                checker = (float) (Math.round(checker * 100000f));
            }
            target.setUnstable(checker);
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
    private void setTargets(List<CoberturaPublisherTarget> targets) throws AbortException {
        healthyTarget.clear();
        unhealthyTarget.clear();
        failingTarget.clear();
        float rounded;
        for (CoberturaPublisherTarget target : targets) {
            if (target.getHealthy() != null) {
                rounded = (Math.round((float) 100f * target.getHealthy()));
                rounded = roundDecimalFloat(rounded);
                healthyTarget.setTarget(target.getMetric(), (int) ((float) 100000f * rounded));
            }
            if (target.getUnhealthy() != null) {
                rounded = (Math.round((float) 100f * target.getUnhealthy()));
                rounded = roundDecimalFloat(rounded);
                unhealthyTarget.setTarget(target.getMetric(), (int) ((float) 100000f * rounded));
            }
            if (target.getUnstable() != null) {
                rounded = (Math.round((float) 100f * target.getUnstable()));
                rounded = roundDecimalFloat(rounded);
                failingTarget.setTarget(target.getMetric(), (int) ((float) 100000f * rounded));
            }
            setTargetString(target);
        }
    }

    private void setTargetString(CoberturaPublisherTarget target) throws AbortException {
        switch (target.getMetric()) {
            case PACKAGES:
                setPackageCoverageTargets(MessageFormat.format("{0}", target.toString()));
                break;
            case FILES:
                setFileCoverageTargets(MessageFormat.format("{0}", target.toString()));
                break;
            case CLASSES:
                setClassCoverageTargets(MessageFormat.format("{0}", target.toString()));
                break;
            case METHOD:
                setMethodCoverageTargets(MessageFormat.format("{0}", target.toString()));
                break;
            case LINE:
                setLineCoverageTargets(MessageFormat.format("{0}", target.toString()));
                break;
            case CONDITIONAL:
                setConditionalCoverageTargets(MessageFormat.format("{0}", target.toString()));
                break;
        }
    }

    /**
     * @param coberturaReportFile the report directory
     */
    @DataBoundSetter
    public void setCoberturaReportFile(String coberturaReportFile) {

        this.coberturaReportFile = coberturaReportFile;
    }

    /**
     * Getter for property 'coberturaReportFile'.
     *
     * @return Value for property 'coberturaReportFile'.
     */
    public String getCoberturaReportFile() {
        return coberturaReportFile;
    }

    @DataBoundSetter
    public void setOnlyStable(boolean onlyStable) {
        this.onlyStable = onlyStable;
    }

    /**
     * Which type of build should be considered.
     * @return the onlyStable
     */
    public boolean getOnlyStable() {
        return onlyStable;
    }

    @DataBoundSetter
    public void setMaxNumberOfBuilds(int maxNumberOfBuilds) {
        this.maxNumberOfBuilds = maxNumberOfBuilds;
    }

    public int getMaxNumberOfBuilds() {
		return maxNumberOfBuilds;
	}

    @DataBoundSetter
    public void setFailUnhealthy(boolean failUnhealthy) {
        this.failUnhealthy = failUnhealthy;
    }

    /**
     * Getter for property 'failUnhealthy'.
     *
     * @return Value for property 'failUnhealthy'.
     */
    public boolean getFailUnhealthy() {
        return failUnhealthy;
    }


    @DataBoundSetter
    public void setFailUnstable(boolean failUnstable) {
        this.failUnstable = failUnstable;
    }

    /**
     * Getter for property 'failUnstable'.
     *
     * @return Value for property 'failUnstable'.
     */
    public boolean getFailUnstable() {
        return failUnstable;
    }

    @DataBoundSetter
    public void setAutoUpdateHealth(boolean autoUpdateHealth) {
        this.autoUpdateHealth = autoUpdateHealth;
    }

    /**
     * Getter for property 'autoUpdateHealth'.
     *
     * @return Value for property 'autoUpdateHealth'.
     */
    public boolean getAutoUpdateHealth() {
        return autoUpdateHealth;
    }

    @DataBoundSetter
    public void setAutoUpdateStability(boolean autoUpdateStability) {
        this.autoUpdateStability = autoUpdateStability;
    }

    /**
     * Getter for property 'autoUpdateStability'.
     *
     * @return Value for property 'autoUpdateStability'.
     */
    public boolean getAutoUpdateStability() {
        return autoUpdateStability;
    }

    @DataBoundSetter
    public void setZoomCoverageChart(boolean zoomCoverageChart) {
        this.zoomCoverageChart = zoomCoverageChart;
    }

    public boolean getZoomCoverageChart() {
        return zoomCoverageChart;
    }

    @DataBoundSetter
    public void setFailNoReports(boolean failNoReports) {
        this.failNoReports = failNoReports;
    }

    public boolean isFailNoReports() {
        return failNoReports;
    }

    /**
     * Setter for property 'lineCoverageTargets'.
     *
     * @param targets Value to set for property 'lineCoverageTargets'.
     */
    @DataBoundSetter
    public void setLineCoverageTargets(String targets) throws AbortException {
      lineCoverageTargets = targets;
    }

    /**
     * Getter for property 'lineCoverageTargets'.
     *
     * @return Value for property 'lineCoverageTargets'.
     */
    public String getLineCoverageTargets() {
      return lineCoverageTargets;
    }

    /**
     * Setter for property 'packageCoverageTargets'.
     *
     * @param targets Value to set for property 'packageCoverageTargets'.
     */
    @DataBoundSetter
    public void setPackageCoverageTargets(String targets) {
      packageCoverageTargets = targets;
    }

    /**
     * Getter for property 'packageCoverageTargets'.
     *
     * @return Value for property 'packageCoverageTargets'.
     */
    public String getPackageCoverageTargets() {
      return packageCoverageTargets;
    }

    /**
     * Setter for property 'fileCoverageTargets'.
     *
     * @param targets Value to set for property 'fileCoverageTargets'.
     */
    @DataBoundSetter
    public void setFileCoverageTargets(String targets) {
      fileCoverageTargets = targets;
    }

    /**
     * Getter for property 'fileCoverageTargets'.
     *
     * @return Value for property 'fileCoverageTargets'.
     */
    public String getFileCoverageTargets() {
      return fileCoverageTargets;
    }

    /**
     * Setter for property 'classCoverageTargets'.
     *
     * @param targets Value to set for property 'classCoverageTargets'.
     */
    @DataBoundSetter
    public void setClassCoverageTargets(String targets) {
      classCoverageTargets = targets;
    }

    /**
     * Getter for property 'classCoverageTargets'.
     *
     * @return Value for property 'classCoverageTargets'.
     */
    public String getClassCoverageTargets() {
      return classCoverageTargets;
    }

    /**
     * Setter for property 'methodCoverageTargets'.
     *
     * @param targets Value to set for property 'methodCoverageTargets'.
     */
    @DataBoundSetter
    public void setMethodCoverageTargets(String targets) {
      methodCoverageTargets = targets;
    }

    /**
     * Getter for property 'methodCoverageTargets'.
     *
     * @return Value for property 'methodCoverageTargets'.
     */
    public String getMethodCoverageTargets() {
      return methodCoverageTargets;
    }

    /**
     * Setter for property 'conditionalCoverageTargets'.
     *
     * @param targets Value to set for property 'conditionalCoverageTargets'.
     */
    @DataBoundSetter
    public void setConditionalCoverageTargets(String targets) {
      conditionalCoverageTargets = targets;
    }

    /**
     * Getter for property 'conditionalCoverageTargets'.
     *
     * @return Value for property 'conditionalCoverageTargets'.
     */
    public String getConditionalCoverageTargets() {
      return conditionalCoverageTargets;
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
    static File[] getCoberturaReports(Run<?, ?> build) {
        return build.getRootDir().listFiles(COBERTURA_FILENAME_FILTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {

        setAllCoverageTargets();
        recoverAutoUpdatedTargets(build);

        Result threshold = onlyStable ? Result.SUCCESS : Result.FAILURE;
        Result buildResult = build.getResult();
        if (buildResult != null && buildResult.isWorseThan(threshold)) {
            logMessage(listener, "Skipping Cobertura coverage report as build was not " + threshold.toString() + " or better ...");
            return;
        }

        logMessage(listener, "Publishing Cobertura coverage report...");
        final File buildCoberturaDir = build.getRootDir();
        FilePath buildTarget = new FilePath(buildCoberturaDir);
        EnvVars env = build.getEnvironment(listener);

        FilePath[] reports = null;
        try {
            reports = workspace.act(new ParseReportCallable(env.expand(coberturaReportFile)));

            // if the build has failed, then there's not
            // much point in reporting an error
            if (buildResult != null && buildResult.isWorseOrEqualTo(Result.FAILURE) && reports.length == 0) {
                return;
            }

        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to find coverage results"));
            throw new CoberturaAbortException("Unable to find coverage results");
        }

        if (reports.length == 0) {
            String msg = "No coverage results were found using the pattern '"
                    + coberturaReportFile + "' relative to '"
                    + workspace.getRemote() + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the XML report(s) for Cobertura?";
            logMessage(listener, msg);
            if (failNoReports) {
                throw new CoberturaAbortException(msg);
            } else {
                logMessage(listener, "Skipped cobertura reports.");
            }
            return;
        }

        for (int i = 0; i < reports.length; i++) {
            final FilePath targetPath = new FilePath(buildTarget, "coverage" + (i == 0 ? "" : i) + ".xml");
            try {
                reports[i].copyTo(targetPath);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                String msg = "Unable to copy coverage from " + reports[i] + " to " + buildTarget;
                e.printStackTrace(listener.fatalError(msg));
                throw new CoberturaAbortException(msg);
            }
        }

        logMessage(listener, "Publishing Cobertura coverage results...");
        Set<String> sourcePaths = new HashSet<String>();
        CoverageResult result = null;
        for (File coberturaXmlReport : getCoberturaReports(build)) {
            try {
                result = CoberturaCoverageParser.parse(coberturaXmlReport, result, sourcePaths);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to parse " + coberturaXmlReport));
                throw new CoberturaAbortException("Unable to parse " + coberturaXmlReport);
            }
        }
        if (result != null) {
            logMessage(listener, "Cobertura coverage report found.");
            result.setOwner(build);
            final FilePath paintedSourcesPath = new FilePath(new File(build.getParent().getRootDir(), "cobertura"));
            paintedSourcesPath.mkdirs();

            if (sourcePaths.contains(".")) {
                sourcePaths.remove(".");
                for (FilePath f : reports) {
                    FilePath p = f.getParent();
                    if (p != null && p.isDirectory()) {
                        sourcePaths.add(p.getRemote());
                    }
                }
            }

            SourceCodePainter painter = new SourceCodePainter(paintedSourcesPath, sourcePaths,
                    result.getPaintedSources(), listener, getSourceEncoding());

            workspace.act(painter);

            final CoberturaBuildAction action = CoberturaBuildAction.load(result, healthyTarget,
                    unhealthyTarget, getOnlyStable(), getFailUnhealthy(), getFailUnstable(), getAutoUpdateHealth(), getAutoUpdateStability(),
                    getZoomCoverageChart(), getMaxNumberOfBuilds());

            build.addAction(action);

            if(enableNewApi) {
                CoberturaReportAdapter newApiAdapter = new CoberturaReportAdapter(env.expand(coberturaReportFile));
                newApiAdapter.performCoveragePlugin(build, workspace, launcher, listener);
            }

            Set<CoverageMetric> failingMetrics = failingTarget.getFailingMetrics(result);
            if (!failingMetrics.isEmpty()) {
                logMessage(listener, "Code coverage enforcement failed for the following metrics:");
                float oldStabilityPercent;
                float setStabilityPercent;
                for (CoverageMetric metric : failingMetrics) {
                    oldStabilityPercent = failingTarget.getObservedPercent(result, metric);
                    setStabilityPercent = failingTarget.getSetPercent(result, metric);
                    logMessage(listener, "    " + metric.getName() + "'s stability is " + roundDecimalFloat(oldStabilityPercent * 100f) + " and set mininum stability is " + roundDecimalFloat(setStabilityPercent * 100f) + ".");
                }
                if (!getFailUnstable()) {
                    logMessage(listener, "Setting Build to unstable.");
                    build.setResult(Result.UNSTABLE);
                } else {
                    action.setFailMessage(String.format("Build failed because following metrics did not meet stability target: %s.", failingMetrics.toString()));
                    throw new CoberturaAbortException("Failing build due to unstability.");
                }
            }
            if (getFailUnhealthy()) {
                Set<CoverageMetric> unhealthyMetrics = unhealthyTarget.getFailingMetrics(result);
                if (!unhealthyMetrics.isEmpty()) {
                    logMessage(listener, "Unhealthy for the following metrics:");
                    float oldHealthyPercent;
                    float setHealthyPercent;
                    for (CoverageMetric metric : unhealthyMetrics) {
                        oldHealthyPercent = unhealthyTarget.getObservedPercent(result, metric);
                        setHealthyPercent = unhealthyTarget.getSetPercent(result, metric);
                        listener.getLogger().println("    " + metric.getName() + "'s health is " + roundDecimalFloat(oldHealthyPercent * 100f) + " and set minimum health is " + roundDecimalFloat(setHealthyPercent * 100f) + ".");
                    }
                    action.setFailMessage(String.format("Build failed because following metrics did not meet health target: %s.", unhealthyMetrics.toString()));
                    throw new CoberturaAbortException("Failing build because it is unhealthy.");
                }
            }
            if (build.getResult() == null || build.getResult() == Result.SUCCESS) {
                if (getAutoUpdateHealth()) {
                    setNewPercentages(result, true, listener);
                }

                if (getAutoUpdateStability()) {
                    setNewPercentages(result, false, listener);
                }
            }
        } else {
            throw new CoberturaAbortException("No coverage results were successfully parsed.  Did you generate "
                    + "the XML report(s) for Cobertura?");
        }
    }

    private void recoverAutoUpdatedTargets(Run<?, ?> build) {
        CoberturaCoverageRecordAction currentRecordAction = new CoberturaCoverageRecordAction();
        currentRecordAction.setAutoUpdateHealth(getAutoUpdateHealth());
        currentRecordAction.setAutoUpdateStability(getAutoUpdateStability());

        CoverageTarget currentSetUnhealthyTarget = new CoverageTarget();
        for (CoverageMetric target : unhealthyTarget.getTargets()) {
            currentSetUnhealthyTarget.setTarget(target, unhealthyTarget.getTarget(target));
        }
        CoverageTarget currentSetFailingTarget = new CoverageTarget();
        for (CoverageMetric target : failingTarget.getTargets()) {
            currentSetFailingTarget.setTarget(target, failingTarget.getTarget(target));
        }

        currentRecordAction.setLastUnhealthyTarget(currentSetUnhealthyTarget);
        currentRecordAction.setLastFailingTarget(currentSetFailingTarget);

        Run lastBuild = BuildUtils.getPreviousNotFailedCompletedBuild(build);
        CoberturaCoverageRecordAction lastRecordAction;
        if (lastBuild != null && (lastRecordAction = lastBuild.getAction(CoberturaCoverageRecordAction.class)) != null) {
            if (getAutoUpdateHealth() && lastRecordAction.isAutoUpdateHealth()) {
                CoverageTarget lastSetUnhealthyTarget = lastRecordAction.getLastUnhealthyTarget();
                CoverageTarget lastUpdatedUnhealthyTarget = lastRecordAction.getLastUpdatedUnhealthyTarget();
                for (CoverageMetric target : unhealthyTarget.getTargets()) {
                    // if the unhealthy target hasn't updated, we will use the auto-updated value from the last build
                    if (unhealthyTarget.getTarget(target).equals(lastSetUnhealthyTarget.getTarget(target))) {
                        unhealthyTarget.setTarget(target, lastUpdatedUnhealthyTarget.getTarget(target));
                    }
                }
            }

            if (getAutoUpdateStability() && lastRecordAction.isAutoUpdateStability()) {
                CoverageTarget lastFailingTarget = lastRecordAction.getLastFailingTarget();
                CoverageTarget lastUpdatedFailingTarget = lastRecordAction.getLastUpdatedFailingTarget();
                for (CoverageMetric target : failingTarget.getTargets()) {
                    // if the failing target hasn't updated, we will use the auto-updated value from the last build
                    if (failingTarget.getTarget(target).equals(lastFailingTarget.getTarget(target))) {
                        failingTarget.setTarget(target, lastUpdatedFailingTarget.getTarget(target));
                    }
                }
            }
        }

        currentRecordAction.setLastUpdatedUnhealthyTarget(unhealthyTarget);
        currentRecordAction.setLastUpdatedFailingTarget(failingTarget);

        build.addAction(currentRecordAction);
    }

    /**
     * Parses any coverage strings provided to the plugin and sets the
     * coverage targets.
     *
     * @throws CoberturaAbortException
     */
    private void setAllCoverageTargets() throws CoberturaAbortException {
      if (lineCoverageTargets != null) {
        try {
          setCoverageTargets(CoverageMetric.LINE, lineCoverageTargets);
        } catch (NumberFormatException e) {
          throw new CoberturaAbortException("Invalid value for lineCoverageTargets");
        }
      }

      if (packageCoverageTargets != null) {
        try {
          setCoverageTargets(CoverageMetric.PACKAGES, packageCoverageTargets);
        } catch (NumberFormatException e) {
          throw new CoberturaAbortException("Invalid value for packageCoverageTargets");
        }
      }

      if (fileCoverageTargets != null) {
        try {
          setCoverageTargets(CoverageMetric.FILES, fileCoverageTargets);
        } catch (NumberFormatException e) {
          throw new CoberturaAbortException("Invalid value for fileCoverageTargets");
        }
      }

      if (classCoverageTargets != null) {
        try {
          setCoverageTargets(CoverageMetric.CLASSES, classCoverageTargets);
        } catch (NumberFormatException e) {
          throw new CoberturaAbortException("Invalid value for classCoverageTargets");
        }
      }

      if (methodCoverageTargets != null) {
        try {
          setCoverageTargets(CoverageMetric.METHOD, methodCoverageTargets);
        } catch (NumberFormatException e) {
          throw new CoberturaAbortException("Invalid value for methodCoverageTargets");
        }
      }

      if (conditionalCoverageTargets != null) {
        try {
          setCoverageTargets(CoverageMetric.CONDITIONAL, conditionalCoverageTargets);
        } catch (NumberFormatException e) {
          throw new CoberturaAbortException("Invalid value for conditionalCoverageTargets");
        }
      }
    }

    /**
     * Parses a coverage string into the parts. A coverage string is of the
     * form <health y%>,<unhealthy %>,<unstable %>
     * @param targets The coverage string
     * @return an array[3] of floats with the coverage thresholds
     */
    private float[] parseCoverageTargets(String targets) {
      String[] targetValues = targets.split(",");
      float[] result = new float[3];

      for (int i = 0; i < targetValues.length && i < result.length; i++) {
        try {
            result[i] = Float.valueOf(targetValues[i]);
        } catch (NumberFormatException ex) {
            result[i] = 0;
        }
      }
      return result;
    }


    /**
     * Sets the coverage for one metric from a coverage string
     * @param metric The metric to set
     * @param targets A coverage string containing healthy %, unhealthy %,
     * unstable %
     */
    private void setCoverageTargets(CoverageMetric metric, String targets) {
      float[] targetValues = parseCoverageTargets(targets);

      healthyTarget.setTarget(metric, Math.round(targetValues[0] * 100000));
      unhealthyTarget.setTarget(metric, Math.round(targetValues[1] * 100000));
      failingTarget.setTarget(metric, Math.round(targetValues[2] * 100000));
    }

    /**
     * Changes unhealthy or unstable percentage fields for ratcheting.
     */
    private void setNewPercentages(CoverageResult result, boolean select, TaskListener listener) {
        Set<CoverageMetric> healthyMetrics = healthyTarget.getAllMetrics(result);
        float newPercent;
        float oldPercent;
        if (!healthyMetrics.isEmpty()) {
            for (CoverageMetric metric : healthyMetrics) {
                newPercent = healthyTarget.getObservedPercent(result, metric);
                newPercent = (float) (Math.round(newPercent * 100f));
                if (select) {
                    oldPercent = unhealthyTarget.getSetPercent(result, metric);
                    oldPercent = (float) (Math.round(oldPercent * 100f));
                } else {
                    oldPercent = failingTarget.getSetPercent(result, metric);
                    oldPercent = (float) (Math.round(oldPercent * 100f));
                }
                if (newPercent > oldPercent) {
                    if (select) {
                        unhealthyTarget.setTarget(metric, (int) (newPercent * 1000f));
                        listener.getLogger().println("    " + metric.getName() + "'s new health minimum is: " + roundDecimalFloat(newPercent));
                    } else {
                        failingTarget.setTarget(metric, (int) (newPercent * 1000f));
                        listener.getLogger().println("    " + metric.getName() + "'s new stability minimum is: " + roundDecimalFloat(newPercent));
                    }
                }
            }
        }
    }

    static private void logMessage(TaskListener listener, String message) {
        listener.getLogger().printf("%s%n", wrappedMessage(message));
    }

    static private String wrappedMessage(String message) {
        return String.format("[Cobertura] %s%n", message);
    }

    /**
     * {@inheritDoc}
     */
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundSetter
    public void setSourceEncoding(SourceEncoding sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public SourceEncoding getSourceEncoding() {
        return sourceEncoding;
    }

    public boolean isEnableNewApi() {
        return enableNewApi;
    }

    @DataBoundSetter
    public void setEnableNewApi(boolean enableNewApi) {
        this.enableNewApi = enableNewApi;
    }

    public static class ParseReportCallable extends MasterToSlaveFileCallable<FilePath[]> {

        private static final long serialVersionUID = 1L;

        private final String reportFilePath;

        public ParseReportCallable(String reportFilePath) {
            this.reportFilePath = reportFilePath;
        }

        public FilePath[] invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            FilePath[] r = new FilePath(f).list(reportFilePath);

            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

            for (FilePath filePath : r) {
                InputStream is = null;
                XMLEventReader reader = null;
                try {
                    is = filePath.read();
                    reader = factory.createXMLEventReader(is);
                    while (reader.hasNext()) {
                        XMLEvent event = reader.nextEvent();
                        if (event.isStartElement()) {
                            StartElement start = (StartElement) event;
                            if (start.getName().getLocalPart().equals("coverage")) {
                                // This is a cobertura coverage report file
                                break;
                            } else {
                                throw new IOException(filePath + " is not a cobertura coverage report, please check your report pattern");
                            }
                        }
                    }
                } catch (XMLStreamException e) {
                    throw new IOException(filePath + " is not an XML file, please check your report pattern");
                } finally {
                    try {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (XMLStreamException ex) {
                                //
                            }
                        }
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }

            }
            return r;
        }
    }

    /**
     * Descriptor for {@link CoberturaPublisher}. Used as a singleton. The class is marked as public so that it can be
     * accessed from views.
     *
     * See <tt>views/hudson/plugins/cobertura/CoberturaPublisher/*.jelly</tt> for the actual HTML fragment for the
     * configuration screen.
     */
    @Extension
    @Symbol("cobertura")
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        CoverageMetric[] metrics = {
            CoverageMetric.PACKAGES,
            CoverageMetric.FILES,
            CoverageMetric.CLASSES,
            CoverageMetric.METHOD,
            CoverageMetric.LINE,
            CoverageMetric.CONDITIONAL,};

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return Messages.CoberturaPublisher_displayName();
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
            result.add(new CoberturaPublisherTarget(CoverageMetric.METHOD, CoberturaPublisherTarget.DEFAULT_HEALTHY_TARGET, null, null));
            result.add(new CoberturaPublisherTarget(CoverageMetric.LINE, CoberturaPublisherTarget.DEFAULT_HEALTHY_TARGET, null, null));
            result.add(new CoberturaPublisherTarget(CoverageMetric.CONDITIONAL, 70f, null, null));
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
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this, "cobertura.");
            save();
            return super.configure(req, formData);
        }

        /**
         * Creates a new instance of {@link CoberturaPublisher} from a submitted form.
         */
        @Override
        public CoberturaPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // Null check because findbugs insists, despite the API guaranteeing this is never null.
            if (req == null) {
                throw new FormException("req cannot be null", "");
            }
            CoberturaPublisher instance = req.bindJSON(CoberturaPublisher.class, formData);
            ConvertUtils.register(CoberturaPublisherTarget.CONVERTER, CoverageMetric.class);
            List<CoberturaPublisherTarget> targets = req
                    .bindParametersToList(CoberturaPublisherTarget.class, "cobertura.target.");
            if (0 == targets.size()) {
                targets = bindTargetsFromForm(formData);
            }
            try {
                instance.setTargets(targets);
            } catch (AbortException ex) {
                Logger.getLogger(CoberturaPublisher.class.getName()).log(Level.SEVERE, null, ex);
            }
            return instance;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    private static List<CoberturaPublisherTarget> bindTargetsFromForm(JSONObject formData) {
        ArrayList<CoberturaPublisherTarget> targets = new ArrayList<>();

        JSONArray coverageTargets = null;
        Object coverageTargetsObject = formData.get("inst");

        if (coverageTargetsObject instanceof JSONObject) {
            CoberturaPublisherTarget target = targetFromJSONObject((JSONObject)coverageTargetsObject);
            if (null != target) {
                targets.add(target);
            }
        }
        else if (coverageTargetsObject instanceof JSONArray) {
            coverageTargets = (JSONArray)coverageTargetsObject;
            for (Object targetObject : coverageTargets) {
                CoberturaPublisherTarget target = targetFromJSONObject((JSONObject)targetObject);
                if (null != target) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }

    private static Float getFloat(JSONObject object, String key) {
        Float floatValue = new Float(object.optDouble(key));

        return floatValue.isNaN() ? null : floatValue;
    }

    private static CoberturaPublisherTarget targetFromJSONObject(Object targetObject) {
        if (targetObject != null && targetObject instanceof JSONObject) {
            JSONObject targetJSONObject = (JSONObject)targetObject;
            try {
                CoverageMetric metric = CoverageMetric.valueOf(targetJSONObject.getString("metric"));
                return new CoberturaPublisherTarget(metric,
                        getFloat(targetJSONObject, "healthy"),
                        getFloat(targetJSONObject, "unhealthy"),
                        getFloat(targetJSONObject, "unstable"));
            } catch (JSONException ex) {
                Logger.getLogger(CoberturaPublisher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;
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

    public float roundDecimalFloat(Float input) {
        float rounded = (float) Math.round(input);
        rounded = rounded / 100f;
        return rounded;
    }
}
