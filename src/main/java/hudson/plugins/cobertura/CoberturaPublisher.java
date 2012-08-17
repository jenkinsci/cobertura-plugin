package hudson.plugins.cobertura;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.maven.ExecutedMojo;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.plugins.cobertura.renderers.SourceCodePainter;
import hudson.plugins.cobertura.renderers.SourceEncoding;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Cobertura {@link Publisher}.
 *
 * @author Stephen Connolly
 */
public class CoberturaPublisher extends Recorder {

    private final String coberturaReportFile;
    private final boolean onlyStable;
    private final boolean failUnhealthy;
    private final boolean failUnstable;
    private final boolean autoUpdateHealth;
    private final boolean autoUpdateStability;
    
    private CoverageTarget healthyTarget;
    private CoverageTarget unhealthyTarget;
    private CoverageTarget failingTarget;
    public static final CoberturaReportFilenameFilter COBERTURA_FILENAME_FILTER = new CoberturaReportFilenameFilter();
	private final SourceEncoding sourceEncoding;

    /**
     * @param coberturaReportFile the report directory
     * @stapler-constructor
     */
    @DataBoundConstructor 
    public CoberturaPublisher(String coberturaReportFile, boolean onlyStable, boolean failUnhealthy, boolean failUnstable, boolean autoUpdateHealth, boolean autoUpdateStability, SourceEncoding sourceEncoding) {
        this.coberturaReportFile = coberturaReportFile;
        this.onlyStable = onlyStable;
        this.failUnhealthy = failUnhealthy;
        this.failUnstable = failUnstable;
        this.autoUpdateHealth = autoUpdateHealth;
        this.autoUpdateStability = autoUpdateStability;
		this.sourceEncoding = sourceEncoding;
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
        float checker;
        for (CoverageMetric metric : healthyTarget.getTargets()) {
            CoberturaPublisherTarget target = targets.get(metric);
            if (target == null) {
                target = new CoberturaPublisherTarget();
                target.setMetric(metric);
            }
            checker = (float)healthyTarget.getTarget(metric)/100000f;
            if(checker <= 0.001f)
            {
            	checker = (float)(Math.round(checker*100000f));
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
            checker = (float)unhealthyTarget.getTarget(metric)/100000f;
            if(checker <= 0.001f)
            {
            	checker = (float)(Math.round(checker*100000f));
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
            checker = (float)failingTarget.getTarget(metric)/100000f;
            if(checker <= 0.001f)
            {
            	checker = (float)(Math.round(checker*100000f));
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
    private void setTargets(List<CoberturaPublisherTarget> targets) {
        healthyTarget.clear();
        unhealthyTarget.clear();
        failingTarget.clear();
        float rounded;
        for (CoberturaPublisherTarget target : targets) {
            if (target.getHealthy() != null) {
            	rounded = (Math.round((float)100f*target.getHealthy()));
            	rounded = roundDecimalFloat(rounded);
                healthyTarget.setTarget(target.getMetric(), (int)((float)100000f*rounded));
            }
            if (target.getUnhealthy() != null) {
            	rounded = (Math.round((float)100f*target.getUnhealthy()));
            	rounded = roundDecimalFloat(rounded);
                unhealthyTarget.setTarget(target.getMetric(), (int)((float)100000f*rounded));
            }
            if (target.getUnstable() != null) {
            	rounded = (Math.round((float)100f*target.getUnstable()));
            	rounded = roundDecimalFloat(rounded);
                failingTarget.setTarget(target.getMetric(), (int)((float)100000f*rounded));
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
     * Getter for property 'failUnhealthy'.
     *
     * @return Value for property 'failUnhealthy'.
     */
    public boolean getFailUnhealthy() {
        return failUnhealthy;
    }
    
    /**
     * Getter for property 'failUnstable'.
     *
     * @return Value for property 'failUnstable'.
     */
    public boolean getFailUnstable() {
        return failUnstable;
    }
    
    /**
     * Getter for property 'autoUpdateHealth'.
     *
     * @return Value for property 'autoUpdateHealth'.
     */
    public boolean getAutoUpdateHealth() {
        return autoUpdateHealth;
    }
    
    /**
     * Getter for property 'autoUpdateStability'.
     *
     * @return Value for property 'autoUpdateStability'.
     */
    public boolean getAutoUpdateStability() {
        return autoUpdateStability;
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
    static File[] getCoberturaReports(AbstractBuild<?,?> build) {
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
        // Make sure Cobertura actually ran
        if(build instanceof MavenBuild) {
          MavenBuild mavenBuild = (MavenBuild)build;
          if(didCoberturaRun(mavenBuild) == false) {
            listener.getLogger().println("Skipping Cobertura coverage report as mojo did not run.");
            return true;
          }
        } else if(build instanceof MavenModuleSetBuild) {
          MavenModuleSetBuild moduleSetBuild = (MavenModuleSetBuild)build;
          if(didCoberturaRun(moduleSetBuild.getModuleLastBuilds().values()) == false) {
            listener.getLogger().println("Skipping Cobertura coverage report as mojo did not run.");
            return true;
          }
        }
        
        listener.getLogger().println("Publishing Cobertura coverage report...");
        final FilePath[] moduleRoots = build.getModuleRoots();
        final boolean multipleModuleRoots =
            moduleRoots != null && moduleRoots.length > 1;
        final FilePath moduleRoot = multipleModuleRoots ? build.getWorkspace() : build.getModuleRoot();
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
        	listener.getLogger().println("Cobertura coverage report found.");
            result.setOwner(build);
            final FilePath paintedSourcesPath = new FilePath(new File(build.getProject().getRootDir(), "cobertura"));
            paintedSourcesPath.mkdirs();
            SourceCodePainter painter = new SourceCodePainter(paintedSourcesPath, sourcePaths,
                    result.getPaintedSources(), listener, getSourceEncoding());

            moduleRoot.act(painter);

            final CoberturaBuildAction action = CoberturaBuildAction.load(build, result, healthyTarget,
                    unhealthyTarget, getOnlyStable(), getFailUnhealthy(), getFailUnstable(), getAutoUpdateHealth(), getAutoUpdateStability());

            build.getActions().add(action);
            Set<CoverageMetric> failingMetrics = failingTarget.getFailingMetrics(result);
            if (!failingMetrics.isEmpty()) {
                listener.getLogger().println("Code coverage enforcement failed for the following metrics:");
                float oldStabilityPercent;
                float setStabilityPercent;
                for (CoverageMetric metric : failingMetrics) {
                	oldStabilityPercent = failingTarget.getObservedPercent(result, metric);
                	setStabilityPercent = failingTarget.getSetPercent(result, metric);
                	listener.getLogger().println("    " + metric.getName() + "'s stability is " + roundDecimalFloat(oldStabilityPercent*100f) + " and set mininum stability is " + roundDecimalFloat(setStabilityPercent*100f) + ".");
                }
                if(!getFailUnstable())
                {
                	listener.getLogger().println("Setting Build to unstable.");
                	build.setResult(Result.UNSTABLE);
                }
                else
                {
                	listener.getLogger().println("Failing build due to unstability.");
                	build.setResult(Result.FAILURE);
                }
            }
            if(getFailUnhealthy())
            {
            	Set<CoverageMetric> unhealthyMetrics = unhealthyTarget.getFailingMetrics(result);
            	if (!unhealthyMetrics.isEmpty()) {
            		listener.getLogger().println("Unhealthy for the following metrics:");
            		float oldHealthyPercent;
            		float setHealthyPercent;
            		for (CoverageMetric metric : unhealthyMetrics) {
            			oldHealthyPercent = unhealthyTarget.getObservedPercent(result, metric);
            			setHealthyPercent = unhealthyTarget.getSetPercent(result, metric);
            			listener.getLogger().println("    " + metric.getName() + "'s health is " + roundDecimalFloat(oldHealthyPercent*100f) + " and set minimum health is " + roundDecimalFloat(setHealthyPercent*100f) + ".");
            		}
                	listener.getLogger().println("Failing build because it is unhealthy.");
            		build.setResult(Result.FAILURE);
            	}
            }
            if(build.getResult() == Result.SUCCESS)
            {	
            	if(getAutoUpdateHealth())
            	{
            		setNewPercentages(result, true, listener);
            	}
            	
            	if(getAutoUpdateStability())
            	{
            		setNewPercentages(result, false, listener);
            	}
            }
        } else {
            listener.getLogger().println("No coverage results were successfully parsed.  Did you generate " +
                    "the XML report(s) for Cobertura?");
            build.setResult(Result.FAILURE);
        }

        return true;
    }
    
	/**
     * Changes unhealthy or unstable percentage fields for ratcheting.
     */
    private void setNewPercentages(CoverageResult result, boolean select, BuildListener listener)
    {
		Set<CoverageMetric> healthyMetrics = healthyTarget.getAllMetrics(result);
		float newPercent;
		float oldPercent;
		if(!healthyMetrics.isEmpty())
		{
			for (CoverageMetric metric : healthyMetrics)
			{    
				newPercent = healthyTarget.getObservedPercent(result, metric);
				newPercent = (float)(Math.round(newPercent*100f));
				if(select)
				{
					oldPercent = unhealthyTarget.getSetPercent(result, metric);
					oldPercent = (float)(Math.round(oldPercent*100f));
				}
				else
				{
					oldPercent = failingTarget.getSetPercent(result, metric);
					oldPercent = (float)(Math.round(oldPercent*100f));
				}
				if(newPercent > oldPercent)
				{
					if(select)
					{
						unhealthyTarget.setTarget(metric, (int)(newPercent*1000f));
    					listener.getLogger().println("    " + metric.getName() + "'s new health minimum is: " + roundDecimalFloat(newPercent));
					}
					else
					{
						failingTarget.setTarget(metric, (int)(newPercent*1000f));
    					listener.getLogger().println("    " + metric.getName() + "'s new stability minimum is: " + roundDecimalFloat(newPercent));
					}
				}
			}
		}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new CoberturaProjectAction(project, getOnlyStable());
    }

    /**
     * {@inheritDoc}
     */
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    public SourceEncoding getSourceEncoding() {
		return sourceEncoding;
	}

	/**
     * Descriptor should be singleton.
     */
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link CoberturaPublisher}. Used as a singleton. The class is marked as public so that it can be
     * accessed from views.
     * <p/>
     * <p/>
     * See <tt>views/hudson/plugins/cobertura/CoberturaPublisher/*.jelly</tt> for the actual HTML fragment for the
     * configuration screen.
     */
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
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
            result.add(new CoberturaPublisherTarget(CoverageMetric.METHOD, 80f, null, null));
            result.add(new CoberturaPublisherTarget(CoverageMetric.LINE, 80f, null, null));
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
            CoberturaPublisher instance = req.bindJSON(CoberturaPublisher.class, formData);
            ConvertUtils.register(CoberturaPublisherTarget.CONVERTER, CoverageMetric.class);
            List<CoberturaPublisherTarget> targets = req
                    .bindParametersToList(CoberturaPublisherTarget.class, "cobertura.target.");
            instance.setTargets(targets);
            return instance;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
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

    private boolean didCoberturaRun(Iterable<MavenBuild> mavenBuilds) {
      for(MavenBuild build: mavenBuilds) {
        if(didCoberturaRun(build)) return true;
      }
      return false;
    }

    private boolean didCoberturaRun(MavenBuild mavenBuild) {
      for(ExecutedMojo mojo : mavenBuild.getExecutedMojos()) {
	  if(mojo.groupId.equals("org.codehaus.mojo") &&
	     ( mojo.artifactId.equals("cobertura-maven-plugin") || mojo.artifactId.equals("cobertura-it-maven-plugin"))) {
          return true;
        }
      }
      return false;
    }
    
    public float roundDecimalFloat(Float input) {
    	float rounded = (float)Math.round(input);
    	rounded = rounded/100f;
    	return rounded;
    }
}
