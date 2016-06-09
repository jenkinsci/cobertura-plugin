/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.cobertura.renderers.SourceEncoding;
import hudson.util.OneShotEvent;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoberturaFunctionalTest {

    public @Rule JenkinsRule j = new JenkinsRule();

    @Test
    public void doNotWaitForPreviousBuild() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.setConcurrentBuild(true);

        final OneShotEvent firstRunning = new OneShotEvent();
        final OneShotEvent firstBlocked = new OneShotEvent();
        
        p.getPublishersList().add(new BlockingCoberturaPublisher(firstRunning, firstBlocked));

        p.scheduleBuild2(0).getStartCondition().get();

        firstRunning.block();
        p.getPublishersList().clear();
        p.getPublishersList().add(new CoberturaPublisher("", true, true, true, true, true, true, true, SourceEncoding.UTF_8, 42));

        p.scheduleBuild2(0).get();

        assertTrue(p.getBuildByNumber(1).getLog(), p.getBuildByNumber(1).isBuilding());
        assertFalse(p.getBuildByNumber(2).getLog(), p.getBuildByNumber(2).isBuilding());
    }

    private static class BlockingCoberturaPublisher extends CoberturaPublisher {
        private OneShotEvent firstRunning;
        private final OneShotEvent firstBlocked;

        public BlockingCoberturaPublisher(OneShotEvent firstRunning, OneShotEvent blockFirst) {
            super("", true, true, true, true, true, true, true, SourceEncoding.UTF_8, 42);
            this.firstRunning = firstRunning;
            this.firstBlocked = blockFirst;
        }

        @Override
        public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
            firstRunning.signal();
            firstBlocked.block();
            return;
        }
    }
}
