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
 * Cobertura Coverage results for the entire project.
 * @author Stephen Connolly
 */
public class ProjectCoverage extends AbstractPackageAggregatedMetrics {

    private List<PackageCoverage> packageCoverages = new ArrayList<PackageCoverage>();

    public boolean addPackageCoverage(PackageCoverage result) {
        return packageCoverages.add(result);
    }

    public List<PackageCoverage> getPackageCoverages() {
        return packageCoverages;
    }

    public List<PackageCoverage> getChildren() {
        return getPackageCoverages();
    }

    public PackageCoverage findPackageCoverage(String name) {
        for (PackageCoverage i : packageCoverages) {
            if (name.equals(i.getName())) return i;
        }
        return null;
    }

    public FileCoverage findFileCoverage(String name) {
        for (PackageCoverage i : packageCoverages) {
            FileCoverage j = i.findFileCoverage(name);
            if (j != null) return j;
        }
        return null;
    }

    public ClassCoverage findClassCoverage(String name) {
        for (PackageCoverage i : packageCoverages) {
            final String prefix = i.getName() + '.';
            if (name.startsWith(prefix)) {
                ClassCoverage j = i.findClassCoverage(name);
                if (j != null) return j;
            }
        }
        return null;
    }

    public PackageCoverage getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException {
        return findPackageCoverage(token);
    }

    public AbstractCoberturaMetrics getPreviousResult() {
        if (owner == null) return null;
        Run prevBuild = owner.getPreviousBuild();
        if (prevBuild == null) return null;
        CoberturaBuildAction action = prevBuild.getAction(CoberturaBuildAction.class);
        if (action == null) return null;
        return action.getResult();
    }

    @Override
    public void setOwner(Build owner) {
        super.setOwner(owner);    //To change body of overridden methods use File | Settings | File Templates.
        for (PackageCoverage p: packageCoverages) {
            p.setOwner(owner);
        }
    }
}
