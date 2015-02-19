package hudson.plugins.cobertura;

import hudson.model.AbstractBuild;

public class BuildUtils {
	public static AbstractBuild<?, ?> getPreviousNotFailedCompletedBuild(AbstractBuild<?, ?> b) {
		while (true) {
			b = b.getPreviousNotFailedBuild();
			if (b == null) {
				return null;
			}
			if (!b.isBuilding()) {
				return b;
			}
		}
	}
}
