package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageMetric;
import java.text.MessageFormat;
import org.apache.commons.beanutils.Converter;

/**
 * Coverage targets for one type of metric
 *
 * @author Stephen Connolly
 * @since 28-Aug-2007 09:51:26
 */
public final class CoberturaPublisherTarget {
    private CoverageMetric metric;
    private Float healthy;
    private Float unhealthy;
    private Float unstable;
    public static final Converter CONVERTER = new TargetConverter();
    
    public static final float DEFAULT_HEALTHY_TARGET = 80f;
    public static final Float DEFAULT_UNHEALTHY_TARGET = 0f;
    public static final Float DEFAULT_UNSTABLE_TARGET = 0f;

    /**
     * Constructs a new CoberturaPublisherTarget.
     */
    public CoberturaPublisherTarget() {
    }

    /**
     * Constructor
     *
     * @param metric Coverage metric
     * @param healthyTarget Healthy target
     * @param unhealthyTarget Unhealthy target
     * @param unstableTarget Unstable target
     */
    public CoberturaPublisherTarget(CoverageMetric metric, Float healthyTarget, Float unhealthyTarget, Float unstableTarget) {
        this.metric = metric;
        this.healthy = null != healthyTarget ? healthyTarget : DEFAULT_HEALTHY_TARGET;
        this.unhealthy = null != unhealthyTarget ? unhealthyTarget : DEFAULT_UNHEALTHY_TARGET;
        this.unstable = null != unstableTarget ? unstableTarget : DEFAULT_UNSTABLE_TARGET;
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
    public Float getHealthy() {
        if(healthy == null)
        {
            return 80f;
        }
        else
        {
            return (float)(Math.round(healthy*100f)/100f);
        }
    }

    /**
     * Setter for property 'healthy'.
     *
     * @param healthy Value to set for property 'healthy'.
     */
    public void setHealthy(Float healthy) {
        if(healthy == null)
        {
            this.healthy = null;
        }
        else if (healthy < 0f)
        {
            this.healthy = 0f;
        }
        else if (healthy > 100f)
        {
            this.healthy = 100f;
        }
        else
        {
            this.healthy = (float)(Math.round(healthy*100f)/100f);
        }
    }

    /**
     * Getter for property 'unhealthy'.
     *
     * @return Value for property 'unhealthy'.
     */
    public Float getUnhealthy() {
        if(unhealthy == null)
        {
            return 0f;
        }
        else
        {
            return (float)(Math.round(unhealthy*100f)/100f);
        }
    }

    /**
     * Setter for property 'unhealthy'.
     *
     * @param unhealthy Value to set for property 'unhealthy'.
     */
    public void setUnhealthy(Float unhealthy) {
        if(unhealthy == null)
        {
            this.unhealthy = null;
        }
        else if (unhealthy < 0f)
        {
            this.unhealthy = 0f;
        }
        else if (unhealthy > 100f)
        {
            this.unhealthy = 100f;
        }
        else
        {
            this.unhealthy = (float)(Math.round(unhealthy*100f)/100f);
        }
    }

    /**
     * Getter for property 'unstable'.
     *
     * @return Value for property 'unstable'.
     */
    public Float getUnstable() {
        if(unstable == null)
        {
            return 0f;
        }
        else
        {
            return (float)(Math.round(unstable*100f)/100f);
        }
    }

    /**
     * Setter for property 'unstable'.
     *
     * @param unstable Value to set for property 'unstable'.
     */
    public void setUnstable(Float unstable) {
        if(unstable == null)
        {
            this.unstable = null;
        }
        else if (unstable < 0f)
        {
            this.unstable = 0f;
        }
        else if (unstable > 100f)
        {
            this.unstable = 100f;
        }
        else
        {
            this.unstable = (float)(Math.round(unstable*100f)/100f);
        }
    }
    
    /**
     * Convert the metrics to string
     * @return 
     */
    @Override
    public String toString() {
        return MessageFormat.format("{0}, {1}, {2}", healthy, unhealthy, unstable);
    }
    
    private static class TargetConverter implements Converter {
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public Object convert(Class type, Object value) {
            return CoverageMetric.valueOf(value.toString());
        }
    }
}
