package hudson.plugins.cobertura;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;
import hudson.model.Result;
import static hudson.plugins.cobertura.CoberturaPublisher.getCoberturaReports;
import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageTarget;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoberturaMatrixAggregator extends MatrixAggregator {

    private final CoberturaPublisher publisher;

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
    }

    @Override
    public boolean endRun(MatrixRun run) {
    	// copies the coverage.xml from each MatrixRun into the root build's directory
    	
        FilePath rootBuildDir = new FilePath(build.getRootBuild().getRootDir());

        for (File coberturaXmlReport : getCoberturaReports(run)) {
            FilePath report = new FilePath(coberturaXmlReport.getAbsoluteFile());
            final FilePath rootTargetPath = new FilePath(
                rootBuildDir,
                "coverage_" + run.getWorkspace().getBaseName() + ".xml"
            );

            try {
                report.copyTo(rootTargetPath);
            } catch (IOException ex) {
                Logger.getLogger(CoberturaMatrixAggregator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(CoberturaMatrixAggregator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return true;
    }

    @Override
    public boolean endBuild() {
    	// files are parsed and aggregated
    	
        CoverageResult result = null;

        for (File report : getCoberturaReports(build)) {
            try {
                result = CoberturaCoverageParser.parse(report, result);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(
                    listener.fatalError("Unable to parse " + report)
                );
                build.setResult(Result.FAILURE);
            }
        }

        if (result != null) {
            result.setOwner(build);
            build.addAction(
                CoberturaBuildAction.load(
                    build,
                    result,
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

}