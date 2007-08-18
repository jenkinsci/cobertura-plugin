package hudson.plugins.cobertura.results;

import hudson.model.Run;
import hudson.plugins.cobertura.CoberturaBuildAction;

/**
 * Cobertura Coverage results for a specific class.
 * @author Stephen Connolly
 */
public class ClassCoverage extends AbstractCoberturaMetrics {
    public AbstractCoberturaMetrics getPreviousResult() {
        if (owner == null) return null;
        Run prevBuild = owner.getPreviousBuild();
        if (prevBuild == null) return null;
        CoberturaBuildAction action = prevBuild.getAction(CoberturaBuildAction.class);
        if (action == null) return null;
        return action.findClassCoverage(getName());
    }
}
