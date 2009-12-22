package hudson.plugins.cobertura.targets;

import hudson.plugins.cobertura.Ratio;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import static hudson.plugins.cobertura.targets.CoverageAggregationMode.COUNT_NON_ZERO;
import static hudson.plugins.cobertura.targets.CoverageAggregationMode.SUM;
import static hudson.plugins.cobertura.targets.CoverageElement.*;
import static hudson.plugins.cobertura.targets.CoverageMetric.*;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:08:46
 */
public class CoverageAggreagtionRule implements Serializable {
    private final CoverageElement source;
    private final CoverageMetric input;
    private final CoverageAggregationMode mode;
    private final CoverageMetric output;

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
        Map<CoverageMetric, Ratio> result = new EnumMap<CoverageMetric,Ratio>(CoverageMetric.class);
        result.putAll(runningTotal);
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
            new CoverageAggreagtionRule(JAVA_METHOD, LINE,          SUM, LINE),
            new CoverageAggreagtionRule(JAVA_METHOD, CONDITIONAL,   SUM, CONDITIONAL),
            new CoverageAggreagtionRule(JAVA_METHOD, LINE,          COUNT_NON_ZERO, METHOD),

            new CoverageAggreagtionRule(JAVA_CLASS, LINE,           SUM, LINE),
            new CoverageAggreagtionRule(JAVA_CLASS, CONDITIONAL,    SUM, CONDITIONAL),
            new CoverageAggreagtionRule(JAVA_CLASS, METHOD,         SUM, METHOD),
            new CoverageAggreagtionRule(JAVA_CLASS, LINE,           COUNT_NON_ZERO, CLASSES),

            new CoverageAggreagtionRule(JAVA_FILE, LINE,            SUM, LINE),
            new CoverageAggreagtionRule(JAVA_FILE, CONDITIONAL,     SUM, CONDITIONAL),
            new CoverageAggreagtionRule(JAVA_FILE, METHOD,          SUM, METHOD),
            new CoverageAggreagtionRule(JAVA_FILE, CLASSES,         SUM, CLASSES),
            new CoverageAggreagtionRule(JAVA_FILE, LINE,            COUNT_NON_ZERO, FILES),

            new CoverageAggreagtionRule(JAVA_PACKAGE, LINE,         SUM, LINE),
            new CoverageAggreagtionRule(JAVA_PACKAGE, CONDITIONAL,  SUM, CONDITIONAL),
            new CoverageAggreagtionRule(JAVA_PACKAGE, METHOD,       SUM, METHOD),
            new CoverageAggreagtionRule(JAVA_PACKAGE, CLASSES,      SUM, CLASSES),
            new CoverageAggreagtionRule(JAVA_PACKAGE, FILES,        SUM, FILES),
            new CoverageAggreagtionRule(JAVA_PACKAGE, LINE,         COUNT_NON_ZERO, PACKAGES),
    };

    public static Ratio combine(CoverageMetric metric, Ratio existingResult, Ratio additionalResult) {
        return Ratio.create(existingResult.numerator + additionalResult.numerator, existingResult.denominator + additionalResult.denominator);
    }
}
