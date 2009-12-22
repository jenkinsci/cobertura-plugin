package hudson.plugins.cobertura.targets;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 10-Jul-2007 14:59:50
 */
public enum CoverageMetric {
    PACKAGES(Messages.CoverageMetrics_Packages()),
    FILES(Messages.CoverageMetrics_Files()),
    CLASSES(Messages.CoverageMetrics_Classes()),
    METHOD(Messages.CoverageMetrics_Methods()),
    LINE(Messages.CoverageMetrics_Lines()),
    CONDITIONAL(Messages.CoverageMetrics_Conditionals());

    private final String name;

    CoverageMetric(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }
}
