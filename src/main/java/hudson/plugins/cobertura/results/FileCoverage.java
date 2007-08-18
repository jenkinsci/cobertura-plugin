package hudson.plugins.cobertura.results;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import hudson.model.Run;
import hudson.model.Build;
import hudson.plugins.cobertura.CoberturaBuildAction;

/**
 * Cobertura Coverage results for a specific file.
 * @author Stephen Connolly
 */
public class FileCoverage extends AbstractClassAggregatedMetrics {

    private List<ClassCoverage> classCoverages = new ArrayList<ClassCoverage>();

    public List<ClassCoverage> getChildren() {
        return getClassCoverages();
    }

    public ClassCoverage getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException {
        return findClassCoverage(token);
    }

    public boolean addClassCoverage(ClassCoverage result) {
        return classCoverages.add(result);
    }

    public List<ClassCoverage> getClassCoverages() {
        return classCoverages;
    }

    public ClassCoverage findClassCoverage(String name) {
        for (ClassCoverage i : classCoverages) {
            if (name.equals(i.getName())) return i;
        }
        return null;
    }

    public AbstractCoberturaMetrics getPreviousResult() {
        if (owner == null) return null;
        Run prevBuild = owner.getPreviousBuild();
        if (prevBuild == null) return null;
        CoberturaBuildAction action = prevBuild.getAction(CoberturaBuildAction.class);
        if (action == null) return null;
        return action.findFileCoverage(getName());
    }

    @Override
    public void setOwner(Build owner) {
        super.setOwner(owner);    //To change body of overridden methods use File | Settings | File Templates.
        for (ClassCoverage classCoverage : classCoverages) {
            classCoverage.setOwner(owner);
        }
    }
}
