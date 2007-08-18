package hudson.plugins.cobertura.targets;

import hudson.plugins.cobertura.results.AbstractCoberturaMetrics;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Holds the target coverage for a specific condition;
 *
 * @author Stephen Connolly
 * @since 1.1
 */
public class CoverageTarget implements Serializable {

    private Integer methodCoverage;

    private Integer conditionalCoverage;

    private Integer lineCoverage;

    public CoverageTarget() {
    }

    public CoverageTarget(Integer methodCoverage, Integer conditionalCoverage, Integer lineCoverage) {
        this.methodCoverage = methodCoverage;
        this.conditionalCoverage = conditionalCoverage;
        this.lineCoverage = lineCoverage;
    }

    public boolean isAlwaysMet() {
        return (methodCoverage == null || methodCoverage < 0) &&
                (conditionalCoverage == null || conditionalCoverage < 0) &&
                (lineCoverage == null || lineCoverage < 0);
    }

    public boolean isEmpty() {
        return methodCoverage == null &&
                conditionalCoverage == null &&
                lineCoverage == null;
    }

    public Set<CoverageMetric> getFailingMetrics(AbstractCoberturaMetrics coverage) {
        Set<CoverageMetric> result = new HashSet<CoverageMetric>();

        if (methodCoverage != null && coverage.getMethodCoverage().getPercentage() < methodCoverage) {
            result.add(CoverageMetric.METHOD);
        }

        if (conditionalCoverage != null && coverage.getConditionalCoverage().getPercentage() < conditionalCoverage) {
            result.add(CoverageMetric.CONDITIONAL);
        }

        if (lineCoverage != null && coverage.getLineCoverage().getPercentage() < lineCoverage) {
            result.add(CoverageMetric.LINE);
        }

        return result;
    }

    public Map<CoverageMetric, Integer> getRangeScores(CoverageTarget min, AbstractCoberturaMetrics coverage) {
        Integer j;
        Map<CoverageMetric, Integer> result = new HashMap<CoverageMetric, Integer>();

        j = CoverageTarget.calcRangeScore(methodCoverage, min.methodCoverage, coverage.getMethodCoverage().getPercentage());
        if (j != null) {
            result.put(CoverageMetric.METHOD, Integer.valueOf(j));
        }
        j = CoverageTarget.calcRangeScore(conditionalCoverage, min.conditionalCoverage, coverage.getConditionalCoverage().getPercentage());
        if (j != null) {
            result.put(CoverageMetric.CONDITIONAL, Integer.valueOf(j));
        }
        j = CoverageTarget.calcRangeScore(lineCoverage, min.lineCoverage, coverage.getLineCoverage().getPercentage());
        if (j != null) {
            result.put(CoverageMetric.LINE, Integer.valueOf(j));
        }
        return result;
    }

    private static int calcRangeScore(Integer max, Integer min, int value) {
        if (min == null || min < 0) min = 0;
        if (max == null || max > 100) max = 100;
        if (min > max) min = max - 1;
        int result = (int)(100f * (value - min.floatValue()) / (max.floatValue() - min.floatValue()));
        if (result < 0) return 0;
        if (result > 100) return 100;
        return result;
    }

    /**
     * Getter for property 'methodCoverage'.
     *
     * @return Value for property 'methodCoverage'.
     */
    public Integer getMethodCoverage() {
        return methodCoverage;
    }

    /**
     * Setter for property 'methodCoverage'.
     *
     * @param methodCoverage Value to set for property 'methodCoverage'.
     */
    public void setMethodCoverage(Integer methodCoverage) {
        this.methodCoverage = methodCoverage;
    }

    /**
     * Getter for property 'conditionalCoverage'.
     *
     * @return Value for property 'conditionalCoverage'.
     */
    public Integer getConditionalCoverage() {
        return conditionalCoverage;
    }

    /**
     * Setter for property 'conditionalCoverage'.
     *
     * @param conditionalCoverage Value to set for property 'conditionalCoverage'.
     */
    public void setConditionalCoverage(Integer conditionalCoverage) {
        this.conditionalCoverage = conditionalCoverage;
    }

    /**
     * Getter for property 'lineCoverage'.
     *
     * @return Value for property 'lineCoverage'.
     */
    public Integer getLineCoverage() {
        return lineCoverage;
    }

    /**
     * Setter for property 'lineCoverage'.
     *
     * @param lineCoverage Value to set for property 'lineCoverage'.
     */
    public void setLineCoverage(Integer lineCoverage) {
        this.lineCoverage = lineCoverage;
    }

}
