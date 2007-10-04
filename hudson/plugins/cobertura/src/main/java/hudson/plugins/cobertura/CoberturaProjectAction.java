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

    public String getIconFileName() {
        return "graph.gif";
    }

    public String getDisplayName() {
        return "Coverage Report";
    }

    public String getUrlName() {
        return "cobertura";
    }

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

    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (getLastResult() != null)
            getLastResult().getResult().doGraph(req, rsp);
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.sendRedirect2("../lastStableBuild/cobertura");
    }

    public String getSearchUrl() {
        return getUrlName();
    }
}
