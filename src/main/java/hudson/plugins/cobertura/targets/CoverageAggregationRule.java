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
 * Rules that determines how coverage ratio of children are aggregated into that of the parent.
 *
 *
 * @author Stephen Connolly
 * @since 22-Aug-2007 18:08:46
 */
public class CoverageAggregationRule implements Serializable {
    private static final long serialVersionUID = 3610276359557022488L;
    private final CoverageElement source;
    private final CoverageMetric input;
    private final CoverageAggregationMode mode;
    private final CoverageMetric output;

    public CoverageAggregationRule(CoverageElement source,
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
        for (CoverageAggregationRule rule : INITIAL_RULESET) {
            if (rule.source==source && rule.input==input) {
                Ratio prevTotal = result.get(rule.output);
                if (prevTotal==null)    prevTotal = rule.mode.ZERO;

                result.put(rule.output, rule.mode.aggregate(prevTotal,inputResult));
            }
        }
        return result;
    }

    // read (a,b,c,d) as "b metric of a is aggregated into d metric of the parent by using method c."
    // for example, line coverage of a Java method is SUMed up to the line coverage of a Java class (its parent) (1st line),
    // the method coverage of a Java class is # of methods that have some coverage among # of methods that have any code (3rd line.)
    // and so on.
    private static final CoverageAggregationRule INITIAL_RULESET[] = {
            new CoverageAggregationRule(JAVA_METHOD, LINE,          SUM, LINE),
            new CoverageAggregationRule(JAVA_METHOD, CONDITIONAL,   SUM, CONDITIONAL),
            new CoverageAggregationRule(JAVA_METHOD, LINE,          COUNT_NON_ZERO, METHOD),

            new CoverageAggregationRule(JAVA_CLASS, LINE,           SUM, LINE),
            new CoverageAggregationRule(JAVA_CLASS, CONDITIONAL,    SUM, CONDITIONAL),
            new CoverageAggregationRule(JAVA_CLASS, METHOD,         SUM, METHOD),
            new CoverageAggregationRule(JAVA_CLASS, LINE,           COUNT_NON_ZERO, CLASSES),

            new CoverageAggregationRule(JAVA_FILE, LINE,            SUM, LINE),
            new CoverageAggregationRule(JAVA_FILE, CONDITIONAL,     SUM, CONDITIONAL),
            new CoverageAggregationRule(JAVA_FILE, METHOD,          SUM, METHOD),
            new CoverageAggregationRule(JAVA_FILE, CLASSES,         SUM, CLASSES),
            new CoverageAggregationRule(JAVA_FILE, LINE,            COUNT_NON_ZERO, FILES),

            new CoverageAggregationRule(JAVA_PACKAGE, LINE,         SUM, LINE),
            new CoverageAggregationRule(JAVA_PACKAGE, CONDITIONAL,  SUM, CONDITIONAL),
            new CoverageAggregationRule(JAVA_PACKAGE, METHOD,       SUM, METHOD),
            new CoverageAggregationRule(JAVA_PACKAGE, CLASSES,      SUM, CLASSES),
            new CoverageAggregationRule(JAVA_PACKAGE, FILES,        SUM, FILES),
            new CoverageAggregationRule(JAVA_PACKAGE, LINE,         COUNT_NON_ZERO, PACKAGES),
    };

    public static Ratio combine(CoverageMetric metric, Ratio existingResult, Ratio additionalResult) {
        return Ratio.create(existingResult.numerator + additionalResult.numerator, existingResult.denominator + additionalResult.denominator);
    }
}
