/*
 * The MIT License
 *
 * Copyright 2021 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.cobertura;

import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import java.io.File;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.ToolInstallations;

public class MavenCoberturaPublisherTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule r = new JenkinsRule();

    // adapted from MavenJavadocArchiverTest.simple and CoberturaPublisherPipelineTest.testReportFile
    @Test
    public void smokes() throws Exception {
        ToolInstallations.configureMaven35();
        r.jenkins.getDescriptorByType(MavenModuleSet.DescriptorImpl.class).setGlobalMavenOpts("-Djava.awt.headless=true");
        MavenModuleSet mms = r.createProject(MavenModuleSet.class, "p");
        mms.setAssignedNode(r.createSlave());
        mms.setScm(new ExtractResourceSCM(MavenCoberturaPublisherTest.class.getResource("maven-project-with-cobertura.zip")));
        mms.setGoals("verify");
        MavenModuleSetBuild b = r.buildAndAssertSuccess(mms);
        MavenModule mm = mms.getModule("demo$maven-project-with-cobertura");
        assertNotNull(mm);
        assertTrue(new File(mm.getRootDir(), "cobertura/demo_2fSample_2ejava").isFile());
        assertTrue(new File(b.getModuleBuilds().get(mm).get(0).getRootDir(), "coverage.xml").isFile());
    }

}
