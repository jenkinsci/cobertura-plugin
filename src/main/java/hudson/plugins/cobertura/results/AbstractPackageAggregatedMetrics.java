package hudson.plugins.cobertura.results;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 03-Jul-2007 15:16:10
 */
public abstract class AbstractPackageAggregatedMetrics extends AbstractFileAggregatedMetrics {
    private int packages;

    public abstract PackageCoverage findPackageCoverage(String name);

    /** {@inheritDoc} */
    public int getPackages() {
        return packages;
    }

    /** {@inheritDoc} */
    public void setPackages(int packages) {
        this.packages = packages;
    }
}
