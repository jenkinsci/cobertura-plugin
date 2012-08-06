package hudson.plugins.cobertura;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 18-Dec-2007 11:11:23
 */
public class MavenCoberturaBuildAction extends CoberturaBuildAction implements AggregatableAction {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(MavenCoberturaBuildAction.class.getName());

    MavenCoberturaBuildAction(MavenBuild build, CoverageResult r, CoverageTarget healthyTarget, CoverageTarget unhealthyTarget, boolean onlyStable, boolean failUnhealthy, boolean failUnstable, boolean autoUpdateHealth, boolean autoUpdateStability) {
        super(build, r, healthyTarget, unhealthyTarget, onlyStable, failUnhealthy, failUnstable, autoUpdateHealth, autoUpdateStability);
    }

    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild build, Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new MavenCoberturaAggregatedReport(build);
    }
}
