package hudson.plugins.cobertura.targets;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 10-Jul-2007 14:59:50
 */
public enum CoverageMetric {
    METHOD("Methods"),
    CONDITIONAL("Conditionals"),
    LINE("Lines");

    private String name;

    CoverageMetric(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
