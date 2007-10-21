package hudson.plugins.cobertura.targets;

import hudson.plugins.cobertura.Ratio;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:08:46
 */
public class CoverageAggreagtionRule implements Serializable {
    private final CoverageMetric output;
    private final CoverageAggregationMode mode;
    private final CoverageMetric input;
    private final CoverageElement source;

    public CoverageAggreagtionRule(CoverageElement source,
                                   CoverageMetric input,
                                   CoverageAggregationMode mode,
                                   CoverageMetric output) {
        this.mode = mode;
        this.input = input;
        this.source = source;
        this.output = output;
    }

    public static Map<CoverageMetric, Ratio> aggregate(CoverageElement source,
                                                       CoverageMetric input,
                                                       Ratio inputResult,
                                                       Map<CoverageMetric, Ratio> runningTotal) {
        Map<CoverageMetric, Ratio> result = new HashMap<CoverageMetric, Ratio>(runningTotal);
        for (CoverageAggreagtionRule rule : INITIAL_RULESET) {
            if (rule.source.equals(source) && rule.input.equals(input)) {
                if (result.containsKey(rule.output)) {
                    Ratio prevTotal = result.get(rule.output);
                    switch (rule.mode) {
                        case SUM:
                            result.put(rule.output, Ratio.create(prevTotal.numerator + inputResult.numerator,
                                    prevTotal.denominator + inputResult.denominator));
                            break;
                        case COUNT_NON_ZERO:
                            if (Math.abs(inputResult.denominator) > 1e-7) {
                                // we are only interested in counting cases where the denominator is non-zero
                                if (Math.abs(inputResult.numerator) > 1e-7) {
                                    result.put(rule.output, Ratio.create(prevTotal.numerator + 1,
                                            prevTotal.denominator + 1));
                                } else {
                                    result.put(rule.output, Ratio.create(prevTotal.numerator,
                                            prevTotal.denominator + 1));
                                }
                            }
                            break;
                        case PRODUCT:
                            result.put(rule.output, Ratio.create(prevTotal.numerator * inputResult.numerator,
                                    prevTotal.denominator * inputResult.denominator));
                            break;
                        case NONE:
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (rule.mode) {
                        case SUM:
                        case PRODUCT:
                            result.put(rule.output, inputResult);
                            break;
                        case COUNT_NON_ZERO:
                            if (Math.abs(inputResult.denominator) > 1e-7) {
                                // we are only interested in counting cases where the denominator is non-zero
                                if (Math.abs(inputResult.numerator) > 1e-7) {
                                    result.put(rule.output, Ratio.create(1, 1));
                                } else {
                                    result.put(rule.output, Ratio.create(0, 1));
                                }
                            }
                            break;
                        case NONE:
                        default:
                            break;
                    }
                }
            }
        }
        return result;
    }

    private static final CoverageAggreagtionRule INITIAL_RULESET[] = {
            new CoverageAggreagtionRule(CoverageElement.JAVA_METHOD, CoverageMetric.LINE,
                    CoverageAggregationMode.SUM, CoverageMetric.LINE),
            new CoverageAggreagtionRule(CoverageElement.JAVA_METHOD, CoverageMetric.CONDITIONAL,
                    CoverageAggregationMode.SUM, CoverageMetric.CONDITIONAL),
            new CoverageAggreagtionRule(CoverageElement.JAVA_METHOD, CoverageMetric.LINE,
                    CoverageAggregationMode.COUNT_NON_ZERO, CoverageMetric.METHOD),

            new CoverageAggreagtionRule(CoverageElement.JAVA_CLASS, CoverageMetric.LINE,
                    CoverageAggregationMode.SUM, CoverageMetric.LINE),
            new CoverageAggreagtionRule(CoverageElement.JAVA_CLASS, CoverageMetric.CONDITIONAL,
                    CoverageAggregationMode.SUM, CoverageMetric.CONDITIONAL),
            new CoverageAggreagtionRule(CoverageElement.JAVA_CLASS, CoverageMetric.METHOD,
                    CoverageAggregationMode.SUM, CoverageMetric.METHOD),
            new CoverageAggreagtionRule(CoverageElement.JAVA_CLASS, CoverageMetric.LINE,
                    CoverageAggregationMode.COUNT_NON_ZERO, CoverageMetric.CLASSES),

            new CoverageAggreagtionRule(CoverageElement.JAVA_FILE, CoverageMetric.LINE,
                    CoverageAggregationMode.SUM, CoverageMetric.LINE),
            new CoverageAggreagtionRule(CoverageElement.JAVA_FILE, CoverageMetric.CONDITIONAL,
                    CoverageAggregationMode.SUM, CoverageMetric.CONDITIONAL),
            new CoverageAggreagtionRule(CoverageElement.JAVA_FILE, CoverageMetric.METHOD,
                    CoverageAggregationMode.SUM, CoverageMetric.METHOD),
            new CoverageAggreagtionRule(CoverageElement.JAVA_FILE, CoverageMetric.CLASSES,
                    CoverageAggregationMode.SUM, CoverageMetric.CLASSES),
            new CoverageAggreagtionRule(CoverageElement.JAVA_FILE, CoverageMetric.LINE,
                    CoverageAggregationMode.COUNT_NON_ZERO, CoverageMetric.FILES),

            new CoverageAggreagtionRule(CoverageElement.JAVA_PACKAGE, CoverageMetric.LINE,
                    CoverageAggregationMode.SUM, CoverageMetric.LINE),
            new CoverageAggreagtionRule(CoverageElement.JAVA_PACKAGE, CoverageMetric.CONDITIONAL,
                    CoverageAggregationMode.SUM, CoverageMetric.CONDITIONAL),
            new CoverageAggreagtionRule(CoverageElement.JAVA_PACKAGE, CoverageMetric.METHOD,
                    CoverageAggregationMode.SUM, CoverageMetric.METHOD),
            new CoverageAggreagtionRule(CoverageElement.JAVA_PACKAGE, CoverageMetric.CLASSES,
                    CoverageAggregationMode.SUM, CoverageMetric.CLASSES),
            new CoverageAggreagtionRule(CoverageElement.JAVA_PACKAGE, CoverageMetric.FILES,
                    CoverageAggregationMode.SUM, CoverageMetric.FILES),
            new CoverageAggreagtionRule(CoverageElement.JAVA_PACKAGE, CoverageMetric.LINE,
                    CoverageAggregationMode.COUNT_NON_ZERO, CoverageMetric.PACKAGES),
    };

    public static Ratio combine(CoverageMetric metric, Ratio existingResult, Ratio additionalResult) {
        return Ratio.create(existingResult.numerator + additionalResult.numerator, existingResult.denominator + additionalResult.denominator);
    }
}
