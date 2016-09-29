/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
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
package CoverageTablePortlet;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.dashboard.CoverageTablePortlet;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.view.dashboard.Dashboard;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class CoverageTablePortletTest {

    public @Rule JenkinsRule j = new JenkinsRule();

    @Test @Bug(26410)
    public void testBuildUrlShouldBeCorrectInFolder() throws Exception {
        Folder folder = j.jenkins.createProject(Folder.class, "folder");

        FreeStyleProject p = folder.createProject(FreeStyleProject.class, "project");
        FreeStyleBuild build = p.scheduleBuild2(0).get();
        // fake cobertura run
        build.addAction(CoberturaBuildAction.load(
                new CoverageResult(null, null, "cresult"), null, null, false, false, false, false, false, false, 0
        ));

        Dashboard view = new Dashboard("view");
        folder.addView(view);
        view.add(p);
        final CoverageTablePortlet portlet = new CoverageTablePortlet("coverage portlet");
        String portletId = portlet.getId();
        view.getTopPortlets().add(portlet);

        WebClient wc = j.createWebClient();
        // FailingHttpStatusCodeException: 404 Not Found for http://localhost:34512/plugin/dashboard-view/js/dashboard-view.js
        wc.setJavaScriptEnabled(false);

        HtmlPage page = wc.getPage(view);
        page.getAnchorByText("project #1").click();

        page = wc.getPage(view, "portlet/" + portletId);
        page.getAnchorByText("project #1").click();
    }
}
