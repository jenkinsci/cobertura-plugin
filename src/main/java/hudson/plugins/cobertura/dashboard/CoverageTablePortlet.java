package hudson.plugins.cobertura.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.Ratio;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.view.dashboard.DashboardPortlet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import org.kohsuke.stapler.DataBoundConstructor;

public class CoverageTablePortlet extends DashboardPortlet {

	@DataBoundConstructor
	public CoverageTablePortlet(String name) {
		super(name);
	}

	public Collection<Run> getCoverageRuns() {
		LinkedList<Run> allResults = new LinkedList<Run>();

		for (Job job : getDashboard().getJobs()) {
			// Find the latest successful coverage data
			Run run = job.getLastSuccessfulBuild();
			if(run == null) continue;
			
			CoberturaBuildAction rbb = run
					.getAction(CoberturaBuildAction.class);

			if (rbb != null) {
				allResults.add(run);
			}
		}

		return allResults;
	}
	
	public CoverageResult getCoverageResult(Run run){
		CoberturaBuildAction rbb = run.getAction(CoberturaBuildAction.class);
		return rbb.getResult();
	}
	
	public HashMap<CoverageMetric, Ratio> getTotalCoverageRatio(){
		HashMap<CoverageMetric, Ratio> totalRatioMap = new HashMap<CoverageMetric, Ratio>();
		for (Job job : getDashboard().getJobs()) {
			// Find the latest successful coverage data
			Run run = job.getLastSuccessfulBuild();
			if(run == null) continue;
			
			CoberturaBuildAction rbb = run
					.getAction(CoberturaBuildAction.class);

			if( rbb == null ) continue;
			
			CoverageResult result = rbb.getResult();
			Set<CoverageMetric> metrics = result.getMetrics();
			
			for (CoverageMetric metric: metrics) {
				if(totalRatioMap.get(metric) == null){
					totalRatioMap.put(metric, result.getCoverage(metric));
				} else{
					float currentNumerator = totalRatioMap.get(metric).numerator;
					float CurrentDenominator = totalRatioMap.get(metric).denominator;
					float sumNumerator = currentNumerator + result.getCoverage(metric).numerator;
					float sumDenominator = CurrentDenominator + result.getCoverage(metric).denominator;
					totalRatioMap.put(metric, Ratio.create(sumNumerator, sumDenominator));
				}
			}
		}
		return totalRatioMap;
	}

	public static class DescriptorImpl extends Descriptor<DashboardPortlet> {

		@Extension(optional = true)
		public static DescriptorImpl newInstance() {
			if (Hudson.getInstance().getPlugin("dashboard-view") != null) {
				return new DescriptorImpl();
			} else {
				return null;
			}
		}

		@Override
		public String getDisplayName() {
			return "Code Coverages(Cobertura)";
		}
	}
}
