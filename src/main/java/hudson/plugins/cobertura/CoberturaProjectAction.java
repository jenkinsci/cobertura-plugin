package hudson.plugins.cobertura;

import hudson.model.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * Project level action.
 *
 * @author Stephen Connolly
 */
public class CoberturaProjectAction extends Actionable implements ProminentProjectAction {

    private final AbstractProject<?, ?> project;

    public CoberturaProjectAction(AbstractProject project) {
        this.project = project;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
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

    /**
     * Getter for property 'lastResult'.
     *
     * @return Value for property 'lastResult'.
     */
    public CoberturaBuildAction getLastResult() {
        for (AbstractBuild<?, ?> b = project.getLastStableBuild(); b != null; b = b.getPreviousNotFailedBuild()) {
            if (b.getResult() == Result.FAILURE)
                continue;
            CoberturaBuildAction r = b.getAction(CoberturaBuildAction.class);
            if (r != null)
                return r;
        }
        return null;
    }

    /**
     * Getter for property 'lastResult'.
     *
     * @return Value for property 'lastResult'.
     */
    public Integer getLastResultBuild() {
        for (AbstractBuild<?, ?> b = project.getLastStableBuild(); b != null; b = b.getPreviousNotFailedBuild()) {
            if (b.getResult() == Result.FAILURE)
                continue;
            CoberturaBuildAction r = b.getAction(CoberturaBuildAction.class);
            if (r != null)
                return b.getNumber();
        }
        return null;
    }

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (getLastResult() != null)
            getLastResult().getResult().doGraph(req, rsp);
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Integer buildNumber = getLastResultBuild();
        if (buildNumber == null) {
            rsp.sendRedirect2("nodata");
        } else {
            rsp.sendRedirect2("../" + buildNumber + "/cobertura");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSearchUrl() {
        return getUrlName();
    }
}
