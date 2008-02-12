package hudson.plugins.cobertura;

import hudson.Plugin;
import hudson.maven.MavenReporters;
import hudson.tasks.BuildStep;

/**
 * Entry point of Cobertura plugin.
 *
 * @author Stephen Connolly
 * @plugin
 */
public class PluginImpl extends Plugin {
    /**
     * {@inheritDoc}
     */
    public void start() throws Exception {
        // plugins normally extend Hudson by providing custom implementations
        // of 'extension points'. In this example, we'll add one builder.
        BuildStep.PUBLISHERS.addRecorder(CoberturaPublisher.DESCRIPTOR);
        MavenReporters.LIST.add(MavenCoberturaPublisher.DESCRIPTOR);
    }
}
