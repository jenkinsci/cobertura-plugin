package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageMetric;
import org.apache.commons.beanutils.Converter;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 28-Aug-2007 09:51:26
 */
public final class CoberturaPublisherTarget {
    private CoverageMetric metric;
    private Integer healthy;
    private Integer unhealthy;
    private Integer unstable;
    public static final Converter CONVERTER = new TargetConverter();


    /**
     * Constructs a new CoberturaPublisherTarget.
     */
    public CoberturaPublisherTarget() {
    }

    /**
     * @param metric
     * @param healthy
     * @param unhealthy
     * @param unstable
     * @stapler-constructor
     */
    public CoberturaPublisherTarget(CoverageMetric metric, Integer healthy, Integer unhealthy, Integer unstable) {
        this.metric = metric;
        this.healthy = healthy;
        this.unhealthy = unhealthy;
        this.unstable = unstable;
    }

    /**
     * Getter for property 'metric'.
     *
     * @return Value for property 'metric'.
     */
    public CoverageMetric getMetric() {
        return metric;
    }

    /**
     * Setter for property 'metric'.
     *
     * @param metric Value to set for property 'metric'.
     */
    public void setMetric(CoverageMetric metric) {
        this.metric = metric;
    }

    /**
     * Getter for property 'healthy'.
     *
     * @return Value for property 'healthy'.
     */
    public Integer getHealthy() {
        return healthy == null ? 80 : healthy;
    }

    /**
     * Setter for property 'healthy'.
     *
     * @param healthy Value to set for property 'healthy'.
     */
    public void setHealthy(Integer healthy) {
        this.healthy = healthy;
    }

    /**
     * Getter for property 'unhealthy'.
     *
     * @return Value for property 'unhealthy'.
     */
    public Integer getUnhealthy() {
        return unhealthy == null ? 0 : unhealthy;
    }

    /**
     * Setter for property 'unhealthy'.
     *
     * @param unhealthy Value to set for property 'unhealthy'.
     */
    public void setUnhealthy(Integer unhealthy) {
        this.unhealthy = unhealthy;
    }

    /**
     * Getter for property 'unstable'.
     *
     * @return Value for property 'unstable'.
     */
    public Integer getUnstable() {
        return unstable == null ? 0 : unstable;
    }

    /**
     * Setter for property 'unstable'.
     *
     * @param unstable Value to set for property 'unstable'.
     */
    public void setUnstable(Integer unstable) {
        this.unstable = unstable;
    }

    private static class TargetConverter implements Converter {
        /**
         * {@inheritDoc}
         */
        public Object convert(Class type, Object value) {
            return CoverageMetric.valueOf(value.toString());
        }
    }
}
