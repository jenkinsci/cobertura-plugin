package hudson.plugins.cobertura;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Job;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

/**
 * A column that shows the line coverage of a job.
 *
 * @author Ullrich Hafner
 */
public class CoverageColumn extends ListViewColumn {
    private final String type;

    /**
     * Creates a new instance of {@link CoverageColumn}.
     *
     * @param type the column type
     */
    @DataBoundConstructor
    public CoverageColumn(final String type) {
        super();

        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getColumnCaption() {
        return Messages.CoverageColumn_columnHeader();
    }

    /**
     * Returns the URL of the referenced project action for the selected job.
     *
     * @param project
     *            the selected project
     * @return the URL of the project action
     */
    public String getUrl(final Job<?, ?> project) {
        CoberturaBuildAction action = getAction(project);

        if (action == null) {
            return null;
        }
        else {
            return project.getUrl() + action.getUrlName();
        }
    }

    private CoberturaBuildAction getAction(final Job<?, ?> project) {
        CoberturaProjectAction action = project.getAction(CoberturaProjectAction.class);

        if (action != null) {
            return action.getLastResult();
        }
        return null;
    }

    /**
     * Returns whether a link can be shown that shows the results of the referenced project action for the selected job.
     *
     * @param project
     *            the selected project
     * @return the URL of the project action
     */
    public boolean hasUrl(final Job<?, ?> project) {
        return getAction(project) != null;
    }

    /**
     * Returns the coverage of the selected job.
     *
     * @param project
     *            the selected project
     * @return line and branch coverage
     */
    public String getCoverage(final Job<?, ?> project) {
        CoberturaProjectAction action = project.getAction(CoberturaProjectAction.class);

        if (action != null) {
            CoberturaBuildAction lastResult = action.getLastResult();
            if (lastResult != null) {
                int line = 0;
                int branch = 0;
                if (lastResult.getResult().getCoverage(CoverageMetric.LINE) != null) {
                    line = lastResult.getResult().getCoverage(CoverageMetric.LINE).getPercentage();
                }
                if (lastResult.getResult().getCoverage(CoverageMetric.CONDITIONAL) != null) {
                    branch = lastResult.getResult().getCoverage(CoverageMetric.CONDITIONAL).getPercentage();
                }

                if ("both".equals(type)) {
                    return Messages.CoverageColumn_both(line, branch);
                }
                else if ("branch".equals(type)) {
                    return Messages.CoverageColumn_branch(branch);
                }
                else {
                    return Messages.CoverageColumn_line(line);
                }
            }
        }
        return Messages.CoverageColumn_empty();
    }

    /**
     * Descriptor for the column.
     */
    @Extension
    public static class ColumnDescriptor extends ListViewColumnDescriptor {
        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.CoverageColumn_columnName();
        }
    }
}
