package hudson.plugins.cobertura.adapter;

import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.plugins.cobertura.targets.CoverageResult;
import io.jenkins.plugins.coverage.CoverageAction;
import io.jenkins.plugins.coverage.adapter.CoverageAdapter;
import io.jenkins.plugins.coverage.targets.CoverageElement;
import io.jenkins.plugins.coverage.targets.Ratio;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Objects;

public class CoberturaReportAdapterTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void XSLT20Test() throws Exception {
        String coberturaReport = "coberturaWithTheSameFileInDifferentPackages.xml";
        StringBuilder sb = new StringBuilder();
        sb.append("node {")
                .append("recordCoverage(");

        sb.append(String.format("tools:[[ parser: 'COBERTURA', pattern:'%s' ]], sourceCodeRetention: 'NEVER_STORE'", coberturaReport));

        sb.append(")").append("}");

        WorkflowJob project = j.createProject(WorkflowJob.class, "coverage-pipeline-test");
        FilePath workspace = j.jenkins.getWorkspaceFor(project);

        Objects.requireNonNull(workspace)
                .child(coberturaReport)
                .copyFrom(getClass().getResourceAsStream(coberturaReport));

        project.setDefinition(new CpsFlowDefinition(sb.toString(), true));
        WorkflowRun r = Objects.requireNonNull(project.scheduleBuild2(0)).waitForStart();
        Assert.assertNotNull(r);
        j.assertBuildStatusSuccess(j.waitForCompletion(r));
        CoverageAction coverageAction = r.getAction(CoverageAction.class);
        Assert.assertNotNull(r);
        Ratio lineCoverage = coverageAction.getResult().getCoverage(CoverageElement.LINE);
        Assert.assertEquals(lineCoverage.toString(),"054.55 (6/11)");
    }
}