package hudson.plugins.cobertura;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;
import java.util.ArrayList;
import java.util.List;

public class CoberturaMatrixAggregator extends MatrixAggregator {

    private final CoberturaPublisher publisher;
    private final ArrayList<CoverageResult> results;

    /**
     * {@inheritDoc}
     *
     * @param build the MatrixBuild to aggregate coverage for.
     * @param launcher the launcher.
     * @param listener the listener.
     * @param publisher the CoberturaPublisher
     */
    public CoberturaMatrixAggregator(
            MatrixBuild build,
            Launcher launcher,
            BuildListener listener,
            CoberturaPublisher publisher) {
        super(build, launcher, listener);
        this.publisher = publisher;
        this.results = new ArrayList<CoverageResult>();
    }

    @Override
    public boolean endRun(MatrixRun run) {
        CoberturaBuildAction action = run.getAction(CoberturaBuildAction.class);
        if (action != null) {
            CoverageResult result = action.getResult();
            if (result != null) {
                this.results.add(result);
            }

        }

        return true;
    }

    @Override
    public boolean endBuild() {
        CoverageResult aggregateResult = mergeCoverageResults(this.results);
        if (aggregateResult != null) {
            this.build.addAction(
                CoberturaBuildAction.load(
                    this.build,
                    aggregateResult,
                    this.getHealthyTarget(), this.getUnhealthyTarget(),
                    this.getOnlyStable(),
                    this.getFailUnhealthy(), this.getFailUnstable(),
                    this.getAutoUpdateHealth(), this.getAutoUpdateStability()
                )
            );
        }

        return true;
    }

    private CoverageTarget getHealthyTarget() {
        return this.publisher.getHealthyTarget();
    }

    private CoverageTarget getUnhealthyTarget() {
        return this.publisher.getUnhealthyTarget();
    }

    private boolean getOnlyStable() {
        return this.publisher.getOnlyStable();
    }

    private boolean getFailUnhealthy() {
        return this.publisher.getFailUnhealthy();
    }

    private boolean getFailUnstable() {
        return this.publisher.getFailUnstable();
    }

    private boolean getAutoUpdateHealth() {
        return this.publisher.getAutoUpdateHealth();
    }

    private boolean getAutoUpdateStability() {
        return this.publisher.getAutoUpdateStability();
    }

    private CoverageResult mergeCoverageResults(
            List<CoverageResult> results) {
        // @todo: All this does is return last result
        // It should merge the results
        if (results.size() > 0) {
            return results.get(results.size() - 1);
        } else {
            return null;
        }
    }

}