package hudson.plugins.cobertura.targets;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 10-Jul-2007 14:59:50
 */
public enum CoverageMetric {
    PACKAGES("Packages"),
    FILES("Files"),
    CLASSES("Classes"),
    METHOD("Methods"),
    LINE("Lines"),
    CONDITIONAL("Conditionals");

    private String name;

    CoverageMetric(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
