package hudson.plugins.cobertura.results;

/**
 * Cobertura Coverage results for multiple classes.
 * @author Stephen Connolly
 */
abstract public class AbstractClassAggregatedMetrics extends AbstractCoberturaMetrics {

    private int classes;
    private int loc;
    private int ncloc;

    abstract public ClassCoverage findClassCoverage(String name);

    /** {@inheritDoc} */
    public int getClasses() {
        return classes;
    }

    /** {@inheritDoc} */
    public void setClasses(int classes) {
        this.classes = classes;
    }

    /** {@inheritDoc} */
    public int getLoc() {
        return loc;
    }

    /** {@inheritDoc} */
    public void setLoc(int loc) {
        this.loc = loc;
    }

    /** {@inheritDoc} */
    public int getNcloc() {
        return ncloc;
    }

    /** {@inheritDoc} */
    public void setNcloc(int ncloc) {
        this.ncloc = ncloc;
    }


}
