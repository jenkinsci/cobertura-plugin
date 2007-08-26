package hudson.plugins.cobertura.targets;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:36:01
 */
public enum CoverageElement {
    PROJECT(),
    JAVA_PACKAGE(PROJECT),
    JAVA_FILE(JAVA_PACKAGE),
    JAVA_CLASS(JAVA_FILE),
    JAVA_METHOD(JAVA_CLASS);

    private final CoverageElement parent;

    CoverageElement() {
        this.parent = null;
    }

    CoverageElement(CoverageElement parent) {
        this.parent = parent;
    }

    public CoverageElement getParent() {
        return parent;
    }
}
