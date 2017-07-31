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
        private Boolean failUnhealthy = true;
        private Boolean failUnstable = true;
        private String lineCoverage = null;
        private String branchCoverage = null;
        private String fileCoverage = null;
        private String packageCoverage = null;
        private String classCoverage = null;
        private String methodCoverage = null;
        
        /**
        * Sets the build result for the script
        * 
        * @param result
        *            The value for result
        * @return ScriptBuilder instance
        */
        ScriptBuilder setBuildResult(String result) {
            this.result = result;
            return this;
        }
        
        /**
        * Sets the value for the onlyStable property
        * 
        * @param onlyStable
        *            The value for onlyStable
        * @return ScriptBuilder instance
        */
        ScriptBuilder setOnlyStable(Boolean onlyStable) {
            this.onlyStable = onlyStable;
            return this;
        }
        
        /**
        * Sets the value for the failUnhealthy property
        * 
        * @param onlyStable
        *            The value for failUnhealthy
        * @return ScriptBuilder instance
        */
        ScriptBuilder setFailUnhealthy(Boolean failUnhealthy) {
            this.failUnhealthy = failUnhealthy;
            return this;
        }
        
        /**
        * Sets the value for the failUnstable property
        * 
        * @param onlyStable
        *            The value for failUnstable
        * @return ScriptBuilder instance
        */
        ScriptBuilder setFailUnstable(Boolean failUnstable) {
            this.failUnstable = failUnstable;
            return this;
        }
        
        /**
        * Sets the targets for line coverage
        * 
        * @param lineCoverage
        *            Targets for line coverage
        * @return ScriptBuilder instance
        */
        ScriptBuilder setLineCoverage(String lineCoverage) {
            this.lineCoverage = lineCoverage;
            return this;
        }
        
        /**
        * Sets the targets for branch coverage
        * 
        * @param lineCoverage
        *            Targets for line coverage
        * @return ScriptBuilder instance
        */
        ScriptBuilder setBranchCoverage(String branchCoverage) {
            this.branchCoverage = branchCoverage;
            return this;
        }
        
        /**
        * Sets the targets for file coverage
        * 
        * @param fileCoverage
        *            Targets for file coverage
        * @return ScriptBuilder instance
        */
        ScriptBuilder setFileCoverage(String fileCoverage) {
            this.fileCoverage = fileCoverage;
            return this;
        }		
        
        /**
        * Sets the targets for package coverage
        * 
        * @param packageCoverage
        *            Targets for package coverage
        * @return ScriptBuilder instance
        */
        ScriptBuilder setPackageCoverage(String packageCoverage) {
            this.packageCoverage = packageCoverage;
            return this;
        }				
        
        /**
        * Sets the targets for class coverage
        * 
        * @param classCoverage
        *            Targets for class coverage
        * @return ScriptBuilder instance
        */
        ScriptBuilder setClassCoverage(String classCoverage) {
            this.classCoverage = classCoverage;
            return this;
        }				
        
        /**
        * Sets the targets for method coverage
        * 
        * @param methodCoverage
        *            Targets for method coverage
        * @return ScriptBuilder instance
        */
        ScriptBuilder setMethodCoverage(String methodCoverage) {
            this.methodCoverage = methodCoverage;
            return this;
        }				
        
        
        /**
        * Gets the script as a string
        * 
        * @return The script
        */
        String getScript() {
            StringBuilder script = new StringBuilder();
            script.append("node {\n");
            script.append(String.format("currentBuild.result = '%s'\n", this.result));
            script.append("step ([$class: 'CoberturaPublisher', ");
            script.append("coberturaReportFile: '**/coverage.xml', ");
            script.append(String.format("onlyStable: %s, ", this.onlyStable.toString()));
            script.append(String.format("failUnhealthy: %s, ", this.failUnhealthy.toString()));
            script.append(String.format("failUnstable: %s, ", this.failUnstable.toString()));
            if (this.lineCoverage != null) {
                script.append(String.format("lineCoverageTargets: '%s', ", this.lineCoverage));
            }
            if (this.branchCoverage != null) {
                script.append(String.format("conditionalCoverageTargets: '%s', ", this.branchCoverage));
            }
            if (this.fileCoverage != null) {
                script.append(String.format("fileCoverageTargets: '%s', ", this.fileCoverage));				
            }
            if (this.packageCoverage != null) {
                script.append(String.format("packageCoverageTargets: '%s', ", this.packageCoverage));				
            }
            if (this.classCoverage != null) {
                script.append(String.format("classCoverageTargets: '%s', ", this.classCoverage));				
            }
            if (this.methodCoverage != null) {
                script.append(String.format("methodCoverageTargets: '%s', ", this.methodCoverage));				
            }
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
    public void testReportFile() throws Exception {
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
    public void testNoReportFile() throws Exception {
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
    public void testUnstableOnlyStableFalse() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(
        new CpsFlowDefinition(new ScriptBuilder().setBuildResult("UNSTABLE").setOnlyStable(false).getScript()));
        
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
    public void testUnstableOnlyStableTrue() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(
        new CpsFlowDefinition(new ScriptBuilder().setBuildResult("UNSTABLE").setOnlyStable(true).getScript()));
        
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
    public void testFailedOnlyStableTrue() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(
        new CpsFlowDefinition(new ScriptBuilder().setBuildResult("FAILED").setOnlyStable(true).getScript()));
        
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
    public void testFailedOnlyStableFalse() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(
        new CpsFlowDefinition(new ScriptBuilder().setBuildResult("FAILED").setOnlyStable(false).getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogNotContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Skipping Cobertura coverage report as build was not UNSTABLE or better", run);
    }
    
    /**
    * Tests failing job when unhealthy due to line coverage
    */
    @Test
    public void testLineCoverageFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setLineCoverage("91,91,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogContains("Lines's health is 90.0 and set minimum health is 91.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build because it is unhealthy.", run);
    }
    
    /**
    * Tests job unhealthy due to line coverage passes when failUnhealthy is
    * false
    */
    @Test
    public void testLineCoverageUnhealthyNoFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(
        new ScriptBuilder().setFailUnhealthy(false).setLineCoverage("91,91,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Finished: SUCCESS", run);
        jenkinsRule.assertLogNotContains("ERROR:", run);
    }
    
    /**
    * Tests failing job when unstable due to line coverage
    */
    @Test
    public void testLineCoverageFailUnstable() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setLineCoverage("91,0,91").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Code coverage enforcement failed for the following metrics:", run);
        jenkinsRule.assertLogContains("Lines's stability is 90.0 and set mininum stability is 91.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build due to unstability.", run);
    }
    
    /**
    * Tests job unstable due to line coverage passes when failUnstable is false
    */
    @Test
    public void testLineCoverageUnstableNoFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(
        new ScriptBuilder().setFailUnstable(false).setLineCoverage("91,0,91").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Finished: UNSTABLE", run);
        jenkinsRule.assertLogNotContains("ERROR:", run);
    }
    
    /**
    * Tests job succeeds when line coverage is exactly target
    */
    @Test
    public void testLineCoverageSuccess() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setLineCoverage("91,90,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogNotContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogNotContains("ERROR: Failing build because it is unhealthy.", run);
    }
    
    /**
    * Tests failing job when unhealthy due to branch coverage
    */
    @Test
    public void testBranchCoverageFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBranchCoverage("76,76,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogContains("Conditionals's health is 75.0 and set minimum health is 76.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build because it is unhealthy.", run);
    }
    
    /**
    * Tests failing job when unstable due to branch coverage
    */
    @Test
    public void testBranchCoverageFailUnstable() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBranchCoverage("76,0,76").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Code coverage enforcement failed for the following metrics:", run);
        jenkinsRule.assertLogContains("Conditionals's stability is 75.0 and set mininum stability is 76.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build due to unstability.", run);
    }
    
    /**
    * Tests job succeeds when branch coverage is exactly target
    */
    @Test
    public void testBranchCoverageSuccess() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder().setBranchCoverage("76,75,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogNotContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogNotContains("ERROR: Failing build because it is unhealthy.", run);
    }
    
    /**
    * Tests failing job when unhealthy due to file coverage
    */
    @Test
    public void testFileCoverageFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder()
        .setFileCoverage("80,101,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogContains("Files's health is 100.0 and set minimum health is 101.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build because it is unhealthy.", run);
        jenkinsRule.assertLogContains("Finished: FAILURE", run);
    }
    
    /**
    * Tests failing job when unhealthy due to package coverage
    */
    @Test
    public void testPackageCoverageFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder()
        .setPackageCoverage("80,101,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogContains("Packages's health is 100.0 and set minimum health is 101.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build because it is unhealthy.", run);
        jenkinsRule.assertLogContains("Finished: FAILURE", run);
    }	
    
    /**
    * Tests failing job when unhealthy due to class coverage
    */
    @Test
    public void testClassCoverageFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder()
        .setClassCoverage("80,101,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogContains("Classes's health is 100.0 and set minimum health is 101.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build because it is unhealthy.", run);
        jenkinsRule.assertLogContains("Finished: FAILURE", run);
    }		
    
    /**
    * Tests failing job when unhealthy due to method coverage
    */
    @Test
    public void testMethodCoverageFail() throws Exception {
        Jenkins jenkins = jenkinsRule.jenkins;
        WorkflowJob project = jenkins.createProject(WorkflowJob.class, "cob-test");
        
        project.setDefinition(new CpsFlowDefinition(new ScriptBuilder()
        .setMethodCoverage("80,101,0").getScript()));
        
        copyCoverageFile("coverage-with-data.xml", "coverage.xml", project);
        
        WorkflowRun run = project.scheduleBuild2(0).get();
        jenkinsRule.waitForCompletion(run);
        
        jenkinsRule.assertLogContains("Publishing Cobertura coverage report...", run);
        jenkinsRule.assertLogContains("Unhealthy for the following metrics:", run);
        jenkinsRule.assertLogContains("Methods's health is 100.0 and set minimum health is 101.0.", run);
        jenkinsRule.assertLogContains("ERROR: Failing build because it is unhealthy.", run);
        jenkinsRule.assertLogContains("Finished: FAILURE", run);
    }	
    
    /**
    * Creates workspace directory if needed, and returns it
    * 
    * @param job
    *            The job for the workspace
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
    * 
    * @param sourceResourceName
    *            The name of the resource to copy
    * @param targetFileName
    *            The name of the file in the target workspace
    * @param job
    *            The job to copy the file to
    * @throws IOException
    */
    private void copyCoverageFile(String sourceResourceName, String targetFileName, WorkflowJob job)
    throws IOException {
        File directory = ensureWorkspaceExists(job);
        
        File dest = new File(directory, targetFileName);
        File src = new File(getClass().getResource(sourceResourceName).getPath());
        
        FileUtils.copyFile(src, dest);
    };
}
