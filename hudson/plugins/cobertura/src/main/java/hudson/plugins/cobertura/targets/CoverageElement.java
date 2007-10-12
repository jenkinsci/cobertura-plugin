package hudson.plugins.cobertura.targets;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:36:01
 */
public enum CoverageElement {
    PROJECT("Project"),
    JAVA_PACKAGE("Package", PROJECT),
    JAVA_FILE("File", JAVA_PACKAGE),
    JAVA_CLASS("Class", JAVA_FILE),
    JAVA_METHOD("Method", JAVA_CLASS);

    private final CoverageElement parent;
    private final String displayName;

    CoverageElement(String displayName) {
        this.parent = null;
        this.displayName= displayName;
    }

    CoverageElement(String displayName, CoverageElement parent) {
        this.parent = parent;
        this.displayName= displayName;
    }

    public CoverageElement getParent() {
        return parent;
    }

    public String getDisplayName() {
        return displayName;
    }
}
