package hudson.plugins.cobertura;

import hudson.model.Run;
import hudson.plugins.cobertura.targets.CoverageMetric;

import java.util.Map;

public interface Chartable
{

	Chartable getPreviousResult();

	Map<CoverageMetric, Ratio> getResults();

	Run<?, ?> getOwner();

}
