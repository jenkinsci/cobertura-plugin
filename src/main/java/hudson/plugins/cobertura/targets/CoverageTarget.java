package hudson.plugins.cobertura.targets;

import hudson.plugins.cobertura.Ratio;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds the target coverage for a specific condition;
 *
 * @author Stephen Connolly
 * @since 1.1
 */
public class CoverageTarget implements Serializable {

    private Map<CoverageMetric, Integer> targets = new HashMap<CoverageMetric, Integer>();

    /**
     * Constructs a new CoverageTarget.
     */
    public CoverageTarget() {
    }

    public CoverageTarget(Map<CoverageMetric, Integer> coverage) {
        this.targets.putAll(coverage);
    }

    /**
     * Getter for property 'alwaysMet'.
     *
     * @return Value for property 'alwaysMet'.
     */
    public boolean isAlwaysMet() {
        for (Integer target : targets.values()) {
            if (target != null && target > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for property 'empty'.
     *
     * @return Value for property 'empty'.
     */
    public boolean isEmpty() {
        for (Integer target : targets.values()) {
            if (target != null) {
                return false;
            }
        }
        return true;
    }

    public Set<CoverageMetric> getFailingMetrics(CoverageResult coverage) {
        Set<CoverageMetric> result = new HashSet<CoverageMetric>();
        for (Map.Entry<CoverageMetric, Integer> target : this.targets.entrySet()) {
            Ratio observed = coverage.getCoverage(target.getKey());
            if (observed != null && observed.getPercentage() < target.getValue()) {
                result.add(target.getKey());
            }
        }

        return result;
    }

    public Map<CoverageMetric, Integer> getRangeScores(CoverageTarget min, CoverageResult coverage) {
        return getRangeScores(min, coverage.getResults());
    }

    public Map<CoverageMetric, Integer> getRangeScores(CoverageTarget min, Map<CoverageMetric, Ratio> results) {
        Integer j;
        Map<CoverageMetric, Integer> result = new HashMap<CoverageMetric, Integer>();
        for (Map.Entry<CoverageMetric, Integer> target : this.targets.entrySet()) {
            Ratio observed = results.get(target.getKey());
            if (observed != null) {
                j = CoverageTarget.calcRangeScore(target.getValue(), min.targets.get(target.getKey()), observed.getPercentage());
                result.put(target.getKey(), Integer.valueOf(j));
            }
        }
        return result;
    }

    private static int calcRangeScore(Integer max, Integer min, int value) {
        if (min == null || min < 0) min = 0;
        if (max == null || max > 100) max = 100;
        if (min > max) min = max - 1;
        int result = (int) (100f * (value - min.floatValue()) / (max.floatValue() - min.floatValue()));
        if (result < 0) return 0;
        if (result > 100) return 100;
        return result;
    }

    /**
     * Getter for property 'targets'.
     *
     * @return Value for property 'targets'.
     */
    public Set<CoverageMetric> getTargets() {
        Set<CoverageMetric> targets = new HashSet<CoverageMetric>();
        for (Map.Entry<CoverageMetric, Integer> target : this.targets.entrySet()) {
            if (target.getValue() != null) {
                targets.add(target.getKey());
            }
        }
        return targets;
    }

    public void setTarget(CoverageMetric metric, Integer target) {
        targets.put(metric, target);
    }

    public Integer getTarget(CoverageMetric metric) {
        return targets.get(metric);
    }

    public void clear() {
        targets.clear();
    }
}
