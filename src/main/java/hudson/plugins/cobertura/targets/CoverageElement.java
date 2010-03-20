package hudson.plugins.cobertura.targets;

/**
 * Type of program construct being covered.
 *
 * @author Stephen Connolly
 * @author manolo
 * 
 * @since 22-Aug-2007 18:36:01
 */
public enum CoverageElement {
  
    PROJECT(new HasName() {
      public String getName() {
        return Messages.CoverageElement_Project();
      }
    }),
    JAVA_PACKAGE(new HasName() {
      public String getName() {
        return Messages.CoverageElement_Package();
      }
    }, PROJECT),
    JAVA_FILE(new HasName() {
      public String getName() {
        return Messages.CoverageElement_File();
      }
    }, JAVA_PACKAGE),
    JAVA_CLASS(new HasName() {
      public String getName() {
        return Messages.CoverageElement_Class();
      }
    }, JAVA_FILE),
    JAVA_METHOD(new HasName() {
      public String getName() {
        return Messages.CoverageElement_Method();
      }
    }, JAVA_CLASS);

    private final CoverageElement parent;
    private final HasName hasName;

    private CoverageElement(HasName hasName) {
        this.parent = null;
        this.hasName = hasName;
    }

    private CoverageElement(HasName hasName, CoverageElement parent) {
        this.parent = parent;
        this.hasName = hasName;
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
     * Return displayName of this coverage element.
     * 
     * Note: This getter has to be evaluated each time in a non static
     * way because the user could change its language
     *
     * @return Value for property 'displayName'.
     */
    public String getDisplayName() {
        return hasName.getName();
    }
}
