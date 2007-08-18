package hudson.plugins.cobertura.results;

/**
 * Cobertura Coverage results for multiple files.
 * @author Stephen Connolly
 */
abstract public class AbstractFileAggregatedMetrics extends AbstractClassAggregatedMetrics {
    private int files;

    public abstract FileCoverage findFileCoverage(String name);

    /** {@inheritDoc} */
    public int getFiles() {
        return files;
    }

    /** {@inheritDoc} */
    public void setFiles(int files) {
        this.files = files;
    }
}
