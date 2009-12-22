package hudson.plugins.cobertura.targets;

/**
 * Type of program construct being covered.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:36:01
 */
public enum CoverageElement {
    PROJECT(Messages.CoverageElement_Project()),
    JAVA_PACKAGE(Messages.CoverageElement_Package(), PROJECT),
    JAVA_FILE(Messages.CoverageElement_File(), JAVA_PACKAGE),
    JAVA_CLASS(Messages.CoverageElement_Class(), JAVA_FILE),
    JAVA_METHOD(Messages.CoverageElement_Method(), JAVA_CLASS);

    private final CoverageElement parent;
    private final String displayName;

    CoverageElement(String displayName) {
        this.parent = null;
        this.displayName = displayName;
    }

    CoverageElement(String displayName, CoverageElement parent) {
        this.parent = parent;
        this.displayName = displayName;
    }

    /**
     * Getter for property 'parent'.
     *
     * @return Value for property 'parent'.
     */
    public CoverageElement getParent() {
        return parent;
    }

    /**
     * Getter for property 'displayName'.
     *
     * @return Value for property 'displayName'.
     */
    public String getDisplayName() {
        return displayName;
    }
}
