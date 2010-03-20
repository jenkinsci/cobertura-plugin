package hudson.plugins.cobertura.targets;

/**
 *
 * @author connollys
 * @author manolo
 * 
 * @since 10-Jul-2007 14:59:50
 */
public enum CoverageMetric {
    CLASSES(new HasName() {
      public String getName() {
        return Messages.CoverageMetrics_Classes();
      }
    }), 
    CONDITIONAL(new HasName() {
      public String getName() {
        return Messages.CoverageMetrics_Conditionals();
      }
    }),
    FILES(new HasName() {
      public String getName() {
        return Messages.CoverageMetrics_Files();
      }
    }),
    LINE(new HasName() {
      public String getName() {
        return Messages.CoverageMetrics_Lines();
      }
    }),
    METHOD(new HasName() {
      public String getName() {
        return Messages.CoverageMetrics_Methods();
      }
    }),
    PACKAGES(new HasName() {
      public String getName() {
        return Messages.CoverageMetrics_Packages();
      }
    });

    private final HasName hasName;

    private CoverageMetric(HasName hasName) {
        this.hasName = hasName;
    }

    /**
     * Return the name of this metric element.
     * 
     * Note: This getter has to be evaluated each time in a non static
     * way because the user could change its language
     *
     * @return Value for property 'displayName'.
     */
    public String getName() {
        return hasName.getName();
    }
}

