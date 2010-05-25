package hudson.plugins.cobertura.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.view.dashboard.DashboardPortlet;

import java.util.Collection;
import java.util.LinkedList;

import org.kohsuke.stapler.DataBoundConstructor;

public class CoverageTablePortlet extends DashboardPortlet {

	@DataBoundConstructor
	public CoverageTablePortlet(String name) {
		super(name);
	}

	public Collection<Run> getCoverageResults() {
		LinkedList<Run> allResults = new LinkedList<Run>();

		for (Job job : getDashboard().getJobs()) {
			// Find the latest completed coverage data
			Run run = job.getLastCompletedBuild();
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

	public static class DescriptorImpl extends Descriptor<DashboardPortlet> {

		@Extension
		public static DescriptorImpl newInstance() {
			if (Hudson.getInstance().getPlugin("dashboard-view") != null) {
				return new DescriptorImpl();
			} else {
				return null;
			}
		}

		@Override
		public String getDisplayName() {
			return "Coverages";
		}
	}
}
