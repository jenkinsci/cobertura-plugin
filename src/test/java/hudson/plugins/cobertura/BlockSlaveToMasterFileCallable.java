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

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.remoting.Callable;
import hudson.remoting.ChannelBuilder;
import jenkins.security.ChannelConfigurator;
import jenkins.util.JenkinsJVM;
import org.jenkinsci.remoting.CallableDecorator;

/**
 * Prevents {@link FilePath#act} (or {@link FilePath#actAsync}) from running in a controller → agent direction during tests, to make sure we do not rely on them.
 */
public class BlockSlaveToMasterFileCallable extends CallableDecorator {

    @Override public <V, T extends Throwable> Callable<V, T> userRequest(Callable<V, T> op, Callable<V, T> stem) {
        if (op.getClass().getName().equals("hudson.FilePath$FileCallableWrapper") && JenkinsJVM.isJenkinsJVM()) {
            throw new SecurityException("blocked");
        }
        return stem;
    }

    @Extension public static class ChannelConfiguratorImpl extends ChannelConfigurator {

        @Override public void onChannelBuilding(ChannelBuilder builder, @Nullable Object context) {
            builder.with(new BlockSlaveToMasterFileCallable());
        }

    }

}
