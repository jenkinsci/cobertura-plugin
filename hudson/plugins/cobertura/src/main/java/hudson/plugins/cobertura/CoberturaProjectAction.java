package hudson.plugins.cobertura;

import hudson.FilePath;
import hudson.model.Actionable;
import hudson.model.Build;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Project;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Project level action.
 *
 * @author Stephen Connolly
 */
public class CoberturaProjectAction extends Actionable implements ProminentProjectAction {

    private final Project<?, ?> project;

    public CoberturaProjectAction(Project project) {
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
        for (Build<?, ?> b = project.getLastStableBuild(); b != null; b = b.getPreviousNotFailedBuild()) {
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

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException,
            InterruptedException {
        new DirectoryBrowserSupport(this).serveFile(req, rsp,
                new FilePath(CoberturaPublisher.getCoberturaReportDir(project)), "graph.gif", false);
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.sendRedirect2("../lastStableBuild/cobertura");
    }

    public String getSearchUrl() {
        return getUrlName();
    }
}
