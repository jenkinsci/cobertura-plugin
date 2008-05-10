package hudson.plugins.cobertura;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 18-Dec-2007 11:12:44
 */
public class MavenCoberturaAggregatedReport implements MavenAggregatedReport {
    private static final Logger LOGGER = Logger.getLogger(MavenCoberturaAggregatedReport.class.getName());
    private final MavenModuleSetBuild owner;

    public MavenCoberturaAggregatedReport(MavenModuleSetBuild owner) {
        this.owner = owner;
    }

    public void update(Map<MavenModule, List<MavenBuild>> map, MavenBuild mavenBuild) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class<? extends AggregatableAction> getIndividualActionType() {
        return MavenCoberturaBuildAction.class;
    }

    public Action getProjectAction(MavenModuleSet mavenModuleSet) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        return "graph.gif";
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return "Coverage Report";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName() {
        return "cobertura";
    }

}
