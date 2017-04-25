package hudson.plugins.cobertura;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;
import jenkins.model.Jenkins;

public class CoberturaPublisherPipelineTest {
	
	/**
	 * Helper class to build a script with a CoverturaPublisher step
	 */
	protected class ScriptBuilder {
		
		private String result = "SUCCESS";
		private Boolean onlyStable = true;
		
		/**
		 * Sets the build result for the script
         * @param result The value for result
         * @return ScriptBuilder instance
		 */
		ScriptBuilder setBuildResult(String result) {
			this.result = result;
			return this;
		}
		
		/**
		 * Sets the build result for the script
         * @param onlyStable The value for onlyStable
         * @return ScriptBuilder instance
		 */
		ScriptBuilder setOnlyStable(Boolean onlyStable) {
			this.onlyStable = onlyStable;
			return this;
		}
		
		/**
		 * Gets the script as a string
         * @return The script
		 */
		String getScript() {
			StringBuilder script = new StringBuilder();
			script.append("node {\n");
			script.append(String.format("currentBuild.result = '%s'\n", this.result));
			script.append("step ([$class: 'CoberturaPublisher', ");
			script.append("coberturaReportFile: '**/coverage.xml', ");
			script.append(String.format("onlyStable: %s, ", this.onlyStable.toString()));				
			script.append("sourceEncoding: 'ASCII'])\n");
			script.append("}");
			return script.toString();
		}		
	}

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    /**
     * Tests that a run with a report file publishes
     */
    @Test
    public void testReportFile()  throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
		
		project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().getScript()));
		
		copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
		                     
		WorkflowRun run = project.scheduleBuild2(0).get();
		jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Cobertura coverage report found.", run);
    }

    /**
     * Tests no report is published if coverage file isn't found
     */
    @Test
    public void testNoReportFile()  throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
		
		project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().getScript()));

		ensureWorkspaceExists(project);
		                     
		WorkflowRun run = project.scheduleBuild2(0).get();
		jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogNotContains("Cobertura coverage report found.", run);
    }
    
    /**
     * Tests report is published for unstable build if onlyStable = false
     */
    @Test
    public void testUnstableOnlyStableFalse()  throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
		
		project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBuildResult("UNSTABLE").setOnlyStable(false).getScript()));
		
		copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
		                     
		WorkflowRun run = project.scheduleBuild2(0).get();
		jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Cobertura coverage report found.", run);
    }

    /**
     * Tests report is not published for unstable build if onlyStable = true
     */
    @Test
    public void testUnstableOnlyStableTrue()  throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
		
		project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBuildResult("UNSTABLE").setOnlyStable(true).getScript()));
		
		copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
		                     
		WorkflowRun run = project.scheduleBuild2(0).get();
		jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogNotContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Skipping Cobertura coverage report as build was not SUCCESS or better", run);
    }

    /**
     * Tests no report is published for failed build if onlyStable = true
     */
    @Test
    public void testFailedOnlyStableTrue()  throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
		
		project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBuildResult("FAILED").setOnlyStable(true).getScript()));
		
		copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
		                     
		WorkflowRun run = project.scheduleBuild2(0).get();
		jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogNotContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Skipping Cobertura coverage report as build was not SUCCESS or better", run);
    }

    /**
     * Tests report is not published for failed build if onlyStable = true
     */
    @Test
    public void testFailedOnlyStableFalse()  throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
		
		project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBuildResult("FAILED").setOnlyStable(false).getScript()));
		
		copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
		                     
		WorkflowRun run = project.scheduleBuild2(0).get();
		jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogNotContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Skipping Cobertura coverage report as build was not UNSTABLE or better", run);
    }

    /**
     * Creates workspace directory if needed, and returns it
     * @param job The job for the workspace
     * @return File representing workspace directory
     */
    private File ensureWorkspaceExists(WorkflowJob job) {
		FilePath path = jenkinsRule.jenkins.getWorkspaceFor(job);
		File directory = new File(path.getRemote());
		directory.mkdirs();
    	
		return directory;
    }
    
    /**
     * Copies a coverage file from resources to a job's workspace directory 
     * @param sourceResourceName The name of the resource to copy
     * @param targetFileName The name of the file in the target workspace
     * @param job The job to copy the file to
     * @throws IOException
     */
    private void copyCoverageFile(String sourceResourceName, String targetFileName, WorkflowJob job) throws IOException {
    	File directory = ensureWorkspaceExists(job);

		File dest = new File(directory, targetFileName);
		File src = new File(getClass().getResource(sourceResourceName).getPath());
		
		FileUtils.copyFile(src, dest);
    };
}
